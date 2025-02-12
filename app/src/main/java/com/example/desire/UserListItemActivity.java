package com.example.desire;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserListItemActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView averageInteractionTextView;
    private Button deleteUserButton;
    private DatabaseReference mDatabase;
    private String mainUserId;
    private String subUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_item);

        userNameTextView = findViewById(R.id.userName);
        averageInteractionTextView = findViewById(R.id.averageInteraction);
        deleteUserButton = findViewById(R.id.deleteUserButton);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mainUserId = getIntent().getStringExtra("mainUserId");
        subUserId = getIntent().getStringExtra("subUserId");

        loadUserInfo();
        loadUserAverageInteraction();

        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserFromList();
            }
        });
    }

    private void loadUserInfo() {
        mDatabase.child("users").child(subUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    userNameTextView.setText(user.getUsername());
                } else {
                    Toast.makeText(UserListItemActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserListItemActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserAverageInteraction() {
        UserRateAvrages userRateAvrages = new UserRateAvrages();
        userRateAvrages.getUserAverageInteraction(mainUserId, subUserId, new UserRateAvrages.UserAverageInteractionCallback() {
            @Override
            public void onCallback(double averageInteraction) {
                averageInteractionTextView.setText("Avg Interaction: " + averageInteraction);
            }
        });
    }

    private void deleteUserFromList() {
        mDatabase.child("users").child(mainUserId).child("samedesire").child(subUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserListItemActivity.this, "User removed from Same Desire list", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UserListItemActivity.this, "Failed to remove user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}