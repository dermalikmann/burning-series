package de.squarecoding.burningseries.mainFragments;


import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.squarecoding.burningseries.MainActivity;
import de.squarecoding.burningseries.R;
import de.squarecoding.burningseries.database.MainDBHelper;
import de.squarecoding.burningseries.ShowActivity;
import de.squarecoding.burningseries.objects.showsListItem;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {

    MainDBHelper mainDBHelper;
    SQLiteDatabase db;

    ListView seriesListView;

    List<showsListItem> seriesList = new ArrayList<>();

    ProgressDialog progressDialog;


    public SeriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_series, container, false);
        seriesListView = (ListView) rootView.findViewById(R.id.seriesListView);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Serienliste wird erstellt.\nBitte warten...");

        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        //mainDBHelper = new MainDBHelper(getContext());
        //db = mainDBHelper.getWritableDatabase();

        new getSeries().execute();

        MenuItem menuItem = MainActivity.menu.findItem(R.id.action_search);
        menuItem.setVisible(true);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return false;
            }
        });

        return rootView;
    }

    public void filterList(String query) {

        System.out.println(query);

        List<showsListItem> filteredList = new ArrayList<>();

        for (showsListItem single : seriesList) {
            if (single.getTitle().contains(query)) filteredList.add(single);
        }

        System.out.println(Integer.toString(filteredList.size()));

        refreshList(filteredList);
    }

    class getSeries extends AsyncTask<Void, Void, Void> {

        private String allSeriesURL = "https://bs.to/serie-genre";

        private Document webDoc;

        public getSeries() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                webDoc = Jsoup.connect(allSeriesURL).get();


                int i = 0;

                for (Element count : webDoc.select("div.genre ul li")) {
                    i++;
                }

                String[][] series = new String[i][3];

                i = 0;

                for (Element genre : webDoc.select("div.genre")) {
                    String curGenre = genre.select("span").first().text();

                    for (Element serie : genre.select("ul li a")) {
                        String title = serie.text();
                        String url = serie.attr("abs:href");

                        series[i][0] = title;
                        series[i][1] = url;
                        series[i][2] = curGenre;

                        /*ContentValues values = new ContentValues();
                        values.put(SeriesContract.seriesTable.COLUMN_NAME_TITLE, title);
                        values.put(SeriesContract.seriesTable.COLUMN_NAME_GENRE, curGenre);
                        values.put(SeriesContract.seriesTable.COLUMN_NAME_URL, url);
                        //values.put(SeriesContract.seriesTable.COLUMN_NAME_FAV, "false");

                        long newRowID = db.insert(
                                SeriesContract.seriesTable.TABLE_NAME,
                                null,
                                values
                        );*/

                        i++;
                    }
                }

                Arrays.sort(series, new Comparator<String[]>() {
                    @Override
                    public int compare(final String[] entry1, final String[] entry2) {
                        final String show1 = entry1[0];
                        final String show2 = entry2[0];
                        return show1.compareTo(show2);
                    }
                });

                for (String[] entry : series) {
                    seriesList.add(new showsListItem(entry[0], entry[1], entry[2]));
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            refreshList(null);
            progressDialog.dismiss();
        }
    }

    private void refreshList(List<showsListItem> list) {
        if (list == null) {
            list = seriesList;
        }

        if (list.size() >= 1) {
            ArrayAdapter<showsListItem> adapter = new seriesListAdapter(list);
            seriesListView.setAdapter(adapter);

            seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView urlView = (TextView) view.findViewById(R.id.seriesUrl);
                    showSeries(urlView.getText().toString());
                }
            });
        } else {
            seriesListView.setAdapter(null);
        }
    }

    private void showSeries(String url) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        i.putExtra("URL", url);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<showsListItem> {

        private List<showsListItem> list;

        public seriesListAdapter(List<showsListItem> list) {
            super(getActivity(), R.layout.list_item_series, seriesList);
            this.list = list;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_series, parent, false);
            }

            showsListItem current = list.get(pos);

            TextView title = (TextView) view.findViewById(R.id.seriesTitle);
            title.setText(current.getTitle());

            TextView genre = (TextView) view.findViewById(R.id.seriesGenre);
            genre.setText(current.getGenre());

            TextView url = (TextView) view.findViewById(R.id.seriesUrl);
            url.setText(current.getUrl());

            return view;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }
    }
}
