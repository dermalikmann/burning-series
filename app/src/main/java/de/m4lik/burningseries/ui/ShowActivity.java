package de.m4lik.burningseries.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.showFragments.EpisodesFragment;
import de.m4lik.burningseries.ui.showFragments.HosterFragment;
import de.m4lik.burningseries.ui.showFragments.SeasonsFragment;
import de.m4lik.burningseries.util.Settings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class ShowActivity extends ActivityBase {

    private Integer selectedShow;
    private Integer selectedSeason;
    private Integer selectedEpisode;
    private String showName;
    private String episodeName;

    public Boolean fav = false;

    @BindView(R.id.fab)
    FloatingActionButton fab;
    String userSession;
    Intent i;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView tbiv = (ImageView) findViewById(R.id.toolbarimage);
        findViewById(R.id.gradient).setBackground(getResources().getDrawable(theme().gradient));

        if (getApplicationContext().getResources().getBoolean(R.bool.isTablet)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        showName = i.getStringExtra("ShowName");
        selectedShow = i.getIntExtra("ShowID", 60);
        Uri imageUri = Uri.parse("https://bs.to/public/img/cover/" + selectedShow + ".jpg");
        toolbar.setTitle(showName);

        Log.v("BS", "Lade Cover.");
        Glide.with(getApplicationContext())
                .load(imageUri)
                .into(tbiv);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = Settings.of(getApplicationContext()).getUserSession();

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

        fab.setOnClickListener(view -> {
            if (!fav) {
                addToFavorites();
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));
                fav = !fav;
            } else {
                removeFromFavorites();
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white));
                fav = !fav;
            }
        });

        setDefaultFragment();
    }

    public void setDefaultFragment() {

        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerShow, new SeasonsFragment())
                .commit();

        visibleFragment = "seasons";
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

    public Integer getSelectedEpisode() {
        return selectedEpisode;
    }

    public String getShowName() {
        return showName;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setSelectedShow(Integer selectedShow) {
        this.selectedShow = selectedShow;
    }

    public void setSelectedSeason(Integer selectedSeason) {
        this.selectedSeason = selectedSeason;
    }

    public void setSelectedEpisode(Integer selectedEpisode) {
        this.selectedEpisode = selectedEpisode;
    }

    public void setShowName(String showName) {
        this.showName = showName;
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

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, 0, 0)
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


        if (!userSession.equals("")) {
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


        if (!userSession.equals("")) {
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
}
