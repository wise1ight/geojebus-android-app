package com.fct.geojebus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fct.geojebus.database.RecentConstants.RecentData;

public class RecentDataHelper extends SQLiteOpenHelper {
    public RecentDataHelper(Context c) {
        super(c, RecentData.DB_NAME, null, RecentData.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String slq = "CREATE TABLE " + RecentData.TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + RecentData.COL_NAME + " text, "
                + RecentData.COL_TYPE + " text, "
                + RecentData.COL_COUNTRY + " int, "
                + RecentData.COL_NUM + " int);";
        db.execSQL(slq);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (RecentData.DB_VERSION > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + RecentData.TABLE_NAME);
            onCreate(db);
        }
    }
}
