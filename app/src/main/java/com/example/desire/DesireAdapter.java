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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
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


        holder.username.setText(post.getUsername() != null ? post.getUsername() : "Unknown User");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "";

                if (currentUserId.equals(post.getUserId())) {
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < 300) {
                        openCommentsBottomSheet(post, holder);
                    }
                    lastClickTime = clickTime;
                } else {
                    openCommentsBottomSheet(post, holder);
                }
            }
        });


        holder.itemView.setOnLongClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                    FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getUserId())) {
                confirmDeletePost(post, position);
            } else {
                if (context != null) {
                    Toast.makeText(context, "You can only delete your own posts!", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void openCommentsBottomSheet(Post post, DesireViewHolder holder) {
        Context baseContext = holder.itemView.getContext();
        if (baseContext == null) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(baseContext);
        View bottomSheetView = LayoutInflater.from(baseContext).inflate(R.layout.layout_bottom_sheet_comments, null);

        RecyclerView commentsRecyclerView = bottomSheetView.findViewById(R.id.commentsRecyclerView);
        EditText inputComment = bottomSheetView.findViewById(R.id.inputComment);
        Button submitComment = bottomSheetView.findViewById(R.id.submitComment);


        Comments commentsAdapter = new Comments(post.getPostId(), new HashMap<>());
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(baseContext));
        commentsRecyclerView.setAdapter(commentsAdapter);


        post.loadComments(new Post.CommentsLoadCallback() {
            @Override
            public void onCommentsLoaded(List<Map.Entry<String, String>> comments) {

                Context toastContext = (commentsRecyclerView.getContext() != null) ?
                        commentsRecyclerView.getContext() : (context != null ? context : null);
                if (toastContext == null) return;


                Map<String, String> commentsMap = new HashMap<>();
                for (Map.Entry<String, String> entry : comments) {
                    commentsMap.put(entry.getKey(), entry.getValue());
                }


                commentsAdapter.setComments(commentsMap);
                commentsAdapter.notifyDataSetChanged();

                Toast.makeText(toastContext, "Comments Loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                if (context != null) {
                    Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show();
                }
            }
        });


        submitComment.setOnClickListener(v -> {
            String newComment = inputComment.getText().toString().trim();
            if (!newComment.isEmpty()) {
                addCommentToPost(post, newComment, commentsAdapter);
                inputComment.setText("");
            } else {
                if (context != null) {
                    Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    private void addCommentToPost(Post post, String commentText, Comments commentsAdapter) {
        if (post.getPostId() == null) {
            if (context != null) {
                Toast.makeText(context, "Post ID is missing", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance()
                .getReference("posts")
                .child(post.getPostId())
                .child("comments");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            if (context != null) {
                Toast.makeText(context, "User is not logged in", Toast.LENGTH_SHORT).show();
            }
            return;
        }


        postRef.child(userId).setValue(commentText).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                post.loadComments(new Post.CommentsLoadCallback() {
                    @Override
                    public void onCommentsLoaded(List<Map.Entry<String, String>> comments) {
                        commentsAdapter.clearComments();

                        Map<String, String> commentsMap = new HashMap<>();
                        for (Map.Entry<String, String> entry : comments) {
                            commentsMap.put(entry.getKey(), entry.getValue());
                        }
                        commentsAdapter.setComments(commentsMap);
                        commentsAdapter.notifyDataSetChanged();

                        if (context != null) {
                            Toast.makeText(context, "Comment added", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (context != null) {
                            Toast.makeText(context, "Failed to reload comments", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                if (context != null) {
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void confirmDeletePost(Post post, int position) {
        if (context == null) return;

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
                if (context != null) {
                    Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (context != null) {
                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class DesireViewHolder extends RecyclerView.ViewHolder {
        public ImageView myDesiresImage;
        public TextView desireLocation, desireDate, desireDescription, desireRating, desireComments, username;

        public DesireViewHolder(@NonNull View itemView) {
            super(itemView);
            myDesiresImage = itemView.findViewById(R.id.myDesiresImage);
            desireLocation = itemView.findViewById(R.id.desireLocation);
            desireDate = itemView.findViewById(R.id.desireDate);
            desireDescription = itemView.findViewById(R.id.desireDescription);
            desireRating = itemView.findViewById(R.id.desireRating);
            desireComments = itemView.findViewById(R.id.desireComments);
            username = itemView.findViewById(R.id.exploreusername);
        }
    }
}