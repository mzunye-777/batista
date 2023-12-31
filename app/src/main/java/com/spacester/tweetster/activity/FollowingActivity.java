package com.spacester.tweetster.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.R;
import com.spacester.tweetster.adapter.AdapterUsers;
import com.spacester.tweetster.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowingActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    //Post
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    ProgressBar pb;

    List<String> idList;

    String userId;

    private static final int TOTAL_ITEMS_TO_LOAD = 7;
    private int mCurrentPage = 1;

    TextView found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        recyclerView = findViewById(R.id.trendingRv);
        pb = findViewById(R.id.pb);

        userId = getIntent().getStringExtra("id");

        ImageView imageView3 = findViewById(R.id.imageView3);
        imageView3.setOnClickListener(v -> onBackPressed());


        found = findViewById(R.id.found);

        EditText editText = findViewById(R.id.editText);

        pb.setVisibility(View.VISIBLE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())){
                    filterUsers(s.toString());
                    pb.setVisibility(View.VISIBLE);
                }else {
                    getFollowers();
                    pb.setVisibility(View.VISIBLE);
                }

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    mCurrentPage++;
                    getFollowers();
                }
            }
        });

        //Post
        userList = new ArrayList<>();
        getFollowers();


    }
    private void filterUsers(String query) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query q = reference.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelUser modelUser = snapshot.getValue(ModelUser.class);
                    for (String id : idList) {
                        assert modelUser != null;
                        if (modelUser.getId().equals(id)){
                            if (Objects.requireNonNull(modelUser).getName().toLowerCase().contains(query.toLowerCase()) || modelUser.getUsername().contains(query.toLowerCase())){
                                userList.add(modelUser);
                            }
                        }
                        adapterUsers = new AdapterUsers(FollowingActivity.this, userList);
                        recyclerView.setAdapter(adapterUsers);
                        pb.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getFollowers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userId).child("Following");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    idList.add(snapshot.getKey());
                }
                if (dataSnapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                }
                showUsers();
                pb.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showUsers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query q = reference.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        adapterUsers = new AdapterUsers(FollowingActivity.this, userList);
                        recyclerView.setAdapter(adapterUsers);
                        pb.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}