package com.spacester.tweetster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.muddzdev.styleabletoast.StyleableToast;
import com.spacester.tweetster.activity.CreateChatActivity;
import com.spacester.tweetster.activity.CreatePostActivity;
import com.spacester.tweetster.activity.EditProfileActivity;
import com.spacester.tweetster.activity.FollowersActivity;
import com.spacester.tweetster.activity.FollowingActivity;
import com.spacester.tweetster.activity.MyProfileActivity;
import com.spacester.tweetster.activity.Policy;
import com.spacester.tweetster.activity.SavedPostActivity;
import com.spacester.tweetster.activity.SettingsActivity;

import com.spacester.tweetster.fragment.HomeFragment;
import com.spacester.tweetster.fragment.MessageFragment;
import com.spacester.tweetster.fragment.NotificationFragment;
import com.spacester.tweetster.fragment.SearchFragment;
import com.spacester.tweetster.live.activities.GoBroadcastActivity;
import com.spacester.tweetster.notifications.Token;
import com.spacester.tweetster.welcome.WelcomeActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    ConstraintLayout home,message,notification;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CircularImageView
        ImageView live = findViewById(R.id.dp);
        CircleImageView dp4 = findViewById(R.id.dp4);

        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String dp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                if (!dp.isEmpty()){
                    Picasso.get().load(dp).placeholder(R.drawable.avatar).into(dp4);
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
                        .gravity(0)
                        .solidBackground()
                        .backgroundColor(getResources().getColor(R.color.colorPrimary))
                        .show();
            }
        });

        live.setOnClickListener(v -> {
            Query query = FirebaseDatabase.getInstance().getReference().child("Live").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        snapshot.getRef().removeValue();
                        Intent intent = new Intent(getApplicationContext(), GoBroadcastActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(getApplicationContext(), GoBroadcastActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

        dp4.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
        });

        ImageView addChat = findViewById(R.id.addChat);
        addChat.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateChatActivity.class);
            startActivity(intent);
        });

        Button tweet = findViewById(R.id.tweet);
        tweet.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
            startActivity(intent);
        });

        home = findViewById(R.id.constraintLayout99);
        message = findViewById(R.id.constraintLayout2);
        notification = findViewById(R.id.constraintLayout3);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        ImageView menu = findViewById(R.id.menu);
        ImageView menu2 = findViewById(R.id.menu3);
        ImageView menu3 = findViewById(R.id.menu4);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        CircleImageView profileDp = headerView.findViewById(R.id.profileDp);
        TextView name = headerView.findViewById(R.id.name);
        TextView username = headerView.findViewById(R.id.username);
        TextView followersNo = headerView.findViewById(R.id.followersNo);
        TextView followingNo = headerView.findViewById(R.id.followingNo);
        TextView followers = headerView.findViewById(R.id.followers);
        TextView following = headerView.findViewById(R.id.following);
        ImageView verified = headerView.findViewById(R.id.verified);

        //ProfileDetails
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mDp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                String mUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                String mName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String mVerified = Objects.requireNonNull(snapshot.child("verified").getValue()).toString();
                username.setText(mUsername);
                name.setText(mName);
                if (!mDp.isEmpty()){
                    Picasso.get().load(mDp).placeholder(R.drawable.avatar).into(profileDp);
                }
                if (mVerified.isEmpty()){
                    verified.setVisibility(View.GONE);
                }else {
                    verified.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Followers");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersNo.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Following");
        reference1.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingNo.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileDp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        name.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        username.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        followersNo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FollowersActivity.class);
            intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        followingNo.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FollowingActivity.class);
            intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        followers.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FollowersActivity.class);
            intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        following.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FollowingActivity.class);
            intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menu2.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menu3.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            switch (id){
                case R.id.profile:
                    Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.edit:
                    Intent intent2 = new Intent(getApplicationContext(), EditProfileActivity.class);
                    startActivity(intent2);
                    break;
                case R.id.live:
                    Query query = FirebaseDatabase.getInstance().getReference().child("Live").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                snapshot.getRef().removeValue();
                                Intent intent = new Intent(getApplicationContext(), GoBroadcastActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Intent intent = new Intent(getApplicationContext(), GoBroadcastActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    break;
                case R.id.save:
                    Intent intent3 = new Intent(getApplicationContext(), SavedPostActivity.class);
                    startActivity(intent3);
                    break;
                case R.id.settings:
                    Intent intent5 = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent5);
                    break;
                case R.id.logout:
                   FirebaseAuth.getInstance().signOut();
                    Intent intent4 = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivity(intent4);
                    break;
                case R.id.privacy:
                    Intent intent8 = new Intent(getApplicationContext(), Policy.class);
                    startActivity(intent8);
                    break;
            }
            return true;

        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationSelected);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        FloatingActionButton createPost = findViewById(R.id.createPost);
        createPost.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
            startActivity(intent);
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationSelected =
            item -> {
                switch (item.getItemId()){
                    case R.id.home:
                        selectedFragment = new HomeFragment();
                        home.setVisibility(View.VISIBLE);
                        message.setVisibility(View.GONE);
                        notification.setVisibility(View.GONE);
                        break;
                    case R.id.search:
                        selectedFragment = new SearchFragment();
                        home.setVisibility(View.GONE);
                        message.setVisibility(View.GONE);
                        notification.setVisibility(View.GONE);
                        break;
                    case R.id.notification:
                        selectedFragment = new NotificationFragment();
                        home.setVisibility(View.GONE);
                        message.setVisibility(View.GONE);
                        notification.setVisibility(View.VISIBLE);
                        break;
                    case R.id.messages:
                        selectedFragment = new MessageFragment();
                        home.setVisibility(View.GONE);
                        message.setVisibility(View.VISIBLE);
                        notification.setVisibility(View.GONE);
                        break;
                }
                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }
                return true;
            };

    private void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(mToken);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Ban").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Intent intent = new Intent(MainActivity.this, BanActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}