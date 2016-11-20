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

    public static abstract class seriesTable implements BaseColumns {
        public static final String TABLE_NAME           = "seriesTable";
        public static final String COLUMN_NAME_ID       = "seriesId";
        public static final String COLUMN_NAME_TITLE    = "title";
        public static final String COLUMN_NAME_GENRE    = "genre";
        public static final String COLUMN_NAME_DESCR    = "description";
        public static final String COLUMN_NAME_ISFAV    = "isFav";
    }

    public static abstract class favoritesTable implements BaseColumns {
        public static final String TABLE_NAME           = "favList";
        public static final String COLUMN_NAME_ID       = "seriesId";
        public static final String COLUMN_NAME_TILTE    = "gerTitle";
        public static final String COLUMN_NAME_GENRE    = "engTitle";
    }

    static final String SQL_CREATE_SERIES_TABLE =
            "CREATE TABLE " + seriesTable.TABLE_NAME + " (" +
                    seriesTable._ID + " INTEGER PRIMARY KEY"    + "," +
                    seriesTable._ID + " INTEGER PRIMARY KEY"    + "," +
                    seriesTable.COLUMN_NAME_ID + " INTEGER"     + "," +
                    seriesTable.COLUMN_NAME_TITLE + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_GENRE + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_DESCR + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_ISFAV + " INTEGER"  + ")";



    static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + favoritesTable.TABLE_NAME + " (" +
                    favoritesTable._ID + " INTEGER PRIMARY KEY" + "," +
                    favoritesTable.COLUMN_NAME_ID + " INTEGER"  + "," +
                    favoritesTable.COLUMN_NAME_TILTE + " TEXT"  + "," +
                    favoritesTable.COLUMN_NAME_GENRE + " TEXT"  + ")";

    static final String SQL_DELETE_SERIES_TABLE =
            "DROP TABLE IF EXISTS " + seriesTable.TABLE_NAME;

    static final String SQL_DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + favoritesTable.TABLE_NAME;

    public static final String SQL_TRUNCATE_FAVORITES_TABLE =
            "DELETE FROM " + favoritesTable.TABLE_NAME;
}
