package com.example.desire;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    public String userId;
    public String email;
    public String name;
    public String username;
    public String birthday;
    public double rating;
    public int numRatings;
    public String bio;
    public String profileImageUrl;
    public int RateGain;
    public int RateGive;
    public List<String> samedesire;
    public List<String> blackdesire;
    public List<String> posts;
    public List<String> rateddesire;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String email, String name, String username, String birthday) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.username = username;
        this.birthday = birthday;
        this.rating = 2.5;
        this.numRatings = 1;
        this.bio = "";
        this.profileImageUrl = "";
        this.RateGain = 0;
        this.RateGive = 0;
        this.samedesire = new ArrayList<>();
        this.samedesire.add("");
        this.blackdesire = new ArrayList<>();
        this.blackdesire.add("");
        this.posts = new ArrayList<>();
        this.posts.add(""); // Add an empty value
        this.rateddesire = new ArrayList<>();
        this.rateddesire.add(""); // Add an empty value

    }

    public String getUsername() {
        return username;
    }

    public interface SaveToFirebaseCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void saveToFirebase(String userId, final SaveToFirebaseCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        Log.d("User", "Attempting to save user to database at path: " + databaseReference.toString());

        Map<String, Object> userValues = new HashMap<>();
        userValues.put("userId", userId);
        userValues.put("email", email);
        userValues.put("name", name);
        userValues.put("username", username);
        userValues.put("birthday", birthday);
        userValues.put("rating", rating);
        userValues.put("numRatings", numRatings);
        userValues.put("bio", bio);
        userValues.put("profileImageUrl", profileImageUrl);
        userValues.put("RateGain", RateGain);
        userValues.put("RateGive", RateGive);
        userValues.put("samedesire", samedesire);
        userValues.put("blackdesire", blackdesire);
        userValues.put("desires", posts);
        userValues.put("rateddesire", rateddesire);

        databaseReference.setValue(userValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });

        }

}
