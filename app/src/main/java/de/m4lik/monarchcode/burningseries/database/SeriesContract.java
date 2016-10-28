package de.m4lik.monarchcode.burningseries.database;

import android.provider.BaseColumns;

/**
 * Created by Malik on 17.08.2016.
 */
public final class SeriesContract {

    public SeriesContract() {}

    public static abstract class seriesTable implements BaseColumns {
        public static final String TABLE_NAME = "series";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_GENRE = "genre";
        public static final String COLUMN_NAME_URL= "url";
        public static final String COLUMN_NAME_FAV = "isFav";
        public static final String COLUMN_NAME_DSCR = "description";
    }

    public static final String SQL_STRUCTURE_SERIES_TABLE =
            "CREATE TABLE " + seriesTable.TABLE_NAME + " ("  +
                    seriesTable._ID + " INTEGER PRIMARY KEY," +
                    seriesTable.COLUMN_NAME_TITLE + " TEXT," +
                    seriesTable.COLUMN_NAME_GENRE + " TEXT," +
                    seriesTable.COLUMN_NAME_URL + " TEXT" + ")";

    /*public static final String SQL_STRUCTURE_SERIES_TABLE =
            "CREATE TABLE " + seriesTable.TABLE_NAME + " ("  +
                    seriesTable._ID + " INTEGER PRIMARY KEY," +
                    seriesTable.COLUMN_NAME_TITLE + " TEXT," +
                    seriesTable.COLUMN_NAME_GENRE + " TEXT," +
                    seriesTable.COLUMN_NAME_URL + " TEXT," +
                    seriesTable.COLUMN_NAME_FAV + " TEXT," +
                    seriesTable.COLUMN_NAME_DSCR+ " TEXT" + ")";*/

    public static final String SQL_DELETE_SERIES_TABLE =
            "DROP TABLE IF EXISTS " + seriesTable.TABLE_NAME;
}
