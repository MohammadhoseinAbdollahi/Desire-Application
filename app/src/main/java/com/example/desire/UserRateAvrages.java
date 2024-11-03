package com.example.desire;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class UserRateAvrages {

    public void saveUserInteraction(String mainUserId, UserInteraction interaction) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("userInteractions")
                .child(mainUserId)
                .child(interaction.subUserId);

        Map<String, Object> interactionValues = interaction.toMap();
        databaseReference.setValue(interactionValues);
    }
}