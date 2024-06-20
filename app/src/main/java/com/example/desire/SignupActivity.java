package com.example.desire;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView signupButtonTextView = findViewById(R.id.signupButtonTextView);
        signupButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignup();
            }
        });

        TextView googleSignupTextView = findViewById(R.id.googleSignupTextView);
        googleSignupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGoogleSignup();
            }
        });

        TextView termsAndConditionsTextView = findViewById(R.id.termsAndConditionsTextView);
        String fullText = "By clicking continue, you agree to our Terms of Service and Privacy Policy";

        SpannableString spannableString = new SpannableString(fullText);

        int indexOfTermsOfService = fullText.indexOf("Terms of Service");
        int indexOfPrivacyPolicy = fullText.indexOf("Privacy Policy");

        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                indexOfTermsOfService, indexOfTermsOfService + "Terms of Service".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                indexOfPrivacyPolicy, indexOfPrivacyPolicy + "Privacy Policy".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsAndConditionsTextView.setText(spannableString);
    }

    private void performSignup() {
        TextView emailTextView = findViewById(R.id.emailTextView);
        String email = emailTextView.getText().toString().trim();

        // Since the XML provided does not include password fields,
        // the example will proceed without password validation.

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignupActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Add your signup logic here (e.g., database operation, API call)
        boolean isSignupSuccessful = true; // Placeholder for actual signup logic

        if (isSignupSuccessful) {
            Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignupActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignupActivity.this, "Signup failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performGoogleSignup() {
        // TODO: Implement Google Signup logic
        Toast.makeText(SignupActivity.this, "Google signup is not implemented yet", Toast.LENGTH_SHORT).show();
    }
}
