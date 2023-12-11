package com.spacester.tweetster.activity;

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
import com.spacester.tweetster.adapter.AdapterUsers;
import com.spacester.tweetster.model.ModelUser;

import java.util.ArrayList;
import java.util.List;

public class CommentLikedByActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    //Post
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    ProgressBar pb;
    TextView found;
    String postId,cId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked);
        recyclerView = findViewById(R.id.trendingRv);
        pb = findViewById(R.id.pb);

        cId = getIntent().getStringExtra("cId");
        postId = getIntent().getStringExtra("pId");
        pb.setVisibility(View.VISIBLE);

        ImageView imageView3 = findViewById(R.id.back);
        imageView3.setOnClickListener(v -> onBackPressed());

        found = findViewById(R.id.found);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("CommentLikes").child(cId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String hisUid = ""+ ds.getRef().getKey();
                    showUsers(hisUid);
                }
                if (snapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void showUsers(String hisUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("id").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            userList.add(modelUser);
                        }
                        adapterUsers = new AdapterUsers(CommentLikedByActivity.this, userList);
                        recyclerView.setAdapter(adapterUsers);
                        pb.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}