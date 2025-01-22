package com.example.desire;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddDesireActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private EditText editLocation, editCaption;
    private Button buttonHalfDay, buttonOneMonth, buttonOneDay, buttonYear, buttonWeek, buttonNoExpiration, buttonPost;
    private ImageView imageAdd;
    private Uri imageUri;
    private StorageReference mStorageRef;
    private String userId;
    private double userRating;
    private String selectedKind;
    private String expirationDate;
    private Button selectedButton = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_desire);

        mStorageRef = FirebaseStorage.getInstance().getReference("posts");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        editLocation = findViewById(R.id.edit_location);
        editCaption = findViewById(R.id.edit_caption);
        buttonHalfDay = findViewById(R.id.button_half_day);
        buttonOneMonth = findViewById(R.id.button_one_month);
        buttonOneDay = findViewById(R.id.button_one_day);
        buttonYear = findViewById(R.id.button_year);
        buttonWeek = findViewById(R.id.button_week);
        buttonNoExpiration = findViewById(R.id.button_no_expiration);
        buttonPost = findViewById(R.id.button_post);
        imageAdd = findViewById(R.id.image_add);

        setButtonVisibilityBasedOnRating();

        // Set up button listeners to set kind and calculate expiration date
        setVisibilityButtonClickListeners();

        buttonPost.setOnClickListener(view -> {
            String location = editLocation.getText().toString();
            String caption = editCaption.getText().toString();
            buttonPost.setEnabled(false);

            if (imageUri != null) {
                uploadImageToFirebase(location, caption);
            } else {
                buttonPost.setEnabled(true);
                Toast.makeText(AddDesireActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        });

        imageAdd.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDesireActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish(); // Close the current activity
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageAdd.setImageURI(imageUri);
        }
    }

    private void setButtonVisibilityBasedOnRating() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    userRating = user.getRating();

                    buttonHalfDay.setVisibility(userRating >= 2.0 ? View.VISIBLE : View.GONE);
                    buttonOneDay.setVisibility(userRating >= 2.5 ? View.VISIBLE : View.GONE);
                    buttonWeek.setVisibility(userRating >= 3.0 ? View.VISIBLE : View.GONE);
                    buttonOneMonth.setVisibility(userRating >= 3.5 ? View.VISIBLE : View.GONE);
                    buttonYear.setVisibility(userRating >= 4.0 ? View.VISIBLE : View.GONE);
                    buttonNoExpiration.setVisibility(userRating >= 4.5 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddDesireActivity.this, "Failed to fetch user rating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setKindAndExpiration(String kind, int days, int hours) {
        this.selectedKind = kind;

        // Calculate the expiration date based on the current date, days, and hours
        if (days >= 0 || hours > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, days);
            calendar.add(Calendar.HOUR_OF_DAY, hours);
            expirationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.getTime());
        } else {
            expirationDate = "No Expiration";
        }

        Toast.makeText(this, "Kind: " + kind + "\nUntil: " + expirationDate, Toast.LENGTH_SHORT).show();
    }

    private void uploadImageToFirebase(String location, String caption) {
        if (imageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        savePostToDatabase(location, caption, imageUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(AddDesireActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    private void savePostToDatabase(String location, String caption, String imageUrl) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
        String postId = mDatabase.push().getKey();
        String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
        String postDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String username = user.getUsername();
                    Post post = new Post(userId, postId, imageUrl, 0.0, 0, location, currentDate, caption, false, postDate, true, username, 0.0, 0, 0, selectedKind, expirationDate);

                    mDatabase.child(postId).setValue(post).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddDesireActivity.this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddDesireActivity.this, HomeActivity.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AddDesireActivity.this, "Failed to add post.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddDesireActivity.this, "Failed to fetch username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVisibilityButtonClickListeners() {
        View.OnClickListener visibilityClickListener = v -> {
            Button clickedButton = (Button) v;

            // Reset the previously selected button to pink
            if (selectedButton != null) {
                selectedButton.setBackgroundResource(R.drawable.button_pink_background); // Reset to pink background
            }

            // Update the selected button's appearance
            clickedButton.setBackgroundResource(R.drawable.button_gray_background); // Change to gray background
            selectedButton = clickedButton;

            // Update the kind and expiration based on the selected button
            switch (clickedButton.getId()) {
                case R.id.button_half_day:
                    setKindAndExpiration("Half Day", 0, 12);
                    break;
                case R.id.button_one_day:
                    setKindAndExpiration("One Day", 1, 0);
                    break;
                case R.id.button_week:
                    setKindAndExpiration("One Week", 7, 0);
                    break;
                case R.id.button_one_month:
                    setKindAndExpiration("One Month", 30, 0);
                    break;
                case R.id.button_year:
                    setKindAndExpiration("One Year", 365, 0);
                    break;
                case R.id.button_no_expiration:
                    setKindAndExpiration("No Expiration", -1, 0);
                    break;
            }
        };

        // Assign the unified click listener to all buttons
        buttonHalfDay.setOnClickListener(visibilityClickListener);
        buttonOneDay.setOnClickListener(visibilityClickListener);
        buttonWeek.setOnClickListener(visibilityClickListener);
        buttonOneMonth.setOnClickListener(visibilityClickListener);
        buttonYear.setOnClickListener(visibilityClickListener);
        buttonNoExpiration.setOnClickListener(visibilityClickListener);
    }


}
