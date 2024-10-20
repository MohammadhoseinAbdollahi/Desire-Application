package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private ImageView profileImageView;
    private TextView gainedStarsTextView, bioTextView, profileNameTextView;
    private RecyclerView desireRecyclerView;
    private DesireAdapter desireAdapter;
    private ArrayList<Post> postList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImage);
        gainedStarsTextView = findViewById(R.id.profileRatingText);
        bioTextView = findViewById(R.id.profileDescription);
        profileNameTextView = findViewById(R.id.profileName);
        desireRecyclerView = findViewById(R.id.desireRecyclerView);

        // Initialize BottomNavigationBar
        View bottomNavigationView = findViewById(R.id.bottom_navigation);
        new BottomNavigationBar(this, bottomNavigationView, userId);

        // Get the user ID from Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        }

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Fetch user profile details
        fetchUserProfile();

        // Setup RecyclerView for displaying user posts in horizontal scroll
        desireRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        desireAdapter = new DesireAdapter(postList);
        desireRecyclerView.setAdapter(desireAdapter);


        // Fetch posts that belong to the signed-in user
        fetchUserPosts();

        // setting icon click listeners
        ImageView settingIcon = findViewById(R.id.settingbutton);
        settingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


    }

    private void fetchUserProfile() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    int rateGain = dataSnapshot.child("RateGain").getValue(Integer.class);

                    // Set user profile data
                    gainedStarsTextView.setText(" " + rateGain);
                    bioTextView.setText(bio);
                    profileNameTextView.setText(name);

                    if (profileImageUrl != null) {
                        Glide.with(ProfileActivity.this).load(profileImageUrl).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void fetchUserPosts() {
        mDatabase.child("posts").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                desireAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
