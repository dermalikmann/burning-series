package de.m4lik.burningseries.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.showFragments.EpisodesFragment;
import de.m4lik.burningseries.ui.showFragments.HosterFragment;
import de.m4lik.burningseries.ui.showFragments.SeasonsFragment;
import de.m4lik.burningseries.util.ShowUtils;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class ShowActivity extends ActivityBase {

    private Integer selectedShow;
    private Integer selectedSeason;
    private Integer selectedEpisode;
    private String showName;
    private String episodeName;

    public Boolean fav = false;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbarimage)
    ImageView toolbarImageView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    Intent i;
    Boolean fromWatchHistory;

    private String visibleFragment = "seasons";

    @Override
    protected void injectComponent(ActivityComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().noActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        i = getIntent();

        findViewById(R.id.gradient).setBackground(ContextCompat.getDrawable(this, theme().gradient));

        showName = i.getStringExtra("ShowName");
        selectedShow = i.getIntExtra("ShowID", 60);
        fromWatchHistory = i.getBooleanExtra("ShowEpisode", false);
        if (fromWatchHistory) {
            selectedSeason = i.getIntExtra("SeasonID", 1);
            selectedEpisode = i.getIntExtra("EpisodeID", 1);
        }

        Uri imageUri = Uri.parse("https://bs.to/public/img/cover/" + selectedShow + ".jpg");
        toolbar.setTitle(showName);

        Log.v("BS", "Lade Cover.");
        Glide.with(getApplicationContext())
                .load(imageUri)
                .into(toolbarImageView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fav = ShowUtils.isFav(this, selectedShow);

        if (fav)
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));

        fab.setOnClickListener(view -> {
            if (!fav) {
                ShowUtils.addToFavorites(this, selectedShow);
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));
                fav = !fav;
            } else {
                ShowUtils.removeFromFavorites(this, selectedShow);
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white));
                fav = !fav;
            }
        });

        if (fromWatchHistory)
            visibleFragment = "hoster";
    }

    @Override
    protected void onResume() {

        switch (visibleFragment) {
            case "seasons":
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerShow, new SeasonsFragment())
                        .commit();
                break;
            case "episodes":
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerShow, new EpisodesFragment())
                        .commit();
                break;
            case "hoster":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerShow, new HosterFragment())
                        .commit();
                break;
        }

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switch (visibleFragment) {
            case "seasons":
                super.onBackPressed();
                break;
            case "episodes":
                switchEpisodesToSeasons();
                break;
            case "hoster":
                switchHosterToEpisodes();
                break;
            default:
                super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        selectedShow = null;
        selectedEpisode = null;
        selectedSeason = null;

        super.onDestroy();
    }

    /*
     * Getter & Setter
     */

    public Integer getSelectedShow() {
        return selectedShow;
    }

    public Integer getSelectedSeason() {
        return selectedSeason;
    }

    public void setSelectedSeason(Integer selectedSeason) {
        this.selectedSeason = selectedSeason;
    }

    public Integer getSelectedEpisode() {
        return selectedEpisode;
    }

    public void setSelectedEpisode(Integer selectedEpisode) {
        this.selectedEpisode = selectedEpisode;
    }

    public String getShowName() {
        return showName;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    /*
     * Fragment transactions
     */

    public void switchSeasonsToEpisodes() {

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, 0, 0)
                .replace(R.id.fragmentContainerShow, new EpisodesFragment())
                .commit();

        visibleFragment = "episodes";
    }

    public void switchEpisodesToSeasons() {

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, 0, 0)
                .replace(R.id.fragmentContainerShow, new SeasonsFragment())
                .commit();

        visibleFragment = "seasons";
    }

    public void switchEpisodesToHosters() {

        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, 0, 0)
                .replace(R.id.fragmentContainerShow, new HosterFragment())
                .commit();

        visibleFragment = "hoster";
    }

    public void switchHosterToEpisodes() {

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, 0, 0)
                .replace(R.id.fragmentContainerShow, new EpisodesFragment())
                .commit();

        visibleFragment = "episodes";
    }
}
