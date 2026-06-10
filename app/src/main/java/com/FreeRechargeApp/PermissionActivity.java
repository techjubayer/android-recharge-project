package com.FreeRechargeApp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Button btnGrantPermissions = findViewById(R.id.btnGrantPermissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            btnGrantPermissions.setOnClickListener(v -> requestRequiredPermissions());
        }

        findViewById(R.id.btnDenyPermissions).setOnClickListener(v -> completeOnboarding());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestRequiredPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.READ_CONTACTS);
        permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE);
        permissionsToRequest.add(Manifest.permission.READ_PHONE_NUMBERS);
        
        if (Build.VERSION.SDK_INT >= 33) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            completeOnboarding();
        }
    }

    private void completeOnboarding() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_first_open", false).apply();

        Intent intent = new Intent(PermissionActivity.this, NextWebActivity.class);
        startActivity(intent);
        finish();
    }
}
