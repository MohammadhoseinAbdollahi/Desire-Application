package com.example.desire;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                Toast.makeText(SettingsActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                // Handle logout logic here
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
                Toast.makeText(SettingsActivity.this, "Manage Account Clicked", Toast.LENGTH_SHORT).show();
                // Handle manage account logic here
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
