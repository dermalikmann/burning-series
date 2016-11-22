package de.monarchcode.m4lik.burningseries;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.VideoView;

public class simpleVideoPlayer extends Activity /*implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener */ {

    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;

    private int position = 0;

    Intent intent;

    String videoURL;
    String header;

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        /*vidSurface = (SurfaceView) findViewById(R.id.surfView);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);*/

        intent = getIntent();

        videoURL = intent.getStringExtra("burning-series.videoURL");
        //header = intent.getStringExtra("burning-series.videoURL.header");

        Uri uri = Uri.parse(videoURL);

        videoView = (VideoView) findViewById(R.id.videoView);
        //if (header.equals(""))
            videoView.setVideoURI(uri);
        /*else {
            Map<String, String> map = new HashMap<>();
            map.put("Referer", header);
            videoView.setVideoURI(uri,map);
        }*/

        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(videoView);
        videoView.setMediaController(vidControl);

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
        videoView.pause();
        super.onResume();
    }
}
