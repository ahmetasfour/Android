package com.example.vize;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;
import android.net.Uri;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

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

    public void playVideo(View view) {
        // Set the path of the video you want to play
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.pushup;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Hide the FloatingActionButton when the video starts
        view.setVisibility(View.INVISIBLE); // Change to INVISIBLE to maintain layout during video playback

        // Start playing the video
        videoView.start();

        // Handle video completion and errors
        videoView.setOnCompletionListener(mediaPlayer -> {
            view.setVisibility(View.VISIBLE); // Show the FloatingActionButton again
            mediaPlayer.release(); // Release media player
        });

        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            view.setVisibility(View.VISIBLE); // Show the FloatingActionButton again if error occurs
            return true; // Error handled
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}
