package com.example.desire;

public class Post {
    public String postId;
    public String imageUrl;
    public double rating;
    public int commentsCount;
    public String location;
    public String date;
    public String description;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String postId, String imageUrl, double rating, int commentsCount, String location, String date, String description) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.commentsCount = commentsCount;
        this.location = location;
        this.date = date;
        this.description = description;
    }
}
