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
    private boolean isSameDesireTabActive = true;
    private SeekBar rateBar;
    private TextView rateTextView;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_desire);

        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userList = new ArrayList<>();
        rateBar = findViewById(R.id.rateBar);
        rateTextView = findViewById(R.id.rateTextView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        }

        if (userId == null) {
            Log.e(TAG, "User ID is null. User might not be authenticated.");
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "User ID: " + userId);

        userListAdapter = new UserListAdapter(userList, userId);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userListAdapter);

        loadCurrentUser();
        loadUsersFromSameDesire();

        findViewById(R.id.tabSameDesire).setOnClickListener(v -> {
            isSameDesireTabActive = true;
            findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_active);
            findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_inactive);
            loadUsersFromSameDesire();
        });

        findViewById(R.id.tabBlacklist).setOnClickListener(v -> {
            isSameDesireTabActive = false;
            findViewById(R.id.tabBlacklist).setBackgroundResource(R.drawable.tab_active);
            findViewById(R.id.tabSameDesire).setBackgroundResource(R.drawable.tab_inactive);
            loadUsersFromBlacklist();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        rateBar.setMax(40);
        rateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rate = 1 + (progress / 10.0);
                rateTextView.setText("Rate: " + rate);
                if (currentUser != null) {
                    currentUser.desiredrate = rate;
                    saveDesiredRateToFirebase(rate);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadCurrentUser() {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    double savedRate = currentUser.desiredrate;
                    int progress = (int) ((savedRate - 1) * 10);
                    rateBar.setProgress(progress);
                    rateTextView.setText("Rate: " + savedRate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void saveDesiredRateToFirebase(double rate) {
        mDatabase.child("users").child(userId).child("desiredrate").setValue(rate);
    }

    private void loadUsersFromSameDesire() {
        loadUsers("samedesire");
    }

    private void loadUsersFromBlacklist() {
        loadUsers("blackdesire");
    }

    private void loadUsers(String type) {
        userList.clear();
        userListAdapter.notifyDataSetChanged();

        mDatabase.child("users").child(userId).child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String subUserId = snapshot.getValue(String.class);
                    if (subUserId != null && !subUserId.isEmpty()) {
                        loadUserDetails(subUserId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void loadUserDetails(String subUserId) {
        mDatabase.child("users").child(subUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                    userListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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
            holder.deleteUserButton.setOnClickListener(v -> deleteUser(user));
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        private void deleteUser(User user) {
            String userCategory = isSameDesireTabActive ? "samedesire" : "blackdesire";
            DatabaseReference userRef = mDatabase.child("users").child(userId).child(userCategory);

            userRef.orderByValue().equalTo(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue();
                    }
                    userList.remove(user);
                    notifyDataSetChanged();
                    Toast.makeText(AdjustDesireActivity.this, "User removed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            TextView userName;
            Button deleteUserButton;

            public UserViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.userName);
                deleteUserButton = itemView.findViewById(R.id.deleteUserButton);
            }
        }
    }
}