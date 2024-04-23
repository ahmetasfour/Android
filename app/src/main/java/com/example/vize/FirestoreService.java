package com.example.vize;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class FirestoreService {
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void addDailyRun(String date, float distance, OnCompleteListener<Void> listener) {
        if (currentUser == null) {
            Log.e("FirestoreService", "Attempted to add daily run without a logged-in user.");
            return;  // User not logged in
        }
        Map<String, Object> run = new HashMap<>();
        run.put("date", date);
        run.put("distance", distance);

        db.collection("users").document(currentUser.getUid())
                .collection("dailyRuns").document(date)
                .set(run)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreService", "Daily run added successfully.");
                    } else {
                        Log.e("FirestoreService", "Failed to add daily run.", task.getException());
                    }
                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }

    public void fetchDailyRuns(OnCompleteListener<QuerySnapshot> listener) {
        if (currentUser == null) {
            Log.e("FirestoreService", "User not logged in.");
            return;
        }
        Log.d("FirestoreService", "Fetching daily runs for user: " + currentUser.getUid());
        db.collection("users").document(currentUser.getUid())
                .collection("dailyRuns").orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreService", "Successfully fetched daily runs. Number of documents: " + (task.getResult() != null ? task.getResult().size() : "null"));
                        if (listener != null) {
                            listener.onComplete(task);
                        }
                    } else {
                        Log.e("FirestoreService", "Failed to fetch daily runs", task.getException());
                        if (listener != null) {
                            listener.onComplete(task);
                        }
                    }
                });
    }

}
