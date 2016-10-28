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
import de.m4lik.monarchcode.burningseries.objects.SeasonObj;
import de.m4lik.monarchcode.burningseries.objects.EpisodeListItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpisodesFragment extends Fragment implements Callback<SeasonObj>{

    View rootview;

    Document webDoc;

    Integer selectedShow;
    Integer selectedSeason;

    ListView episodesListView;
    ArrayList<EpisodeListItem> episodesList = new ArrayList<>();

    boolean loaded = false;
    Integer lastselection;


    public EpisodesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview =  inflater.inflate(R.layout.fragment_episodes, container, false);

        episodesListView = (ListView) rootview.findViewById(R.id.episodesListView);


        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();

        API api = new API();
        api.generateToken("series/" + selectedShow + "/"+ selectedSeason + "?s=" + api.getSession());
        APIInterface apii = api.getApiInterface();
        Call<SeasonObj> call = apii.getSeason(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, "");
        call.enqueue(this);

        //new getEpisodes(url).execute();

        return rootview;
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {

        SeasonObj season = response.body();

        for (SeasonObj.Episode episode : season.getEpisodes()) {
            episodesList.add(new EpisodeListItem(episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID()));
        }

        loaded = true;

        refreshList();
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {
        Snackbar.make(rootview, "Fehler beim Laden der Episoden", Snackbar.LENGTH_SHORT);
    }



    /*private class getEpisodes extends AsyncTask<Void, Void, Void> {

        private String url;
        private String debug;

        public getEpisodes(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EpisodesActivity.this);
            progressDialog.setMessage("Folgen werden aufgelistet.\nBitte warten...");

            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

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

                    episodesList.add(new listItemEpisodes(titleGer, title, url));
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
        ArrayAdapter<EpisodeListItem> adapter = new episodesListAdapter();
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
                TextView idView = (TextView) view.findViewById(R.id.episodeId);
                showEpisode(Integer.parseInt(idView.getText().toString()));
            }
        });
    }

    private void showEpisode(Integer id) {
        ((ShowActivity) getActivity()).setSelectedEpisode(id);
        ((ShowActivity) getActivity()).switchEpisodesToHosters();
    }

    class episodesListAdapter extends ArrayAdapter<EpisodeListItem> {

        public episodesListAdapter() {
            super(getActivity(), R.layout.list_item_episodes, episodesList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_episodes, parent, false);
            }

            EpisodeListItem current = episodesList.get(pos);

            TextView titleGerView = (TextView) view.findViewById(R.id.episodeTitleGer);
            titleGerView.setText(current.getTitleGer());

            TextView titleView = (TextView) view.findViewById(R.id.episodeTitle);
            titleView.setText(current.getTitle());

            TextView idView = (TextView) view.findViewById(R.id.episodeId);
            idView.setText(current.getId().toString());

            return view;
        }
    }

}
