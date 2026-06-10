package com.FreeRechargeApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstOpen = prefs.getBoolean("is_first_open", true);



        Intent intent;
        if (isFirstOpen) {
            intent = new Intent(SplashActivity.this, PermissionActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, NextWebActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
