package com.example.desire;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DesireAdapter extends RecyclerView.Adapter<DesireAdapter.DesireViewHolder> {

    private ArrayList<Post> postList;

    public DesireAdapter(ArrayList<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public DesireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the desire_item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.desire_item, parent, false);
        return new DesireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DesireViewHolder holder, int position) {
        // Get the current post object
        Post post = postList.get(position);

        // Bind the data to the view holder elements
        holder.desireLocation.setText(post.getLocation());
        holder.desireDate.setText(post.getDate());
        holder.desireDescription.setText(post.getDescription());

        // Get the actual rating and comments count
        holder.desireRating.setText(String.valueOf(post.getRating()));
        holder.desireComments.setText(String.valueOf(post.getCommentsCount()));

        // Load the image with Glide
        Glide.with(holder.itemView.getContext()).load(post.getImageUrl()).into(holder.myDesiresImage);
    }

    @Override
    public int getItemCount() {
        return postList.size(); // Return the size of the list
    }

    // ViewHolder class to hold reference to each item view
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
