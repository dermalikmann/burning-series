package de.m4lik.burningseries.database;

import android.provider.BaseColumns;

/**
 * Contract class for the main database.
 * Defines tables and operations on these tables.
 *
 * @author Malik Mann
 */
public final class SeriesContract {

    public static final String SQL_TRUNCATE_SERIES_TABLE =
            "DELETE FROM " + seriesTable.TABLE_NAME;

    public static final String SQL_TRUNCATE_GENRES_TABLE =
            "DELETE FROM " + genresTable.TABLE_NAME;

    public static final String SQL_TRUNCATE_NEWS_TABLE =
            "DELETE FROM " + newsTable.TABLE_NAME;

    static final String SQL_CREATE_SERIES_TABLE =
            "CREATE TABLE " + seriesTable.TABLE_NAME + " (" +
                    seriesTable._ID + " INTEGER PRIMARY KEY" + "," +
                    seriesTable.COLUMN_NAME_ID + " INTEGER" + "," +
                    seriesTable.COLUMN_NAME_TITLE + " TEXT" + "," +
                    seriesTable.COLUMN_NAME_GENRE + " TEXT" + "," +
                    seriesTable.COLUMN_NAME_DESCR + " TEXT" + "," +
                    seriesTable.COLUMN_NAME_ISFAV + " INTEGER" + ")";

    static final String SQL_CREATE_GENRES_TABLE =
            "CREATE TABLE " + genresTable.TABLE_NAME + " (" +
                    genresTable._ID + " INTEGER PRIMARY KEY" + "," +
                    genresTable.COLUMN_NAME_ID + " INTEGER" + "," +
                    genresTable.COLUMN_NAME_GENRE + " TEXT" + ")";

    static final String SQL_CREATE_HISTORY_TABLE =
            "CREATE TABLE " + historyTable.TABLE_NAME + " (" +
                    historyTable._ID + " INTEGER PRIMARY KEY" + "," +
                    historyTable.COLUMN_NAME_SHOW_ID + " INTEGER" + "," +
                    historyTable.COLUMN_NAME_SEASON_ID + " INTEGER" + "," +
                    historyTable.COLUMN_NAME_EPISODE_ID + " INTEGER" + "," +
                    historyTable.COLUMN_NAME_SHOW_NAME + " TEXT" + "," +
                    historyTable.COLUMN_NAME_EPISODE_NAME + " TEXT" + "," +
                    historyTable.COLUMN_NAME_DATE + " TEXT" + "," +
                    historyTable.COLUMN_NAME_TIME + " TEXT" + ")";

    static final String SQL_CREATE_NEWS_TABLE =
            "CREATE TABLE " + newsTable.TABLE_NAME + " (" +
                    newsTable._ID + " INTEGER PRIMARY KEY" + "," +
                    newsTable.COLUMN_NAME_ID + " INTEGER" + "," +
                    newsTable.COLUMN_NAME_TITLE + " TEXT" + "," +
                    newsTable.COLUMN_NAME_DATE + " TEXT" + "," +
                    newsTable.COLUMN_NAME_CONTENT + " TEXT" + ")";

    static final String SQL_DELETE_SERIES_TABLE =
            "DROP TABLE IF EXISTS " + seriesTable.TABLE_NAME;

    static final String SQL_DELETE_GENRES_TABLE =
            "DROP TABLE IF EXISTS " + genresTable.TABLE_NAME;

    static final String SQL_DELETE_HISTORY_TABLE =
            "DROP TABLE IF EXISTS " + historyTable.TABLE_NAME;

    static final String SQL_DELETE_NEWS_TABLE =
            "DROP TABLE IF EXISTS " + newsTable.TABLE_NAME;

    public SeriesContract() {}

    public static abstract class seriesTable implements BaseColumns {
        public static final String TABLE_NAME = "seriesTable";
        public static final String COLUMN_NAME_ID = "seriesID";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_GENRE = "genre";
        public static final String COLUMN_NAME_DESCR = "description";
        public static final String COLUMN_NAME_ISFAV = "isFav";
    }

    public static abstract class genresTable implements BaseColumns {
        public static final String TABLE_NAME = "genresTable";
        public static final String COLUMN_NAME_ID = "genreID";
        public static final String COLUMN_NAME_GENRE = "genreName";
    }

    public static abstract class historyTable implements BaseColumns {
        public static final String TABLE_NAME = "historyTable";
        public static final String COLUMN_NAME_SHOW_ID = "showID";
        public static final String COLUMN_NAME_SEASON_ID = "seasonID";
        public static final String COLUMN_NAME_EPISODE_ID = "episodeID";
        public static final String COLUMN_NAME_SHOW_NAME = "showName";
        public static final String COLUMN_NAME_EPISODE_NAME = "episodeName";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_TIME = "time";
    }

    public static abstract class newsTable implements BaseColumns {
        public static final String TABLE_NAME = "newsTable";
        public static final String COLUMN_NAME_ID = "newsID";
        public static final String COLUMN_NAME_TITLE = "newsTitle";
        public static final String COLUMN_NAME_DATE = "newsDate";
        public static final String COLUMN_NAME_CONTENT = "newsContent";
    }
}
