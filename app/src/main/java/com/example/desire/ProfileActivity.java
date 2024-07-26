package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView profileNameTextView;
    private TextView profileDescriptionTextView;
    private LinearLayout desiresContainer;
    private String userId;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImage);
        profileNameTextView = findViewById(R.id.profileName);
        profileDescriptionTextView = findViewById(R.id.profileDescription);
        desiresContainer = findViewById(R.id.myDesiresScrollView).findViewById(R.id.desireItem);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            loadUserProfile();
            loadUserDesires();
        }

        // Handle Settings Icon Click
        ImageView settingsIcon = findViewById(R.id.settingicon);
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    profileNameTextView.setText(name);
                    profileDescriptionTextView.setText(bio);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
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

    private void loadUserDesires() {
        DatabaseReference desiresRef = mDatabase.child("desires");
        desiresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                desiresContainer.removeAllViews(); // Clear the container first
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot desireSnapshot : dataSnapshot.getChildren()) {
                        String imageUrl = desireSnapshot.child("imageUrl").getValue(String.class);
                        Double rating = desireSnapshot.child("rating").getValue(Double.class);
                        Integer comments = desireSnapshot.child("comments").getValue(Integer.class);
                        String location = desireSnapshot.child("location").getValue(String.class);
                        String date = desireSnapshot.child("date").getValue(String.class);
                        String description = desireSnapshot.child("description").getValue(String.class);

                        View desireItemView = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.desire_item, desiresContainer, false);

                        ImageView desireImage = desireItemView.findViewById(R.id.myDesiresImage);
                        TextView desireRating = desireItemView.findViewById(R.id.desireRating);
                        TextView desireComments = desireItemView.findViewById(R.id.desireComments);
                        TextView desireLocation = desireItemView.findViewById(R.id.desireLocation);
                        TextView desireDate = desireItemView.findViewById(R.id.desireDate);
                        TextView desireDescription = desireItemView.findViewById(R.id.desireDescription);

                        Glide.with(ProfileActivity.this).load(imageUrl).into(desireImage);
                        desireRating.setText("★ " + (rating != null ? rating : 0.0));
                        desireComments.setText(String.valueOf(comments != null ? comments : 0));
                        desireLocation.setText(location != null ? location : "");
                        desireDate.setText(date != null ? date : "");
                        desireDescription.setText(description != null ? description : "");

                        desiresContainer.addView(desireItemView);
                    }
                } else {
                    // Handle case when there are no desires
                    TextView noDesiresText = new TextView(ProfileActivity.this);
                    noDesiresText.setText("No desires to display.");
                    desiresContainer.addView(noDesiresText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
