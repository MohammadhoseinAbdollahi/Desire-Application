package com.example.desire;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileSetting extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText editUsername, editBio;
    private Button changePhotoButton, saveButton;
    private Uri imageUri;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileSetting.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish(); // Close the current activity
            }
        });

        profileImage = findViewById(R.id.profile_image);
        editUsername = findViewById(R.id.edit_username);
        editBio = findViewById(R.id.edit_bio);
        changePhotoButton = findViewById(R.id.change_photo_button);
        saveButton = findViewById(R.id.save_button);

        // Initialize Firebase references
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(userId + ".jpg");
        }

        // Load user data
        loadUserData();

        // Change photo button click listener
        changePhotoButton.setOnClickListener(view -> openImageSelector());

        // Save button click listener
        saveButton.setOnClickListener(view -> saveProfileChanges());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String bio = snapshot.child("bio").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    // Set user data
                    editUsername.setText(username);
                    editBio.setText(bio);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ProfileSetting.this)
                                .load(profileImageUrl)
                                .circleCrop() // Apply circular crop
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.user); // Placeholder image
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileSetting.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveProfileChanges() {
        String newUsername = editUsername.getText().toString().trim();
        String newBio = editBio.getText().toString().trim();

        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the save button to prevent multiple clicks
        Button saveButton = findViewById(R.id.save_button);
        saveButton.setEnabled(false);

        // Save new profile picture if changed
        if (imageUri != null) {
            uploadProfileImage(newUsername, newBio);
        } else {
            saveUserData(newUsername, newBio, null);
        }
    }

    private void uploadProfileImage(String username, String bio) {
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveUserData(username, bio, imageUrl);
        })).addOnFailureListener(e -> Toast.makeText(ProfileSetting.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show());
    }

    private void saveUserData(String username, String bio, String imageUrl) {
        userRef.child("username").setValue(username);
        userRef.child("bio").setValue(bio);

        if (imageUrl != null) {
            userRef.child("profileImageUrl").setValue(imageUrl).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileSetting.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    refreshActivity(); // Refresh the activity
                } else {
                    Toast.makeText(ProfileSetting.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ProfileSetting.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            refreshActivity(); // Refresh the activity
        }
        saveButton.setEnabled(true);

    }

    private void refreshActivity() {
        Intent intent = getIntent(); // Get the intent that started this activity
        finish(); // Finish the current activity
        startActivity(intent); // Restart the activity
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Apply fade transition
    }


}
