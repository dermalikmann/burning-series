package de.monarchcode.m4lik.burningseries;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.devbrackets.android.exomedia.ui.widget.EMVideoView;

public class BufferedVideoPlayerActivity extends AppCompatActivity {


    private int position = 0;

    Intent intent;

    String videoURL;

    EMVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffered_video_player);

        intent = getIntent();

        videoURL = intent.getStringExtra("burning-series.videoURL");

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.black));

        Uri uri = Uri.parse(videoURL);


        videoView = (EMVideoView) findViewById(R.id.bufferedVideoView);
        videoView.setVideoURI(uri);

        videoView.start();

    }

    @Override
    protected void onPause() {
        videoView.pause();
        position = videoView.getCurrentPosition();
        super.onPause();
    }

    @Override
    protected void onResume() {
        videoView.seekTo(position);
        videoView.start();
        super.onResume();
    }
}
