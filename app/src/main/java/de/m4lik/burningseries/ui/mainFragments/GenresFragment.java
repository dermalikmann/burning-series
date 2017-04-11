package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.databinding.ListItemGenresBinding;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.GenreListItem;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Settings;

import static de.m4lik.burningseries.database.SeriesContract.genresTable;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;

/**
 * A simple {@link Fragment} subclass.
 */
public class GenresFragment extends Fragment {


    ListView genresListView;
    List<GenreListItem> genresList = new ArrayList<>();
    List<ShowListItem> seriesList = new ArrayList<>();

    boolean genreShown = false;

    public GenresFragment() {}

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

                binding.executePendingBindings();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_genres, container, false);
        genresListView = (ListView) rootView.findViewById(R.id.genresListView);


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
            genresList.add(new GenreListItem(
                    c.getInt(c.getColumnIndex(genresTable.COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(genresTable.COLUMN_NAME_GENRE))
            ));
        }
        c.close();

        populateGenreList();

        return rootView;
    }

    private void populateGenreList() {
        ArrayAdapter<GenreListItem> adapter = new genresListAdapter();
        genresListView.setAdapter(adapter);

    }

    private class genresListAdapter extends ArrayAdapter<GenreListItem> {

         genresListAdapter() {
            super(getActivity().getApplicationContext(), R.layout.list_item_genres, genresList);
        }

        @Override
        @NonNull
        public View getView(int pos, View view, @NonNull ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_genres, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(ContextCompat.getDrawable(getActivity(), theme().listItemBackground));

            GenreListItem current = genresList.get(pos);

            TextView lable = (TextView) view.findViewById(R.id.genreLable);
            lable.setText(current.getLable());

            return view;
        }
    }


    /**
     * Show Matching Series
     **/

    private void populateSeriesList(String genre) {

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                seriesTable.COLUMN_NAME_TITLE,
                seriesTable.COLUMN_NAME_ID,
                seriesTable.COLUMN_NAME_GENRE,
                seriesTable.COLUMN_NAME_ISFAV
        };

        String selection = seriesTable.COLUMN_NAME_GENRE + " = ?";
        String[] selectionArgs = {genre};

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
                seriesList.add(new ShowListItem(
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_TITLE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)),
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_GENRE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ISFAV)) == 1
                ));
            }

        c.close();

        ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(seriesList);
        genresListView.setAdapter(adapter);

        genresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView nameView = (TextView) view.findViewById(R.id.seriesTitle);
                TextView idView = (TextView) view.findViewById(R.id.seriesId);
                TextView genreView = (TextView) view.findViewById(R.id.seriesGenre);
                showSeries(Integer.parseInt(idView.getText().toString()),
                        nameView.getText().toString(),
                        genreView.getText().toString());
            }
        });
    }

    private void showSeries(Integer id, String name, String genre) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowName", name);
        i.putExtra("ShowID", id);
        i.putExtra("ShowGenre", genre);
        startActivity(i);
    }

    private class seriesListAdapter extends ArrayAdapter<ShowListItem> {

        private List<ShowListItem> list;

        seriesListAdapter(List<ShowListItem> list) {
            super(getActivity().getApplicationContext(), R.layout.list_item_series, seriesList);
            this.list = list;
        }

        @Override
        @NonNull
        public View getView(int pos, View view, @NonNull ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_series, parent, false);
            }

            view.findViewById(R.id.listItemContainer).setBackground(getResources().getDrawable(theme().listItemBackground));

            ShowListItem current = list.get(pos);

            TextView title = (TextView) view.findViewById(R.id.seriesTitle);
            title.setText(current.getTitle());

            TextView genre = (TextView) view.findViewById(R.id.seriesGenre);
            genre.setText(current.getGenre());

            TextView id = (TextView) view.findViewById(R.id.seriesId);
            id.setText(current.getId().toString());

            ImageView fav = (ImageView) view.findViewById(R.id.favImageView);
            fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isFav() ? R.drawable.ic_star : R.drawable.ic_star_border));

            return view;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }
    }
}

