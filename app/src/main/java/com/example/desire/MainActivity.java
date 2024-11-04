package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        // Delay and check login status after verifying post expirations
        new Handler().postDelayed(this::checkExpiredPostsAndProceed, SPLASH_DISPLAY_LENGTH);
    }

    private void checkExpiredPostsAndProceed() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && post.isVisibility()) {
                        if (isPostExpired(post.getEndDate())) {
                            // Update visibility to false if expired
                            postSnapshot.getRef().child("visibility").setValue(false);
                        }
                    }
                }
                // After checking and updating expired posts, proceed to login status check
                checkLoginStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to check post expirations", Toast.LENGTH_SHORT).show();
                // Proceed with login status check regardless
                checkLoginStatus();
            }
        });
    }

    private boolean isPostExpired(String expirationDate) {
        if (expirationDate != null && !expirationDate.equals("No Expiration")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date expiry = sdf.parse(expirationDate);
                return expiry != null && new Date().after(expiry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void checkLoginStatus() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            if (userId != null) {
                verifyUserInDatabase(userId);
            } else {
                Toast.makeText(MainActivity.this, "User ID is null. Please log in again.", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        } else {
            redirectToLogin();
        }
    }

    private void verifyUserInDatabase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User exists in the database, proceed to profile
                    redirectToProfile(userId);
                } else {
                    // User does not exist in the database, redirect to login
                    Toast.makeText(MainActivity.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error checking user data", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
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
