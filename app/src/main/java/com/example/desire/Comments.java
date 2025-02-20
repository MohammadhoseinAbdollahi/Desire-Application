package com.example.desire;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comments extends RecyclerView.Adapter<Comments.CommentViewHolder> {

    private List<Map.Entry<String, String>> commentsList;
    private String postId; // Used to update the comments count attribute

    // Existing constructor with comments map
    public Comments(Map<String, String> comments) {
        this.commentsList = new ArrayList<>(comments.entrySet());
    }

    // Default constructor
    public Comments() {
        this.commentsList = new ArrayList<>();
    }

    // New constructor to pass the post ID along with comments
    public Comments(String postId, Map<String, String> comments) {
        this.postId = postId;
        this.commentsList = new ArrayList<>(comments.entrySet());
        updateCommentsCount();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_comments, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        if (position < commentsList.size()) {  // Avoid IndexOutOfBoundsException
            Map.Entry<String, String> commentEntry = commentsList.get(position);
            String userId = commentEntry.getKey(); // Get user ID of the commenter
            String commentText = commentEntry.getValue();

            // Set the comment text
            holder.commentText.setText(commentText);

            // Fetch username and profile image from Firebase using the user ID
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                        // Set username (fallback to default if null)
                        if (username != null) {
                            holder.commentUsername.setText(username);
                        } else {
                            holder.commentUsername.setText("Unknown User");
                        }

                        // Load profile image using Glide (fallback to default image)
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext()).load(profileImageUrl).into(holder.commentProfileImage);
                        } else {
                            holder.commentProfileImage.setImageResource(R.drawable.blacklogo);
                        }
                    } else {
                        // User data not found – fallback to defaults
                        holder.commentUsername.setText("Unknown User");
                        holder.commentProfileImage.setImageResource(R.drawable.blacklogo);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // On error, use default values
                    holder.commentUsername.setText("Unknown User");
                    holder.commentProfileImage.setImageResource(R.drawable.blacklogo);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentsList != null ? commentsList.size() : 0;
    }

    public void setComments(Map<String, String> newComments) {
        this.commentsList.clear();
        this.commentsList.addAll(newComments.entrySet());
        notifyDataSetChanged();
        updateCommentsCount();
    }

    public void clearComments() {
        this.commentsList.clear();
        notifyDataSetChanged();
        updateCommentsCount();
    }


    private void updateCommentsCount() {
        if (postId != null && !postId.isEmpty()) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);
            postRef.child("commentsCount").setValue(commentsList.size());
        }
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
