package com.example.desire;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_CODE = 100;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ImageView profileImageView;
    private TextView gainedStarsTextView, givenStarsTextView, bioTextView;
    private String userId;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImage);
        gainedStarsTextView = findViewById(R.id.gainedStars);
        givenStarsTextView = findViewById(R.id.givenStars);
        bioTextView = findViewById(R.id.profileDescription);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Get the user ID from the intent
        userId = getIntent().getStringExtra("userId");

        // Initialize Firebase Database and Storage
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mStorage = FirebaseStorage.getInstance().getReference().child("profile_pictures");

        // Retrieve user data from the database
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get user data
                    String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);

                    // Update gainedStars and givenStars
                    int rateGain = dataSnapshot.child("RateGain").getValue(Integer.class);
                    int rateGive = dataSnapshot.child("RateGive").getValue(Integer.class);

                    gainedStarsTextView.setText("Gained\n" + rateGain);
                    givenStarsTextView.setText("Given\n" + rateGive);
                    bioTextView.setText(bio);

                    // Set user data to views
                    if (profileImageUrl != null) {
                        Glide.with(ProfileActivity.this)
                                .load(profileImageUrl)
                                .into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        profileImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showImageOptions();
                return true;
            }
        });

        findViewById(R.id.settingicon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    private void showImageOptions() {
        String[] options = {"Choose from Gallery", "Take Photo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openFileChooser();
                } else if (which == 1) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                }
            }
        });
        builder.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageUri = getImageUri(imageBitmap);
            uploadImage();
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Profile Picture", null);
        return Uri.parse(path);
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = mStorage.child(userId + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri downloadUri = task.getResult();
                                            mDatabase.child("profileImageUrl").setValue(downloadUri.toString());
                                            Glide.with(ProfileActivity.this).load(downloadUri).into(profileImageView);
                                        }
                                    }
                                });
                            }
                        }
                    });
        }
    }
}
