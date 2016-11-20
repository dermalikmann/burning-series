package de.monarchcode.m4lik.burningseries;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.jsoup.nodes.Document;

import de.monarchcode.m4lik.burningseries.api.API;
import de.monarchcode.m4lik.burningseries.api.APIInterface;
import de.monarchcode.m4lik.burningseries.database.MainDBHelper;
import de.monarchcode.m4lik.burningseries.database.SeriesContract.favoritesTable;
import de.monarchcode.m4lik.burningseries.objects.SeasonObj;
import de.monarchcode.m4lik.burningseries.showFragments.EpisodesFragment;
import de.monarchcode.m4lik.burningseries.showFragments.HosterFragment;
import de.monarchcode.m4lik.burningseries.showFragments.SeasonsFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowActivity extends AppCompatActivity implements Callback<SeasonObj> {

    private String title;
    private String genre;
    private String description;

    public Integer selectedShow;
    public Integer selectedSeason;
    public Integer selectedEpisode;
    public Integer selectedHoster;

    private String visibleFragment;

    public Integer seasonCount;

    public Boolean fav = false;

    public View fragmentView;

    String userSession;

    Document webDoc;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        i = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView tbiv = (ImageView) findViewById(R.id.toolbarimage);
        title = i.getStringExtra("ShowName");
        genre = i.getStringExtra("ShowGenre");
        selectedShow = i.getIntExtra("ShowID", 60);
        Uri imageUri = Uri.parse("https://s.bs.to/img/cover/" + selectedShow + ".jpg");
        toolbar.setTitle(title);

        Log.v("BS", "Lade Cover.");
        Picasso.with(getApplicationContext())
                .load(imageUri)
                .into(tbiv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                "de.monarchcode.m4lik.burningseries.LOGIN",
                Context.MODE_PRIVATE);
        userSession = sharedPreferences.getString("session", "");

        System.out.println(selectedShow);
        Log.d("BS", "Checking for Fav");
        MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                favoritesTable.COLUMN_NAME_ID
        };

        String selection = favoritesTable.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {selectedShow.toString()};

        Cursor c = db.query(
                favoritesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 1)
            fav = true;

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fav)
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fav) {
                    ContentValues cv = new ContentValues();
                    cv.put(favoritesTable.COLUMN_NAME_ID, selectedShow);
                    cv.put(favoritesTable.COLUMN_NAME_TILTE, title);
                    cv.put(favoritesTable.COLUMN_NAME_GENRE, genre);
                    db.insert(favoritesTable.TABLE_NAME, null, cv);
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));
                    fav = !fav;
                } else {
                    db.delete(favoritesTable.TABLE_NAME, favoritesTable.COLUMN_NAME_ID + " = " + selectedShow, null);
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white));
                    fav = !fav;
                }
            }
        });

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/1");
        APIInterface apii = api.getApiInterface();
        Call<SeasonObj> call = apii.getSeason(api.getToken(), api.getUserAgent(), selectedShow, 1, api.getSession());
        call.enqueue(this);

    }

    public void setDefaultFragment() {

        Fragment fragment = new SeasonsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerShow, fragment);
        transaction.commit();

        fragmentView = fragment.getView();

        setVisibleFragment("seasons");
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {
        SeasonObj show = response.body();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        description = show.getSeries().getDescription();
        seasonCount = show.getSeries().getSeasonCount();

        setDefaultFragment();
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {

        Snackbar.make(findViewById(android.R.id.content), "Fehler beim Laden der Seriendetails", Snackbar.LENGTH_SHORT);
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

    /**
     * Getter & Setter
     **/

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

    public void setFragmentView(View fragmentView) {
        this.fragmentView = fragmentView;
    }

    /**
     * Fragment transactions
     **/

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
