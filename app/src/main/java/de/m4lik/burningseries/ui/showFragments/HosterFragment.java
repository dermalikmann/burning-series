package de.m4lik.burningseries.ui.showFragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.m4lik.burningseries.FullscreenVideoActivity;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ShowActivity;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.hoster.Hoster;
import de.m4lik.burningseries.ui.dialogs.DialogBuilder;
import de.m4lik.burningseries.ui.listitems.HosterListItem;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * A simple {@link Fragment} subclass.
 */
public class HosterFragment extends Fragment implements Callback<EpisodeObj> {

    View rootview;

    Integer selectedShow;
    Integer selectedSeason;
    Integer selectedEpisode;

    ProgressDialog progressDialog;

    ListView hostersListView;
    ArrayList<HosterListItem> hostersList = new ArrayList<>();
    String hosterReturn;


    public HosterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_hoster, container, false);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();
        selectedEpisode = ((ShowActivity) getActivity()).getSelectedEpisode();

        LinearLayout epicontainer = (LinearLayout) rootview.findViewById(R.id.hostercontainer);
        epicontainer.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        API api = new API();
        api.setSession(sharedPreferences.getString("pref_session", ""));
        api.generateToken("series/" + selectedShow + "/" + selectedSeason + "/" + selectedEpisode);
        APIInterface apii = api.getInterface();
        Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, selectedEpisode, api.getSession());
        call.enqueue(this);


        hostersListView = (ListView) rootview.findViewById(R.id.hosterListView);


        return rootview;
    }

    @Override
    public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {
        EpisodeObj episode = response.body();

        LinearLayout epicontainer = (LinearLayout) rootview.findViewById(R.id.hostercontainer);
        epicontainer.setVisibility(View.VISIBLE);

        for (EpisodeObj.Hoster hoster : episode.getHoster())
            if (Hoster.compatibleHosters.contains(hoster.getHoster())) hostersList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart(), true));
        for (EpisodeObj.Hoster hoster : episode.getHoster())
            if (!Hoster.compatibleHosters.contains(hoster.getHoster())) hostersList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

        refreshList();
    }

    @Override
    public void onFailure(Call<EpisodeObj> call, Throwable t) {

        Snackbar snackbar = Snackbar.make(rootview, "Fehler beim laden der Hoster.", Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
        snackbar.show();
    }

    private void refreshList() {
        ArrayAdapter<HosterListItem> adapter = new hostersListAdapter();
        hostersListView.setAdapter(adapter);

        Integer numOfItems = adapter.getCount();

        Integer totalItemsHeigt = 0;
        for (int pos = 0; pos < numOfItems; pos++) {
            View item = adapter.getView(pos, null, hostersListView);
            item.measure(0, 0);
            totalItemsHeigt += item.getMeasuredHeight();
        }

        Integer totalDividersHeight = hostersListView.getDividerHeight() * (numOfItems - 1);
        ViewGroup.LayoutParams params = hostersListView.getLayoutParams();
        params.height = totalItemsHeigt + totalDividersHeight;
        hostersListView.setLayoutParams(params);
        hostersListView.requestLayout();

        hostersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                if (Settings.of(getContext()).alarmOnMobile() &&
                        AndroidUtility.isOnMobile(getContext())) {

                    DialogBuilder.start(getActivity())
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

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

    class getVideo extends AsyncTask<Void, Void, Void> {

        private VideoObj videoObj;

        getVideo(VideoObj videoObj) {
            this.videoObj = videoObj;
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
                    snackbar = Snackbar.make(rootview, "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "2":
                    snackbar = Snackbar.make(rootview, "Video wurde wahrscheinlich gelöscht.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "3":
                    snackbar = Snackbar.make(rootview, "Fehler beim auflösen der Video URL.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "4":
                    snackbar = Snackbar.make(rootview, "Hoster hat nicht geantwortet.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                    snackbar.show();
                    return;
                case "5":
                    snackbar = Snackbar.make(rootview, "Da ist etwas ganz schief gelaufen. Fehler bitte melden.", Snackbar.LENGTH_SHORT);
                    snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                    snackbar.show();
                    return;
            }

            if (hosterReturn.equals("unkown_hoster")) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getActivity(), Uri.parse(videoObj.getFullUrl()));
            } else {
                //Intent intent = new Intent(getContext(), BufferedVideoPlayerActivity.class);
                Intent intent = new Intent(getContext(), FullscreenVideoActivity.class);
                intent.putExtra("burning-series.videoURL", hosterReturn);
                startActivity(intent);
            }
            super.onPostExecute(aVoid);
        }
    }

    class hostersListAdapter extends ArrayAdapter<HosterListItem> {

        hostersListAdapter() {
            super(getActivity(), R.layout.list_item_hoster, hostersList);
        }

        @Override
        @NonNull
        public View getView(int pos, View view, @NonNull ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_hoster, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            HosterListItem current = hostersList.get(pos);

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
    }

}
