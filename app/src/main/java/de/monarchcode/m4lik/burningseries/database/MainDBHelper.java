package de.monarchcode.m4lik.burningseries.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The main database helper class.
 * Needed to create/open the main database;
 * @author M4lik, mm.malik.mann@gmail.com
 */
public class MainDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "main.db";


    public MainDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SeriesContract.SQL_CREATE_SERIES_TABLE);
        db.execSQL(SeriesContract.SQL_CREATE_GENRES_TABLE);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void dropTable(SQLiteDatabase db, String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
    }
}
