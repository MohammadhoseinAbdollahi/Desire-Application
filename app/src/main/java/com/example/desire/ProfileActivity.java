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

    private ImageView[] stars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImage);
        gainedStarsTextView = findViewById(R.id.profileRatingText);
        bioTextView = findViewById(R.id.profileDescription);
        profileNameTextView = findViewById(R.id.profileName);
        desireRecyclerView = findViewById(R.id.desireRecyclerView);

        stars = new ImageView[]{
                findViewById(R.id.star1),
                findViewById(R.id.star2),
                findViewById(R.id.star3),
                findViewById(R.id.star4),
                findViewById(R.id.star5)
        };

        View bottomNavigationView = findViewById(R.id.bottom_navigation);
        new BottomNavigationBar(this, bottomNavigationView, userId);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchUserProfile();
        desireRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        desireAdapter = new DesireAdapter(postList);
        desireRecyclerView.setAdapter(desireAdapter);

        fetchUserPosts();

        ImageView settingIcon = findViewById(R.id.settingbutton);
        settingIcon.setOnClickListener(v -> {
            if (!isFinishing()) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
                // Apply fade transition
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void fetchUserProfile() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !isFinishing()) {
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    Integer rateGain = dataSnapshot.child("RateGain").getValue(Integer.class);
                    Integer numRatings = dataSnapshot.child("numRatings").getValue(Integer.class);
                    Double rating = dataSnapshot.child("rating").getValue(Double.class);

                    if (rateGain != null && numRatings != null && rating != null && !isDestroyed()) {
                        gainedStarsTextView.setText(String.valueOf(rateGain));
                        bioTextView.setText(bio);
                        profileNameTextView.setText(username);
                        updateStarRating(rating);

                        if (profileImageUrl != null) {
                            Glide.with(ProfileActivity.this).load(profileImageUrl).into(profileImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isFinishing()) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateStarRating(double rating) {
        int fullStars = (int) Math.round(rating);
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < fullStars ? R.drawable.star_fill : R.drawable.star_empty);
        }
    }

    private void fetchUserPosts() {
        mDatabase.child("posts").orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                if (!isFinishing()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            postList.add(post);
                        }
                    }
                    desireAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isFinishing()) {
                    Toast.makeText(ProfileActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}