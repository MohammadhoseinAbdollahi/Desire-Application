package com.example.desire;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class BottomNavigationBar {

    private Context context;
    private ImageView profileIcon, homeIcon, compassIcon;
    private String userId;

    public BottomNavigationBar(Context context, View view, String userId) {
        this.context = context;
        this.userId = userId;
        profileIcon = view.findViewById(R.id.nav_profile);
        homeIcon = view.findViewById(R.id.nav_home);
        compassIcon = view.findViewById(R.id.nav_compass);

        setupListeners();
    }

    private void setupListeners() {
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });

        compassIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ExploreActivity.class);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });
    }
}