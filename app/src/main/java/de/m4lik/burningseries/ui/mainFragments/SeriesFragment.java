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

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.DatabaseUtils;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.ui.viewAdapters.SortedSeriesRecyclerAdapter;
import de.m4lik.burningseries.util.Logger;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.ShowUtils;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

import static de.m4lik.burningseries.services.ThemeHelperService.theme;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {

    View rootView;

    @BindView(R.id.seriesRecyclerView)
    RecyclerView seriesRecyclerView;

    SortedSeriesRecyclerAdapter seriesRecyclerAdapter;
    List<ShowListItem> seriesList = new ArrayList<>();

    public SeriesFragment() {
        // Required empty public constructor
    }

    private static List<ShowListItem> filter(List<ShowListItem> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<ShowListItem> filteredModelList = new ArrayList<>();
        for (ShowListItem model : models) {
            final String text = model.getTitle().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_series, container, false);

        ButterKnife.bind(this, rootView);

        seriesRecyclerAdapter = new SortedSeriesRecyclerAdapter(getActivity());
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        seriesRecyclerView.setLayoutManager(llm);
        seriesRecyclerView.setAdapter(seriesRecyclerAdapter);
        ((FastScrollRecyclerView) seriesRecyclerView).setPopupBgColor(ContextCompat.getColor(getActivity(), theme().primaryColor));
        ((FastScrollRecyclerView) seriesRecyclerView).setThumbColor(ContextCompat.getColor(getActivity(), theme().primaryColorDark));
        ((FastScrollRecyclerView) seriesRecyclerView).setPopupTextSize(100);
        seriesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), seriesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        String title = ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString();
                        Integer id = Integer.parseInt(((TextView) view.findViewById(R.id.seriesId)).getText().toString());
                        Logger.seriesSelection(id, title);
                        showSeries(id, title);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

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

                        if (!Settings.of(getActivity()).isDarkTheme())
                            fav.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    !isFav ? R.drawable.ic_star : R.drawable.ic_star_border));
                        else
                            fav.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    !isFav ? R.drawable.ic_star_white : R.drawable.ic_star_border_white));

                        if (isFav)
                            ShowUtils.removeFromFavorites(getActivity(), selectID);
                        else
                            ShowUtils.addToFavorites(getActivity(), selectID);

                        c.close();
                        db.close();
                    }
                })
        );

        fillList();

        return rootView;
    }

    public void filterList(String query) {

        final List<ShowListItem> filteredModelList = filter(seriesList, query);
        seriesRecyclerAdapter.replaceAll(filteredModelList);
        seriesRecyclerView.scrollToPosition(0);
    }

    private void fillList() {
        seriesList = DatabaseUtils.with(getActivity()).getSeriesList();
        seriesRecyclerAdapter.add(seriesList);
    }

    private void showSeries(Integer id, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowName", name);
        i.putExtra("ShowID", id);
        startActivity(i);
    }
}