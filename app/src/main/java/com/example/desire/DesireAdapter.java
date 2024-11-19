package com.example.desire;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DesireAdapter extends RecyclerView.Adapter<DesireAdapter.DesireViewHolder> {

    private ArrayList<Post> postList;
    private Context context;

    public DesireAdapter(ArrayList<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }
    public DesireAdapter(ArrayList<Post> postList) {
        this.postList = postList;
    }


    @NonNull
    @Override
    public DesireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.desire_item, parent, false);
        return new DesireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DesireViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.desireLocation.setText(post.getLocation());
        holder.desireDate.setText(post.getDate());
        holder.desireDescription.setText(post.getDescription());
        holder.desireRating.setText(String.valueOf(post.getRating()));
        holder.desireComments.setText(String.valueOf(post.getCommentsCount()));

        Glide.with(holder.itemView.getContext()).load(post.getImageUrl()).into(holder.myDesiresImage);

        // Show comments in a BottomSheetDialog when the profile image is clicked
        holder.myDesiresImage.setOnClickListener(v -> openCommentsBottomSheet(post));

        // Long-click listener for deleting a post
        holder.itemView.setOnLongClickListener(v -> {
            confirmDeletePost(post, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private void openCommentsBottomSheet(Post post) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_comments, null);

        RecyclerView commentsRecyclerView = bottomSheetView.findViewById(R.id.commentsRecyclerView);
        EditText inputComment = bottomSheetView.findViewById(R.id.inputComment);
        Button submitComment = bottomSheetView.findViewById(R.id.submitComment);

        // Set up the RecyclerView for comments
        post.loadComments(new Post.CommentsLoadCallback() {
            @Override
            public void onCommentsLoaded(List<Map.Entry<String, String>> comments) {
                Comments commentsAdapter = new Comments((Map<String, String>) comments);
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                commentsRecyclerView.setAdapter(commentsAdapter);

                // Handle the submit button click for adding a new comment
                submitComment.setOnClickListener(v -> {
                    String newComment = inputComment.getText().toString().trim();
                    if (!newComment.isEmpty()) {
                        addCommentToPost(post, newComment, commentsAdapter);
                        inputComment.setText(""); // Clear input field after submission
                    } else {
                        Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCommentToPost(Post post, String commentText, Comments commentsAdapter) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getPostId()).child("comments");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Add the comment to Firebase
        postRef.child(userId).setValue(commentText).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Reload comments to reflect the new addition
                post.loadComments(new Post.CommentsLoadCallback() {
                    @Override
                    public void onCommentsLoaded(List<Map.Entry<String, String>> comments) {
                        commentsAdapter.setComments((Map<String, String>) comments); // Update the adapter data
                        commentsAdapter.notifyDataSetChanged(); // Refresh the adapter to show the new comment
                        Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(context, "Failed to reload comments", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeletePost(Post post, int position) {
        // Show confirmation dialog
        new android.app.AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> deletePost(post, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void deletePost(Post post, int position) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getPostId());
        postRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                postList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class DesireViewHolder extends RecyclerView.ViewHolder {

        public ImageView myDesiresImage;
        public TextView desireLocation, desireDate, desireDescription, desireRating, desireComments;

        public DesireViewHolder(@NonNull View itemView) {
            super(itemView);
            myDesiresImage = itemView.findViewById(R.id.myDesiresImage);
            desireLocation = itemView.findViewById(R.id.desireLocation);
            desireDate = itemView.findViewById(R.id.desireDate);
            desireDescription = itemView.findViewById(R.id.desireDescription);
            desireRating = itemView.findViewById(R.id.desireRating);
            desireComments = itemView.findViewById(R.id.desireComments);
        }
    }
}
