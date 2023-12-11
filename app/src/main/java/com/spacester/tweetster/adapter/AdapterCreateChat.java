package com.spacester.tweetster.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.R;
import com.spacester.tweetster.activity.ChatActivity;
import com.spacester.tweetster.model.ModelUser;
import com.squareup.picasso.Picasso;
import java.util.List;


public class AdapterCreateChat extends RecyclerView.Adapter<AdapterCreateChat.MyHolder>{

    final Context context;
    final List<ModelUser> userList;

    public AdapterCreateChat(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final String hisUID = userList.get(position).getId();
        String userImage = userList.get(position).getPhoto();
        final String userName = userList.get(position).getName();
        String userUsername = userList.get(position).getUsername();
        String mVerified = userList.get(position).getVerified();


        holder.mName.setText(userName);
        holder.mUsername.setText(userUsername);

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


        if (!userImage.isEmpty()){
            Picasso.get().load(userImage).placeholder(R.drawable.avatar).into(holder.avatar);
        }

        if (mVerified.isEmpty()){
            holder.verified.setVisibility(View.GONE);
        }else {
            holder.verified.setVisibility(View.VISIBLE);
        }


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("id", hisUID);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final ImageView avatar;
        final ImageView verified;
        final TextView mName;
        final TextView mUsername;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.circleImageView2);
            mName = itemView.findViewById(R.id.name);
            mUsername = itemView.findViewById(R.id.username);
            verified = itemView.findViewById(R.id.verified);

        }

    }
}
