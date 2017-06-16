package de.m4lik.burningseries.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

import java.util.ArrayList;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.util.Settings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenVideoActivity extends ActivityBase implements Callback<EpisodeObj> {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private View mContentView;
    private VideoView videoView;

    private long position = 0;
    private boolean visible;

    private Integer show;
    private Integer season;
    private Integer episode;
    private String hoster;

    private final Runnable hideUI = this::hide;

    private final Runnable mShowPart2Runnable = () -> {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };

    private final Runnable mHidePart2Runnable = () -> {
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    };


    @Override
    protected void injectComponent(ActivityComponent appComponent) {
        appComponent.inject(this);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_video);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        visible = true;
        mContentView = findViewById(R.id.bufferedVideoView);

        mContentView.setOnClickListener(view -> toggle());

        Intent intent = getIntent();

        String videoURL = intent.getStringExtra("burning-series.videoURL");

        show = intent.getIntExtra("show", -1);
        season = intent.getIntExtra("show", -1);
        episode = intent.getIntExtra("show", -1);
        hoster = intent.getStringExtra("hoster");

        if (!Patterns.WEB_URL.matcher(videoURL).matches()) {

            DialogBuilder.start(FullscreenVideoActivity.this)
                    .title("Ungültige URL")
                    .content("Beim Parsen der Video-URL ist eine Fehler aufgetreten.\n" +
                            "Bitte melde dich bei M4lik im Forum.")
                    .positive("OK")
                    .build()
                    .show();

            Answers.getInstance().logCustom(new CustomEvent("Invalid URL")
                    .putCustomAttribute("URL", videoURL));

            return;
        }

        Uri uri = Uri.parse(videoURL);

        checkForAutoplay();

        videoView = (VideoView) mContentView;
        videoView.setVideoURI(uri);

        /*
        ImageButton test = new ImageButton(this);
        test.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_next_episode));
        test.setBackground(null);

        videoView.getVideoControls().addExtraView(test);
        */

        videoView.start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkForAutoplay() {
        API api = new API();
        api.setSession(Settings.of(this).getUserSession());
        episode++;
        api.generateToken("series/" + show + "/" + season + "/" + episode);
        APIInterface apii = api.getInterface();
        Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), show, season, episode, api.getSession());
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {
        EpisodeObj episode = response.body();

        ArrayList<String> hosters = new ArrayList<>();

        for (EpisodeObj.Hoster tmp : episode.getHosters())
            hosters.add(tmp.getName().toLowerCase());

        if (hosters.contains(hoster.toLowerCase())) {

            Snackbar.make(findViewById(android.R.id.content), "Yaay", Snackbar.LENGTH_SHORT).show();

            return;
        }


        for (String tmp : Hoster.compatibleHosters)
            if (hosters.contains(tmp)) {

                Snackbar.make(findViewById(android.R.id.content), "Yay", Snackbar.LENGTH_SHORT).show();
                // TODO: Implement auto play for any supported hoster

                return;
            }

        // Bad luck: No auto play for you...
    }

    @Override
    public void onFailure(Call<EpisodeObj> call, Throwable t) {

    }

    private void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        visible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        visible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(hideUI);
        mHideHandler.postDelayed(hideUI, delayMillis);
    }
}
