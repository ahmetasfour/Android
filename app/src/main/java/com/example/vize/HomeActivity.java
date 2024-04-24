package com.example.vize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;
import android.net.Uri;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import android.util.Log;


public class HomeActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the VideoView here to avoid finding it every time in playVideo
        VideoView videoView = findViewById(R.id.videoView);
    }

    // Method to navigate to RunActivity
    public void navigateToCardio(View view) {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }

    public void navigateToStrength(View view) {
        Intent intent = new Intent(this, PushActivity.class);
        startActivity(intent);
    }

    public void playVideo(View view) {
        VideoView videoView = findViewById(R.id.videoView); // Make sure videoView is initialized
        FloatingActionButton fab = findViewById(R.id.fab_robot_tutorial); // Initialize FAB

        // Set the path of the video you want to play
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.pushup;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Hide the FloatingActionButton when the video starts
        fab.setVisibility(View.INVISIBLE);

        // Start playing the video
        videoView.start();

        // Show the VideoView when the video is prepared and starts playing
        videoView.setOnPreparedListener(mp -> videoView.setVisibility(View.VISIBLE));

        // Handle video completion
        videoView.setOnCompletionListener(mediaPlayer -> {
            fab.setVisibility(View.VISIBLE);  // Show the FloatingActionButton again
            videoView.setVisibility(View.GONE); // Hide the VideoView
            mediaPlayer.release(); // Release media player
        });

        // Handle errors during video playback
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            fab.setVisibility(View.VISIBLE);  // Show the FloatingActionButton again if error occurs
            videoView.setVisibility(View.GONE); // Hide the VideoView
            Toast.makeText(getApplicationContext(), "Error playing video", Toast.LENGTH_SHORT).show();
            Log.e("VideoView", "Playback error - What: " + what + ", Extra: " + extra);
            return true; // Error handled
        });
    }

}
