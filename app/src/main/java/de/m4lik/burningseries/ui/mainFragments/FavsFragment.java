package de.m4lik.burningseries.ui.mainFragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.ui.viewAdapters.SeriesRecyclerAdapter;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavsFragment extends Fragment {

    @BindView(R.id.favsRecyclerView)
    RecyclerView favsRecyclerView;

    List<ShowListItem> favs = new ArrayList<>();

    public FavsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favs, container, false);
        ButterKnife.bind(this, rootView);

        favs.clear();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                seriesTable.COLUMN_NAME_ID,
                seriesTable.COLUMN_NAME_TITLE,
                seriesTable.COLUMN_NAME_GENRE,
                seriesTable.COLUMN_NAME_ISFAV
        };

        String sortOrder =
                seriesTable.COLUMN_NAME_TITLE + " ASC";

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                seriesTable.COLUMN_NAME_ISFAV + " = ?",
                new String[]{"1"},
                null,
                null,
                sortOrder
        );

        if (c.moveToFirst())
            do {
                favs.add(new ShowListItem(
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_TITLE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)),
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_GENRE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ISFAV)) == 1
                ));
            } while (c.moveToNext());

        c.close();
        db.close();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        favsRecyclerView.setLayoutManager(llm);
        favsRecyclerView.setAdapter(new SeriesRecyclerAdapter(getActivity(), favs));
        favsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), favsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        TextView idView = (TextView) view.findViewById(R.id.seriesId);
                        TextView nameView = (TextView) view.findViewById(R.id.seriesTitle);
                        showSeries(Integer.parseInt(idView.getText().toString()), nameView.getText().toString());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

        return rootView;
    }

    private void showSeries(Integer id, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowID", id);
        i.putExtra("ShowName", name);
        startActivity(i);
    }
}

