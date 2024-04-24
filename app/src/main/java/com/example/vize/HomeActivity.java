package com.example.vize;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import android.net.Uri;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;
public class HomeActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


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
        FrameLayout videoContainer = findViewById(R.id.videoContainer);
        videoContainer.setVisibility(View.VISIBLE); // Show the video container

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        String embedUrl = "https://www.youtube.com/embed/yGgpjif4ogo";
        String iframeHTML = "<html><body style='margin:0;padding:0;'>" +
                "<iframe width='100%' height='100%' src='" + embedUrl +
                "' frameborder='0' allowfullscreen></iframe>" +
                "</body></html>";

        webView.loadData(iframeHTML, "text/html", "utf-8");
    }

    public void closeVideo(View view) {
        FrameLayout videoContainer = findViewById(R.id.videoContainer);
        videoContainer.setVisibility(View.GONE);
    }





    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}
