package com.example.desire;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserRateAvrages {

    public interface UserAverageInteractionCallback {
        void onCallback(double averageInteraction);
    }

    public void getUserAverageInteraction(String mainUserId, String subUserId, UserAverageInteractionCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("userInteractions")
                .child(mainUserId)
                .child(subUserId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInteraction interaction = dataSnapshot.getValue(UserInteraction.class);
                if (interaction != null) {
                    callback.onCallback(interaction.getAverageRating());
                } else {
                    callback.onCallback(0.0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onCallback(0.0);
            }
        });
    }
}