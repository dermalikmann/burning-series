package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.databinding.ListItemHistoryBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.HistoryListItem;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_DATE;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_EPISODE_ID;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_EPISODE_NAME;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_SEASON_ID;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_SHOW_ID;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_SHOW_NAME;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.COLUMN_NAME_TIME;
import static de.m4lik.burningseries.database.SeriesContract.historyTable.TABLE_NAME;
import static de.m4lik.burningseries.database.SeriesContract.historyTable._ID;


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

        MainDBHelper dbHelper = new MainDBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_SHOW_ID,
                COLUMN_NAME_SEASON_ID,
                COLUMN_NAME_EPISODE_ID,
                COLUMN_NAME_SHOW_NAME,
                COLUMN_NAME_EPISODE_NAME,
                COLUMN_NAME_DATE,
                COLUMN_NAME_TIME
        };

        String sortOrder =
                _ID + " DESC";

        Cursor c = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        int i = 0;
        while (c.moveToNext() && i < 20) {
            historyList.add(new HistoryListItem(
                    c.getInt(c.getColumnIndex(COLUMN_NAME_SHOW_ID)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_SEASON_ID)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_EPISODE_ID)),
                    c.getString(c.getColumnIndex(COLUMN_NAME_SHOW_NAME)),
                    c.getString(c.getColumnIndex(COLUMN_NAME_EPISODE_NAME))
            ));
            i++;
        }

        c.close();
        db.close();

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
                            showSeries(showId, seasonId, episodeId, name);
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

    private void showSeries(Integer showID, Integer seasonID, Integer episodeID, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowID", showID);
        i.putExtra("SeasonID", seasonID);
        i.putExtra("EpisodeID", episodeID);
        i.putExtra("ShowName", name);
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

                binding.executePendingBindings();
            }
        }
    }
}

