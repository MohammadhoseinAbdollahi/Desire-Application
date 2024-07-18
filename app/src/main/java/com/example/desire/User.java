package com.example.desire;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    public String[] posts;
    public String[] followers;
    public String[] following;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String email, String name, String username, String birthday, double rating, int numRatings, String bio, String profileImageUrl, String[] posts, String[] followers, String[] following) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.username = username;
        this.birthday = birthday;
        this.rating = rating;
        this.numRatings = numRatings;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.posts = posts;
        this.followers = followers;
        this.following = following;
    }

    public void saveToFirebase(DatabaseReference.CompletionListener completionListener) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.child(userId).setValue(this, completionListener);
    }


}