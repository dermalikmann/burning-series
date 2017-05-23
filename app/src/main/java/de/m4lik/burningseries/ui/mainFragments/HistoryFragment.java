package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.DatabaseUtils;
import de.m4lik.burningseries.databinding.ListItemHistoryBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.HistoryListItem;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    @BindView(R.id.historyRecyclerView)
    RecyclerView historyRecyclerView;

    List<HistoryListItem> historyList = new ArrayList<>();

    public HistoryFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);

        historyList.clear();

        historyList = DatabaseUtils.with(getActivity())
                .getWatchHistory();

        if (historyList.size() > 0) {

            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            historyRecyclerView.setLayoutManager(llm);
            historyRecyclerView.setAdapter(new HistoryRecyclerAdapter(historyList));
            historyRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getActivity(), historyRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                        @Override
                        public void onItemClick(View view, int position) {
                            Integer showId = Integer.parseInt(((TextView) view.findViewById(R.id.showId)).getText().toString());
                            Integer seasonId = Integer.parseInt(((TextView) view.findViewById(R.id.seasonId)).getText().toString());
                            Integer episodeId = Integer.parseInt(((TextView) view.findViewById(R.id.episodeId)).getText().toString());
                            String name = ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString();
                            String epiName = ((TextView) view.findViewById(R.id.episodeTitle)).getText().toString();
                            showSeries(showId, seasonId, episodeId, name, epiName);
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {

                        }
                    })
            );
        } else {
            rootView.findViewById(R.id.nothingWatched).setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void showSeries(Integer showID, Integer seasonID, Integer episodeID, String name, String epiName) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowID", showID);
        i.putExtra("SeasonID", seasonID);
        i.putExtra("EpisodeID", episodeID);
        i.putExtra("ShowName", name);
        i.putExtra("EpisodeName", epiName);
        i.putExtra("ShowEpisode", true);
        startActivity(i);
    }

    private class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {

        List<HistoryListItem> list;

        HistoryRecyclerAdapter(List<HistoryListItem> list) {
            this.list = list;
        }

        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            ListItemHistoryBinding binding = ListItemHistoryBinding.inflate(layoutInflater, parent, false);
            return new HistoryViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            HistoryListItem current = list.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {

            private final ListItemHistoryBinding binding;

            HistoryViewHolder(ListItemHistoryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(HistoryListItem item) {
                binding.setHistoryItem(item);

                if (Settings.of(getActivity()).isDarkTheme())
                    ((ImageView) binding.getRoot().findViewById(R.id.goImageView)).setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_action_arrow_right_white));

                binding.getRoot().findViewById(R.id.seriesTitle).setSelected(true);

                binding.executePendingBindings();
            }
        }
    }
}