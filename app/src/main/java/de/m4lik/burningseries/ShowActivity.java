package de.m4lik.burningseries;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.bumptech.glide.Glide;

import butterknife.BindView;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.ui.showFragments.EpisodesFragment;
import de.m4lik.burningseries.ui.showFragments.HosterFragment;
import de.m4lik.burningseries.ui.showFragments.SeasonsFragment;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable;

public class ShowActivity extends AppCompatActivity implements Callback<SeasonObj> {

    private String title;
    private String description;

    public Integer selectedShow;
    public Integer selectedSeason;
    public Integer selectedEpisode;

    private String visibleFragment;

    public Integer seasonCount;

    public Boolean fav = false;

    public View fragmentView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    String userSession;

    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        i = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView tbiv = (ImageView) findViewById(R.id.toolbarimage);
        title = i.getStringExtra("ShowName");
        selectedShow = i.getIntExtra("ShowID", 60);
        Uri imageUri = Uri.parse("https://s.bs.to/img/cover/" + selectedShow + ".jpg");
        toolbar.setTitle(title);

        Log.v("BS", "Lade Cover.");
        Glide.with(getApplicationContext())
                .load(imageUri)
                .into(tbiv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userSession = sharedPreferences.getString("pref_session", "");

        final MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        final String[] projection = {
                seriesTable.COLUMN_NAME_ID
        };

        final String selection = seriesTable.COLUMN_NAME_ID + " = ? AND "
                         + seriesTable.COLUMN_NAME_ISFAV + " = ?";
        final String[] selectionArgs = {selectedShow.toString(), "1"};

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 1)
            fav = true;

        c.close();
        db.close();

        if (fav)
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fav) {
                    addToFavorites();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));
                    fav = !fav;
                } else {
                    removeFromFavorites();
                    fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white));
                    fav = !fav;
                }
            }
        });

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/1");
        APIInterface apii = api.getInterface();
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

        super.onDestroy();
    }

    /*
     * Getter & Setter
     */

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

    public Integer getSeasonCount() {
        return seasonCount;
    }

    public void setSelectedEpisode(Integer selectedEpisode) {
        this.selectedEpisode = selectedEpisode;
    }

    public void setSelectedSeason(Integer selectedSeason) {
        this.selectedSeason = selectedSeason;
    }

    public void setVisibleFragment(String visibleFragment) {
        this.visibleFragment = visibleFragment;
    }

    public void setFragmentView(View fragmentView) {
        this.fragmentView = fragmentView;
    }

    /*
     * Fragment transactions
     */

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

    private void addToFavorites() {

        MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 1);
        db.update(SeriesContract.seriesTable.TABLE_NAME, cv, SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + selectedShow, null);


        String[] projection = {
                SeriesContract.seriesTable.COLUMN_NAME_ID
        };

        String selection = SeriesContract.seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                SeriesContract.seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ID)) + ",";
        favs = favs.substring(0, favs.length() - 1);

        c.close();
        db.close();


        API api = new API();
        APIInterface apiInterface = api.getInterface();
        api.setSession(userSession);
        api.generateToken("user/series/set/" + favs);
        Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void removeFromFavorites() {

        MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 0);
        db.update(SeriesContract.seriesTable.TABLE_NAME, cv, SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + selectedShow, null);

        String[] projection = {
                SeriesContract.seriesTable.COLUMN_NAME_ID
        };

        String selection = SeriesContract.seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                SeriesContract.seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ID)) + ",";
        if (!favs.equals(""))
            favs = favs.substring(0, favs.length() - 1);

        c.close();
        db.close();


        API api = new API();
        APIInterface apiInterface = api.getInterface();
        api.setSession(userSession);
        api.generateToken("user/series/set/" + favs);
        Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}
