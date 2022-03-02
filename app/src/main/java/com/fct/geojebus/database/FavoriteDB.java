package com.fct.geojebus.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.BaseColumns;

import com.fct.geojebus.database.FavoriteConstants.FavoriteData;

import java.io.File;

public class FavoriteDB {

    private static FavoriteDB sInstance;
    private Context mContext;
    private SQLiteDatabase mDb;

    private FavoriteDB(Context context) {
        mContext = context;
    }

    public synchronized static FavoriteDB getInstance(Context context) {

        sInstance = new FavoriteDB(context);
        if (sInstance.open(context) == false) {
            sInstance = null;
        }

        return sInstance;
    }

    private boolean open(Context context) {

        FavoriteDataHelper dbHelper;
        dbHelper = new FavoriteDataHelper(context);

        try {
            mDb = dbHelper.getWritableDatabase();
            if (mDb == null)
                mDb = dbHelper.getWritableDatabase();

        } catch (Exception e) {

            File dbDir = new File(Environment.getDataDirectory()
                    .getAbsolutePath()
                    + "/data/com.fct.geojebus/databases/Favorite.db");
            dbDir.mkdirs();

            mDb = dbHelper.getWritableDatabase();
        }

        return (mDb == null) ? false : true;
    }

    public Cursor selectFavorite() {
        String slq = "select * from Favorite" + " ORDER BY "
                + FavoriteData.ORDER + " ASC;";

        return mDb.rawQuery(slq, null);
    }

    public boolean deleteFavorite(String type, String value) {
        String slq = "delete from Favorite where " + FavoriteData.COL_TYPE
                + " = '" + type + "' and " + FavoriteData.COL_VALUE + " = '"
                + value + "'";

        try {
            mDb.execSQL(slq);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFavoriteByID(int id) {
        String slq = "delete from Favorite where _id = " + id;

        try {
            mDb.execSQL(slq);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean insertFavorite(String type, String name, String value) {

        String slq = "insert into Favorite(" + FavoriteData.COL_TYPE + ", "
                + FavoriteData.COL_NAME + ", " + FavoriteData.COL_VALUE + ") values('" + type + "', '" + name
                + "', '" + value + "');";

        try {
            mDb.execSQL(slq);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRegisterFavorite(String type, String value) {
        String slq = "select * from Favorite where " + FavoriteData.COL_TYPE
                + " = '" + type + "' and " + FavoriteData.COL_VALUE + " = '"
                + value + "'";

        Cursor cursor = mDb.rawQuery(slq, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;

    }

    public int getMaxOrderNum(String table) {
        int maxOrderNum = 0;

        String slq = "SELECT MAX(" + FavoriteData.ORDER + ")" + " FROM "
                + table + ";";

        Cursor cursor = mDb.rawQuery(slq, null);
        if (cursor.moveToFirst()) {
            maxOrderNum = cursor.getInt(0);
        }
        cursor.close();

        return maxOrderNum;
    }

    public int updateOrderNum(String table, int _id, int orderNum) {
        ContentValues values = new ContentValues();
        values.put(FavoriteData.ORDER, orderNum);
        return mDb.update(table, values, BaseColumns._ID + " = ? ",
                new String[]{String.valueOf(_id)});
    }

    public void updateBySql(String table, String value, String selection) {

        String sql = "UPDATE " + table + " SET " + value;
        if (selection != null)
            sql += " WHERE " + selection;

        mDb.execSQL(sql);
    }

    public void execUndo(ContentValues value) {
        mDb.insert(FavoriteData.TABLE_NAME, null, value);
    }

}
