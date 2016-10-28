package de.m4lik.monarchcode.burningseries.mainFragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.monarchcode.burningseries.MainActivity;
import de.m4lik.monarchcode.burningseries.R;
import de.m4lik.monarchcode.burningseries.objects.GenreListItem;
import de.m4lik.monarchcode.burningseries.objects.ShowListItem;

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
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Genrenliste wird erstellt.\nBitte warten...");

        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        MenuItem menuItem = MainActivity.menu.findItem(R.id.action_search);
        menuItem.setVisible(false);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(null);


        //new getGenres().execute();

        return rootView;
    }

    /*class getGenres extends AsyncTask<Void, Void, Void> {

        //private String genresTitle;
        //private String genresGenre;

        private String allGenresURL = "https://bs.to/serie-genre";

        private Document webDoc;

        public getGenres() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                webDoc = Jsoup.connect(allGenresURL).get();

                for (Element genre : webDoc.select("div.genre")) {
                    String curGenre = genre.select("span").first().text();
                    genresList.add(new listItemGenre(curGenre));

                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            populateListGenres();
            progressDialog.dismiss();
        }
    }

    private void populateListGenres() {
        ArrayAdapter<listItemGenre> adapter = new genresListAdapter();
        genresListView.setAdapter(adapter);

        genresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView lableView = (TextView) view.findViewById(R.id.genreLable);
                new getSeries(lableView.getText().toString()).execute();
            }
        });
    }

    class genresListAdapter extends ArrayAdapter<listItemGenre> {

        public genresListAdapter() {
            super(getActivity(), R.layout.list_item_genres, genresList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_genres, parent, false);
            }

            listItemGenre current = genresList.get(pos);

            TextView lable = (TextView) view.findViewById(R.id.genreLable);
            lable.setText(current.getLable());

            return view;
        }
    }*/




    /** Show Matching Sereis **/


    /*class getSeries extends AsyncTask<Void, Void, Void> {

        //private String seriesTitle;
        //private String seriesGenre;

        private String allSeriesURL = "https://bs.to/serie-genre";

        private Document webDoc;

        private String genreLable;

        public getSeries(String genreLable) {
            this.genreLable = genreLable;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                webDoc = Jsoup.connect(allSeriesURL).get();

                for (Element genre : webDoc.select("div.genre")) {
                    String curGenre = genre.select("span").first().text();
                    if (curGenre.equals(genreLable)) {
                        for (Element serie : genre.select("ul li a")) {
                            String title = serie.text();
                            String url = serie.attr("abs:href");

                            seriesList.add(new listItemSeries(title, url, curGenre));
                        }
                    }
                }

            } catch (IOException e) {
                Log.d("JSOUP", "Can't connect:", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            populateListSeries();
            progressDialog.dismiss();
        }
    }



    private void populateListSeries() {
        ArrayAdapter<listItemSeries> adapter = new seriesListAdapter();
        genresListView.setAdapter(adapter);

        genresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView urlView = (TextView) view.findViewById(R.id.seriesId);
                showSeries(urlView.getText().toString());
            }
        });
    }

    private void showSeries(String url) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        i.putExtra("URL", url);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<listItemSeries> {

        public seriesListAdapter() {
            super(getActivity(), R.layout.list_item_series, seriesList);
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.list_item_series, parent, false);
            }

            listItemSeries current = seriesList.get(pos);

            TextView title = (TextView) view.findViewById(R.id.seriesTitle);
            title.setText(current.getTitle());

            TextView genre = (TextView) view.findViewById(R.id.seriesGenre);
            genre.setText(current.getGenre());

            TextView id = (TextView) view.findViewById(R.id.seriesId);
            id.setText(current.getId());

            return view;
        }
    }*/
}

