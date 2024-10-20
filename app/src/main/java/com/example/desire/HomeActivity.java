package com.example.desire;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout desireContainer;
    private String userId;
    private Button addDesireButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        addDesireButton = findViewById(R.id.addDesireButton);
        desireContainer = findViewById(R.id.desireContainer);
        // Initialize BottomNavigationBar
        View bottomNavigationView = findViewById(R.id.bottom_navigation);
        new BottomNavigationBar(this, bottomNavigationView, userId);

        // Get the user ID from the intent
        userId = getIntent().getStringExtra("userId");
        addDesireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddDesireActivity(view);
            }

        });

        // Load and add desire items
        loadDesireItems();

        addDesireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open AddDesireActivity with the user ID
                Intent intent = new Intent(HomeActivity.this, AddDesireActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }
        });
    }

    private void loadDesireItems() {
        // Mock data for demonstration purposes
        for (int i = 0; i < 3; i++) {
            View desireItemView = LayoutInflater.from(this).inflate(R.layout.desire_item, desireContainer, false);

            ImageView myDesiresImage = desireItemView.findViewById(R.id.myDesiresImage);
            TextView desireRating = desireItemView.findViewById(R.id.desireRating);
            TextView desireComments = desireItemView.findViewById(R.id.desireComments);
            TextView desireLocation = desireItemView.findViewById(R.id.desireLocation);
            TextView desireDate = desireItemView.findViewById(R.id.desireDate);
            TextView desireDescription = desireItemView.findViewById(R.id.desireDescription);

            // Mock data
            String imageUrl = "https://example.com/path/to/image.jpg";
            double rating = 3.9;
            int comments = 12;
            String location = "Genova,Italy";
            String date = "23 May,2024";
            String description = "Sun VS Sunglasses.";

            // Load data into views
            Glide.with(this).load(imageUrl).into(myDesiresImage);
            desireRating.setText(String.valueOf(rating));
            desireComments.setText(String.valueOf(comments));
            desireLocation.setText(location);
            desireDate.setText(date);
            desireDescription.setText(description);

            desireContainer.addView(desireItemView);
        }
    }

    // open add desire activity when add desire button is clicked with userid
    public void openAddDesireActivity(View view) {
        Intent intent = new Intent(this, AddDesireActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

}



