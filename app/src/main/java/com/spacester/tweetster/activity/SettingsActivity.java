package com.spacester.tweetster.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spacester.tweetster.R;
import com.spacester.tweetster.admin.AdminActivity;
import com.spacester.tweetster.welcome.WelcomeActivity;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Back
        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        ConstraintLayout privacy = findViewById(R.id.privacy);
        ConstraintLayout terms = findViewById(R.id.terms);

        privacy.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Policy.class);
            startActivity(intent);
        });
        terms.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Terms.class);
            startActivity(intent);
        });

        //ConstrainLayout
        ConstraintLayout editProfile = findViewById(R.id.editProfile);
        editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        ConstraintLayout about = findViewById(R.id.about);
        about.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        });

        ConstraintLayout editEmail = findViewById(R.id.editEmail);
        editEmail.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditEmailActivity.class);
            startActivity(intent);
        });

        ConstraintLayout editPassword = findViewById(R.id.editPassword);
        editPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditPasswordActivity.class);
            startActivity(intent);
        });


        ConstraintLayout logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent4 = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent4);
        });

        ConstraintLayout applyVerification = findViewById(R.id.applyVerification);
        applyVerification.setOnClickListener(v -> {
            Intent intent4 = new Intent(getApplicationContext(), VerifyActivity.class);
            startActivity(intent4);
        });


        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String phone = Objects.requireNonNull(snapshot.child("phone").getValue()).toString();
                if (!phone.isEmpty()){
                    editEmail.setVisibility(View.GONE);
                    editPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Admin
        TextView adminText = findViewById(R.id.textView69);
        ConstraintLayout adminPanel = findViewById(R.id.admin);

        adminPanel.setOnClickListener(v -> {
            Intent intent4 = new Intent(getApplicationContext(), AdminActivity.class);
            startActivity(intent4);
        });
        adminText.setOnClickListener(v -> {
            Intent intent4 = new Intent(getApplicationContext(), AdminActivity.class);
            startActivity(intent4);
        });

        FirebaseDatabase.getInstance().getReference().child("Admin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            adminPanel.setVisibility(View.VISIBLE);
                            adminText.setVisibility(View.VISIBLE);
                        }
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}