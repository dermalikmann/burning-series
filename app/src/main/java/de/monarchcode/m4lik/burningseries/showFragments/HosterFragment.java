package de.monarchcode.m4lik.burningseries.showFragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import de.monarchcode.m4lik.burningseries.BufferedVideoPlayerActivity;
import de.monarchcode.m4lik.burningseries.R;
import de.monarchcode.m4lik.burningseries.ShowActivity;
import de.monarchcode.m4lik.burningseries.api.API;
import de.monarchcode.m4lik.burningseries.api.APIInterface;
import de.monarchcode.m4lik.burningseries.hoster.Hoster;
import de.monarchcode.m4lik.burningseries.objects.EpisodeObj;
import de.monarchcode.m4lik.burningseries.objects.HosterListItem;
import de.monarchcode.m4lik.burningseries.objects.VideoObj;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                "de.monarchcode.m4lik.burningseries.LOGIN",
                Context.MODE_PRIVATE
        );

        API api = new API();
        api.setSession(sharedPreferences.getString("session", ""));
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
        Snackbar.make(rootview, "Fehler beim laden der Hoster", Snackbar.LENGTH_SHORT);
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idView = (TextView) view.findViewById(R.id.linkId);
                showVideo(Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    private void showVideo(Integer id) {
        ((ShowActivity) getActivity()).setSelectedHoster(id);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                "de.monarchcode.m4lik.burningseries.LOGIN",
                Context.MODE_PRIVATE
        );

        API api = new API();
        api.setSession(sharedPreferences.getString("session", ""));
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

        public getVideo(VideoObj videoObj) {
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

            if (hosterReturn == "unkown_hoster") {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getActivity(), Uri.parse(videoObj.getFullUrl()));
            } else {
                Intent intent = new Intent(getContext(), BufferedVideoPlayerActivity.class);
                intent.putExtra("burning-series.videoURL", hosterReturn);
                startActivity(intent);
            }
            super.onPostExecute(aVoid);
        }
    }

    class hostersListAdapter extends ArrayAdapter<HosterListItem> {

        public hostersListAdapter() {
            super(getActivity(), R.layout.list_item_hoster, hostersList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_hoster, parent, false);
            }

            HosterListItem current = hostersList.get(pos);

            TextView lable = (TextView) view.findViewById(R.id.hosterLabel);
            lable.setText(current.getHoster());

            TextView url = (TextView) view.findViewById(R.id.linkId);
            url.setText(current.getLinkId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.supImgView);
            fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isSupported() ? R.drawable.ic_ondemand_video : R.drawable.ic_public));

            return view;
        }
    }

}
