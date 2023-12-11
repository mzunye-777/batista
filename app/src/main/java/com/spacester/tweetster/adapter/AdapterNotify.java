package com.spacester.tweetster.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.spacester.tweetster.GetTimeAgo;
import com.spacester.tweetster.R;
import com.spacester.tweetster.activity.CommentActivity;
import com.spacester.tweetster.activity.UserProfileActivity;
import com.spacester.tweetster.model.ModelNotification;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("CodeBlock2Expr")
public class AdapterNotify extends RecyclerView.Adapter<AdapterNotify.Holder>  {

    private final Context context;
    private final ArrayList<ModelNotification> notifications;
    private String userId;

    public AdapterNotify(Context context, ArrayList<ModelNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_view, parent, false);

        return new Holder(view);
    }

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("unused")
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        ModelNotification modelNotification = notifications.get(position);
        //noinspection unused
        @SuppressWarnings("unused") String mName = modelNotification.getsName();
        String notification = modelNotification.getNotification();
        String image = modelNotification.getsImage();
        String timestamp = modelNotification.getTimestamp();
        String senderUid = modelNotification.getsUid();
        String postId = modelNotification.getpId();

        FirebaseDatabase.getInstance().getReference().child("Ban").child(senderUid).addValueEventListener(new ValueEventListener() {
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


        long lastTime = Long.parseLong(timestamp);
        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("id").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String mName = ""+ds.child("name").getValue();
                            String image = ""+ds.child("photo").getValue();
                            String mVerified = ""+ds.child("verified").getValue();

                            modelNotification.setsName(mName);
                            modelNotification.setsImage(image);
                            holder.name.setText(mName);
                            if (!image.isEmpty()){
                                Picasso.get().load(image).placeholder(R.drawable.avatar).into(holder.circleImageView);
                            }

                            if (mVerified.isEmpty()){
                                holder.verified.setVisibility(View.GONE);
                            }else {
                                holder.verified.setVisibility(View.VISIBLE);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.username.setText(notification +" - "+ lastSeenTime);
        holder.itemView.setOnClickListener(v -> {
            if (!postId.isEmpty()){
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("id", postId);
                context.startActivity(intent);
            }else {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("id", senderUid);
                context.startActivity(intent);
            }
        });

     holder.itemView.setOnLongClickListener(v -> {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle("Delete");
         builder.setMessage("Are you sure to delete this notification?");
         builder.setPositiveButton("Delete", (dialog, which) -> {
             DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
             ref.child(userId).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(aVoid -> {
                 new StyleableToast
                         .Builder(context)
                         .text("Deleted")
                         .textColor(Color.WHITE)
                         .textBold()
                         .gravity(0)
                         .length(2000)
                         .solidBackground()
                         .backgroundColor(context.getResources().getColor(R.color.colorPrimary))
                         .show();
             }).addOnFailureListener(e -> {
                 new StyleableToast
                         .Builder(context)
                         .text(e.getMessage())
                         .textColor(Color.WHITE)
                         .textBold()
                         .length(2000)
                         .solidBackground()
                         .gravity(0)
                         .backgroundColor(context.getResources().getColor(R.color.colorPrimary))
                         .show();
             });
         }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
         builder.create().show();
         return false;
     });

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class Holder extends RecyclerView.ViewHolder{

        final CircleImageView circleImageView;
        final TextView username;
        final TextView name;
        final ImageView verified;

        public Holder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.circleImageView2);
            username = itemView.findViewById(R.id.username);
            name = itemView.findViewById(R.id.name);
            verified = itemView.findViewById(R.id.verified);
        }
    }
}
