package de.m4lik.monarchcode.burningseries.showFragments;

import android.os.Bundle;
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
import de.m4lik.monarchcode.burningseries.objects.SeasonListItem;

public class SeasonsFragment extends Fragment {

    Document webDoc;

    View rootview;

    ListView seasonsListView;
    ArrayList<SeasonListItem> seasonsList = new ArrayList<>();

    Boolean loaded = false;

    public SeasonsFragment() {
        //Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_seasons, container, false);


        seasonsListView = (ListView) rootview.findViewById(R.id.seasonsListView);

        Integer count = ((ShowActivity) getActivity()).getSeasonCount();
        String description = ((ShowActivity) getActivity()).getDescription();

        TextView descriptionView = (TextView) rootview.findViewById(R.id.descriptionTextView);
        descriptionView.setText(description);
        for (int i = 1; i <= count; i++) {
            seasonsList.add(new SeasonListItem(i));
        }

        if (!loaded) {
            refreshList();
            loaded = true;
        }

        return rootview;
    }



    /*private class getSeasons extends AsyncTask<Void, Void, Void> {

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
            progressDialog.setCanceledOnTouchOutside(false);

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                webDoc = Jsoup.connect(this.url).get();

                description = webDoc.select("#sp_left div p").first().text();

                Elements seasons = webDoc.select("#sp_left ul.pages li").not(".button");
                seasons = seasons.select("a");

                for (Element season : seasons) {
                    seasonsList.add(new listItemSeasons(season.attr("abs:href"), Integer.parseInt(season.text())));
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            refreshList();

            TextView descriptionTextView = (TextView) rootview.findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
            //progressDialog.dismiss();
        }
    }*/

    private void refreshList() {
        ArrayAdapter<SeasonListItem> adapter = new seasonsListAdapter();
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
                TextView idView = (TextView) view.findViewById(R.id.seasonId);
                showSeason(Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    class seasonsListAdapter extends ArrayAdapter<SeasonListItem> {

        public seasonsListAdapter() {
            super(getActivity(), R.layout.list_item_seasons, seasonsList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_seasons, parent, false);
            }

            SeasonListItem current = seasonsList.get(pos);

            TextView label = (TextView) view.findViewById(R.id.seasonLabel);
            label.setText("Staffel " + current.getSeasonId());

            TextView urlText = (TextView) view.findViewById(R.id.seasonId);
            urlText.setText(current.getSeasonId().toString());

            return view;
        }
    }

    private void showSeason(Integer id) {
        ((ShowActivity) getActivity()).setSelectedSeason(id);
        ((ShowActivity) getActivity()).switchSeasonsToEpisodes();
    }
}
