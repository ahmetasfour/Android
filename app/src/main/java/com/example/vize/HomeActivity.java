package com.example.vize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Initialization code, if any
    }

    // Method to navigate to RunActivity
    public void navigateToCardio(View view) {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }
}

