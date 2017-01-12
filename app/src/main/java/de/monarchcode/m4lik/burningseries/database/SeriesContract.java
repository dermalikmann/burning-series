package de.monarchcode.m4lik.burningseries.database;

import android.provider.BaseColumns;

/**
 * Contract class for the main database.
 * Defines tables and operations on these tables.
 * @author Malik Mann
 */
public final class SeriesContract {

    public SeriesContract() {
    }

    public static abstract class seriesTable implements BaseColumns {
        public static final String TABLE_NAME           = "seriesTable";
        public static final String COLUMN_NAME_ID       = "seriesID";
        public static final String COLUMN_NAME_TITLE    = "title";
        public static final String COLUMN_NAME_GENRE    = "genre";
        public static final String COLUMN_NAME_DESCR    = "description";
        public static final String COLUMN_NAME_ISFAV    = "isFav";
    }

    public static abstract class genresTable implements BaseColumns {
        public static final String TABLE_NAME           = "genresTable";
        public static final String COLUMN_NAME_ID       = "genreID";
        public static final String COLUMN_NAME_GENRE    = "genreName";
    }

    static final String SQL_CREATE_SERIES_TABLE =
            "CREATE TABLE " + seriesTable.TABLE_NAME + " (" +
                    seriesTable._ID + " INTEGER PRIMARY KEY"    + "," +
                    seriesTable.COLUMN_NAME_ID + " INTEGER"     + "," +
                    seriesTable.COLUMN_NAME_TITLE + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_GENRE + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_DESCR + " TEXT"     + "," +
                    seriesTable.COLUMN_NAME_ISFAV + " INTEGER"  + ")";



    static final String SQL_CREATE_GENRES_TABLE =
            "CREATE TABLE " + genresTable.TABLE_NAME + " (" +
                    genresTable._ID + " INTEGER PRIMARY KEY" + "," +
                    genresTable.COLUMN_NAME_ID + " INTEGER"  + "," +
                    genresTable.COLUMN_NAME_GENRE + " TEXT"  + ")";

    static final String SQL_DELETE_SERIES_TABLE =
            "DROP TABLE IF EXISTS " + seriesTable.TABLE_NAME;

    static final String SQL_DELETE_GENRES_TABLE =
            "DROP TABLE IF EXISTS " + genresTable.TABLE_NAME;

    public static final String SQL_TRUNCATE_SERIES_TABLE =
            "DELETE FROM " + seriesTable.TABLE_NAME;

    public static final String SQL_TRUNCATE_GENRES_TABLE =
            "DELETE FROM " + genresTable.TABLE_NAME;
}
