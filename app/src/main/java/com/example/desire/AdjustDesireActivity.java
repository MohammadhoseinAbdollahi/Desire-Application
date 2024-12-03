package com.example.desire;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdjustDesireActivity extends AppCompatActivity {

    private RecyclerView userListRecyclerView;
    private UserListAdapter userListAdapter;
    private List<User> userList;
    private DatabaseReference mDatabase;
    private String userId;
    private boolean isSameDesireTabActive = true;  // Track active tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_desire);

        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userList = new ArrayList<>();
        userListAdapter = new UserListAdapter(userList, userId);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userListAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load users from SameDesire by default
        loadUsersFromSameDesire();

        // Handle tab clicks (Same Desire and Blacklist)
        findViewById(R.id.tabSameDesire).setOnClickListener(v -> loadUsersFromSameDesire());
        findViewById(R.id.tabBlacklist).setOnClickListener(v -> loadUsersFromBlacklist());
    }

    private void loadUsersFromSameDesire() {
        isSameDesireTabActive = true;
        findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_active);
        findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_inactive);

        mDatabase.child("users").child(userId).child("samedesire").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdjustDesireActivity.this, "Failed to load SameDesire users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersFromBlacklist() {
        isSameDesireTabActive = false;
        findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_active);
        findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_inactive);

        mDatabase.child("users").child(userId).child("blackdesire").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                userListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdjustDesireActivity.this, "Failed to load Blacklist users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

        private List<User> userList;
        private DatabaseReference mDatabase;
        private String userId;

        public UserListAdapter(List<User> userList, String userId) {
            this.userList = userList;
            this.userId = userId;
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_user_list_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);
            holder.userName.setText(user.getUsername());
            holder.averageInteraction.setText("Avg Interaction: " + user.getRating());
            // Set the delete button behavior
            holder.deleteUserButton.setOnClickListener(v -> deleteUser(user));
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        private void deleteUser(User user) {
            if (AdjustDesireActivity.this.isSameDesireTabActive) {
                mDatabase.child("users").child(userId).child("samedesire").child(user.getUserId())
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Notify that the user is removed from the list
                                Toast.makeText(AdjustDesireActivity.this, "User removed from Same Desire list", Toast.LENGTH_SHORT).show();
                                userList.remove(user); // Remove the user from the list
                                notifyDataSetChanged(); // Refresh the list
                            }
                        });
            } else {
                mDatabase.child("users").child(userId).child("blackdesire").child(user.getUserId())
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Notify that the user is removed from the Blacklist
                                Toast.makeText(AdjustDesireActivity.this, "User removed from Blacklist", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged(); // Refresh the list
                            }
                        });
            }
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {

            TextView userName, averageInteraction;
            Button deleteUserButton;

            public UserViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.userName);
                averageInteraction = itemView.findViewById(R.id.averageInteraction);
                deleteUserButton = itemView.findViewById(R.id.deleteUserButton);
            }
        }
    }
}