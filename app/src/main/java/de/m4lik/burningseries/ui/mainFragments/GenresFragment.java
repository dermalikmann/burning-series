package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.databinding.ListItemGenresBinding;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.GenreListItem;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

import static de.m4lik.burningseries.database.SeriesContract.genresTable;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * Fragment for displaying a list of available genres.
 * After selection of one, a list of shows with this genre shall come up.
 */
public class GenresFragment extends Fragment {

    @BindView(R.id.genresRecyclerView)
    RecyclerView genresRecyclerView;

    boolean genreShown = false;
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
            genreShown = true;
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
        genresRecyclerView.setAdapter(new GenresRecyclerAdapter(getGenreList()));
        genresRecyclerView.addOnItemTouchListener(genreClickListener);
    }

    private List<GenreListItem> getGenreList() {
        List<GenreListItem> list = new ArrayList<>();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                genresTable.COLUMN_NAME_ID,
                genresTable.COLUMN_NAME_GENRE
        };

        String sortOrder =
                genresTable.COLUMN_NAME_GENRE + " ASC";

        Cursor c = db.query(
                genresTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (c.moveToNext()) {
            list.add(new GenreListItem(
                    c.getInt(c.getColumnIndex(genresTable.COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(genresTable.COLUMN_NAME_GENRE))
            ));
        }

        c.close();
        db.close();

        return list;
    }

    private void populateSeriesList(String genre) {
        genresRecyclerView.setAdapter(new SeriesRecyclerAdapter(getSeriesList(genre)));
        genresRecyclerView.removeOnItemTouchListener(genreClickListener);
        genresRecyclerView.addOnItemTouchListener(seriesClickListener);
    }

    private List<ShowListItem> getSeriesList(String lable) {
        List<ShowListItem> list = new ArrayList<>();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                seriesTable.COLUMN_NAME_TITLE,
                seriesTable.COLUMN_NAME_ID,
                seriesTable.COLUMN_NAME_GENRE,
                seriesTable.COLUMN_NAME_ISFAV
        };

        String selection = seriesTable.COLUMN_NAME_GENRE + " = ?";
        String[] selectionArgs = {lable};

        String sortOrder =
                seriesTable.COLUMN_NAME_GENRE + " ASC";

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        if (c.getCount() > 0)
            while (c.moveToNext()) {
                list.add(new ShowListItem(
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_TITLE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)),
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_GENRE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ISFAV)) == 1
                ));
            }

        c.close();
        db.close();

        return list;
    }


    /*
     * Show Matching Series
     */

    private class GenresRecyclerAdapter extends RecyclerView.Adapter<GenresRecyclerAdapter.GenresViewHolder> {

        List<GenreListItem> list;

        GenresRecyclerAdapter(List<GenreListItem> list) {
            this.list = list;
        }

        @Override
        public GenresRecyclerAdapter.GenresViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            ListItemGenresBinding binding = ListItemGenresBinding.inflate(layoutInflater, parent, false);
            return new GenresViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(GenresRecyclerAdapter.GenresViewHolder holder, int position) {
            GenreListItem current = list.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class GenresViewHolder extends RecyclerView.ViewHolder {

            private final ListItemGenresBinding binding;

            GenresViewHolder(ListItemGenresBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(GenreListItem item) {
                binding.setGenre(item);
                binding.getRoot().findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(getActivity(), theme().listItemBackground));
                binding.executePendingBindings();
            }
        }
    }

    private class SeriesRecyclerAdapter extends RecyclerView.Adapter<SeriesRecyclerAdapter.SeriesViewHolder> {

        List<ShowListItem> list;

        SeriesRecyclerAdapter(List<ShowListItem> list) {
            this.list = list;
        }

        @Override
        public SeriesRecyclerAdapter.SeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            ListItemSeriesBinding binding = ListItemSeriesBinding.inflate(layoutInflater, parent, false);
            return new SeriesViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(SeriesRecyclerAdapter.SeriesViewHolder holder, int position) {
            ShowListItem current = list.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class SeriesViewHolder extends RecyclerView.ViewHolder {

            private final ListItemSeriesBinding binding;

            SeriesViewHolder(ListItemSeriesBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(ShowListItem item) {
                binding.setShow(item);
                binding.getRoot().findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(getActivity(), theme().listItemBackground));

                if (!item.loaded && Settings.of(getActivity()).showCovers())
                    Glide.with(getActivity())
                            .load(Uri.parse("https://bs.to/public/img/cover/" + item.getId() + ".jpg"))
                            .into((ImageView) binding.getRoot().findViewById(R.id.coverImage));

                if (!Settings.of(getActivity()).showCovers()) {
                    binding.getRoot().findViewById(R.id.coverImage).setVisibility(View.GONE);
                }

                item.loaded = true;

                binding.getRoot().findViewById(R.id.favImageView).setVisibility(View.GONE);

                binding.executePendingBindings();
            }
        }
    }


}

