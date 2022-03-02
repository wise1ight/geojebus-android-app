package com.fct.geojebus.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.fct.geojebus.database.RecentConstants.RecentData;

import java.io.File;

public class RecentDB {

    private static RecentDB sInstance;
    @SuppressWarnings("unused")
    private Context mContext;
    private SQLiteDatabase mDb;

    private RecentDB(Context context) {
        mContext = context;
    }

    public synchronized static RecentDB getInstance(Context context) {

        sInstance = new RecentDB(context);
        if (sInstance.open(context) == false) {
            sInstance = null;
        }

        return sInstance;
    }

    private boolean open(Context context) {

        RecentDataHelper dbHelper;
        dbHelper = new RecentDataHelper(context);

        try {
            mDb = dbHelper.getWritableDatabase();
            if (mDb == null)
                mDb = dbHelper.getWritableDatabase();

        } catch (Exception e) {

            File dbDir = new File(Environment.getDataDirectory()
                    .getAbsolutePath()
                    + "/data/com.fct.geojebus/databases/Recent.db");
            dbDir.mkdirs();

            mDb = dbHelper.getWritableDatabase();
        }

        return (mDb == null) ? false : true;
    }

    public Cursor selectRecent() {
        String slq = "select * from Recent" + " ORDER BY _id DESC LIMIT 0,10;";

        return mDb.rawQuery(slq, null);
    }

    private boolean isRegisterRecent(String type, int country, int num) {
        String slq = "select * from Recent where " + RecentData.COL_TYPE
                + " = '" + type + "' and " + RecentData.COL_COUNTRY + " = '"
                + country + "' and " + RecentData.COL_NUM + " = '"
                + num + "'";

        Cursor cursor = mDb.rawQuery(slq, null);
        int count = cursor.getCount();
        cursor.close();

        return count > 0;

    }

    public boolean insertRecent(String name, String type, int country, int num) {
        if (isRegisterRecent(type, country, num)) {
            deleteRecent(type, country, num);
        }

        String slq = "insert into Recent(" + RecentData.COL_NAME + ", "
                + RecentData.COL_TYPE + ", " + RecentData.COL_COUNTRY + ", " + RecentData.COL_NUM + ") values('" + name + "', '" + type
                + "', '" + country + "', '" + num + "');";

        try {
            mDb.execSQL(slq);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteRecent(String type, int country, int num) {
        String slq = "delete from Recent where " + RecentData.COL_TYPE
                + " = '" + type + "' and " + RecentData.COL_COUNTRY + " = '"
                + country + "' and " + RecentData.COL_NUM + " = '"
                + num + "'";

        try {
            mDb.execSQL(slq);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteAllRecent() {
        String slq = "DELETE FROM Recent";

        try {
            mDb.execSQL(slq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
