package de.squarecoding.burningseries;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import de.squarecoding.burningseries.showFragments.EpisodesFragment;
import de.squarecoding.burningseries.showFragments.HosterFragment;
import de.squarecoding.burningseries.showFragments.SeasonsFragment;

public class ShowActivity extends AppCompatActivity {

    String url;
    String title;
    String description;

    public String selectedSeason;
    public String selectedEpisode;
    public String selectedHoster;

    Document webDoc;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Test");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_star_white));
            }
        });

        i = getIntent();

        url = i.getStringExtra("URL");

        System.out.println("Los gehts");
        new getTitle(url).execute();

        Fragment fragment = new SeasonsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, fragment);
        transaction.commit();
    }

    /**
     * Using jSoup to get the title
     * of the selected and presented
     * show
     *
     * @string url
     */

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
        SeasonsFragment sfrag = (SeasonsFragment) getSupportFragmentManager().findFragmentByTag("SEASONS");
        if (sfrag != null && sfrag.isVisible()) {
            super.onBackPressed();
        }
        EpisodesFragment efrag = (EpisodesFragment) getSupportFragmentManager().findFragmentByTag("EPISODES");
        if (efrag != null && efrag.isVisible()) {
            switchEpisodesToSeasons();
        }
        HosterFragment hfrag = (HosterFragment) getSupportFragmentManager().findFragmentByTag("HOSTERS");
        if (hfrag != null && hfrag.isVisible()) {
            switchHosterToEpisodes();
        }

        super.onBackPressed();
    }

    /** Fragment stuff **/

    public String passUrl() {
        return url;
    }

    public String getSelectedSeason() {
        return selectedSeason;
    }

    public void setSelectedSeason(String selectedSeason) {
        this.selectedSeason = selectedSeason;
    }

    public String getSelectedEpisode() {
        return selectedEpisode;
    }

    public void setSelectedEpisode(String selectedEpisode) {
        this.selectedEpisode = selectedEpisode;
    }

    public String getSelectedHoster() {
        return selectedHoster;
    }

    public void setSelectedHoster(String selectedHoster) {
        this.selectedHoster = selectedHoster;
    }

    /** Fragment transactions **/

    public void switchSeasonsToEpisodes() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new EpisodesFragment(), "EPISODES");
        transaction.commit();
    }

    public void switchEpisodesToSeasons() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new SeasonsFragment(), "SEASONS");
        transaction.commit();
    }

    public void switchEpisodesToHosters() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, new HosterFragment(), "HOSTERS");
        transaction.commit();
    }

    public void switchHosterToEpisodes() {
        Fragment fragment = new EpisodesFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, fragment, "EPISODES");
        transaction.commit();
    }

}
