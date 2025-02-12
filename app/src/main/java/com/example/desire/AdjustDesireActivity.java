package com.example.desire;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
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

    private static final String TAG = "AdjustDesireActivity";

    private RecyclerView userListRecyclerView;
    private UserListAdapter userListAdapter;
    private List<User> userList;
    private DatabaseReference mDatabase;
    private String userId;
    private boolean isSameDesireTabActive = true;  // Track active tab
    private SeekBar rateBar;
    private TextView rateTextView;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_desire);

        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userList = new ArrayList<>();
        userListAdapter = new UserListAdapter(userList, userId);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userListAdapter);

        rateBar = findViewById(R.id.rateBar);
        rateTextView = findViewById(R.id.rateTextView);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadCurrentUser();

        // Load users from SameDesire by default
        loadUsersFromSameDesire();

        // Handle tab clicks (Same Desire and Blacklist)
        findViewById(R.id.tabSameDesire).setOnClickListener(v -> loadUsersFromSameDesire());
        findViewById(R.id.tabBlacklist).setOnClickListener(v -> loadUsersFromBlacklist());

        // Handle SeekBar changes
        rateBar.setMax(40); // Set max to 40 for 0.1 increments between 1 and 5
        rateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rate = 1 + (progress / 10.0); // Map progress to range 1 to 5
                rateTextView.setText("Rate: " + rate);
                if (currentUser != null) {
                    currentUser.desiredrate = rate;
                    currentUser.saveToFirebase(userId, new User.SaveToFirebaseCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(AdjustDesireActivity.this, "Desired rate updated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdjustDesireActivity.this, "Failed to update desired rate", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

    private void loadCurrentUser() {
        Log.d(TAG, "Loading current user data...");
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    Log.d(TAG, "Current user data loaded: " + currentUser.getUsername());
                    rateBar.setProgress((int) (currentUser.desiredrate * 10));
                    rateTextView.setText("Rate: " + currentUser.desiredrate);
                } else {
                    Log.e(TAG, "Failed to load current user data: User data is null");
                    Toast.makeText(AdjustDesireActivity.this, "User data is not in the expected format", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load current user data: " + databaseError.getMessage());
                Toast.makeText(AdjustDesireActivity.this, "Failed to load current user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersFromSameDesire() {
        isSameDesireTabActive = true;
        findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_active);
        findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_inactive);

        Log.d(TAG, "Loading users from SameDesire...");
        mDatabase.child("users").child(userId).child("samedesire").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subUserId = snapshot.getValue(String.class);
                    if (subUserId != null && !subUserId.isEmpty()) {
                        Log.d(TAG, "Loading user details for subUserId: " + subUserId);
                        loadUserDetails(subUserId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load SameDesire users: " + databaseError.getMessage());
                Toast.makeText(AdjustDesireActivity.this, "Failed to load SameDesire users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersFromBlacklist() {
        isSameDesireTabActive = false;
        findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_active);
        findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_inactive);

        Log.d(TAG, "Loading users from Blacklist...");
        mDatabase.child("users").child(userId).child("blackdesire").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subUserId = snapshot.getValue(String.class);
                    if (subUserId != null && !subUserId.isEmpty()) {
                        Log.d(TAG, "Loading user details for subUserId: " + subUserId);
                        loadUserDetails(subUserId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load Blacklist users: " + databaseError.getMessage());
                Toast.makeText(AdjustDesireActivity.this, "Failed to load Blacklist users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDetails(String subUserId) {
        Log.d(TAG, "Fetching user details for subUserId: " + subUserId);
        mDatabase.child("users").child(subUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot for subUserId " + subUserId + ": " + dataSnapshot.toString());
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Log.d(TAG, "User details loaded: " + user.getUsername());
                    userList.add(user);
                    userListAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Failed to load user details: User data is null for subUserId: " + subUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load user details: " + databaseError.getMessage());
                Toast.makeText(AdjustDesireActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show();
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
            if (user == null) {
                Log.e(TAG, "Cannot delete user: User is null");
                Toast.makeText(AdjustDesireActivity.this, "Cannot delete user: User is null", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.getUserId() == null) {
                Log.e(TAG, "Cannot delete user: User ID is null");
                Toast.makeText(AdjustDesireActivity.this, "Cannot delete user: User ID is null", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == null) {
                Log.e(TAG, "Cannot delete user: Main User ID is null");
                Toast.makeText(AdjustDesireActivity.this, "Cannot delete user: Main User ID is null", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Deleting user with ID: " + user.getUserId() + " from main user ID: " + userId);

            DatabaseReference userRef;
            if (AdjustDesireActivity.this.isSameDesireTabActive) {
                userRef = mDatabase.child("users").child(userId).child("samedesire").child(user.getUserId());
            } else {
                userRef = mDatabase.child("users").child(userId).child("blackdesire").child(user.getUserId());
            }

            userRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdjustDesireActivity.this, "User removed from list", Toast.LENGTH_SHORT).show();
                    userList.remove(user);
                    notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Failed to remove user: " + task.getException().getMessage());
                    Toast.makeText(AdjustDesireActivity.this, "Failed to remove user", Toast.LENGTH_SHORT).show();
                }
            });
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