package com.example.desire;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Comments extends RecyclerView.Adapter<Comments.CommentViewHolder> {

    private List<Map.Entry<String, String>> commentsList;

    public Comments(Map<String, String> comments) {
        this.commentsList = new ArrayList<>(comments.entrySet());
    }
    public Comments() {
        this.commentsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_comments, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Map.Entry<String, String> commentEntry = commentsList.get(position);
        String userId = commentEntry.getKey();
        String commentText = commentEntry.getValue();

        // Set the comment text
        holder.commentText.setText(commentText);

        // Fetch user information from Firebase and set the username and profile image
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                    // Set username
                    holder.commentUsername.setText(username);

                    // Load profile image with Glide
                    if (profileImageUrl != null) {
                        Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.commentProfileImage);
                    } else {
                        holder.commentProfileImage.setImageResource(R.drawable.blacklogo); // Placeholder if image URL is null
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(holder.itemView.getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    // Method to update the comments list and notify the adapter of changes
    public void setComments(Map<String, String> newComments) {
        this.commentsList = new ArrayList<>(newComments.entrySet());
        notifyDataSetChanged();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        public ImageView commentProfileImage;
        public TextView commentUsername;
        public TextView commentText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentProfileImage = itemView.findViewById(R.id.comment_profile_image);
            commentUsername = itemView.findViewById(R.id.comment_username);
            commentText = itemView.findViewById(R.id.comment_text);
        }
    }
}
