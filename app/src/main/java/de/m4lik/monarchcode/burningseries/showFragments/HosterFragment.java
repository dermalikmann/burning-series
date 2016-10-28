package de.m4lik.monarchcode.burningseries.showFragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.nodes.Document;

import java.util.ArrayList;

import de.m4lik.monarchcode.burningseries.R;
import de.m4lik.monarchcode.burningseries.ShowActivity;
import de.m4lik.monarchcode.burningseries.api.API;
import de.m4lik.monarchcode.burningseries.api.APIInterface;
import de.m4lik.monarchcode.burningseries.objects.EpisodeObj;
import de.m4lik.monarchcode.burningseries.objects.HosterListItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class HosterFragment extends Fragment implements Callback<EpisodeObj>{

    View rootview;

    Document webDoc;

    Integer selectedShow;
    Integer selectedSeason;
    Integer selectedEpisode;

    ListView hostersListView;
    ArrayList<HosterListItem> hostersList = new ArrayList<>();


    public HosterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.fragment_hoster, container, false);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();
        selectedEpisode = ((ShowActivity) getActivity()).getSelectedEpisode();

        API api = new API();
        api.generateToken("series/" + selectedShow + "/" + selectedSeason + "/" + selectedEpisode + "?s=" + api.getSession());
        APIInterface apii = api.getApiInterface();
        Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, selectedEpisode, "");
        call.enqueue(this);


        hostersListView = (ListView) rootview.findViewById(R.id.hosterListView);



        return rootview;
    }

    @Override
    public void onResponse(Call<EpisodeObj> call, Response<EpisodeObj> response) {
        EpisodeObj episode = response.body();

        for (EpisodeObj.Hoster hoster : episode.getHoster())
            hostersList.add(new HosterListItem(hoster.getLinkId(), hoster.getHoster(), hoster.getPart()));

        refreshList();
    }

    @Override
    public void onFailure(Call<EpisodeObj> call, Throwable t) {
        Snackbar.make(rootview, "Fehler beim laden der Hoster", Snackbar.LENGTH_SHORT);
    }



    /*private class getHosters extends AsyncTask<Void, Void, Void> {

        private String url;
        private String debug;

        public getHosters(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();/*
            progressDialog = new ProgressDialog(HostersActivity.this);
            progressDialog.setMessage("Folgen werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                webDoc = Jsoup.connect(url).get();

                webDoc.select("ul.pages").remove();

                String url;

                Elements hosterList = webDoc.select("#sp_left ul li");

                for (Element hoster : hosterList) {
                    Element link = hoster.select("a").first();

                    url = link.attr("abs:href").toString();
                    String[] chost = link.text().split(" - ");

                    hostersList.add(new listItemHoster(url, chost[0], chost[1]));
                }


            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //TextView dbgTv = (TextView) findViewById(R.id.debug);
            //dbgTv.setText(debug);

            refreshList();
            //progressDialog.dismiss();
        }
    }*/



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
                showHoster(Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    private void showHoster(Integer id) {
        ((ShowActivity) getActivity()).setSelectedHoster(id);

        //new getVideo(url).execute();
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


            return view;
        }
    }


    /* * VIDEO **

    class getVideo extends AsyncTask<Void, Void, Void> {

        private String url;
        private Document webDoc;
        private Boolean failed = false;

        public getVideo(String url) {
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                hosterWebDoc = Jsoup.connect(url).get();

                url = hosterWebDoc.select("#video_actions div a").attr("href").toString();

                try {

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));

                } catch (ActivityNotFoundException e) {
                    failed = true;
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (failed) {
                Snackbar.make(rootview.findViewById(R.id.hosterListView), "Da ist was schiefgelaufen. Bitte versuche einen anderen Hoster", Snackbar.LENGTH_LONG).show();
            }
        }
    }*/

}
