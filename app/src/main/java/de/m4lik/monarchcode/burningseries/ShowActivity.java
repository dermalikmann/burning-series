package de.m4lik.monarchcode.burningseries;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import de.m4lik.monarchcode.burningseries.api.API;
import de.m4lik.monarchcode.burningseries.api.APIInterface;
import de.m4lik.monarchcode.burningseries.objects.GenreMap;
import de.m4lik.monarchcode.burningseries.objects.SeasonObj;
import de.m4lik.monarchcode.burningseries.showFragments.EpisodesFragment;
import de.m4lik.monarchcode.burningseries.showFragments.HosterFragment;
import de.m4lik.monarchcode.burningseries.showFragments.SeasonsFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowActivity extends AppCompatActivity implements Callback<SeasonObj>{

    public String title;
    public String description;

    public Integer selectedShow;
    public Integer selectedSeason;
    public Integer selectedEpisode;
    public Integer selectedHoster;

    private String visibleFragment;

    public Integer seasonCount;

    public Boolean fav = false;

    Document webDoc;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        i = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(i.getStringExtra("ShowName"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fav) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white));
                    fav = !fav;
                } else {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_border_white));
                    fav = !fav;
                }
            }
        });

        selectedShow = i.getIntExtra("ShowID", 60);


        API api = new API();
        api.generateToken("series/" + selectedShow + "/1"+ "?s=" + api.getSession());
        APIInterface apii = api.getApiInterface();
        Call<SeasonObj> call = apii.getSeason(api.getToken(), api.getUserAgent(), selectedShow, 1, "");
        call.enqueue(this);

    }

    public void setDefaultFragment() {

        Fragment fragment = new SeasonsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, fragment);
        transaction.commit();

        setVisibleFragment("seasons");
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {
        SeasonObj show = response.body();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Test");
        //toolbar.setTitle(show.getSeries().getSeriesName());

        description = show.getSeries().getDescription();
        seasonCount = show.getSeries().getSeasonCount();

        setDefaultFragment();
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {

        Snackbar.make(new CoordinatorLayout(getApplicationContext()), "Fehler beim Laden der Seriendetails", Snackbar.LENGTH_SHORT);
    }

    private class getTitle extends AsyncTask<Void, Void, Void> {

        private String url;

        public getTitle(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*progressDialog = new ProgressDialog(EpisodesActivity.this);
            progressDialog.setMessage("Staffeln werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);*/

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                webDoc = Jsoup.connect(this.url).get();

                title = webDoc.select("#sp_left h2").first().text();

                /*description = webDoc.select("#sp_left div p").first().text();

                Elements seasons = webDoc.select("#sp_left ul.pages li").not(".button");
                seasons = seasons.select("a");

                for (Element season : seasons) {
                    seasonsList.add(new seasonsListItem(season.attr("abs:href"), Integer.parseInt(season.text())));
                }*/

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
            setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
        selectedHoster = null;

        super.onDestroy();
    }

    public String getDescription() {
        return description;
    }
    public Integer getSelectedShow() {
        return selectedShow;
    }
    public Integer getSelectedSeason() {
        return selectedSeason;
    }
    public Integer getSelectedEpisode() {
        return selectedEpisode;
    }
    public Integer getSelectedHoster() {
        return selectedHoster;
    }
    public Integer getSeasonCount() {
        return seasonCount;
    }

    public void setSelectedHoster(Integer selectedHoster) {
        this.selectedHoster = selectedHoster;
    }
    public void setSelectedEpisode(Integer selectedEpisode) {
        this.selectedEpisode = selectedEpisode;
    }
    public void setSelectedSeason(Integer selectedSeason) {
        this.selectedSeason = selectedSeason;
    }
    public void setSelectedShow(Integer selectedShow) {
        this.selectedShow = selectedShow;
    }
    public void setVisibleFragment(String visibleFragment) {
        this.visibleFragment = visibleFragment;
    }

    /** Fragment transactions **/

    public void switchSeasonsToEpisodes() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new EpisodesFragment(), "EPISODES");
        transaction.commit();

        setVisibleFragment("episodes");
    }

    public void switchEpisodesToSeasons() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new SeasonsFragment(), "SEASONS");
        transaction.commit();

        setVisibleFragment("seasons");
    }

    public void switchEpisodesToHosters() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new HosterFragment(), "HOSTERS");
        transaction.commit();

        setVisibleFragment("hoster");
    }

    public void switchHosterToEpisodes() {
        Fragment fragment = new EpisodesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, fragment, "EPISODES");
        transaction.commit();

        setVisibleFragment("episodes");
    }

}
