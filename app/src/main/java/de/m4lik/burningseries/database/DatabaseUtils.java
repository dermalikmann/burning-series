package de.m4lik.burningseries.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.ui.listitems.GenreListItem;
import de.m4lik.burningseries.ui.listitems.HistoryListItem;
import de.m4lik.burningseries.ui.listitems.ShowListItem;

import static android.provider.BaseColumns._ID;
import static de.m4lik.burningseries.database.SeriesContract.genresTable;
import static de.m4lik.burningseries.database.SeriesContract.historyTable;
import static de.m4lik.burningseries.database.SeriesContract.seriesTable;

/**
 * Created by malik on 09.05.17.
 */

public class DatabaseUtils {

    private Context context;
    private MainDBHelper dbHelper;
    private SQLiteDatabase db;
    private boolean writeable;

    private DatabaseUtils(Context context) {
        this(context, false);
    }

    private DatabaseUtils(Context context, boolean writable) {
        this.context = context;
        this.dbHelper = new MainDBHelper(context);
        if (writable)
            this.db = dbHelper.getWritableDatabase();
        else
            this.db = dbHelper.getReadableDatabase();
    }

    public static DatabaseUtils with(Context context) {
        return new DatabaseUtils(context);
    }

    public List<Integer> getFavorites() {

        String[] projection = {
                seriesTable.COLUMN_NAME_ID
        };

        String selection = seriesTable.COLUMN_NAME_ISFAV + " = 1";

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                projection,
                selection,
                null,
                null,
                null,
                null
        );

        List<Integer> favs = new ArrayList<>();
        if (c.moveToFirst())
            do {
                favs.add(c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)));
                c.moveToNext();
            } while (c.moveToNext());

        c.close();
        db.close();

        return favs;
    }

    public void addToFavorites(int showID) {
        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 1);
        db.update(SeriesContract.seriesTable.TABLE_NAME,
                cv,
                SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + showID,
                null);
        db.close();
    }

    public void removeFromFavorites(int showID) {
        ContentValues cv = new ContentValues();
        cv.put(SeriesContract.seriesTable.COLUMN_NAME_ISFAV, 0);
        db.update(SeriesContract.seriesTable.TABLE_NAME,
                cv,
                SeriesContract.seriesTable.COLUMN_NAME_ID + " = " + showID,
                null);
        db.close();
    }

    public List<HistoryListItem> getWatchHistory() {

        List<HistoryListItem> list = new ArrayList<>();

        String[] projection = {
                historyTable.COLUMN_NAME_SHOW_ID,
                historyTable.COLUMN_NAME_SEASON_ID,
                historyTable.COLUMN_NAME_EPISODE_ID,
                historyTable.COLUMN_NAME_SHOW_NAME,
                historyTable.COLUMN_NAME_EPISODE_NAME,
                historyTable.COLUMN_NAME_DATE,
                historyTable.COLUMN_NAME_TIME
        };

        String sortOrder =
                _ID + " DESC";

        Cursor c = db.query(
                historyTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        int i = 0;
        while (c.moveToNext() && i < 20) {
            list.add(new HistoryListItem(
                    c.getInt(c.getColumnIndex(historyTable.COLUMN_NAME_SHOW_ID)),
                    c.getInt(c.getColumnIndex(historyTable.COLUMN_NAME_SEASON_ID)),
                    c.getInt(c.getColumnIndex(historyTable.COLUMN_NAME_EPISODE_ID)),
                    c.getString(c.getColumnIndex(historyTable.COLUMN_NAME_SHOW_NAME)),
                    c.getString(c.getColumnIndex(historyTable.COLUMN_NAME_EPISODE_NAME))
            ));
            i++;
        }

        c.close();
        db.close();

        return list;
    }

    public List<GenreListItem> getGenreList() {
        List<GenreListItem> list = new ArrayList<>();

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
            list.add(new GenreListItem(
                    c.getInt(c.getColumnIndex(genresTable.COLUMN_NAME_ID)),
                    c.getString(c.getColumnIndex(genresTable.COLUMN_NAME_GENRE))
            ));
        }

        c.close();
        db.close();

        return list;
    }

    public List<ShowListItem> getSeriesListOfGenre(String lable) {
        List<ShowListItem> list = new ArrayList<>();

        String[] projection = {
                seriesTable.COLUMN_NAME_TITLE,
                seriesTable.COLUMN_NAME_ID,
                seriesTable.COLUMN_NAME_GENRE,
                seriesTable.COLUMN_NAME_ISFAV
        };

        String selection = seriesTable.COLUMN_NAME_GENRE + " = ?";
        String[] selectionArgs = {lable};

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

        if (c.moveToFirst())
            do {
                list.add(new ShowListItem(
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_TITLE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)),
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_GENRE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ISFAV)) == 1
                ));
            } while (c.moveToNext());

        c.close();
        db.close();

        return list;
    }

    public List<ShowListItem> getSeriesList() {
        List<ShowListItem> list = new ArrayList<>();

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
                null,
                null,
                null,
                null,
                sortOrder
        );


        if (c.moveToFirst())
            do {
                list.add(new ShowListItem(
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_TITLE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ID)),
                        c.getString(c.getColumnIndex(seriesTable.COLUMN_NAME_GENRE)),
                        c.getInt(c.getColumnIndex(seriesTable.COLUMN_NAME_ISFAV)) == 1
                ));
            } while (c.moveToNext());

        c.close();
        db.close();

        return list;
    }

    public Boolean isSeriesListEmpty() {

        Cursor c = db.query(
                seriesTable.TABLE_NAME,
                new String[]{seriesTable.COLUMN_NAME_ID},
                null,
                null,
                null,
                null,
                null
        );

        int count = c.getCount();
        c.close();
        db.close();

        return count == 0;

    }
}
