package de.m4lik.monarchcode.burningseries.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Malik on 17.08.2016.
 */
public class MainDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "main.db";


    public MainDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SeriesContract.SQL_STRUCTURE_SERIES_TABLE);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SeriesContract.SQL_DELETE_SERIES_TABLE);
        onCreate(db);

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void dropTable(SQLiteDatabase db, String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
    }
}
