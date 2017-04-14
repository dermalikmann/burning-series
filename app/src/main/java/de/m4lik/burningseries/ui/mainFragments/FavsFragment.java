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
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ISFAV;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_TITLE;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavsFragment extends Fragment {

    @BindView(R.id.favsRecyclerView)
    RecyclerView favsRecyclerView;

    List<ShowListItem> favs = new ArrayList<>();

    public FavsFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favs, container, false);
        ButterKnife.bind(this, rootView);

        favs.clear();

        MainDBHelper dbHelper = new MainDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

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

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        favsRecyclerView.setLayoutManager(llm);
        favsRecyclerView.setAdapter(new SeriesRecyclerAdapter(favs));
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

