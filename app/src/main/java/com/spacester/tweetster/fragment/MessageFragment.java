package com.spacester.tweetster.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.Adpref;
import com.spacester.tweetster.R;
import com.spacester.tweetster.adapter.AdapterChatList;
import com.spacester.tweetster.model.ModelChat;
import com.spacester.tweetster.model.ModelChatlist;
import com.spacester.tweetster.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatList adapterChatList;

    ProgressBar pb;

    public MessageFragment() {
    }
    TextView found;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_message, container, false);


        pb = view.findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        found = view.findViewById(R.id.found);

        recyclerView = view.findViewById(R.id.chatList);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatlistList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                if (snapshot.getChildrenCount() == 0){
                    found.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        MobileAds.initialize(getContext(), initializationStatus -> {
        });
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Adpref adpref;
        adpref = new Adpref(Objects.requireNonNull(getContext()));
        if (adpref.loadAdsModeState()){
            mAdView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatlist chatlist: chatlistList){
                        if (Objects.requireNonNull(user).getId() != null && user.getId().equals(chatlist.getId())){
                            userList.add(user);
                            pb.setVisibility(View.GONE);
                            break;
                        }
                    }
                    adapterChatList = new AdapterChatList(getContext(), userList);
                    recyclerView.setAdapter(adapterChatList);
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getId());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat == null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if(sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(currentUser.getUid())){
                        switch (chat.getType()) {
                            case "image":
                                theLastMessage = "Sent a photo";
                                break;
                            case "video":
                                theLastMessage = "Sent a video";
                                break;
                            case "story":
                                theLastMessage = "Commented on story";
                                break;
                            default:
                                theLastMessage = chat.getMsg();
                                break;
                        }
                    }
                }
                adapterChatList.notifyDataSetChanged();
                adapterChatList.setLastMessageMap(userId, theLastMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
