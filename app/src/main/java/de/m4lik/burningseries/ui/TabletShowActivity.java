package de.m4lik.burningseries.ui;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import butterknife.BindView;
import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.FullShowObj;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.dialogs.BusyDialog;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.ui.listitems.PlayerChooserListItem;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;
import de.m4lik.burningseries.ui.viewAdapters.EpisodesRecyclerAdapter;
import de.m4lik.burningseries.ui.viewAdapters.HosterRecyclerAdapter;
import de.m4lik.burningseries.ui.viewAdapters.PlayerChooserListAdapter;
import de.m4lik.burningseries.ui.viewAdapters.SeasonsListAdapter;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.ShowUtils;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.historyTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class TabletShowActivity extends ActivityBase {

    Integer currentShow;
    Integer currentSeason;
    Integer currentEpisode;
    String showName;
    String episodeName;

    Boolean fav = false;
    Boolean loaded = false;
    Boolean fromWatchHistory = false;

    @BindView(R.id.favButton)
    Button favButton;

    @BindView(R.id.descriptionTV)
    TextView descriptionTV;

    @BindView(R.id.seasonsListView)
    ListView seasonsListView;

    @BindView(R.id.episodesRecyclerView)
    RecyclerView episodesRecyclerView;

    @BindView(R.id.hosterRecyclerView)
    RecyclerView hosterRecyclerView;

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

    @BindView(R.id.episodeName)
    TextView episodeNameTV;

    @BindView(R.id.seasonTV)
    TextView seasonTV;

    String userSession;

    List<SeasonListItem> seasons = new ArrayList<>();
    List<EpisodeListItem> episodes = new ArrayList<>();
    List<HosterListItem> hosterList = new ArrayList<>();

    Intent i;

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

        findViewById(R.id.descriptionTV).setBackground(ContextCompat.getDrawable(TabletShowActivity.this, theme().listItemBackground));

        String title = i.getStringExtra("ShowName");
        currentShow = i.getIntExtra("ShowID", 60);

        fromWatchHistory = i.getBooleanExtra("ShowEpisode", false);
        if (fromWatchHistory) {
            currentSeason = i.getIntExtra("SeasonID", 1);
            currentEpisode = i.getIntExtra("EpisodeID", 1);
            episodeName = i.getStringExtra("EpisodeName");
        }

        showName = title;
        Uri imageUri = Uri.parse("https://bs.to/public/img/cover/" + currentShow + ".jpg");
        getSupportActionBar().setTitle(title);

        Log.v("BS", "Lade Cover.");
        Glide.with(TabletShowActivity.this)
                .load(imageUri)
                .into(coverImageView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = Settings.of(this).getUserSession();

        fav = ShowUtils.isFav(this, currentShow);

        final Drawable favStar = ContextCompat.getDrawable(TabletShowActivity.this, R.drawable.ic_star_white);
        favStar.setBounds(0, 0, 50, 50);
        final Drawable notFavStar = ContextCompat.getDrawable(TabletShowActivity.this, R.drawable.ic_star_border_white);
        notFavStar.setBounds(0, 0, 50, 50);
        if (fav)
            favButton.setCompoundDrawables(favStar, null, null, null);
        else
            favButton.setCompoundDrawables(notFavStar, null, null, null);

        favButton.setOnClickListener(view -> {
            if (!fav) {
                ShowUtils.addToFavorites(this, currentShow);
                favButton.setCompoundDrawables(favStar, null, null, null);
                fav = !fav;
            } else {
                ShowUtils.removeFromFavorites(this, currentShow);
                favButton.setCompoundDrawables(notFavStar, null, null, null);
                fav = !fav;
            }
        });

        setupEpisodeList();
        setupHosterList();
    }


    public void showSeason(final Integer season) {

        API api = new API();
        APIInterface apiInterface = api.getInterface();
        api.setSession(userSession);
        api.generateToken("series/" + currentShow + "/" + season);
        Call<SeasonObj> seasonCall = apiInterface.getSeason(api.getToken(), api.getUserAgent(), currentShow, season, api.getSession());
        seasonCall.enqueue(new Callback<SeasonObj>() {
            @Override
            public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {
                currentSeason = season;

                SeasonObj season = response.body();

                if (!loaded)
                    refreshSeries(season);

                seasonTV.setText("Staffel " + currentSeason + ":");

                episodes = new ArrayList<>();

                for (SeasonObj.Episode episode : season.getEpisodes()) {
                    episodes.add(new EpisodeListItem(episode.getGermanTitle(),
                            episode.getEnglishTitle(),
                            episode.getEpisodeID(),
                            episode.isWatched() == 1));
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
                snackbarView.setBackgroundColor(ContextCompat.getColor(TabletShowActivity.this, theme().primaryColorDark));
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
                currentEpisode = episode;
                EpisodeObj episode = response.body();

                hosterList = new ArrayList<>();

                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (Hoster.compatibleHosters.contains(hoster.getHoster())) {
                        hosterList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart(), true));

                    }
                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (!Hoster.compatibleHosters.contains(hoster.getHoster()))
                        hosterList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

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
                snackbarView.setBackgroundColor(ContextCompat.getColor(TabletShowActivity.this, theme().primaryColorDark));
                snackbar.show();
            }
        });

    }

    private void setupEpisodeList() {
        LinearLayoutManager llm = new LinearLayoutManager(TabletShowActivity.this, LinearLayoutManager.VERTICAL, false);
        episodesRecyclerView.setLayoutManager(llm);
        episodesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(TabletShowActivity.this, episodesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        EpisodeListItem clickedEpisode = episodes.get(position);
                        episodeName = clickedEpisode.getTitleGer().equals("") ? clickedEpisode.getTitle() : clickedEpisode.getTitleGer();
                        episodeNameTV.setText(episodeName);
                        TextView idView = (TextView) view.findViewById(R.id.episodeId);
                        showEpisode(currentSeason, Integer.parseInt(idView.getText().toString()));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

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
                                        if (!Settings.of(TabletShowActivity.this).isDarkTheme())
                                            titleGerView.setTextColor(ContextCompat.getColor(TabletShowActivity.this, android.R.color.black));

                                        ImageView fav1 = (ImageView) view.findViewById(R.id.watchedImageView);
                                        fav1.setImageDrawable(null);
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
                    }
                })
        );

        showSeason(fromWatchHistory ? currentSeason : 1);
    }

    private void setupHosterList() {
        LinearLayoutManager llm = new LinearLayoutManager(TabletShowActivity.this, LinearLayoutManager.VERTICAL, false);
        hosterRecyclerView.setLayoutManager(llm);
        hosterRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(TabletShowActivity.this, hosterRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String playerType = hosterList.get(position).isSupported() ? "internal" : "appbrowser";
                        if (Settings.of(TabletShowActivity.this).alarmOnMobile() &&
                                AndroidUtility.isOnMobile(TabletShowActivity.this)) {

                            DialogBuilder.start(TabletShowActivity.this)
                                    .title("Mobile Daten")
                                    .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                                    .positive("Weiter", dialog -> {
                                        TextView idView = (TextView) view.findViewById(R.id.linkId);
                                        showVideo(Integer.parseInt(idView.getText().toString()), playerType);
                                    })
                                    .negative()
                                    .cancelable()
                                    .build()
                                    .show();

                        } else {
                            showVideo(hosterList.get(position).getLinkId(), playerType);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        if (Settings.of(TabletShowActivity.this).alarmOnMobile() &&
                                AndroidUtility.isOnMobile(TabletShowActivity.this)) {
                            List<PlayerChooserListItem> players = new ArrayList<>();

                            if (hosterList.get(position).isSupported()) {
                                players.add(new PlayerChooserListItem("Interner Player", "internal",
                                        Settings.of(TabletShowActivity.this).isDarkTheme() ?
                                                R.drawable.ic_ondemand_video_white : R.drawable.ic_ondemand_video));

                                players.add(new PlayerChooserListItem("Externer Player", "external",
                                        Settings.of(TabletShowActivity.this).isDarkTheme() ?
                                                R.drawable.ic_live_tv_white : R.drawable.ic_live_tv));

                                players.add(new PlayerChooserListItem("In-App Browser", "appbrowser",
                                        Settings.of(TabletShowActivity.this).isDarkTheme() ?
                                                R.drawable.ic_open_in_browser_white : R.drawable.ic_open_in_browser));
                            }

                            players.add(new PlayerChooserListItem("Im Browser öffnen", "browser",
                                    Settings.of(TabletShowActivity.this).isDarkTheme() ?
                                            R.drawable.ic_public_white : R.drawable.ic_public));

                            DialogBuilder.start(TabletShowActivity.this)
                                    .title(getString(R.string.choose_player_title))
                                    .adapter(new PlayerChooserListAdapter(TabletShowActivity.this, players), (dialog2, id) ->

                                        DialogBuilder.start(TabletShowActivity.this)
                                                .title("Mobile Daten")
                                                .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                                                .positive("Weiter", dialog ->
                                                    showVideo(hosterList.get(position).getLinkId(), players.get(id).getType())
                                                )
                                                .negative()
                                                .cancelable()
                                                .build()
                                                .show()
                                    )
                                    .cancelable()
                                    .negative()
                                    .build()
                                    .show();
                        }
                    }
                })
        );

        if (fromWatchHistory) {
            episodeNameTV.setText(episodeName);
            showEpisode(currentSeason, currentEpisode);
        } else
            showEpisode(1, 1);
    }

    private void refreshSeries(SeasonObj seasonObj) {
        loaded = true;
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
            seasons.add(new SeasonListItem(i));
        }

        seasonsListView.setAdapter(new SeasonsListAdapter(this, seasons));
        seasonsListView.setOnItemClickListener((parent, view, position, id) ->
            showSeason(Integer.parseInt(((TextView) view.findViewById(R.id.seasonId)).getText().toString()))
        );
    }

    private void refreshEpisodesList() {
        episodesRecyclerView.setAdapter(new EpisodesRecyclerAdapter(this, episodes));
    }

    private void refreshHosterList() {
        hosterRecyclerView.setAdapter(new HosterRecyclerAdapter(this, hosterList));
    }

    private void showVideo(Integer id, String type) {

        API api = new API();
        api.setSession(userSession);
        api.generateToken("watch/" + id);
        APIInterface apii = api.getInterface();
        Call<VideoObj> call = apii.watch(api.getToken(), api.getUserAgent(), id, api.getSession());
        call.enqueue(new Callback<VideoObj>() {
            @Override
            public void onResponse(Call<VideoObj> call, Response<VideoObj> response) {
                VideoObj videoObj = response.body();

                String hoster = videoObj.getHoster().toLowerCase();

                switch (type) {
                    case "internal":
                        if (hoster.equals("openload") || hoster.equals("openloadhd")) {
                            new OpenloadParser(videoObj.getFullUrl(), false).execute();
                        } else {
                            new GetVideo(videoObj).execute();
                        }
                        break;
                    case "external":
                        if (hoster.equals("openload") || hoster.equals("openloadhd")) {
                            new OpenloadParser(videoObj.getFullUrl(), true).execute();
                        } else {
                            new GetVideo(videoObj, true).execute();
                        }
                        break;
                    case "browser":
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoObj.getFullUrl()));
                        startActivity(browserIntent);
                        break;
                    case "appbrowser":
                    default:
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(TabletShowActivity.this, Uri.parse(videoObj.getFullUrl()));
                        break;
                }

                MainDBHelper dbHelper = new MainDBHelper(TabletShowActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues cv = new ContentValues();
                Calendar calendar = Calendar.getInstance();

                cv.put(historyTable.COLUMN_NAME_SHOW_ID, currentShow);
                cv.put(historyTable.COLUMN_NAME_SEASON_ID, currentSeason);
                cv.put(historyTable.COLUMN_NAME_EPISODE_ID, currentEpisode);
                cv.put(historyTable.COLUMN_NAME_SHOW_NAME, showName);
                cv.put(historyTable.COLUMN_NAME_EPISODE_NAME, episodeName);
                cv.put(historyTable.COLUMN_NAME_DATE, calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH));
                cv.put(historyTable.COLUMN_NAME_TIME, calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

                db.insert(historyTable.TABLE_NAME, null, cv);
            }

            @Override
            public void onFailure(Call<VideoObj> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Probleme beim Verbinden mit BS", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(TabletShowActivity.this, theme().primaryColorDark));
                snackbar.show();

                DialogBuilder.start(TabletShowActivity.this)
                        .title("Unerwartete Antwort")
                        .content("Hmm, da ist was schief gelaufen. Hast du vielleicht ein paarmal schnell hintereinander auf einen Hoster getippt?\n" +
                                "Wenn ja, warte eine halbe Minute, dann sollte wieder alles funktionieren ;)")
                        .negative()
                        .build().show();
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void Openload(String videoID, Boolean external) {
        try {
            BusyDialog dialog = BusyDialog.newInstace("Hoster wird geöffnet...");

            WebView wv = new WebView(TabletShowActivity.this);
            wv.getSettings().setJavaScriptEnabled(true);
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.evaluateJavascript("document.getElementById('streamurl').innerHTML", valueFromJS -> {
                        dialog.dismiss();
                                String vurl = "https://openload.co/stream/" + valueFromJS.replace("\"", "") + "?mime=true";
                                if (!external) {
                                    Intent intent = new Intent(TabletShowActivity.this, FullscreenVideoActivity.class);
                                    intent.putExtra("burning-series.videoURL", vurl);
                                    startActivity(intent);
                                } else {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(vurl));
                                    startActivity(browserIntent);
                                }
                            }
                    );
                }
            });

            //wv.loadUrl(fullURL);
            wv.loadDataWithBaseURL("https://openload.co", videoID, null, null, null);
            dialog.show(getSupportFragmentManager(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class OpenloadParser extends AsyncTask<Void, Void, Void> {
        String url;
        String content;
        Boolean external;

        OpenloadParser(String url, Boolean external) {
            this.url = url;
            this.external = external;
        }

        @Override
        protected Void doInBackground(Void... params) {
            content = null;
            try {
                URLConnection connection = new URL(url).openConnection();
                Scanner scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
                content = content.replace("<video", "<video preload=none");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Openload(content, external);
            super.onPostExecute(aVoid);
        }
    }

    private class GetVideo extends AsyncTask<Void, Void, Void> {

        private VideoObj videoObj;
        private Boolean external;
        private BusyDialog busyDialog;
        private String hosterReturn;
        private Context context;

        GetVideo(VideoObj videoObj) {
            this(videoObj, false);
        }

        GetVideo(VideoObj videoObj, Boolean external) {
            this.videoObj = videoObj;
            this.external = external;
            this.context = TabletShowActivity.this;
        }

        @Override
        protected void onPreExecute() {
            busyDialog = BusyDialog.newInstace("Hoster wird geöffnet...");
            busyDialog.show(getSupportFragmentManager(), null);
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

            busyDialog.dismiss();

            Snackbar snackbar;
            View snackbarView;

            switch (hosterReturn) {
                case "1":
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "2":
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Video wurde wahrscheinlich gelöscht.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "3":
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Fehler beim auflösen der Video URL.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "4":
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "5":
                    snackbar = Snackbar.make(findViewById(android.R.id.content), "Da ist etwas ganz schief gelaufen. Fehler bitte melden.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(context, theme().primaryColorDark));
                    snackbar.show();
                    return;
            }

            if (!external) {
                Intent intent = new Intent(context, FullscreenVideoActivity.class);
                intent.putExtra("burning-series.videoURL", hosterReturn);
                startActivity(intent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(hosterReturn));
                startActivity(browserIntent);
            }
            super.onPostExecute(aVoid);
        }
    }
}