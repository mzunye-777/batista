package com.spacester.tweetster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.spacester.tweetster.welcome.WelcomeActivity;

public class CheckActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            Intent intent = new Intent(CheckActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(CheckActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}