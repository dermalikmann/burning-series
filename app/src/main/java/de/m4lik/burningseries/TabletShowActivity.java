package de.m4lik.burningseries;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.FullShowObj;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class TabletShowActivity extends ActivityBase {

    Integer currentShow;
    Integer currentSeason;
    Integer currentEpisode;
    Integer seasonCount;

    Boolean fav = false;
    Boolean loaded = false;

    @BindView(R.id.favButton)
    Button favButton;

    @BindView(R.id.descriptionTV)
    TextView descriptionTV;

    @BindView(R.id.seasonsListView)
    ListView seasonsListView;

    @BindView(R.id.episodesListView)
    ListView episodesListView;

    @BindView(R.id.hosterListView)
    ListView hosterListView;

    @BindView(R.id.genresTV)
    TextView genresTV;

    @BindView(R.id.yearsTV)
    TextView yearsTV;

    //@BindView(R.id.mainActorTV)
    //TextView mainActorTV;

    @BindView(R.id.producersTV)
    TextView producersTV;

    @BindView(R.id.directionTV)
    TextView directionTV;

    @BindView(R.id.authorsTV)
    TextView authorsTV;

    @BindView(R.id.coverImageView)
    ImageView coverImageView;

    String userSession;

    ArrayList<SeasonListItem> seasonListItems = new ArrayList<>();
    ArrayList<EpisodeListItem> episodeListItems = new ArrayList<>();
    ArrayList<HosterListItem> hosterListItems = new ArrayList<>();

    Intent i;

    private String title;

    @Override
    protected void injectComponent(ActivityComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(theme().basic);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tablet);

        i = getIntent();

        findViewById(R.id.descriptionTV).setBackground(getResources().getDrawable(theme().listItemBackground));

        title = i.getStringExtra("ShowName");
        currentShow = i.getIntExtra("ShowID", 60);
        Uri imageUri = Uri.parse("https://bs.to/public/img/cover/" + currentShow + ".jpg");
        getSupportActionBar().setTitle(title);

        Log.v("BS", "Lade Cover.");
        Glide.with(getApplicationContext())
                .load(imageUri)
                .into(coverImageView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userSession = sharedPreferences.getString("pref_session", "");

        fav = isFav();

        final Drawable favStar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white);
        favStar.setBounds(0, 0, 50, 50);
        final Drawable notFavStar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white);
        notFavStar.setBounds(0, 0, 50, 50);
        if (fav)
            favButton.setCompoundDrawables(favStar, null, null, null);
        else
            favButton.setCompoundDrawables(notFavStar, null, null, null);

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!userSession.equals("")) {
                    if (!fav) {
                        addToFavorites();
                        favButton.setCompoundDrawables(favStar, null, null, null);
                        fav = !fav;
                    } else {
                        removeFromFavorites();
                        favButton.setCompoundDrawables(notFavStar, null, null, null);
                        fav = !fav;
                    }

                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Die Favoriten sind nur verfügbar wenn du angemeldet bist.", Snackbar.LENGTH_SHORT);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), theme().primaryColorDark));
                    snackbar.show();
                }
            }
        });

        showSeason(1);
        showEpisode(1, 1);
    }

    public void showSeason(final Integer season) {

        API api = new API();
        APIInterface apii = api.getInterface();
        api.setSession(userSession);
        api.generateToken("series/" + currentShow + "/" + season);
        Call<SeasonObj> seasonCall = apii.getSeason(api.getToken(), api.getUserAgent(), currentShow, season, api.getSession());
        seasonCall.enqueue(new Callback<SeasonObj>() {
            @Override
            public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {
                currentSeason = season;

                SeasonObj season = response.body();

                if (!loaded)
                    refreshSeries(season);

                episodeListItems = new ArrayList<>();

                for (SeasonObj.Episode episode : season.getEpisodes()) {
                    episodeListItems.add(new EpisodeListItem(episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID(), episode.isWatched() == 1));
                }

                refreshEpisodesList();
            }

            @Override
            public void onFailure(Call<SeasonObj> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        "Fehler beim Laden der Seriendetails",
                        Snackbar.LENGTH_SHORT
                );
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), theme().primaryColorDark));
                snackbar.show();
            }
        });
    }

    public void showEpisode(final Integer season, final Integer episode) {
        API api = new API();
        APIInterface apii = api.getInterface();
        api.setSession(userSession);
        api.generateToken("series/" + currentShow + "/" + season + "/" + episode);
        Call<EpisodeObj> episodeCall = apii.getEpisode(api.getToken(), api.getUserAgent(), currentShow, season, episode, api.getSession());
        episodeCall.enqueue(new Callback<EpisodeObj>() {
            @Override
            public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {
                EpisodeObj episode = response.body();

                hosterListItems = new ArrayList<>();

                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (Hoster.compatibleHosters.contains(hoster.getHoster()))
                        hosterListItems.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart(), true));
                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (!Hoster.compatibleHosters.contains(hoster.getHoster()))
                        hosterListItems.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

                refreshHosterList();
            }

            @Override
            public void onFailure(Call<EpisodeObj> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(
                        findViewById(android.R.id.content),
                        "Fehler beim Laden der Seriendetails",
                        Snackbar.LENGTH_SHORT
                );
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), theme().primaryColorDark));
                snackbar.show();
            }
        });

    }

    private void refreshSeries(SeasonObj seasonObj) {
        FullShowObj show = seasonObj.getSeries();

        descriptionTV.setText(show.getDescription());

        String genres = show.getData().getMainGenre();
        if (show.getData().getGenre() != null)
            for (String genre : show.getData().getGenre())
                genres += ", " + genre;

        genresTV.setText(genres);

        String years = show.getStartYear() == null ? "? - " : show.getStartYear() + " - ";
        years += (show.getEndYear() == null || show.getEndYear() == 0) ? "?" : show.getEndYear();
        yearsTV.setText(years);

        String producers = "";
        if (show.getData().getProducer() != null)
            for (String producer : show.getData().getProducer())
                if (producer.equals(show.getData().getProducer()[0]))
                    producers = producer;
                else
                    producers += ", " + producer;
        else
            producers = "Keine Angaben";
        producersTV.setText(producers);

        String directors = "";
        if (show.getData().getDirector() != null)
            for (String director : show.getData().getDirector())
                if (director.equals(show.getData().getDirector()[0]))
                    directors = director;
                else
                    directors += ", " + director;
        else
            directors = "Keine Angaben";
        directionTV.setText(directors);

        String authors = "";
        if (show.getData().getAuthor() != null)
            for (String author : show.getData().getAuthor())
                if (author.equals(show.getData().getAuthor()[0]))
                    authors = author;
                else
                    authors += ", " + author;
        else
            authors = "Keine Angaben";
        authorsTV.setText(authors);


        for (int i = 1; i <= show.getSeasonCount(); i++) {
            seasonListItems.add(new SeasonListItem(i));
        }

        seasonsListView.setAdapter(new SeasonsListAdapter());
    }

    private void refreshEpisodesList() {
        ArrayAdapter<EpisodeListItem> adapter = new EpisodesListAdapter();
        episodesListView.setAdapter(adapter);

        Integer numOfItems = adapter.getCount();

        Integer totalItemsHeigt = 0;
        for (int pos = 0; pos < numOfItems; pos++) {
            View item = adapter.getView(pos, null, episodesListView);
            item.measure(0, 0);
            totalItemsHeigt += item.getMeasuredHeight();
        }

        Integer totalDividersHeight = episodesListView.getDividerHeight() * (numOfItems - 1);
        ViewGroup.LayoutParams params = episodesListView.getLayoutParams();
        params.height = totalItemsHeigt + totalDividersHeight;
        episodesListView.setLayoutParams(params);
        episodesListView.requestLayout();

        episodesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {

                final TextView idView = (TextView) view.findViewById(R.id.episodeId);
                Integer selectedEpisode = Integer.parseInt(idView.getText().toString());

                final API api = new API();
                api.setSession(userSession);
                api.generateToken("series/" + currentShow + "/" + currentSeason + "/" + selectedEpisode);
                APIInterface apii = api.getInterface();
                Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), currentShow, currentSeason, selectedEpisode, api.getSession());
                call.enqueue(new Callback<EpisodeObj>() {
                    @Override
                    public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {

                        Integer episodeID = response.body().getEpisode().getEpisodeId();

                        api.generateToken("unwatch/" + episodeID);
                        APIInterface apii = api.getInterface();
                        Call<VideoObj> ucall = apii.unwatch(api.getToken(), api.getUserAgent(), episodeID, api.getSession());
                        ucall.enqueue(new Callback<VideoObj>() {
                            @Override
                            public void onResponse(Call<VideoObj> call, Response<VideoObj> response) {

                                TextView titleGerView = (TextView) view.findViewById(R.id.episodeTitleGer);
                                titleGerView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

                                ImageView fav = (ImageView) view.findViewById(R.id.watchedImageView);
                                fav.setImageDrawable(null);
                            }

                            @Override
                            public void onFailure(Call<VideoObj> call, Throwable t) {

                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<EpisodeObj> call, Throwable t) {

                    }
                });
                return true;
            }
        });

        episodesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EpisodeListItem clickedEpisode = episodeListItems.get(position);
                ((TextView) findViewById(R.id.episodeName)).setText(clickedEpisode.getTitleGer().equals("") ? clickedEpisode.getTitle() : clickedEpisode.getTitleGer());
                TextView idView = (TextView) view.findViewById(R.id.episodeId);
                showEpisode(currentSeason, Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    private void refreshHosterList() {
        ArrayAdapter<HosterListItem> adapter = new HosterListAdpter();
        hosterListView.setAdapter(adapter);

        hosterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                if (Settings.of(getApplicationContext()).alarmOnMobile() &&
                        AndroidUtility.isOnMobile(getApplicationContext())) {

                    DialogBuilder.start(TabletShowActivity.this)
                            .title("Mobile Daten")
                            .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                            .positive("Weiter", new DialogBuilder.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialog) {
                                    TextView idView = (TextView) view.findViewById(R.id.linkId);
                                    showVideo(Integer.parseInt(idView.getText().toString()));
                                }
                            })
                            .negative("Abbrechen")
                            .build()
                            .show();

                } else {
                    TextView idView = (TextView) view.findViewById(R.id.linkId);
                    showVideo(Integer.parseInt(idView.getText().toString()));
                }
            }
        });
    }

    private void showVideo(Integer id) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        API api = new API();
        api.setSession(sharedPreferences.getString("pref_session", ""));
        api.generateToken("watch/" + id);
        APIInterface apii = api.getInterface();
        Call<VideoObj> call = apii.watch(api.getToken(), api.getUserAgent(), id, api.getSession());
        call.enqueue(new Callback<VideoObj>() {
            @Override
            public void onResponse(Call<VideoObj> call, Response<VideoObj> response) {
                VideoObj videoObj = response.body();

                new getVideo(videoObj).execute();
            }

            @Override
            public void onFailure(Call<VideoObj> call, Throwable t) {

            }
        });
    }

    private boolean isFav() {

        boolean fav;

        MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] projection = {
                seriesTable.COLUMN_NAME_ID
        };

        String selection = seriesTable.COLUMN_NAME_ID + " = ? AND "
                + seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {currentShow.toString(), "1"};

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        fav = c.getCount() == 1;

        c.close();
        db.close();

        return fav;
    }

    private void addToFavorites() {

        MainDBHelper dbHelper = new MainDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(seriesTable.COLUMN_NAME_ISFAV, 1);
        db.update(seriesTable.TABLE_NAME, cv, seriesTable.COLUMN_NAME_ID + " = " + currentShow, null);


        String[] projection = {
                seriesTable.COLUMN_NAME_ID
        };

        String selection = seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)) + ",";
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
        cv.put(seriesTable.COLUMN_NAME_ISFAV, 0);
        db.update(seriesTable.TABLE_NAME, cv, seriesTable.COLUMN_NAME_ID + " = " + currentShow, null);

        String[] projection = {
                seriesTable.COLUMN_NAME_ID
        };

        String selection = seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)) + ",";
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

    class SeasonsListAdapter extends ArrayAdapter<SeasonListItem> {

        public SeasonsListAdapter() {
            super(getApplicationContext(), R.layout.list_item_seasons, seasonListItems);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_seasons, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            SeasonListItem current = seasonListItems.get(pos);

            TextView label = (TextView) view.findViewById(R.id.seasonLabel);
            label.setText(getString(R.string.season) + current.getSeasonId());

            TextView urlText = (TextView) view.findViewById(R.id.seasonId);
            urlText.setText(current.getSeasonId().toString());

            return view;
        }
    }

    class EpisodesListAdapter extends ArrayAdapter<EpisodeListItem> {

        public EpisodesListAdapter() {
            super(getApplicationContext(), R.layout.list_item_episodes, episodeListItems);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_episodes, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            EpisodeListItem current = episodeListItems.get(pos);

            TextView titleGerView = (TextView) view.findViewById(R.id.episodeTitleGer);
            titleGerView.setText((pos + 1) + " " + current.getTitleGer());
            if (!Settings.of(getContext()).themeName().contains("_DARK"))
                titleGerView.setTextColor(ContextCompat.getColor(getContext(), current.isWatched() ? android.R.color.darker_gray : android.R.color.black));

            String enTitle = current.getTitle();
            if (enTitle.equals(""))
                view.findViewById(R.id.episodeTitle).setVisibility(View.GONE);
            else {
                ((TextView) view.findViewById(R.id.episodeTitle)).setText(current.getTitle());
            }

            ((TextView) view.findViewById(R.id.episodeId)).setText(current.getId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.watchedImageView);
            if (current.isWatched())
                if (!Settings.of(getContext()).themeName().contains("_DARK"))
                    fav.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_watched));
                else
                    fav.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_watched_white));
            else
                fav.setImageDrawable(null);

            return view;
        }
    }

    class getVideo extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;
        boolean externalPlayer;
        String hosterReturn;
        private VideoObj videoObj;

        getVideo(VideoObj videoObj) {
            this(videoObj, false);
        }

        getVideo(VideoObj videoObj, boolean externalPlayer) {
            this.videoObj = videoObj;
            this.externalPlayer = externalPlayer;
        }

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(TabletShowActivity.this);
            progressDialog.setMessage("Hoster wird geöffnet...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Hoster hoster = new Hoster();
            hosterReturn = hoster.get(videoObj.getHoster(), videoObj.getUrl());
            Log.v("BS", hosterReturn);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            progressDialog.dismiss();

            Context context = getApplicationContext();

            Snackbar snackbar;
            View snackbarView;

            switch (hosterReturn) {
                case "1":
                    snackbar = Snackbar.make(findViewById(R.id.showContent), "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "2":
                    snackbar = Snackbar.make(findViewById(R.id.showContent), "Video wurde wahrscheinlich gelöscht.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "3":
                    snackbar = Snackbar.make(findViewById(R.id.showContent), "Fehler beim auflösen der Video URL.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "4":
                    snackbar = Snackbar.make(findViewById(R.id.showContent), "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "5":
                    snackbar = Snackbar.make(findViewById(R.id.showContent), "Da ist etwas ganz schief gelaufen. Fehler bitte melden.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
            }

            if (hosterReturn.equals("unkown_hoster")) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(videoObj.getFullUrl()));
            } else {
                //Intent intent = new Intent(getContext(), BufferedVideoPlayerActivity.class);
                Intent intent = new Intent(context, FullscreenVideoActivity.class);
                intent.putExtra("burning-series.videoURL", hosterReturn);
                startActivity(intent);
            }
            super.onPostExecute(aVoid);
        }
    }

    class HosterListAdpter extends ArrayAdapter<HosterListItem> implements AdapterView.OnItemClickListener {

        HosterListAdpter() {
            super(getApplicationContext(), R.layout.list_item_hoster, hosterListItems);
        }

        @Override
        @NonNull
        public View getView(int pos, View view, @NonNull ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_hoster, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            HosterListItem current = hosterListItems.get(pos);

            TextView lable = (TextView) view.findViewById(R.id.hosterLabel);
            lable.setText(current.getHoster());

            TextView url = (TextView) view.findViewById(R.id.linkId);
            url.setText(current.getLinkId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.supImgView);
            if (!Settings.of(getContext()).themeName().contains("_DARK"))
                fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isSupported() ? R.drawable.ic_ondemand_video : R.drawable.ic_public));
            else
                fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isSupported() ? R.drawable.ic_ondemand_video_white : R.drawable.ic_public_white));

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
            Context context = getApplicationContext();

            if (Settings.of(context).alarmOnMobile() &&
                    AndroidUtility.isOnMobile(context)) {

                DialogBuilder.start(context)
                        .title("Mobile Daten")
                        .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                        .positive("Weiter", new DialogBuilder.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialog) {
                                TextView idView = (TextView) view.findViewById(R.id.linkId);
                                showVideo(Integer.parseInt(idView.getText().toString()));
                            }
                        })
                        .negative("Abbrechen")
                        .build()
                        .show();

            } else {
                TextView idView = (TextView) view.findViewById(R.id.linkId);
                showVideo(Integer.parseInt(idView.getText().toString()));
            }
        }
    }
}
