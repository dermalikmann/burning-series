package de.squarecoding.burningseries.showFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import de.squarecoding.burningseries.R;
import de.squarecoding.burningseries.ShowActivity;
import de.squarecoding.burningseries.objects.seasonsListItem;

public class SeasonsFragment extends Fragment {

    String url;
    String title;
    String description;

    Document webDoc;

    View rootview;

    ListView seasonsListView;
    ArrayList<seasonsListItem> seasonsList = new ArrayList<>();

    public SeasonsFragment() {
        //Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_seasons, container, false);


        seasonsListView = (ListView) rootview.findViewById(R.id.seasonsListView);

        url = ((ShowActivity) getActivity()).passUrl();

        new getSeasons(url).execute();

        return rootview;
    }



    private class getSeasons extends AsyncTask<Void, Void, Void> {

        private String url;

        public getSeasons(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /*progressDialog = new ProgressDialog(EpisodesActivity.this);
            progressDialog.setMessage("Staffeln werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);*/

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                webDoc = Jsoup.connect(this.url).get();

                description = webDoc.select("#sp_left div p").first().text();

                Elements seasons = webDoc.select("#sp_left ul.pages li").not(".button");
                seasons = seasons.select("a");

                for (Element season : seasons) {
                    seasonsList.add(new seasonsListItem(season.attr("abs:href"), Integer.parseInt(season.text())));
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            populateList();

            TextView descriptionTextView = (TextView) rootview.findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
            //progressDialog.dismiss();
        }
    }

    private void populateList() {
        ArrayAdapter<seasonsListItem> adapter = new seasonsListAdapter();
        seasonsListView.setAdapter(adapter);

        Integer numOfItems = adapter.getCount();

        Integer totalItemsHeigt = 0;
        for (int pos = 0; pos < numOfItems; pos++) {
            View item = adapter.getView(pos, null, seasonsListView);
            item.measure(0, 0);
            totalItemsHeigt += item.getMeasuredHeight();
        }

        Integer totalDividersHeight = seasonsListView.getDividerHeight() * (numOfItems - 1);
        ViewGroup.LayoutParams params = seasonsListView.getLayoutParams();
        params.height = totalItemsHeigt + totalDividersHeight;
        seasonsListView.setLayoutParams(params);
        seasonsListView.requestLayout();

        seasonsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView urlView = (TextView) view.findViewById(R.id.seriesseasonsUrl);
                showSeason(urlView.getText().toString());
            }
        });
    }

    class seasonsListAdapter extends ArrayAdapter<seasonsListItem> {

        public seasonsListAdapter() {
            super(getActivity(), R.layout.list_item_seasons, seasonsList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_seasons, parent, false);
            }

            seasonsListItem current = seasonsList.get(pos);

            TextView label = (TextView) view.findViewById(R.id.seasonLabel);
            label.setText("Staffel " + current.getSeasonId());

            TextView urlText = (TextView) view.findViewById(R.id.seriesseasonsUrl);
            urlText.setText(current.getUrl());

            return view;
        }
    }

    private void showSeason(String url) {
        seasonsListView.setAdapter(null);

        ((ShowActivity) getActivity()).setSelectedSeason(url);
        ((ShowActivity) getActivity()).switchSeasonsToEpisodes();
    }
}
