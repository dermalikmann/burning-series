package de.m4lik.burningseries.ui.mainFragments;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ShowActivity;
import de.m4lik.burningseries.TabletShowActivity;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Logger;
import de.m4lik.burningseries.util.Settings;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.MainActivity.userSession;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_GENRE;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ID;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ISFAV;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_TITLE;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.TABLE_NAME;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {

    View rootView;

    ListView getSeriesListView;
    List<ShowListItem> seriesList = new ArrayList<>();


    public SeriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_series, container, false);
        getSeriesListView = (ListView) rootView.findViewById(R.id.seriesListView);

        fillList();
        showList();

        return rootView;
    }

    public void filterList(String query) {

        query = query.toLowerCase();

        List<ShowListItem> filteredList = new ArrayList<>();

        for (ShowListItem single : seriesList) {
            if (single.getTitle().toLowerCase().contains(query))
                filteredList.add(single);
        }

        showList(filteredList);
    }

    public void fillList() {

        seriesList.clear();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                COLUMN_NAME_ID,
                COLUMN_NAME_TITLE,
                COLUMN_NAME_GENRE,
                COLUMN_NAME_ISFAV
        };

        String sortOrder =
                COLUMN_NAME_TITLE + " ASC";

        Cursor c = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (c.moveToNext()) {
            seriesList.add(new ShowListItem(
                    c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(COLUMN_NAME_GENRE)),
                    c.getInt(c.getColumnIndex(COLUMN_NAME_ISFAV)) == 1
            ));
        }

        c.close();
        db.close();
    }

    public void showList() {
        showList(null);
    }

    private void showList(List<ShowListItem> inputList) {

        getSeriesListView.setVisibility(View.GONE);
        rootView.findViewById(R.id.nothing_found).setVisibility(View.VISIBLE);

        if (inputList == null) {
            inputList = seriesList;
        }

        if (!inputList.isEmpty()) {

            ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(inputList);
            getSeriesListView.setAdapter(adapter);

            getSeriesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                    if (!userSession.equals("")) {

                        SQLiteDatabase db = new MainDBHelper(getContext()).getReadableDatabase();

                        String[] projection = new String[]{
                                SeriesContract.seriesTable.COLUMN_NAME_ISFAV
                        };

                        Integer selectID = Integer.parseInt(((TextView) view.findViewById(R.id.seriesId)).getText().toString());


                        Cursor c = db.query(
                                SeriesContract.seriesTable.TABLE_NAME,
                                projection,
                                SeriesContract.seriesTable.COLUMN_NAME_ID + " = ?",
                                new String[]{selectID.toString()},
                                null,
                                null,
                                null
                        );

                        c.moveToFirst();
                        Boolean isFav = c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ISFAV)) == 1;

                        ImageView fav = (ImageView) view.findViewById(R.id.favImageView);
                        fav.setImageDrawable(ContextCompat.getDrawable(getContext(),
                                !isFav ? R.drawable.ic_star : R.drawable.ic_star_border)
                        );

                        if (isFav)
                            removeFromFavorites(selectID);
                        else
                            addToFavorites(selectID);

                        c.close();
                        db.close();

                    } else {
                        Snackbar snackbar = Snackbar.make(rootView, "Die Favoriten sind nur verfügbar wenn du angemeldet bist.", Snackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(ContextCompat.getColor(getContext(), theme().primaryColorDark));
                        snackbar.show();
                    }
                    return true;
                }
            });

            getSeriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String nameString = ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString();
                    String idString = ((TextView) view.findViewById(R.id.seriesId)).getText().toString();
                    String genreString = ((TextView) view.findViewById(R.id.seriesGenre)).getText().toString();
                    Logger.seriesSelection(getContext(), idString, nameString);
                    showSeries(Integer.parseInt(idString), nameString, genreString);
                }
            });

            rootView.findViewById(R.id.nothing_found).setVisibility(View.GONE);
            getSeriesListView.setVisibility(View.VISIBLE);
        }
    }

    private void showSeries(Integer id, String name, String genre) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowName", name);
        i.putExtra("ShowID", id);
        startActivity(i);
    }

    class seriesListAdapter extends ArrayAdapter<ShowListItem> {

        private List<ShowListItem> list;

        seriesListAdapter(List<ShowListItem> list) {
            super(getActivity(), R.layout.list_item_series, seriesList);
            this.list = list;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
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
            if (!Settings.of(getContext()).themeName().contains("_DARK"))
                fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isFav() ? R.drawable.ic_star : R.drawable.ic_star_border));
            else
                fav.setImageDrawable(ContextCompat.getDrawable(getContext(), current.isFav() ? R.drawable.ic_star_white : R.drawable.ic_star_border_white));

            return view;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }
    }

    private void addToFavorites(Integer id) {

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 1);
        db.update(SeriesContract.seriesTable.TABLE_NAME, cv, SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + id, null);


        String[] projection = {
                SeriesContract.seriesTable.COLUMN_NAME_ID
        };

        String selection = SeriesContract.seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                SeriesContract.seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ID)) + ",";
        favs = favs.substring(0, favs.length() - 1);

        c.close();
        db.close();


        API api = new API();
        APIInterface apiInterface = api.getInterface();
        api.setSession(userSession);
        api.generateToken("user/series/set/" + favs);
        Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void removeFromFavorites(Integer id) {

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 0);
        db.update(SeriesContract.seriesTable.TABLE_NAME, cv, SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + id, null);

        String[] projection = {
                SeriesContract.seriesTable.COLUMN_NAME_ID
        };

        String selection = SeriesContract.seriesTable.COLUMN_NAME_ISFAV + " = ?";
        String[] selectionArgs = {"1"};

        Cursor c = db.query(
                SeriesContract.seriesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String favs = "";
        while (c.moveToNext())
            favs += c.getInt(c.getColumnIndex(SeriesContract.seriesTable.COLUMN_NAME_ID)) + ",";
        if (!favs.equals(""))
            favs = favs.substring(0, favs.length() - 1);

        c.close();
        db.close();


        API api = new API();
        APIInterface apiInterface = api.getInterface();
        api.setSession(userSession);
        api.generateToken("user/series/set/" + favs);
        Call<ResponseBody> call = apiInterface.setFavorites(api.getToken(), api.getUserAgent(), favs, api.getSession());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}