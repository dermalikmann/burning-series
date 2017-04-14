package de.m4lik.burningseries.ui.mainFragments;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.api.API;
import de.m4lik.burningseries.api.APIInterface;
import de.m4lik.burningseries.database.MainDBHelper;
import de.m4lik.burningseries.database.SeriesContract;
import de.m4lik.burningseries.databinding.ListItemSeriesBinding;
import de.m4lik.burningseries.ui.ShowActivity;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.listitems.ShowListItem;
import de.m4lik.burningseries.util.Logger;
import de.m4lik.burningseries.util.Settings;
import de.m4lik.burningseries.util.listeners.RecyclerItemClickListener;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_GENRE;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ID;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_ISFAV;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.COLUMN_NAME_TITLE;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable.TABLE_NAME;
import static de.m4lik.burningseries.services.ThemeHelperService.theme;
import static de.m4lik.burningseries.ui.MainActivity.userSession;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeriesFragment extends Fragment {

    View rootView;

    @BindView(R.id.seriesRecyclerView)
    RecyclerView seriesRecyclerView;

    SeriesRecyclerAdapter seriesRecyclerAdapter;
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

        seriesRecyclerAdapter = new SeriesRecyclerAdapter();
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
                        String nameString = ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString();
                        String idString = ((TextView) view.findViewById(R.id.seriesId)).getText().toString();
                        Logger.seriesSelection(getContext(), idString, nameString);
                        showSeries(Integer.parseInt(idString), nameString);
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
                            removeFromFavorites(selectID);
                        else
                            addToFavorites(selectID);

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

        seriesRecyclerAdapter.add(seriesList);

        c.close();
        db.close();
    }

    public void showList() {
        showList(null);
    }

    private void showList(List<ShowListItem> inputList) {

        //getSeriesListView.setVisibility(View.GONE);
        rootView.findViewById(R.id.nothing_found).setVisibility(View.VISIBLE);

        if (inputList == null) {
            inputList = seriesList;
        }

        if (!inputList.isEmpty()) {

            /*ArrayAdapter<ShowListItem> adapter = new seriesListAdapter(inputList);
            getSeriesListView.setAdapter(adapter);

            getSeriesListView.setOnItemLongClickListener((adapterView, view, position, id) -> {

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
                return true;
            });

            getSeriesListView.setOnItemClickListener((parent, view, position, id) -> {
                String nameString = ((TextView) view.findViewById(R.id.seriesTitle)).getText().toString();
                String idString = ((TextView) view.findViewById(R.id.seriesId)).getText().toString();
                String genreString = ((TextView) view.findViewById(R.id.seriesGenre)).getText().toString();
                Logger.seriesSelection(getContext(), idString, nameString);
                showSeries(Integer.parseInt(idString), nameString, genreString);
            });*/

            rootView.findViewById(R.id.nothing_found).setVisibility(View.GONE);
            //getSeriesListView.setVisibility(View.VISIBLE);
        }
    }

    private void showSeries(Integer id, String name) {
        Intent i = new Intent(getActivity(), ShowActivity.class);
        if (getContext().getResources().getBoolean(R.bool.isTablet))
            i = new Intent(getActivity(), TabletShowActivity.class);
        i.putExtra("ShowName", name);
        i.putExtra("ShowID", id);
        startActivity(i);
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


        if (!userSession.equals("")) {
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


        if (!userSession.equals("")) {
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

    private class SeriesRecyclerAdapter extends RecyclerView.Adapter<SeriesRecyclerAdapter.SeriesViewHolder>
            implements FastScrollRecyclerView.SectionedAdapter {

        private final SortedList<ShowListItem> showListItemSortedList = new SortedList<ShowListItem>(ShowListItem.class, new SortedList.Callback<ShowListItem>() {

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public int compare(ShowListItem o1, ShowListItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ShowListItem oldItem, ShowListItem newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areItemsTheSame(ShowListItem item1, ShowListItem item2) {
                return item1.getId().equals(item2.getId());
            }
        });

        @Override
        public SeriesRecyclerAdapter.SeriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            ListItemSeriesBinding binding = ListItemSeriesBinding.inflate(layoutInflater, parent, false);
            return new SeriesViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(SeriesRecyclerAdapter.SeriesViewHolder holder, int position) {
            ShowListItem current = showListItemSortedList.get(position);
            holder.bind(current);
        }

        @Override
        public int getItemCount() {
            return showListItemSortedList.size();
        }

        @NonNull
        @Override
        public String getSectionName(int position) {
            return String.valueOf(showListItemSortedList.get(position).getTitle().charAt(0));
        }

        public void add(ShowListItem model) {
            showListItemSortedList.add(model);
        }

        public void remove(ShowListItem model) {
            showListItemSortedList.remove(model);
        }

        public void add(List<ShowListItem> models) {
            showListItemSortedList.addAll(models);
        }

        public void remove(List<ShowListItem> models) {
            showListItemSortedList.beginBatchedUpdates();
            models.forEach(showListItemSortedList::remove);
            showListItemSortedList.endBatchedUpdates();
        }

        public void replaceAll(List<ShowListItem> models) {
            showListItemSortedList.beginBatchedUpdates();
            for (int i = showListItemSortedList.size() - 1; i >= 0; i--) {
                final ShowListItem model = showListItemSortedList.get(i);
                if (!models.contains(model)) {
                    showListItemSortedList.remove(model);
                }
            }
            showListItemSortedList.addAll(models);
            showListItemSortedList.endBatchedUpdates();
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
                if (!Settings.of(getActivity()).isDarkTheme())
                    ((ImageView) binding.getRoot().findViewById(R.id.favImageView))
                            .setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    item.isFav() ? R.drawable.ic_star : R.drawable.ic_star_border));
                else
                    ((ImageView) binding.getRoot().findViewById(R.id.favImageView))
                            .setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                    item.isFav() ? R.drawable.ic_star_white : R.drawable.ic_star_border_white));
                binding.executePendingBindings();
            }
        }
    }
}