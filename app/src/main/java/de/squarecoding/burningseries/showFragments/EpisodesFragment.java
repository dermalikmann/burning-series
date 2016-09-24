package de.squarecoding.burningseries.showFragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import de.squarecoding.burningseries.objects.episodesListItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpisodesFragment extends Fragment {

    View rootview;

    Document webDoc;

    String url;

    ListView episodesListView;
    ArrayList<episodesListItem> episodesList = new ArrayList<>();


    public EpisodesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.fragment_episodes, container, false);

        episodesListView = (ListView) rootview.findViewById(R.id.episodesListView);

        url = ((ShowActivity) getActivity()).getSelectedSeason();

        new getEpisodes(url).execute();

        return rootview;
    }



    private class getEpisodes extends AsyncTask<Void, Void, Void> {

        private String url;
        private String debug;

        public getEpisodes(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();/*
            progressDialog = new ProgressDialog(EpisodesActivity.this);
            progressDialog.setMessage("Folgen werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);*/

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                webDoc = Jsoup.connect(url).get();

                Element table = webDoc.select("#sp_left table").first();

                String url, titleGer, title;
                Element titleGerElement, titleElement;

                table.select("tr").first().remove();

                Elements rows = table.select("tr");

                for (Element row : rows) {
                    Element link = row.select("td").get(1).select("a").first();

                    url = link.attr("abs:href").toString();
                    titleGerElement = link.select("strong").first();
                    titleElement = link.select("span").first();
                    if (titleGerElement == null) { titleGer = ""; } else titleGer = titleGerElement.text();
                    if (titleElement == null) { title = " "; } else title = titleElement.text();

                    episodesList.add(new episodesListItem(titleGer, title, url));
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

            populateList();
            //progressDialog.dismiss();
        }
    }



    private void populateList() {
        ArrayAdapter<episodesListItem> adapter = new episodesListAdapter();
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

        episodesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView urlView = (TextView) view.findViewById(R.id.episodeUrl);
                showEpisode(urlView.getText().toString());
            }
        });
    }

    private void showEpisode(String url) {
        ((ShowActivity) getActivity()).setSelectedEpisode(url);
        ((ShowActivity) getActivity()).switchEpisodesToHosters();
    }

    class episodesListAdapter extends ArrayAdapter<episodesListItem> {

        public episodesListAdapter() {
            super(getActivity(), R.layout.list_item_episodes, episodesList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_episodes, parent, false);
            }

            episodesListItem current = episodesList.get(pos);

            TextView titleGerView = (TextView) view.findViewById(R.id.episodeTitleGer);
            titleGerView.setText(current.getTitleGer());

            TextView titleView = (TextView) view.findViewById(R.id.episodeTitle);
            titleView.setText(current.getTitle());

            TextView urlView = (TextView) view.findViewById(R.id.episodeUrl);
            urlView.setText(current.getUrl());

            return view;
        }
    }

}
