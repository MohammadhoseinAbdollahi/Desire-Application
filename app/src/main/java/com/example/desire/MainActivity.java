package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_LENGTH = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is logged in
                boolean isLoggedIn = checkIfLoggedIn();
                if (isLoggedIn) {
                    // Redirect to ProfileActivity
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {
                    // Redirect to LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private boolean checkIfLoggedIn() {
        // Add your logic to check if the user is logged in
        return false; // Placeholder
    }
}
