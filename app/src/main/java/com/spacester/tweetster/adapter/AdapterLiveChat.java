package com.spacester.tweetster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.R;
import com.spacester.tweetster.model.ModelLiveChat;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterLiveChat extends RecyclerView.Adapter<AdapterLiveChat.MyHolder>{

    final Context context;
    final List<ModelLiveChat> modelLiveChats;

    public AdapterLiveChat(Context context, List<ModelLiveChat> modelLiveChats) {
        this.context = context;
        this.modelLiveChats = modelLiveChats;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.live_chat_left, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String hisUID = modelLiveChats.get(position).getUserId();
        String message = modelLiveChats.get(position).getMsg();

        FirebaseDatabase.getInstance().getReference().child("Ban").child(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.itemView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mUsername = snapshot.child("username").getValue().toString();
                String dp = snapshot.child("photo").getValue().toString();
                String mVerified = snapshot.child("verified").getValue().toString();

                if (!dp.isEmpty()){
                    Glide.with(context).asBitmap().load(dp).centerCrop().into(holder.live_photo);
                }else {
                    Glide.with(context).asBitmap().load(R.drawable.avatar).centerCrop().into(holder.live_photo);
                }
                holder.story_username.setText(mUsername);
                if (mVerified.isEmpty()){
                    holder.verify.setVisibility(View.GONE);
                }else {
                    holder.verify.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.msg.setText(message);

    }


    @Override
    public int getItemCount() {
        return modelLiveChats.size();
    }

    @SuppressWarnings("CanBeFinal")
    static class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView live_photo;
        TextView story_username,msg;
        ImageView verify;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            live_photo = itemView.findViewById(R.id.dp);
            story_username = itemView.findViewById(R.id.username);
            msg = itemView.findViewById(R.id.msg);
            verify = itemView.findViewById(R.id.verify);
        }
    }
}
