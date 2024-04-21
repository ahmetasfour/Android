package com.example.vize;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
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
            // Handle the case where the user is not logged in
            return;
        }
        Map<String, Object> run = new HashMap<>();
        run.put("date", date);
        run.put("distance", distance);

        db.collection("users")
                .document(currentUser.getUid())
                .collection("dailyRuns")
                .document(date)  // Using date as document ID to avoid duplicate entries per day
                .set(run)
                .addOnCompleteListener(listener);
    }

    public void fetchDailyRuns(OnCompleteListener<QuerySnapshot> listener) {
        if (currentUser == null) {
            // Handle the case where the user is not logged in
            return;
        }
        db.collection("users")
                .document(currentUser.getUid())
                .collection("dailyRuns")
                .orderBy("date")
                .get()
                .addOnCompleteListener(listener);
    }
}
