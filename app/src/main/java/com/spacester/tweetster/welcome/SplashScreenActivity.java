package com.spacester.tweetster.welcome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.spacester.tweetster.CheckActivity;
import com.spacester.tweetster.R;

@SuppressWarnings("deprecation")
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Timer to Intent
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), CheckActivity.class);
            startActivity(intent);
            finish();
        },3000);

    }
}