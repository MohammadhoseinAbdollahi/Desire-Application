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
import java.util.Date;
import java.util.Locale;

public class AddDesireActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private EditText editLocation;
    private EditText editCaption;
    private Button buttonHalfDay;
    private Button buttonOneMonth;
    private Button buttonOneDay;
    private Button buttonYear;
    private Button buttonWeek;
    private Button buttonNoExpiration;
    private Button buttonPost;
    private ImageView imageAdd;
    private Uri imageUri;
    private StorageReference mStorageRef;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_desire);

        mStorageRef = FirebaseStorage.getInstance().getReference("posts");

        // Get the user ID of the currently signed-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();  // This will be used to identify the user in the post
        }

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        // Set visibility button listeners
        View.OnClickListener visibilityClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                String visibility = button.getText().toString();
                Toast.makeText(AddDesireActivity.this, "Visibility: " + visibility, Toast.LENGTH_SHORT).show();
                // Store visibility in a variable or use as needed
            }
        };

        buttonHalfDay.setOnClickListener(visibilityClickListener);
        buttonOneMonth.setOnClickListener(visibilityClickListener);
        buttonOneDay.setOnClickListener(visibilityClickListener);
        buttonYear.setOnClickListener(visibilityClickListener);
        buttonWeek.setOnClickListener(visibilityClickListener);
        buttonNoExpiration.setOnClickListener(visibilityClickListener);

        // Set image add listener
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open gallery or camera
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        // Set post button listener
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = editLocation.getText().toString();
                String caption = editCaption.getText().toString();

                if (imageUri != null) {
                    uploadImageToFirebase(location, caption);
                } else {
                    Toast.makeText(AddDesireActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageAdd.setImageURI(imageUri);
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageUri = getImageUri(photo);
            imageAdd.setImageBitmap(photo);
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImageToFirebase(String location, String caption) {
        if (imageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    savePostToDatabase(location, caption, imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDesireActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void savePostToDatabase(String location, String caption, String imageUrl) {
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
    String postId = mDatabase.push().getKey();
    String currentDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(new Date());
    String postDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

    // Fetch the current user's username
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            if (user != null) {
                String username = user.getUsername();

                // Create a new post object with the current user's ID and username
                Post post = new Post(userId, postId, imageUrl, 0.0, 0, location, currentDate, caption, false, postDate, true, username);

                mDatabase.child(postId).setValue(post).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddDesireActivity.this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                        // Redirect to HomeActivity and finish this activity
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
}
