package com.example.vize;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;  // Use this if you are using AndroidX


public class PushActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView pushUpCount;
    private TextView countdownText;

    private TextView calibrationText;
    private Button startButton, pauseResumeButton;
    private LinearLayout mainLayout;
    private Vibrator vibrator;
    private int count = 0;
    private boolean isTracking = false;
    private boolean isCalibrated = false;
    private float calibrationDistance = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        countdownText = findViewById(R.id.countdownText);
        pushUpCount = findViewById(R.id.pushUpCount);
        calibrationText = findViewById(R.id.calibrationText);
        startButton = findViewById(R.id.startButton);
        pauseResumeButton = findViewById(R.id.pauseResumeButton);  // Reference the pause/resume button
        mainLayout = findViewById(R.id.mainLayout);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (proximitySensor == null) {
            pushUpCount.setText("No Proximity Sensor Found");
        }

        startButton.setOnClickListener(view -> startTracking());
        pauseResumeButton.setOnClickListener(this::togglePauseResume);  // Set up the toggle event
        mainLayout.setOnTouchListener(this);
    }
    public void stopTracking() {
        // Stop the sensor updates by unregistering the listener
        if (sensorManager != null && isTracking) {
            sensorManager.unregisterListener(this, proximitySensor);
            isTracking = false; // Update the tracking flag
        }

        pauseResumeButton.setText("Start Training");

        count = 0; // Reset the count if that's desired when stopping
        pushUpCount.setText("Push-up Count: 0"); // Reset the display of the push-up count

        // Optionally, you could show a Toast message or log info
        Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();
    }
    public void startTracking() {
        countdownText.setVisibility(View.VISIBLE); // Show the countdown text
        pushUpCount.setVisibility(View.GONE); // Hide the push-up count text
        calibrationText.setVisibility(View.GONE); // Hide the calibration text

        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.d("CountDown", "Seconds remaining: " + millisUntilFinished / 1000);
                countdownText.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                Log.d("CountDown", "Countdown finished");
                countdownText.setVisibility(View.GONE); // Hide the countdown text
                pushUpCount.setVisibility(View.VISIBLE); // Show the push-up count text
                calibrationText.setVisibility(View.VISIBLE); // Show the calibration text
                pushUpCount.setText("Push-up Count: 0"); // Reset the push-up count display
                isTracking = true;
                sensorManager.registerListener(PushActivity.this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }.start();
    }




    public void togglePauseResume(View view) {
        if (isTracking) {
            isTracking = false;
            ((Button) view).setText("Resume");
            sensorManager.unregisterListener(this);  // Pause sensor updates
        } else {
            isTracking = true;
            ((Button) view).setText("Pause");
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);  // Resume sensor updates
        }
    }


    private void vibrateDevice() {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(500);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (isTracking) {
                processProximityData(event.values[0]);
            }
        }
    }

    private void processProximityData(float proximityValue) {
        if (!isCalibrated) {
            calibrationDistance = proximityValue;
            isCalibrated = true;
            pushUpCount.setText("Push-up Count: 0");
            calibrationText.setText("Calibration Complete");
        } else {
            float distanceThreshold = calibrationDistance - 2.0f;

            if (proximityValue <= distanceThreshold) {
                count++;
                pushUpCount.setText("Push-up Count: " + count);
                vibrateDevice();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) {
            // Stop tracking and update button text to "Resume"
            stopTracking();
            pauseResumeButton.setText("Resume");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTracking) {
            startTracking();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isTracking) {
                count++;
                pushUpCount.setText("Push-up Count: " + count);
                vibrateDevice();
            }
            return true;
        }
        return false;
    }
    public void resetTraining(View view) {
        // Reset the push-up count to 0
        count = 0;  // Reset the actual counter variable to zero
        TextView pushUpCount = findViewById(R.id.pushUpCount);
        pushUpCount.setText("Push-up Count: " + count);

        // Optional: Reset any other state or variables related to the training
        // For example, if you have a timer or a counter, reset it here

        // Show a message to the user indicating that the training has been reset
        Toast.makeText(this, "Training has been reset", Toast.LENGTH_SHORT).show();
    }

    public void showHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void completeTraining(View view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> history = new HashSet<>(prefs.getStringSet("trainingHistory", new HashSet<>())); // Fetch and copy existing history
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        // Create a session string that includes the date and count
        String session = currentDate + " - Push-ups: " + count;
        history.add(session); // Add new session to history

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("trainingHistory", history); // Save the modified history
        editor.apply(); // Commit changes

        Toast.makeText(this, "Exercise completed and saved!", Toast.LENGTH_SHORT).show();

    }

}
