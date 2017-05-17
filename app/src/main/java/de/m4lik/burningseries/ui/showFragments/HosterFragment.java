package de.m4lik.burningseries.ui.showFragments;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.FullscreenVideoActivity;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.ui.listitems.PlayerChooserListItem;
import de.m4lik.burningseries.ui.viewAdapters.HosterRecyclerAdapter;
import de.m4lik.burningseries.ui.viewAdapters.PlayerChooserListAdapter;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.historyTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * A simple {@link Fragment} subclass.
 */
public class HosterFragment extends Fragment implements Callback<EpisodeObj> {

    View rootView;

    Integer selectedShow;
    Integer selectedSeason;
    Integer selectedEpisode;
    String showName;
    String episodeName;

    String userSession;

    ProgressDialog progressDialog;

    List<HosterListItem> hosterList = new ArrayList<>();
    String hosterReturn;

    @BindView(R.id.hosterRecyclerView)
    RecyclerView hosterRecyclerView;


    public HosterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_hoster, container, false);

        ButterKnife.bind(this, rootView);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();
        selectedEpisode = ((ShowActivity) getActivity()).getSelectedEpisode();
        showName = ((ShowActivity) getActivity()).getShowName();
        episodeName = ((ShowActivity) getActivity()).getEpisodeName();

        userSession = Settings.of(getActivity()).getUserSession();

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/" + selectedSeason + "/" + selectedEpisode);
        APIInterface apii = api.getInterface();
        Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, selectedEpisode, api.getSession());
        call.enqueue(this);

        return rootView;
    }

    @Override
    public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {
        EpisodeObj episode = response.body();

        for (EpisodeObj.Hoster hoster : episode.getHoster())
            if (Hoster.compatibleHosters.contains(hoster.getHoster()))
                hosterList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart(), true));
        for (EpisodeObj.Hoster hoster : episode.getHoster())
            if (!Hoster.compatibleHosters.contains(hoster.getHoster()))
                hosterList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

        refreshList();
    }

    @Override
    public void onFailure(Call<EpisodeObj> call, Throwable t) {

        Snackbar snackbar = Snackbar.make(rootView, "Fehler beim laden der Hoster.", Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity().getApplicationContext(), theme().primaryColorDark));
        snackbar.show();
    }

    private void refreshList() {

        hosterRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        hosterRecyclerView.setAdapter(new HosterRecyclerAdapter(getActivity(), hosterList));
        hosterRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), hosterRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String playerType = hosterList.get(position).isSupported() ? "internal" : "appbrowser";
                        if (Settings.of(getActivity()).alarmOnMobile() &&
                                AndroidUtility.isOnMobile(getActivity())) {

                            DialogBuilder.start(getActivity())
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
                        if (Settings.of(getActivity()).alarmOnMobile() &&
                                AndroidUtility.isOnMobile(getActivity())) {
                            List<PlayerChooserListItem> players = new ArrayList<>();

                            if (hosterList.get(position).isSupported()) {
                                players.add(new PlayerChooserListItem("Interner Player", "internal",
                                        Settings.of(getActivity()).isDarkTheme() ?
                                                R.drawable.ic_ondemand_video_white : R.drawable.ic_ondemand_video));

                                players.add(new PlayerChooserListItem("Externer Player", "external",
                                        Settings.of(getActivity()).isDarkTheme() ?
                                                R.drawable.ic_live_tv_white : R.drawable.ic_live_tv));

                                players.add(new PlayerChooserListItem("In-App Browser", "appbrowser",
                                        Settings.of(getActivity()).isDarkTheme() ?
                                                R.drawable.ic_open_in_browser_white : R.drawable.ic_open_in_browser));
                            }

                            players.add(new PlayerChooserListItem("Im Browser öffnen", "browser",
                                    Settings.of(getActivity()).isDarkTheme() ?
                                            R.drawable.ic_public_white : R.drawable.ic_public));

                            DialogBuilder.start(getActivity())
                                    .title(getString(R.string.choose_player_title))
                                    .adapter(new PlayerChooserListAdapter(getActivity(), players), (dialog2, id) -> {

                                        DialogBuilder.start(getActivity())
                                                .title("Mobile Daten")
                                                .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                                                .positive("Weiter", dialog -> {
                                                    showVideo(hosterList.get(position).getLinkId(), players.get(id).getType());
                                                })
                                                .negative()
                                                .cancelable()
                                                .build()
                                                .show();
                                    })
                                    .cancelable()
                                    .negative()
                                    .build()
                                    .show();
                        }
                    }
                })
        );
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

                switch (type) {
                    case "internal":
                        new GetVideo(videoObj).execute();
                        break;
                    case "external":
                        new GetVideo(videoObj, true).execute();
                        break;
                    case "browser":
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoObj.getFullUrl()));
                        startActivity(browserIntent);
                        break;
                    case "appbrowser":
                    default:
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(getActivity(), Uri.parse(videoObj.getFullUrl()));
                        break;
                }

                MainDBHelper dbHelper = new MainDBHelper(getActivity());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues cv = new ContentValues();
                Calendar calendar = Calendar.getInstance();

                cv.put(historyTable.COLUMN_NAME_SHOW_ID, selectedShow);
                cv.put(historyTable.COLUMN_NAME_SEASON_ID, selectedSeason);
                cv.put(historyTable.COLUMN_NAME_EPISODE_ID, selectedEpisode);
                cv.put(historyTable.COLUMN_NAME_SHOW_NAME, showName);
                cv.put(historyTable.COLUMN_NAME_EPISODE_NAME, episodeName);
                cv.put(historyTable.COLUMN_NAME_DATE, calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH));
                cv.put(historyTable.COLUMN_NAME_TIME, calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

                db.insert(historyTable.TABLE_NAME, null, cv);

                db.close();
            }

            @Override
            public void onFailure(Call<VideoObj> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(rootView, "Probleme beim Verbinden mit BS", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                snackbar.show();


                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw, true);
                t.printStackTrace(pw);

                DialogBuilder.start(getActivity())
                        .title("Error")
                        .content(sw.getBuffer().toString())
                        .negative()
                        .build().show();
            }
        });
    }

    private class GetVideo extends AsyncTask<Void, Void, Void> {

        private VideoObj videoObj;
        private Boolean external;

        GetVideo(VideoObj videoObj) {
            this(videoObj, false);
        }

        GetVideo(VideoObj videoObj, Boolean external) {
            this.videoObj = videoObj;
            this.external = external;
        }

        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getActivity());
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

            Snackbar snackbar;
            View snackbarView;

            switch (hosterReturn) {
                case "1":
                    snackbar = Snackbar.make(rootView, "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "2":
                    snackbar = Snackbar.make(rootView, "Video wurde wahrscheinlich gelöscht.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "3":
                    snackbar = Snackbar.make(rootView, "Fehler beim auflösen der Video URL.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "4":
                    snackbar = Snackbar.make(rootView, "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "5":
                    snackbar = Snackbar.make(rootView, "Da ist etwas ganz schief gelaufen. Fehler bitte melden.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
                    snackbar.show();
                    return;
            }

            if (!external) {
                Intent intent = new Intent(getActivity().getApplicationContext(), FullscreenVideoActivity.class);
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
