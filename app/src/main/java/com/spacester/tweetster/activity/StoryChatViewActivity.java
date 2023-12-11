package com.spacester.tweetster.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.muddzdev.styleabletoast.StyleableToast;
import com.spacester.tweetster.R;
import com.spacester.tweetster.model.ModelStory;
import com.spacester.tweetster.model.ModelUser;
import com.spacester.tweetster.notifications.Data;
import com.spacester.tweetster.notifications.Sender;
import com.spacester.tweetster.notifications.Token;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

@SuppressWarnings("ALL")
public class StoryChatViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    final long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView imageView;
    TextView textView;
    CircleImageView pic;
    TextView username;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete,imageView2;
    List<String> images;
    List<String> storyids;
    String userid;

    EditText sendMessage;
    ImageView verify;
    private RequestQueue requestQueue;

    private boolean notify = false;

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_view);
        userid = getIntent().getStringExtra("userid");

        getStories(userid);
        userInfo(userid);

        verify = findViewById(R.id.verify);
        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);
        imageView = findViewById(R.id.image);
        textView = findViewById(R.id.text);
        View reverse =  findViewById(R.id.reverse);
        View skip =  findViewById(R.id.skip);
        storiesProgressView =  findViewById(R.id.stories);
        pic =  findViewById(R.id.pic);
        username =  findViewById(R.id.username);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        ConstraintLayout message = findViewById(R.id.message);

        if (userid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);
        }
        r_seen.setOnClickListener(v -> {
            Intent intent = new Intent(StoryChatViewActivity.this, ViewedActivity.class);
            intent.putExtra("id",userid);
            intent.putExtra("storyid",storyids.get(counter));
            startActivity(intent);
        });

        story_delete.setOnClickListener(v -> {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                    .child(userid).child(storyids.get(counter));
            reference.removeValue();
        });

        reverse.setOnClickListener(v -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);

        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        sendMessage = findViewById(R.id.sendMessage);
        imageView2 = findViewById(R.id.imageView2);
        imageView2.setOnClickListener(v -> {
            String msg = sendMessage.getText().toString();
            if (msg.isEmpty()){
                new StyleableToast
                        .Builder(getApplicationContext())
                        .text("Type something")
                        .textColor(Color.WHITE)
                        .gravity(0)
                        .textBold()
                        .length(2000)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }else {
                notify = true;
                DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", userid);
                hashMap.put("msg", msg);
                hashMap.put("isSeen", false);
                hashMap.put("timeStamp", images.get(counter));
                hashMap.put("type", "story");
                databaseReference1.child("Chats").push().setValue(hashMap);

                final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(userid);
                chatRef1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            chatRef1.child("id").setValue(userid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        new StyleableToast
                                .Builder(getApplicationContext())
                                .text(error.getMessage())
                                .textColor(Color.WHITE)
                                .textBold()
                                .length(2000)
                                .solidBackground()
                                .gravity(0)
                                .backgroundColor(getResources().getColor(R.color.colorPrimary))
                                .show();
                    }
                });

                final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(userid)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                chatRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()){
                            chatRef2.child("id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        new StyleableToast
                                .Builder(getApplicationContext())
                                .text(error.getMessage())
                                .textColor(Color.WHITE)
                                .gravity(0)
                                .textBold()
                                .length(2000)
                                .solidBackground()
                                .backgroundColor(getResources().getColor(R.color.colorPrimary))
                                .show();
                    }
                });

                new StyleableToast
                        .Builder(getApplicationContext())
                        .text("Message sent")
                        .textColor(Color.WHITE)
                        .gravity(0)
                        .textBold()
                        .length(2000)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();

                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                dataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ModelUser user = dataSnapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(userid, Objects.requireNonNull(user).getName(), "Sent a message");

                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        sendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                storiesProgressView.pause();


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(imageView);
        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Glide.with(getApplicationContext()).load(images.get(--counter)).into(imageView);
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onComplete() {
        finish();

    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }


    private void getStories(String userid){
        images = new ArrayList<>();

        storyids = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyids.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    ModelStory modelStory = snapshot1.getValue(ModelStory.class);
                    long timecurrent = System.currentTimeMillis();
                        images.add(modelStory.imageUri);

                        storyids.add(modelStory.storyid);

                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryChatViewActivity.this);
                storiesProgressView.startStories(counter);
                Glide.with(getApplicationContext()).load(images.get(counter)).into(imageView);
                addView(storyids.get(counter));
                seenNumber(storyids.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void userInfo(String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser modelUser = snapshot.getValue(ModelUser.class);
                if (modelUser.getPhoto().isEmpty()){
                    Glide.with(getApplicationContext()).load(R.drawable.avatar).into(pic);
                }else {
                    Glide.with(getApplicationContext()).load(Objects.requireNonNull(modelUser).getPhoto()).into(pic);
                }

                username.setText(modelUser.getUsername());
                username.setOnClickListener(v -> {
                    Intent intent = new Intent(StoryChatViewActivity.this, UserProfileActivity.class);
                    intent.putExtra("id", modelUser.getId());
                    startActivity(intent);
                });
                pic.setOnClickListener(v -> {
                    Intent intent = new Intent(StoryChatViewActivity.this, UserProfileActivity.class);
                    intent.putExtra("id", modelUser.getId());
                    startActivity(intent);
                });

                if (modelUser.getVerified().isEmpty()){
                    verify.setVisibility(View.GONE);
                }else {
                    verify.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
    }
    private void seenNumber(String storyid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyid).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId, R.drawable.ic_logo);
                    Sender sender = new Sender(data, Objects.requireNonNull(token).getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Log.d("JSON_RESPONSE", "onResponse" + response.toString()), error -> Log.d("JSON_RESPONSE", "onResponse" + error.toString())){
                            @SuppressWarnings("RedundantThrows")
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAWJPP3Sk:APA91bFAzquy6I3Rjd_ic4UvKF5yXgn9qNvp59OnEzj3VXVE7P8XLSm4FFhhDHrOPOL5rhYS7Og7NeMTrmK7twcaojPXr5AH3LCVp8Tzjl_Y3hgovMSw_9nG7s1I2GheCxm_zcZyY_gc");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                new StyleableToast
                        .Builder(getApplicationContext())
                        .text(error.getMessage())
                        .textColor(Color.WHITE)
                        .textBold()
                        .length(2000)
                        .solidBackground()
                        .gravity(0)
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });
    }

}