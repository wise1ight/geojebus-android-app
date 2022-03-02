/**
 * 안드로이드 거제버스 어플리케이션
 * <p/>
 * BusDB.java
 * 버스 DB호출과 관련된 정의를 합니다.
 * <p/>
 * Copyright(C) 2013 FCT. All Rights Reserved.
 */

package com.fct.geojebus.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class BusDB {

    private static final String DATABASE_PATH = Environment.getDataDirectory()
            .getAbsolutePath() + "/data/com.fct.geojebus/databases/bus_data.sqlite";

    private static BusDB sInstance;
    private SQLiteDatabase mDb;

    private BusDB(Context context) {
    }

    public synchronized static BusDB getInstance(Context context) {

        sInstance = new BusDB(context);
        sInstance.open(context);

        return sInstance;
    }

    private boolean open(Context context) {
        if (isDbFileExist()) {
            try {
                mDb = SQLiteDatabase.openDatabase(DATABASE_PATH, null,
                        SQLiteDatabase.CREATE_IF_NECESSARY);
                return true;
            } catch (Exception e) {
                Toast.makeText(context, "데이터베이스에 문제가 있습니다.", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        return false;
    }

    public boolean isDbFileExist() {
        File dbFile = new File(DATABASE_PATH);
        return dbFile.exists();
    }

    @SuppressLint("DefaultLocale")
    public String getDBChecksum() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(DATABASE_PATH);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5Bytes = digest.digest();
            String returnVal = "";
            for (int i = 0; i < md5Bytes.length; i++) {
                returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16)
                        .substring(1);
            }
            return returnVal.toUpperCase();
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 데이터베이스 삭제
     */
    public boolean remove() {
        File dir = new File(DATABASE_PATH);
        if (dir.exists()) {
            dir.delete();
            return true;
        }
        return false;
    }

    /**
     * 버스 노선 검색
     */
    public Cursor RouteQuery(String query) {
        String slq;
        if (!query.equals("")) {
            slq = "select * from bus_route where rt_name like '%"
                    + query + "%' order by cast (rt_name as number), substr(rt_name,length(trim('-',rt_name))) asc, rt_name_extra asc";
        } else {
            slq = "SELECT * FROM `bus_route` WHERE `rt_name` IS NULL";
        }

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 정류장 검색
     */
    public Cursor StopQuery(String query) {
        String slq;
        if (!query.equals("")) {
            slq = "select * from bus_stop where st_name like '%" + query
                    + "%' or st_ars like '" + query
                    + "%' order by st_name COLLATE LOCALIZED ASC";
        } else {
            slq = "SELECT * FROM `bus_stop` WHERE `st_name` IS NULL";
        }

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 노선 BIS 코드로 검색
     */
    public Cursor RouteBisQuery(String bis_code, String country) {
        String slq = "select * from bus_route where rt_country='" + country + "' and rt_num='" + bis_code + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 정류장 BIS 코드로 검색
     */
    public Cursor StopBisQuery(String bis_code, String country) {
        String slq = "";

        slq = "select * from bus_stop where st_country='" + country + "' and st_num='" + bis_code + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 정류장 즐겨찾기 마이그레이션 전용
     */
    public Cursor MStopArsQuery(String ars) {
        String slq = "";

        slq = "select * from bus_stop where st_country='1' and st_ars='" + ars + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 노선 데이터 (노선 고유번호 기준)
     */
    public Cursor selectBusRoute(int _id) {

        String slq = "select * from bus_route where _id='" + _id + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 노선 데이터 (지역, num)
     */
    public Cursor selectBusRouteAdditional(String country, String num) {

        String slq = "select * from bus_route_additional where rt_country='" + country + "' and rt_num='" + num + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스 정류장 데이터 (정류장 번호 기준)
     */
    public Cursor selectBusStop(int _id) {

        String slq = "select * from bus_stop where _id='" + _id + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 지역 데이터
     */
    public Cursor selectCountry(String _id) {

        String slq = "select * from bus_country where _id='" + _id + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 노선 종류 데이터
     */
    public Cursor selectType(String _id) {

        String slq = "select * from bus_type where _id='" + _id + "'";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 경유 노선 구하기
     */
    public Cursor queryViaRouteList(String st_bis_code, String country) {

        String slq = "select * from bus_route where rt_country='" + country + "' and ',' || rt_stop_list || ',' like '%,"
                + st_bis_code + ",%' order by cast (rt_name as number), substr(rt_name,length(trim('-',rt_name))) asc, rt_name_extra asc";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 주변 정류장 구하기
     */
    public Cursor selectLocation(double Lat, double Long) {

        double longP = Long + 0.01F;
        double longM = Long - 0.01F;

        double latP = Lat + 0.01F;
        double latM = Lat - 0.01F;

        String slq = "select * from bus_stop where st_lng <= " + longP
                + " and st_lng >= " + longM + " and st_lat <= "
                + latP + " and st_lat >= " + latM;

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            return cursor;
        } catch (SQLiteException e) {

        }
        return null;
    }

    /**
     * 버스정보 데이터베이스 버전 가져오기
     */
    public String getBusDbVer() {

        String slq = "select * from version";

        if (mDb == null)
            return "";

        try {
            Cursor cursor = mDb.rawQuery(slq, null);
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("version"));
        } catch (SQLiteException e) {
            return "error";
        }
    }

}
