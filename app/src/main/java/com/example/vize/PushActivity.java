package com.example.vize;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Vibrator;

public class PushActivity extends AppCompatActivity implements SensorEventListener, View.OnTouchListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView pushUpCount;
    private TextView calibrationText;
    private Button startButton, stopButton;
    private Vibrator vibrator;
    private int count = 0;
    private boolean isTracking = false;
    private boolean isCalibrated = false;
    private float calibrationDistance = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        pushUpCount = findViewById(R.id.pushUpCount);
        calibrationText = findViewById(R.id.calibrationText);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        if (proximitySensor == null) {
            pushUpCount.setText("No Proximity Sensor Found");
        }

        startButton.setOnClickListener(view -> startTracking());
        stopButton.setOnClickListener(view -> stopTracking());
        View mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setOnTouchListener(this);
    }

    public void startTracking() {
        isTracking = true;
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopTracking() {
        isTracking = false;
        sensorManager.unregisterListener(this);
    }

    private void vibrateDevice() {
        if (vibrator.hasVibrator()) {
            // Vibrate for 500 milliseconds
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
        // Not used in this context
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTracking();
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
}
