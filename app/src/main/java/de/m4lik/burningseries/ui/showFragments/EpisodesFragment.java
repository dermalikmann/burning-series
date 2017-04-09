package de.m4lik.burningseries.ui.showFragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.api.objects.EpisodeObj;
import de.m4lik.burningseries.api.objects.SeasonObj;
import de.m4lik.burningseries.api.objects.VideoObj;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.listitems.EpisodeListItem;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
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

    @BindView(R.id.episodesRecyclerView)
    RecyclerView episodesRecyclerView;


    ArrayList<EpisodeListItem> episodesList = new ArrayList<>();

    boolean loaded = false;


    public EpisodesFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_episodes, container, false);

        ButterKnife.bind(this, rootview);

        selectedShow = ((ShowActivity) getActivity()).getSelectedShow();
        selectedSeason = ((ShowActivity) getActivity()).getSelectedSeason();

        userSession = Settings.of(getActivity()).getUserSession();

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
            episodesList.add(new EpisodeListItem(episode.getGermanTitle(), episode.getEnglishTitle(), episode.getEpisodeID(), episode.isWatched() == 1));
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

        episodesRecyclerView.setAdapter(new EpisodesRecyclerAdapter(episodesList));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        episodesRecyclerView.setLayoutManager(llm);
        episodesRecyclerView.setHasFixedSize(true);
        episodesRecyclerView.setNestedScrollingEnabled(false);
        episodesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), episodesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        TextView idView = (TextView) view.findViewById(R.id.episodeId);
                        showEpisode(Integer.parseInt(idView.getText().toString()));
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
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
                                        titleGerView.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), android.R.color.black));

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
                    }
                })
        );


    }

    private void showEpisode(Integer id) {
        ((ShowActivity) getActivity()).setSelectedEpisode(id);
        ((ShowActivity) getActivity()).switchEpisodesToHosters();
    }

    private class EpisodesRecyclerAdapter extends RecyclerView.Adapter<EpisodesRecyclerAdapter.EpisodesViewHolder> {

        ArrayList<EpisodeListItem> list;

        EpisodesRecyclerAdapter(ArrayList<EpisodeListItem> list) {
            this.list = list;
        }

        @Override
        public EpisodesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_episodes, parent, false);
            return new EpisodesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(EpisodesViewHolder holder, int position) {
            EpisodeListItem c = list.get(position);

            holder.id.setText(c.getId().toString());

            holder.titleGer.setText((position + 1) + " " + c.getTitleGer());
            if (!Settings.of(getActivity().getApplicationContext()).themeName().contains("_DARK"))
                holder.titleGer.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), c.isWatched() ? android.R.color.darker_gray : android.R.color.black));

            holder.title.setText(c.getTitle());

            if (c.isWatched())
                if (!Settings.of(getActivity().getApplicationContext()).themeName().contains("_DARK"))
                    holder.watchedImg.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_watched));
                else
                    holder.watchedImg.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_watched_white));
            else
                holder.watchedImg.setImageDrawable(null);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class EpisodesViewHolder extends RecyclerView.ViewHolder {

            TextView id;
            TextView title;
            TextView titleGer;
            ImageView watchedImg;

            EpisodesViewHolder(View itemView) {
                super(itemView);

                id = (TextView) itemView.findViewById(R.id.episodeId);
                title = (TextView) itemView.findViewById(R.id.episodeTitle);
                titleGer = (TextView) itemView.findViewById(R.id.episodeTitleGer);
                watchedImg = (ImageView) itemView.findViewById(R.id.watchedImageView);
            }
        }
    }

}
