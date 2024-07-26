package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profileNameTextView;
    private LinearLayout profileRatingLayout;
    private TextView gainedStarsTextView;
    private TextView givenStarsTextView;
    private TextView profileDescriptionTextView;
    private LinearLayout myDesiresLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // If no user is logged in, redirect to LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        profileImageView = findViewById(R.id.profileImage);
        profileNameTextView = findViewById(R.id.profileName);
        profileRatingLayout = findViewById(R.id.profileRating);
        gainedStarsTextView = findViewById(R.id.gainedStars);
        givenStarsTextView = findViewById(R.id.givenStars);
        profileDescriptionTextView = findViewById(R.id.profileDescription);
        myDesiresLayout = findViewById(R.id.myDesiresScrollView).findViewById(R.id.desireItem);

        // Load user data
        loadUserData();
    }

    private void loadUserData() {
        DatabaseReference userRef = mDatabase.child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Set user data to views
                        profileNameTextView.setText(user.name);
                        profileDescriptionTextView.setText(user.bio);
                        gainedStarsTextView.setText("Gained\n" + user.RateGain);
                        givenStarsTextView.setText("Given\n" + user.RateGive);

                        // Set rating stars
                        int fullStars = (int) user.rating;
                        for (int i = 0; i < fullStars; i++) {
                            ((ImageView) profileRatingLayout.getChildAt(i)).setImageResource(R.drawable.star_fill);
                        }
                        if (user.rating - fullStars >= 0.5) {
                            ((ImageView) profileRatingLayout.getChildAt(fullStars)).setImageResource(R.drawable.star_half);
                        }

                        // Load profile image
                        if (!user.profileImageUrl.isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(user.profileImageUrl)
                                    .into(profileImageView);
                        }

                        // Load user's desires (posts)
                        loadUserDesires(user.posts);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDesires(List<String> postIds) {
        DatabaseReference postsRef = mDatabase.child("posts");
        for (String postId : postIds) {
            postsRef.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            // Inflate and populate desire item
                            View desireItemView = getLayoutInflater().inflate(R.layout.desire_item, myDesiresLayout, false);
                            ImageView desireImageView = desireItemView.findViewById(R.id.myDesiresImage);
                            TextView desireRatingTextView = desireItemView.findViewById(R.id.desireRating);
                            TextView desireCommentsTextView = desireItemView.findViewById(R.id.desireComments);
                            TextView desireLocationTextView = desireItemView.findViewById(R.id.desireLocation);
                            TextView desireDateTextView = desireItemView.findViewById(R.id.desireDate);
                            TextView desireDescriptionTextView = desireItemView.findViewById(R.id.desireDescription);

                            Glide.with(ProfileActivity.this)
                                    .load(post.imageUrl)
                                    .into(desireImageView);

                            desireRatingTextView.setText("★ " + post.rating);
                            desireCommentsTextView.setText(String.valueOf(post.commentsCount));
                            desireLocationTextView.setText(post.location);
                            desireDateTextView.setText(post.date);
                            desireDescriptionTextView.setText(post.description);

                            myDesiresLayout.addView(desireItemView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load desires.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
