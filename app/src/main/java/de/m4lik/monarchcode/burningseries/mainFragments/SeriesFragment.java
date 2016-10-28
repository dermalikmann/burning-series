package de.m4lik.monarchcode.burningseries.mainFragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.m4lik.monarchcode.burningseries.MainActivity;
import de.m4lik.monarchcode.burningseries.R;
import de.m4lik.monarchcode.burningseries.api.API;
import de.m4lik.monarchcode.burningseries.api.APIInterface;
import de.m4lik.monarchcode.burningseries.database.MainDBHelper;
import de.m4lik.monarchcode.burningseries.ShowActivity;
import de.m4lik.monarchcode.burningseries.objects.GenreMap;
import de.m4lik.monarchcode.burningseries.objects.GenreObj;
import de.m4lik.monarchcode.burningseries.objects.ShowObj;
import de.m4lik.monarchcode.burningseries.objects.ShowListItem;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment implements Callback<GenreMap>{

    View rootView;

    ListView seriesListView;

    List<ShowListItem> seriesList = new ArrayList<>();

    ProgressDialog progressDialog;

    MainDBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor = null;


    public SeriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_series, container, false);
        seriesListView = (ListView) rootView.findViewById(R.id.seriesListView);

        /*progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Serienliste wird erstellt.\nBitte warten...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);*/

        //mainDBHelper = new MainDBHelper(getContext());
        //db = mainDBHelper.getWritableDatabase();

        //new getSeries().execute();



        API api = new API();
        api.generateToken("series:genre" + "?s=" + api.getSession());
        APIInterface apii = api.getApiInterface();
        //Call<ResponseBody> call = apii.getSeriesGenreList(api.getToken(), api.getUserAgent(), "");
        Call<GenreMap> call = apii.getSeriesGenreList(api.getToken(), api.getUserAgent(), "");
        call.enqueue(this);


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


    @Override
    public void onResponse(Call<GenreMap> call, Response<GenreMap> response) {

        List<ShowListItem> tempseries = new ArrayList<>();
        for (Map.Entry<String, GenreObj> entry : response.body().entrySet()) {
            String currentGenre = entry.getKey().toString();
            GenreObj go = entry.getValue();
            for (ShowObj show : go.getShows()) {
                tempseries.add(new ShowListItem(show.getName(), show.getId(), currentGenre));
            }
        }

        String[][] series = new String[tempseries.size()][3];

        int i = 0;

        for (ShowListItem show : tempseries) {
            series[i][0] = show.getTitle();
            series[i][1] = show.getId().toString();
            series[i][2] = show.getGenre();
            i++;
        }

        Arrays.sort(series, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                final String show1 = entry1[0];
                final String show2 = entry2[0];
                return show1.compareTo(show2);
            }
        });

        for (String[] show : series) {
            seriesList.add(new ShowListItem(show[0], Integer.parseInt(show[1]), show[2]));
        }

        refreshList();

    }

    @Override
    public void onFailure(Call<GenreMap> call, Throwable t) {
        Snackbar.make(rootView, "Verbindungsfehler.", Snackbar.LENGTH_SHORT);
    }


    public void filterList(String query) {

        List<ShowListItem> filteredList = new ArrayList<>();

        for (ShowListItem single : seriesList) {
            if (single.getTitle().toLowerCase().contains(query.toLowerCase())) filteredList.add(single);
        }

        refreshList(filteredList);
    }

    private void refreshList() {refreshList(null);}

    private void refreshList(List<ShowListItem> list) {
        if (list == null) {
            list = seriesList;
        }

        if (list.size() >= 1) {
            ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(list);
            seriesListView.setAdapter(adapter);

            seriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TextView idView = (TextView) view.findViewById(R.id.seriesId);
                    TextView nameView = (TextView) view.findViewById(R.id.seriesTitle);
                    showSeries(Integer.parseInt(idView.getText().toString()), nameView.getText().toString());
                }
            });
        } else {
            seriesListView.setAdapter(null);
        }
    }

    private void showSeries(Integer id, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        i.putExtra("ShowID", id);
        i.putExtra("ShowName", name);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<ShowListItem> {

        private List<ShowListItem> list;

        public seriesListAdapter(List<ShowListItem> list) {
            super(getActivity(), R.layout.list_item_series, seriesList);
            this.list = list;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_series, parent, false);
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
