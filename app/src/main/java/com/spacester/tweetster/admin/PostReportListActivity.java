package com.spacester.tweetster.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.R;
import com.spacester.tweetster.adapter.AdapterAdminPost;
import com.spacester.tweetster.model.ModelPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostReportListActivity extends AppCompatActivity {

    RecyclerView trendingRv;

    //Post
    AdapterAdminPost adapterPost;
    List<ModelPost> postList;
    List<String> mySaves;

    ProgressBar pb;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked);

        trendingRv = findViewById(R.id.trendingRv);
        pb = findViewById(R.id.pb);

        ImageView imageView3 = findViewById(R.id.back);
        imageView3.setOnClickListener(v -> onBackPressed());

        TextView title = findViewById(R.id.title);
        title.setText("Reported posts");


        pb.setVisibility(View.VISIBLE);

        trendingRv.setHasFixedSize(true);
        trendingRv.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapterPost = new AdapterAdminPost(this, postList);
        trendingRv.setAdapter(adapterPost);
        mySaved();

    }

    private void mySaved() {
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Report");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mySaves.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    mySaves.add(snapshot1.getKey());
                }
                readSave();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readSave() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    ModelPost post = snapshot1.getValue(ModelPost.class);
                    for (String id: mySaves){
                        if (Objects.requireNonNull(post).getpId().equals(id)){
                            postList.add(post);
                        }
                    }
                }
                adapterPost.notifyDataSetChanged();
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}