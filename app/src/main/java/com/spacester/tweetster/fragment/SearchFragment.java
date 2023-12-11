package com.spacester.tweetster.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.spacester.tweetster.R;
import com.spacester.tweetster.adapter.AdapterPost;
import com.spacester.tweetster.adapter.AdapterUsers;
import com.spacester.tweetster.model.ModelPost;
import com.spacester.tweetster.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment {

    RecyclerView trendingRv,usersRv;

    //Post
    AdapterPost adapterPost;
    List<ModelPost> postList;

    //User
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    private static final int TOTAL_ITEMS_TO_LOAD = 7;
    private int mCurrentPage = 1;

    ProgressBar progressBar;
    TextView found;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        trendingRv = view.findViewById(R.id.trendingRv);
        usersRv = view.findViewById(R.id.usersRv);
        found = view.findViewById(R.id.found);

        EditText editText = view.findViewById(R.id.editText);
        TextView posts = view.findViewById(R.id.users);
        TextView users = view.findViewById(R.id.groups);

        progressBar = view.findViewById(R.id.pb);
        progressBar.setVisibility(View.VISIBLE);

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
                    filterUser(s.toString());
                    filterPost(s.toString());
                    progressBar.setVisibility(View.VISIBLE);
                }else {
                    getAllUsers();
                    getAllTrend();
                    progressBar.setVisibility(View.VISIBLE);
                }

            }
        });

        trendingRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    mCurrentPage++;
                    getAllTrend();
                }
            }
        });

        usersRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    mCurrentPage++;
                    getAllUsers();
                }
            }
        });

        posts.setTextColor(Color.parseColor("#1DA1F2"));

        //onClick
        posts.setOnClickListener(v -> {
            users.setTextColor(Color.parseColor("#657786"));
            posts.setTextColor(Color.parseColor("#1DA1F2"));
            trendingRv.setVisibility(View.VISIBLE);
            usersRv.setVisibility(View.GONE);
        });

        users.setOnClickListener(v -> {
            users.setTextColor(Color.parseColor("#1DA1F2"));
            posts.setTextColor(Color.parseColor("#657786"));
            usersRv.setVisibility(View.VISIBLE);
            trendingRv.setVisibility(View.GONE);
        });

        //Post
        postList = new ArrayList<>();
        getAllTrend();

        //User
        userList = new ArrayList<>();
        getAllUsers();

        return view;
    }

    private void filterPost(String query) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query q = ref.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (Objects.requireNonNull(modelPost).getText().toLowerCase().contains(query.toLowerCase()) || modelPost.getType().contains(query.toLowerCase())){
                        postList.add(modelPost);
                        progressBar.setVisibility(View.GONE);
                    }
                    adapterPost = new AdapterPost(getActivity(), postList);
                    trendingRv.setAdapter(adapterPost);

                }
                if (dataSnapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filterUser(String query) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query q = databaseReference.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getUsername().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    adapterUsers.notifyDataSetChanged();
                    usersRv.setAdapter(adapterUsers);
                }
                if (dataSnapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new StyleableToast
                        .Builder(Objects.requireNonNull(getActivity()))
                        .text(databaseError.getMessage())
                        .textColor(Color.WHITE)
                        .textBold()
                        .length(2000)
                        .gravity(0)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });

    }

    private void getAllTrend() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        trendingRv.setLayoutManager(layoutManager);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query q = ref.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    progressBar.setVisibility(View.GONE);
                }
                if (dataSnapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                adapterPost = new AdapterPost(getActivity(), postList);
                trendingRv.setAdapter(adapterPost);
                adapterPost.notifyDataSetChanged();
                layoutManager.scrollToPosition(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new StyleableToast
                        .Builder(Objects.requireNonNull(getActivity()))
                        .gravity(0)
                        .text(databaseError.getMessage())
                        .textColor(Color.WHITE)
                        .textBold()
                        .length(2000)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });
    }

    private void getAllUsers() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        usersRv.setLayoutManager(layoutManager);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query q = databaseReference.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                        userList.add(modelUser);
                        progressBar.setVisibility(View.GONE);
                    }
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    usersRv.setAdapter(adapterUsers);
                    adapterUsers.notifyDataSetChanged();
                    layoutManager.scrollToPosition(0);
                }
                if (dataSnapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new StyleableToast
                        .Builder(Objects.requireNonNull(getActivity()))
                        .text(databaseError.getMessage())
                        .textColor(Color.WHITE)
                        .textBold()
                        .length(2000)
                        .gravity(0)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });
    }

}