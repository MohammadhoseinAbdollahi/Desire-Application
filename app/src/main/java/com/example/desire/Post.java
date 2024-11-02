package com.example.desire;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Post {
    private String userId;
    private String postId;
    private String imageUrl;
    private double rating;
    private int commentsCount;
    private String location;
    private String date;
    private String description;
    private boolean isRated;
    private String postdate;
    private boolean visibility;
    private String username;
    private double totalRating;
    private int sameDesireCount;
    private int numberOfRatings;


    // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    public Post() {
    }

    // Constructor with parameters
    public Post(String userId, String postId, String imageUrl, double rating, int commentsCount, String location,
                String date, String description, boolean isRated, String postDate, boolean visibility, String username,
                double totalRating, int sameDesireCount, int numberOfRatings) {
        this.userId = userId;
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.commentsCount = commentsCount;
        this.location = location;
        this.date = date;
        this.description = description;
        this.isRated = isRated;
        this.postdate = postdate;
        this.visibility = visibility;
        this.username = username;
        this.totalRating = totalRating;
        this.sameDesireCount = sameDesireCount;
        this.numberOfRatings = numberOfRatings;
    }

    // Getters and Setters for new attributes

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public int getSameDesireCount() {
        return sameDesireCount;
    }

    public void setSameDesireCount(int sameDesireCount) {
        this.sameDesireCount = sameDesireCount;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    // Existing getters and setters for other attributes

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }

    public String getPostDate() {
        return postdate;
    }

    public void setPostDate(String postDate) {
        this.postdate = postDate;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
