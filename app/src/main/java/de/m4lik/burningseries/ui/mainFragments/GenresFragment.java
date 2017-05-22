package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.DatabaseUtils;
import de.m4lik.burningseries.ui.MainActivity;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.viewAdapters.GenresRecyclerAdapter;
import de.m4lik.burningseries.ui.viewAdapters.SeriesRecyclerAdapter;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

/**
 * Fragment for displaying a list of available genres.
 * After selection of one, a list of shows with this genre shall come up.
 */
public class GenresFragment extends Fragment {

    @BindView(R.id.genresRecyclerView)
    RecyclerView genresRecyclerView;

    private RecyclerItemClickListener seriesClickListener = new RecyclerItemClickListener(getActivity(), genresRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent i = new Intent(getActivity(), ShowActivity.class);
            if (getContext().getResources().getBoolean(R.bool.isTablet))
                i = new Intent(getActivity(), TabletShowActivity.class);
            i.putExtra("ShowName", ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString());
            i.putExtra("ShowID", Integer.parseInt(((TextView) view.findViewById(R.id.seriesId)).getText().toString()));
            i.putExtra("ShowGenre", ((TextView) view.findViewById(R.id.seriesGenre)).getText().toString());
            startActivity(i);
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    });
    private RecyclerItemClickListener genreClickListener = new RecyclerItemClickListener(getActivity(), genresRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            populateSeriesList(((TextView) view.findViewById(R.id.genreLable)).getText().toString());
            ((MainActivity) getActivity()).setSeriesList(true);
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    });

    public GenresFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_genres, container, false);
        ButterKnife.bind(this, rootView);

        populateGenreList();

        return rootView;
    }

    private void populateGenreList() {
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        genresRecyclerView.setLayoutManager(llm);
        genresRecyclerView.setAdapter(new GenresRecyclerAdapter(getActivity(), DatabaseUtils.with(getActivity()).getGenreList()));
        genresRecyclerView.addOnItemTouchListener(genreClickListener);
    }

    private void populateSeriesList(String genre) {
        genresRecyclerView.setAdapter(new SeriesRecyclerAdapter(getActivity(), DatabaseUtils.with(getActivity()).getSeriesListOfGenre(genre)));
        genresRecyclerView.removeOnItemTouchListener(genreClickListener);
        genresRecyclerView.addOnItemTouchListener(seriesClickListener);
    }
}

