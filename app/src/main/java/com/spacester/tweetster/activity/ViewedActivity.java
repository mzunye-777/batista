package com.spacester.tweetster.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.Adpref;
import com.spacester.tweetster.R;
import com.spacester.tweetster.adapter.AdapterUsers;
import com.spacester.tweetster.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewedActivity extends AppCompatActivity {

    List<String> idList;
    String id;

    ProgressBar pb;
    RecyclerView recyclerView;
    List<ModelUser> userList;
    TextView found;
    AdapterUsers adapterUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewed);
        recyclerView = findViewById(R.id.trendingRv);
        pb = findViewById(R.id.pb);
        id = getIntent().getStringExtra("id");
        pb.setVisibility(View.VISIBLE);
        found = findViewById(R.id.found);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();

        ImageView imageView3 = findViewById(R.id.back);
        imageView3.setOnClickListener(v -> onBackPressed());
        getViews();

        MobileAds.initialize(this, initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Adpref adpref;
        adpref = new Adpref(this);
        if (adpref.loadAdsModeState()){
            mAdView.setVisibility(View.VISIBLE);

        }

    }

    private void getViews(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(Objects.requireNonNull(getIntent().getStringExtra("storyid"))).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    idList.add(snapshot1.getKey());

                }
                if (snapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                }
                showUsers();
                pb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelUser modelUser = snapshot.getValue(ModelUser.class);
                    for (String id : idList) {
                        assert modelUser != null;
                        if (modelUser.getId().equals(id)){
                            userList.add(modelUser);
                        }
                        pb.setVisibility(View.GONE);
                        adapterUsers = new AdapterUsers(ViewedActivity.this, userList);
                        recyclerView.setAdapter(adapterUsers);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}