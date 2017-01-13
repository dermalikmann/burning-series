package de.monarchcode.m4lik.burningseries.showFragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import de.monarchcode.m4lik.burningseries.R;
import de.monarchcode.m4lik.burningseries.ShowActivity;
import de.monarchcode.m4lik.burningseries.api.API;
import de.monarchcode.m4lik.burningseries.api.APIInterface;
import de.monarchcode.m4lik.burningseries.objects.EpisodeListItem;
import de.monarchcode.m4lik.burningseries.objects.EpisodeObj;
import de.monarchcode.m4lik.burningseries.objects.SeasonObj;
import de.monarchcode.m4lik.burningseries.objects.VideoObj;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class EpisodesFragment extends Fragment implements Callback<SeasonObj> {

    View rootview;

    Integer selectedShow;
    Integer selectedSeason;

    String userSession;

    ListView episodesListView;
    ArrayList<EpisodeListItem> episodesList = new ArrayList<>();

    boolean loaded = false;


    public EpisodesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_episodes, container, false);

        episodesListView = (ListView) rootview.findViewById(R.id.episodesListView);

        LinearLayout epicontainer = (LinearLayout) rootview.findViewById(R.id.episodescontainer);
        epicontainer.setVisibility(View.GONE);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        userSession = sharedPreferences.getString("pref_session", "");

        API api = new API();
        api.setSession(userSession);
        api.generateToken("series/" + selectedShow + "/" + selectedSeason);
        APIInterface apii = api.getInterface();
        Call<SeasonObj> call = apii.getSeason(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, api.getSession());
        call.enqueue(this);

        return rootview;
    }

    @Override
    public void onResponse(Call<SeasonObj> call, Response<SeasonObj> response) {

        SeasonObj season = response.body();

        for (SeasonObj.Episode episode : season.getEpisodes()) {
            episodesList.add(new EpisodeListItem(episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID(), episode.isWatched() == 1 ));
        }

        loaded = true;

        LinearLayout seasonscontainer = (LinearLayout) rootview.findViewById(R.id.episodescontainer);
        seasonscontainer.setVisibility(View.VISIBLE);

        refreshList();
    }

    @Override
    public void onFailure(Call<SeasonObj> call, Throwable t) {
        Snackbar.make(rootview, "Fehler beim Laden der Episoden", Snackbar.LENGTH_SHORT);
    }

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

        episodesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {

                final TextView idView = (TextView) view.findViewById(R.id.episodeId);
                Integer selectedEpisode = Integer.parseInt(idView.getText().toString());

                final API api = new API();
                api.setSession(userSession);
                api.generateToken("series/" + selectedShow + "/" + selectedSeason + "/" + selectedEpisode);
                APIInterface apii = api.getInterface();
                Call<EpisodeObj> call = apii.getEpisode(api.getToken(), api.getUserAgent(), selectedShow, selectedSeason, selectedEpisode, api.getSession());
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
                                titleGerView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

                                ImageView fav = (ImageView) view.findViewById(R.id.watchedImageView);
                                fav.setImageDrawable(null);
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


                return true;
            }
        });

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
            titleGerView.setText( (pos + 1) + " " + current.getTitleGer());
            titleGerView.setTextColor(ContextCompat.getColor(getContext() , current.isWatched()? android.R.color.darker_gray : android.R.color.black));

            TextView titleView = (TextView) view.findViewById(R.id.episodeTitle);
            titleView.setText(current.getTitle());

            TextView idView = (TextView) view.findViewById(R.id.episodeId);
            idView.setText(current.getId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.watchedImageView);
            if (current.isWatched())
                fav.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_watched));
            else
                fav.setImageDrawable(null);

            return view;
        }
    }

}
