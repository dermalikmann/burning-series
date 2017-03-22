package de.m4lik.burningseries.ui.mainFragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.ui.MainActivity;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.ui.listitems.GenreListItem;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Logger;

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

    ProgressDialog progressDialog;


    public GenresFragment() {
        // Required empty public constructor
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

        genresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String lableString = ((TextView) view.findViewById(R.id.genreLable)).getText().toString();
                Logger.genreSelection(getContext(), lableString);
                populateSeriesList(lableString);
                ((MainActivity) getActivity()).seriesList = true;
            }
        });
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

