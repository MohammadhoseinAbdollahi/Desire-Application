package com.example.desire;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDesireActivity extends AppCompatActivity {

    private EditText editLocation;
    private EditText editCaption;
    private Button buttonHalfDay;
    private Button buttonOneMonth;
    private Button buttonOneDay;
    private Button buttonYear;
    private Button buttonWeek;
    private Button buttonNoExpiration;
    private Button buttonPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_desire);

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

        // Set post button listener
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = editLocation.getText().toString();
                String caption = editCaption.getText().toString();

                // Handle the post action here
                Toast.makeText(AddDesireActivity.this, "Desire Posted!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set image add listener
        ImageView imageAdd = findViewById(R.id.image_add);
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle image add action here
                Toast.makeText(AddDesireActivity.this, "Add Image Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
