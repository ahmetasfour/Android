package com.example.vize;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello); // Assuming your layout file is named activity_hello.xml

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome!");
    }
}
