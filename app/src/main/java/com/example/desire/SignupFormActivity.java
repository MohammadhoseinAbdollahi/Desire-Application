package com.example.desire;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupFormActivity extends AppCompatActivity {

    private EditText etName, etUsername, etBirthday, etPassword, etConfirmPassword;
    private Button btnContinue;
    private CheckBox cbTerms;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signupform);

        emailText = getIntent().getStringExtra("emailText");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username);
        etBirthday = findViewById(R.id.et_birthday);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        TextView tvTerms = findViewById(R.id.tv_terms);
        btnContinue = findViewById(R.id.btn_continue);

        // Auto-format birthday input
        etBirthday.addTextChangedListener(new BirthdayTextWatcher(etBirthday));

        // Toggle checkbox when clicking "I agree to Terms and Service"
        tvTerms.setOnClickListener(v -> cbTerms.setChecked(!cbTerms.isChecked()));

        // Toggle password visibility
        etPassword.setOnTouchListener((v, event) -> handlePasswordToggle(event, etPassword));
        etConfirmPassword.setOnTouchListener((v, event) -> handlePasswordToggle(event, etConfirmPassword));

        // Continue button click listener
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }


    private boolean handlePasswordToggle(MotionEvent event, EditText editText) {
        if (event.getAction() == MotionEvent.ACTION_UP &&
                event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
            togglePasswordVisibility(editText);
            return true;
        }
        return false;
    }

    private void togglePasswordVisibility(EditText editText) {
        int selection = editText.getSelectionEnd();
        boolean isPasswordVisible = editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        int icon = isPasswordVisible ? R.drawable.ic_eye_open : R.drawable.ic_eye_closed;

        Drawable drawable = getResources().getDrawable(icon);
        drawable.setBounds(0, 0, 60, 60); // Adjust icon size (width, height in pixels)

        editText.setCompoundDrawables(null, null, drawable, null);
        editText.setInputType(isPasswordVisible ?
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setSelection(selection);
    }


    private static class BirthdayTextWatcher implements TextWatcher {
        private final EditText editText;
        private boolean isEditing = false;

        public BirthdayTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isEditing) return;
            isEditing = true;

            String clean = s.toString().replaceAll("[^\\d]", ""); // Remove non-digit characters
            String formatted = "";

            if (clean.length() > 4) {
                formatted = clean.substring(0, 4) + "/";
                if (clean.length() > 6) {
                    formatted += clean.substring(4, 6) + "/" + clean.substring(6);
                } else {
                    formatted += clean.substring(4);
                }
            } else {
                formatted = clean;
            }

            editText.setText(formatted);
            editText.setSelection(formatted.length()); // Keep cursor at correct position
            isEditing = false;
        }
    }


    private void signUp() {
        final String name = etName.getText().toString().trim();
        final String username = etUsername.getText().toString().trim();
        final String birthday = etBirthday.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean termsChecked = cbTerms.isChecked();

        // Validate input fields
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (birthday.isEmpty()) {
            etBirthday.setError("Birthday is required");
            etBirthday.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password is required");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!termsChecked) {
            Toast.makeText(this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user in Firebase Authentication
        mAuth.createUserWithEmailAndPassword(emailText, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Get the user ID from Firebase Authentication
                                String userId = firebaseUser.getUid();

                                // Create User object
                                User user = new User(userId, emailText, name, username, birthday);

                                // Save the User object to Firebase Realtime Database
                                // Assuming saveToFirebase expects a userId and a callback
                                user.saveToFirebase(userId, new User.SaveToFirebaseCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(SignupFormActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignupFormActivity.this, ProfileActivity.class);
                                        intent.putExtra("userId", userId);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Sign up failed
                                        Log.e(SignupFormActivity.class.getSimpleName(), "Sign up failed: " + task.getException().getMessage());
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            Toast.makeText(SignupFormActivity.this, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
                                            // Redirect to login activity if needed
                                            Intent intent = new Intent(SignupFormActivity.this, LoginActivity.class);
                                            intent.putExtra("emailText", emailText);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SignupFormActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                Toast.makeText(SignupFormActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignupFormActivity.this, ProfileActivity.class);
                                intent.putExtra("userId", userId);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Sign up failed
                            Log.e(SignupFormActivity.class.getSimpleName(), "Sign up failed: " + task.getException().getMessage());
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignupFormActivity.this, "The email address is already in use by another account.", Toast.LENGTH_SHORT).show();
                                // Redirect to login activity if needed
                                Intent intent = new Intent(SignupFormActivity.this, LoginActivity.class);
                                intent.putExtra("emailText", emailText);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignupFormActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
