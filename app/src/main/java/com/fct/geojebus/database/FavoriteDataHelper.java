package com.fct.geojebus.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fct.geojebus.database.FavoriteConstants.FavoriteData;

import org.json.JSONObject;

import java.util.HashMap;

public class FavoriteDataHelper extends SQLiteOpenHelper {
    private BusDB mDbHelper;

    public FavoriteDataHelper(Context context) {
        super(context, FavoriteData.DB_NAME, null, FavoriteData.DB_VERSION);
        mDbHelper = BusDB.getInstance(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String slq = "CREATE TABLE " + FavoriteData.TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + FavoriteData.COL_TYPE + " text, "
                + FavoriteData.COL_NAME + " text, "
                + FavoriteData.COL_VALUE + " text, "
                + FavoriteData.ORDER + " INT DEFAULT 0);";
        db.execSQL(slq);
        Log.i("", slq);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (FavoriteData.DB_VERSION > oldVersion) {
            //임시 테이블 생성
            String temp_sql = "CREATE TABLE temp ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + FavoriteData.COL_TYPE + " text, "
                    + FavoriteData.COL_NAME + " text, "
                    + FavoriteData.COL_VALUE + " text, "
                    + FavoriteData.ORDER + " INT DEFAULT 0);";
            db.execSQL(temp_sql);

            //값 이주
            Cursor cursor = db.rawQuery("SELECT * FROM " + FavoriteData.TABLE_NAME, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                try {
                    String type = cursor.getString(cursor.getColumnIndex("item_type"));
                    String name = "null";
                    String id = cursor.getString(cursor.getColumnIndex("item_number"));
                    int order = cursor.getInt(cursor.getColumnIndex("ORDERING"));

                    //값 이주
                    HashMap<String, Object> data = new HashMap<>();
                    if (type.equals("stop")) {
                        Cursor c = mDbHelper.MStopArsQuery(id);
                        c.moveToNext();
                        data.put("num", c.getInt(c.getColumnIndex("st_num")));
                        data.put("country", 1);
                        name = c.getString(c.getColumnIndex("st_name"));
                    } else {
                        data.put("num", id);
                        data.put("country", 1);
                        Cursor c = mDbHelper.RouteBisQuery(id, "1");
                        c.moveToFirst();
                        name = c.getString(c.getColumnIndex("rt_name"));
                    }
                    ContentValues value = new ContentValues();
                    value.put("item_type", type);
                    value.put("item_name", name);
                    value.put("item_value", new JSONObject(data).toString());
                    value.put("item_order", order);
                    db.insert("temp", null, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }

            String drop_sql = "DROP TABLE " + FavoriteData.TABLE_NAME;
            db.execSQL(drop_sql);

            String alter_sql = "ALTER TABLE temp RENAME TO " + FavoriteData.TABLE_NAME;
            db.execSQL(alter_sql);

            cursor.close();
        }
    }
}
