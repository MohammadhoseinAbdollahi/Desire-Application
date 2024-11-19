package com.example.desire;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView desireRecyclerView;
    private DesireAdapter desireAdapter;
    private List<Post> desireList;
    private List<Post> allPosts;
    private DatabaseReference mDatabase;
    private String userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize RecyclerView
        desireRecyclerView = findViewById(R.id.desireScrollView);
        desireRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize variables
        desireList = new ArrayList<>();
        allPosts = new ArrayList<>();
        desireAdapter = new DesireAdapter((ArrayList<Post>) desireList, this);
        desireRecyclerView.setAdapter(desireAdapter);

        // Firebase setup
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            loadUserData();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData() {
        DatabaseReference userRef = mDatabase.child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    loadFilteredPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilteredPosts() {
        mDatabase.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPosts.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);

                    if (post != null && post.isVisibility()) {
                        boolean isInSameDesire = currentUser.samedesire.contains(post.getUserId());
                        boolean isNotBlacklisted = !currentUser.blackdesire.contains(post.getUserId());
                        boolean hasHighInteraction = UserInteraction.getAverageRating() >= 4.0;

                        if ((isInSameDesire || hasHighInteraction) && isNotBlacklisted) {
                            allPosts.add(post);
                        }
                    }
                }

                desireList.clear();
                desireList.addAll(allPosts);
                desireAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
