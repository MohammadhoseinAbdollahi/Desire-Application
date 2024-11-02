package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);

        // Display the GIF
        ImageView splashImage = findViewById(R.id.splash_image);
        if (splashImage != null) {
            Glide.with(this).load(R.drawable.startlogo).into(splashImage);
        } else {
            Toast.makeText(this, "Splash image view is null", Toast.LENGTH_SHORT).show();
        }

        // Delay and check login status
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void checkLoginStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            if (userId != null) {
                redirectToProfile(userId);
            } else {
                Toast.makeText(MainActivity.this, "User ID is null. Please log in again.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        } else {
            redirectToLogin();
        }
    }

    private void redirectToProfile(String userId) {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
