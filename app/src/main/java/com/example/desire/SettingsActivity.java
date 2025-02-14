package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Retrieve userId from Intent
        userId = getIntent().getStringExtra("userId");

        // If userId is still null, fetch it from FirebaseAuth
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        }

        Log.d("SettingsActivity", "User ID: " + userId);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        findViewById(R.id.button_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(SettingsActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.profile_security).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePassword.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        findViewById(R.id.manage_account).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileSetting.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        findViewById(R.id.adjust_your_desire).setOnClickListener(v -> {
            Log.d("SettingsActivity", "Passing userId: " + userId);
            Intent intent = new Intent(SettingsActivity.this, AdjustDesireActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }
}
