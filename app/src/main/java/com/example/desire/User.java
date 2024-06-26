package com.example.desire;

public class User {
    public String userId;
    public String name;
    public String username;
    public String birthday;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String name, String username, String birthday) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.birthday = birthday;
    }
}
