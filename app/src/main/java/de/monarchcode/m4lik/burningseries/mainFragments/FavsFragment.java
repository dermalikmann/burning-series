package de.monarchcode.m4lik.burningseries.mainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.monarchcode.m4lik.burningseries.MainActivity;
import de.monarchcode.m4lik.burningseries.R;
import de.monarchcode.m4lik.burningseries.ShowActivity;
import de.monarchcode.m4lik.burningseries.database.MainDBHelper;
import de.monarchcode.m4lik.burningseries.database.SeriesContract;
import de.monarchcode.m4lik.burningseries.objects.ShowListItem;

import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ISFAV;
import static de.monarchcode.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_TITLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavsFragment extends Fragment {

    List<ShowListItem> favs = new ArrayList<>();

    public FavsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favs, container, false);

        MenuItem menuItem = MainActivity.menu.findItem(R.id.action_search);
        menuItem.setVisible(false);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(null);

        ListView favsListView = (ListView) rootView.findViewById(R.id.favsListView);

        favs.clear();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SeriesContract.seriesTable.COLUMN_NAME_ID,
                COLUMN_NAME_TITLE,
                SeriesContract.seriesTable.COLUMN_NAME_GENRE,
                COLUMN_NAME_ISFAV
        };

        String sortOrder =
                COLUMN_NAME_TITLE + " ASC";

        Cursor c = db.query(
                SeriesContract.seriesTable.TABLE_NAME,
                projection,
                SeriesContract.seriesTable.COLUMN_NAME_ISFAV + " = ?",
                new String[]{"1"},
                null,
                null,
                sortOrder
        );

        while (c.moveToNext()) {
            favs.add(new ShowListItem(
                    c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)),
                    c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_GENRE)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_ISFAV)) == 1
            ));
        }

        c.close();

        ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(favs);
        favsListView.setAdapter(adapter);

        favsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView idView = (TextView) view.findViewById(R.id.seriesId);
                TextView nameView = (TextView) view.findViewById(R.id.seriesTitle);
                showSeries(Integer.parseInt(idView.getText().toString()), nameView.getText().toString());
            }
        });

        return rootView;
    }

    private void showSeries(Integer id, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        i.putExtra("ShowID", id);
        i.putExtra("ShowName", name);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<ShowListItem> {

        private List<ShowListItem> list;

        seriesListAdapter(List<ShowListItem> list) {
            super(getActivity(), R.layout.list_item_favorites, favs);
            this.list = list;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_favorites, parent, false);
            }

            ShowListItem current = list.get(pos);

            TextView title = (TextView) view.findViewById(R.id.seriesTitle);
            title.setText(current.getTitle());

            TextView genre = (TextView) view.findViewById(R.id.seriesGenre);
            genre.setText(current.getGenre());

            TextView id = (TextView) view.findViewById(R.id.seriesId);
            id.setText(current.getId().toString());

            return view;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }
    }
}

