package com.example.vize;

        import android.Manifest;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.util.Log;
        import android.view.View;
        import android.widget.Chronometer;
        import android.widget.TextView;
        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;

        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationCallback;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationResult;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.MapView;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.firebase.firestore.DocumentSnapshot;

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Collections;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Locale;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gMap;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private FusedLocationProviderClient locationClient;
    private Location lastLocation;
    private float totalDistance = 0;
    private TextView distanceTextView, day1Distance, day2Distance, day3Distance;
    private FirestoreService firestoreService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);
        day1Distance = findViewById(R.id.day1Distance);
        day2Distance = findViewById(R.id.day2Distance);
        day3Distance = findViewById(R.id.day3Distance);
        firestoreService = new FirestoreService();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        updateDailyRunsDisplay();
    }

    public void startChronometer(View view) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            startLocationUpdates();
            running = true;
        }
    }

    public void pauseChronometer(View view) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            stopLocationUpdates();
            running = false;
        }
    }

    public void resetChronometer(View view) {
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        totalDistance = 0;
        updateDistanceDisplay();
        lastLocation = null;
        running = false;
    }

    private void updateDistanceDisplay() {
        distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f m", totalDistance));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        setupMap();
    }

    private void setupMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(location);
                        totalDistance += distance;
                        updateDistanceDisplay();
                    }
                    lastLocation = location;
                }
            }
        }
    };

    private void stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback);
    }

    private void updateDailyRunsDisplay() {
        // Assume firestoreService is a properly initialized FirestoreService object
        firestoreService.fetchDailyRuns(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                updateUIWithDistances(documents);
            } else {
                Log.e("RunActivity", "Error getting documents: ", task.getException());
            }
        });
    }

    private void updateUIWithDistances(List<DocumentSnapshot> documents) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String today = dateFormat.format(new Date());
        HashMap<String, Float> lastThreeDays = new HashMap<>();

        // Initialize last three days with 0 distance
        for (int i = 0; i < 3; i++) {
            calendar.add(Calendar.DATE, -i);
            lastThreeDays.put(dateFormat.format(calendar.getTime()), 0f);
        }

        // Calculate distances for the last three days
        for (DocumentSnapshot document : documents) {
            String date = document.getString("date");
            Float distance = document.getDouble("distance").floatValue();
            if (lastThreeDays.containsKey(date)) {
                lastThreeDays.put(date, lastThreeDays.get(date) + distance);
            }
        }

        List<String> dates = new ArrayList<>(lastThreeDays.keySet());
        Collections.sort(dates, Collections.reverseOrder());

        day1Distance.setText(dates.get(0) + " - " + String.format(Locale.getDefault(), "%.2f m", lastThreeDays.get(dates.get(0))));
        day2Distance.setText(dates.get(1) + " - " + String.format(Locale.getDefault(), "%.2f m", lastThreeDays.get(dates.get(1))));
        day3Distance.setText(dates.get(2) + " - " + String.format(Locale.getDefault(), "%.2f m", lastThreeDays.get(dates.get(2))));
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (running) {
            startLocationUpdates();
        }
        updateDailyRunsDisplay();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (running) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}