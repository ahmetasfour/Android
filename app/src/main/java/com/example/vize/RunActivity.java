package com.example.vize;

import static android.content.ContentValues.TAG;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private TextView distanceTextView;

    private FirestoreService firestoreService;
    private Location startLocation;
    private Location pauseLocation;

    private TextView dailyRunsText;

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        chronometer = findViewById(R.id.chronometer);
        distanceTextView = findViewById(R.id.distanceTextView);

        firestoreService = new FirestoreService();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        updateDailyRunsDisplay();
    }*/
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_run);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);

    chronometer = findViewById(R.id.chronometer);
    distanceTextView = findViewById(R.id.distanceTextView);
    // Initialize dailyRunsText with the correct ID from your layout
    dailyRunsText = findViewById(R.id.day1Distance);

    firestoreService = new FirestoreService();
    locationClient = LocationServices.getFusedLocationProviderClient(this);

    // It's important to call updateDailyRunsDisplay after initializing dailyRunsText
    updateDailyRunsDisplay();
}

    public void startChronometer(View view) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            // Capture the start location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                startLocation = location;
                            }
                        });
            }
            running = true;
        }
    }

    public void pauseChronometer(View view) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            // Capture the stop location and calculate the distance
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                pauseLocation = location;
                                calculateDistance();
                            }
                        });
            }
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
        startLocation = null;
        pauseLocation = null;
        running = false;
    }

    private void calculateDistance() {
        if (startLocation != null && pauseLocation != null) {
            String origin = startLocation.getLatitude() + "," + startLocation.getLongitude();
            String destination = pauseLocation.getLatitude() + "," + pauseLocation.getLongitude();
            String apiKey = "AIzaSyAQ3I21JOwxLFQOIcZ-rVafBPLw-By5Cqk"; // Replace with your API key

            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray routes = response.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            JSONObject distance = leg.getJSONObject("distance");
                            String distanceText = distance.getString("text");

                            // Display the distanceText on your UI
                            distanceTextView.setText("Distance: " + distanceText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> Log.e(TAG, "Error getting directions: " + error.getMessage()));

            // Add the request to the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        }
    }

    private void updateDistanceDisplay() {
        distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f m", totalDistance));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(new Date());
        firestoreService.addDailyRun(date, totalDistance, task -> {
            if (task.isSuccessful()) {
                updateDailyRunsDisplay();
            } else {
                // Log or handle the error
                Log.e(TAG, "Error adding run to Firestore");
            }
        });
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
            locationRequest.setSmallestDisplacement(10); // Set displacement in meters

            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    Log.d("Location Update", "Location: " + location.getLatitude() + ", " + location.getLongitude() + " Accuracy: " + location.getAccuracy());
                    if (lastLocation != null && location.getAccuracy() < 20) { // Check if accuracy is within an acceptable range
                        float distance = lastLocation.distanceTo(location);
                        if (distance > 5) { // Further filter by a realistic minimum movement threshold
                            totalDistance += distance;
                            updateDistanceDisplay();
                        }
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
        firestoreService.fetchDailyRuns(task -> {
            if (task.isSuccessful()) {
                StringBuilder builder = new StringBuilder();
                if (task.getResult() != null && !task.getResult().isEmpty()) {
                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                        String date = document.getString("date");
                        Double distance = document.getDouble("distance");
                        builder.append(String.format(Locale.getDefault(), "%s - %.2f m\n", date, distance));
                    }
                    dailyRunsText.setText(builder.toString());
                } else {
                    dailyRunsText.setText("No runs logged yet.");
                }
            } else {
                dailyRunsText.setText("Failed to load runs.");
                Log.e("RunActivity", "Error getting documents: ", task.getException());
            }
        });
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
