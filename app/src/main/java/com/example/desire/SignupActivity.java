package com.example.desire;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        LinearLayout signupButtonLayout = findViewById(R.id.signupButtonLayout);
        EditText emailSignupEditText = findViewById(R.id.emailsignup);

        signupButtonLayout.setOnClickListener(v -> {
            String emailText = emailSignupEditText.getText().toString().trim();
            if (TextUtils.isEmpty(emailText) || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(SignupActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(SignupActivity.this, SignupFormActivity.class);
                intent.putExtra("emailText", emailText);
                startActivity(intent);
            }
        });

        TextView googleSignupTextView = findViewById(R.id.googleSignupTextView);
        googleSignupTextView.setOnClickListener(v -> performGoogleSignup());

        TextView termsAndConditionsTextView = findViewById(R.id.termsAndConditionsTextView);
        String fullText = "By clicking continue, you agree to our Terms of Service and Privacy Policy";

        SpannableString spannableString = new SpannableString(fullText);
        int indexOfTermsOfService = fullText.indexOf("Terms of Service");
        int indexOfPrivacyPolicy = fullText.indexOf("Privacy Policy");

        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                indexOfTermsOfService, indexOfTermsOfService + "Terms of Service".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                indexOfPrivacyPolicy, indexOfPrivacyPolicy + "Privacy Policy".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsAndConditionsTextView.setText(spannableString);
    }

    private void performGoogleSignup() {

        Toast.makeText(SignupActivity.this, "Google signup is not implemented yet", Toast.LENGTH_SHORT).show();
    }
}
