package de.monarchcode.m4lik.burningseries.database;

import android.provider.BaseColumns;

/**
 * Created by Malik (M4lik) on 17.08.2016.
 *
 * @author M4lik, mm.malik.mann@gmail.com
 */
public final class SeriesContract {

    public SeriesContract() {
    }

    public static abstract class favoritesTable implements BaseColumns {
        public static final String TABLE_NAME = "favList";
        public static final String COLUMN_NAME_ID = "seriesId";
    }

    public static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + favoritesTable.TABLE_NAME + " (" +
                    favoritesTable._ID + " INTEGER PRIMARY KEY," +
                    favoritesTable.COLUMN_NAME_ID + " INTEGER" + ")";

    public static final String SQL_DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + favoritesTable.TABLE_NAME;
}
