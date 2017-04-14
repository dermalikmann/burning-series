package de.m4lik.burningseries.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

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
import de.m4lik.burningseries.databinding.ListItemEpisodesBinding;
import de.m4lik.burningseries.databinding.ListItemHosterBinding;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.base.ActivityBase;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.ui.listitems.SeasonListItem;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

public class TabletShowActivity extends ActivityBase {

    Integer currentShow;
    Integer currentSeason;

    Boolean fav = false;
    Boolean loaded = false;

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

    String userSession;

    List<SeasonListItem> seasons = new ArrayList<>();
    EpisodesRecyclerAdapter episodesAdapter;
    List<EpisodeListItem> episodes = new ArrayList<>();
    HosterRecyclerAdapter hosterAdapter;
    List<HosterListItem> hosters = new ArrayList<>();

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

        findViewById(R.id.descriptionTV).setBackground(getResources().getDrawable(theme().listItemBackground));

        String title = i.getStringExtra("ShowName");
        currentShow = i.getIntExtra("ShowID", 60);
        Uri imageUri = Uri.parse("https://bs.to/public/img/cover/" + currentShow + ".jpg");
        getSupportActionBar().setTitle(title);

        Log.v("BS", "Lade Cover.");
        Glide.with(getApplicationContext())
                .load(imageUri)
                .into(coverImageView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userSession = Settings.of(this).getUserSession();

        fav = isFav();

        final Drawable favStar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white);
        favStar.setBounds(0, 0, 50, 50);
        final Drawable notFavStar = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_border_white);
        notFavStar.setBounds(0, 0, 50, 50);
        if (fav)
            favButton.setCompoundDrawables(favStar, null, null, null);
        else
            favButton.setCompoundDrawables(notFavStar, null, null, null);

        favButton.setOnClickListener(view -> {
            if (!fav) {
                addToFavorites();
                favButton.setCompoundDrawables(favStar, null, null, null);
                fav = !fav;
            } else {
                removeFromFavorites();
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

                episodes = new ArrayList<>();

                int i = 1;
                for (SeasonObj.Episode episode : season.getEpisodes()) {
                    episodes.add(new EpisodeListItem(i + " " + episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID(), episode.isWatched() == 1));
                    i++;
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

                hosters = new ArrayList<>();

                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (Hoster.compatibleHosters.contains(hoster.getHoster())) {
                        hosters.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart(), true));

                    }
                for (EpisodeObj.Hoster hoster : episode.getHoster())
                    if (!Hoster.compatibleHosters.contains(hoster.getHoster()))
                        hosters.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

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

    private void setupEpisodeList() {
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        episodesRecyclerView.setLayoutManager(llm);
        episodesAdapter = new EpisodesRecyclerAdapter();
        episodesRecyclerView.setAdapter(episodesAdapter);
        episodesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), episodesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        EpisodeListItem clickedEpisode = episodes.get(position);
                        ((TextView) findViewById(R.id.episodeName)).setText(clickedEpisode.getTitleGer().equals("") ? clickedEpisode.getTitle() : clickedEpisode.getTitleGer());
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
                                        if (!Settings.of(getApplicationContext()).isDarkTheme())
                                            titleGerView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

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

        showSeason(1);
    }

    private void setupHosterList() {
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        hosterRecyclerView.setLayoutManager(llm);
        hosterAdapter = new HosterRecyclerAdapter();
        hosterRecyclerView.setAdapter(hosterAdapter);
        hosterRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), hosterRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        if (Settings.of(getApplicationContext()).alarmOnMobile() &&
                                AndroidUtility.isOnMobile(getApplicationContext())) {

                            DialogBuilder.start(TabletShowActivity.this)
                                    .title("Mobile Daten")
                                    .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                                    .positive("Weiter", dialog -> {
                                        TextView idView = (TextView) view.findViewById(R.id.linkId);
                                        showVideo(Integer.parseInt(idView.getText().toString()));
                                    })
                                    .negative("Abbrechen")
                                    .build()
                                    .show();

                        } else {
                            TextView idView = (TextView) view.findViewById(R.id.linkId);
                            showVideo(Integer.parseInt(idView.getText().toString()));
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

        showEpisode(1, 1);
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
            seasons.add(new SeasonListItem(i));
        }

        seasonsListView.setAdapter(new SeasonsListAdapter());
        seasonsListView.setOnItemClickListener((parent, view, position, id) -> {
            showSeason(Integer.parseInt(((TextView) view.findViewById(R.id.seasonId)).getText().toString()));
        });
    }

    private void refreshEpisodesList() {
        episodesAdapter.replaceAll(episodes);
    }

    private void refreshHosterList() {
        hosterAdapter.replaceAll(hosters);
    }



    /* Fav functions */

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


        if (!userSession.equals("")) {
            API api = new API();
            APIInterface apiInterface = api.getInterface();
            api.setSession(userSession);
            api.generateToken("user/series/set/" + favs);
            Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    //TODO Some error handling. Just in case.
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //TODO Some error handling. Just in case.
                }
            });
        }

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


    private void showVideo(Integer id) {

        API api = new API();
        api.setSession(userSession);
        api.generateToken("watch/" + id);
        APIInterface apii = api.getInterface();
        Call<VideoObj> call = apii.watch(api.getToken(), api.getUserAgent(), id, api.getSession());
        call.enqueue(new Callback<VideoObj>() {
            @Override
            public void onResponse(Call<VideoObj> call, Response<VideoObj> response) {
                VideoObj videoObj = response.body();

                new GetVideo(videoObj).execute();
            }

            @Override
            public void onFailure(Call<VideoObj> call, Throwable t) {

            }
        });
    }

    private class GetVideo extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;
        boolean externalPlayer;
        String hosterReturn;
        private VideoObj videoObj;

        GetVideo(VideoObj videoObj) {
            this(videoObj, false);
        }

        GetVideo(VideoObj videoObj, boolean externalPlayer) {
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



    /* View classes */

    private class SeasonsListAdapter extends ArrayAdapter<SeasonListItem> {

        SeasonsListAdapter() {
            super(getApplicationContext(), R.layout.list_item_seasons, seasons);
        }

        @NonNull
        @Override
        public View getView(int pos, View view, @NonNull ViewGroup parent) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_seasons, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            SeasonListItem current = seasons.get(pos);

            TextView label = (TextView) view.findViewById(R.id.seasonLabel);
            label.setText(getString(R.string.season) + current.getSeasonId());

            TextView urlText = (TextView) view.findViewById(R.id.seasonId);
            urlText.setText(current.getSeasonId().toString());

            return view;
        }
    }

    private class EpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodesRecyclerAdapter.EpisodesViewHolder> {

        private final SortedList<EpisodeListItem> list = new SortedList<EpisodeListItem>(EpisodeListItem.class, new SortedList.Callback<EpisodeListItem>() {

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public int compare(EpisodeListItem o1, EpisodeListItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(EpisodeListItem oldItem, EpisodeListItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areItemsTheSame(EpisodeListItem item1, EpisodeListItem item2) {
                return item1.hashCode() == item2.hashCode();
            }
        });
        Context context = getApplicationContext();

        @Override
        public EpisodesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            ListItemEpisodesBinding binding = ListItemEpisodesBinding.inflate(layoutInflater, parent, false);
            return new EpisodesViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(EpisodesViewHolder holder, int position) {
            EpisodeListItem current = list.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        void add(EpisodeListItem model) {
            list.add(model);
        }

        void remove(EpisodeListItem model) {
            list.remove(model);
        }

        void add(List<EpisodeListItem> models) {
            list.addAll(models);
        }

        void remove(List<EpisodeListItem> models) {
            list.beginBatchedUpdates();
            models.forEach(list::remove);
            list.endBatchedUpdates();
        }

        void replaceAll(List<EpisodeListItem> models) {
            list.beginBatchedUpdates();
            for (int i = list.size() - 1; i >= 0; i--) {
                final EpisodeListItem model = list.get(i);
                list.remove(model);
            }
            list.addAll(models);
            list.endBatchedUpdates();
            Log.d("BSTV", String.valueOf(list.size()));
        }

        class EpisodesViewHolder extends RecyclerView.ViewHolder {

            ListItemEpisodesBinding binding;

            EpisodesViewHolder(ListItemEpisodesBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(EpisodeListItem item) {
                binding.setEpisode(item);

                View root = binding.getRoot();

                boolean isDark = Settings.of(context).isDarkTheme();

                root.findViewById(R.id.listItemContainer)
                        .setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

                if (!isDark) {
                    ((TextView) root.findViewById(R.id.episodeTitleGer))
                            .setTextColor(ContextCompat.getColor(context, item.isWatched() ?
                                    android.R.color.darker_gray : android.R.color.black));
                } else {
                    ((TextView) root.findViewById(R.id.episodeTitleGer))
                            .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                    ((TextView) root.findViewById(R.id.episodeTitle))
                            .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                }


                if (item.isWatched())
                    ((ImageView) root.findViewById(R.id.watchedImageView))
                            .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                    R.drawable.ic_watched_white : R.drawable.ic_watched));
                else
                    ((ImageView) root.findViewById(R.id.watchedImageView))
                            .setImageDrawable(null);

                binding.executePendingBindings();
            }
        }
    }

    private class HosterRecyclerAdapter extends RecyclerView.Adapter<HosterRecyclerAdapter.HosterViewHolder> {

        private final SortedList<HosterListItem> list = new SortedList<HosterListItem>(HosterListItem.class, new SortedList.Callback<HosterListItem>() {

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public int compare(HosterListItem o1, HosterListItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(HosterListItem oldItem, HosterListItem newItem) {
                return oldItem.getHoster().equals(newItem.getHoster());
            }

            @Override
            public boolean areItemsTheSame(HosterListItem item1, HosterListItem item2) {
                return item1.hashCode() == item2.hashCode();
            }
        });
        Context context = getApplicationContext();

        @Override
        public HosterRecyclerAdapter.HosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            ListItemHosterBinding binding = ListItemHosterBinding.inflate(layoutInflater, parent, false);
            return new HosterViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(HosterViewHolder holder, int position) {
            HosterListItem current = list.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        void add(HosterListItem model) {
            list.add(model);
        }

        void remove(HosterListItem model) {
            list.remove(model);
        }

        void add(List<HosterListItem> models) {
            list.addAll(models);
        }

        void remove(List<HosterListItem> models) {
            list.beginBatchedUpdates();
            models.forEach(list::remove);
            list.endBatchedUpdates();
        }

        void replaceAll(List<HosterListItem> models) {
            list.beginBatchedUpdates();
            for (int i = list.size() - 1; i >= 0; i--) {
                final HosterListItem model = list.get(i);
                list.remove(model);
            }
            list.addAll(models);
            list.endBatchedUpdates();
            Log.d("BSTV", String.valueOf(list.size()));
        }

        class HosterViewHolder extends RecyclerView.ViewHolder {

            ListItemHosterBinding binding;

            HosterViewHolder(ListItemHosterBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(HosterListItem item) {
                binding.setHoster(item);

                View root = binding.getRoot();
                boolean isDark = Settings.of(context).isDarkTheme();

                root.findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(context, theme().listItemBackground));

                if (isDark)
                    ((TextView) root.findViewById(R.id.hosterLabel))
                            .setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));

                if (item.isSupported())
                    ((ImageView) root.findViewById(R.id.supImgView))
                            .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                    R.drawable.ic_ondemand_video_white : R.drawable.ic_ondemand_video));
                else
                    ((ImageView) root.findViewById(R.id.supImgView))
                            .setImageDrawable(ContextCompat.getDrawable(context, isDark ?
                                    R.drawable.ic_public_white : R.drawable.ic_public));

                binding.executePendingBindings();
            }
        }
    }
}
