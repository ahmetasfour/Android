package com.example.vize;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Date;
import java.util.Locale;
import java.util.Map;


public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    private FusedLocationProviderClient locationClient;
    private Location lastLocation;
    private float totalDistance = 0;
    private TextView distanceTextView;
    private MapView mapView;
    private GoogleMap gMap;

    private FirestoreService firestoreService;
    private TextView dailyRunsText;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (lastLocation != null) {
                    float distance = lastLocation.distanceTo(location);
                    totalDistance += distance;
                    updateDistanceDisplay();
                }
                lastLocation = location;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);
        dailyRunsText = findViewById(R.id.day1Distance);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        locationClient = LocationServices.getFusedLocationProviderClient(this);

        updateDailyRunsDisplay();
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
            running = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
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

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(7000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null) {
                        float distance = lastLocation.distanceTo(location);
                        totalDistance += distance;
                        updateDistanceDisplay();
                    }
                    lastLocation = location;
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateDistanceDisplay() {
        distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f m", totalDistance));

        // Get SharedPreferences Editor
        SharedPreferences sharedPreferences = getSharedPreferences("RunHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store run data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(new Date());
        editor.putString(date, String.format(Locale.getDefault(), "%.2f m", totalDistance));
        editor.apply();  // Commit changes

        // Update UI with history
        updateDailyRunsDisplay();
    }

    private void updateDailyRunsDisplay() {
        SharedPreferences sharedPreferences = getSharedPreferences("RunHistory", MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            builder.append(entry.getKey()).append(" - ").append(entry.getValue()).append("\n");
        }

        if (builder.toString().isEmpty()) {
            dailyRunsText.setText("No runs logged yet.");
        } else {
            dailyRunsText.setText(builder.toString());
        }
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

    private void stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationCallback);
    }
}
