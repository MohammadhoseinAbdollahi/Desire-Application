package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
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
        userId = getIntent().getStringExtra("userId");

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out the user
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(SettingsActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();

                // Navigate to LoginActivity
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.profile_security).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "Profile Security Clicked", Toast.LENGTH_SHORT).show();
                // Handle profile security logic here
            }
        });

        findViewById(R.id.manage_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to the ProfileSetting activity
                Intent intent = new Intent(SettingsActivity.this, ProfileSetting.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        findViewById(R.id.my_followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "My Followers and Following Clicked", Toast.LENGTH_SHORT).show();
                // Handle followers and following logic here
            }
        });
    }
}
