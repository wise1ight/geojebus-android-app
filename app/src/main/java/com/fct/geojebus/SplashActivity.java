/**
 * 안드로이드 거제버스 어플리케이션
 * <p/>
 * SplashActivity.java
 * 거제버스 어플리케이션의 시작화면 출력과 사용을 위해 로딩합니다.
 * <p/>
 * Copyright(C) 2013 FCT. All Rights Reserved.
 */

package com.fct.geojebus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.util.Common;
import com.fct.geojebus.util.HttpJSONQuery;
import com.fct.geojebus.util.HttpJSONQueryCallback;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SplashActivity extends Activity {
    private HttpJSONQuery aq;
    private Common mCommon;
    private BusDB mDbHelper;

    private JSONObject mAppQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        aq = new HttpJSONQuery();
        mCommon = new Common();
        mDbHelper = BusDB.getInstance(getApplicationContext());

        //권한 체크
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //이용 약관
            checkToSAgree();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkToSAgree();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // 사용약관 동의 여부
    private void checkToSAgree() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (pref.getBoolean("tos_agreement", false)) {
            // 약관에 동의를 이미 하였으므로 앱 버전을 체크하러 간다.
            checkInternet();
        } else {
            // 처음 실행이므로 약관 확인창을 띄운다
            final CardView tosCard = (CardView) findViewById(R.id.tosCard);
            final Button declineButton = (Button) findViewById(R.id.button_decline);
            final Button acceptButton = (Button) findViewById(R.id.button_accept);
            final CheckBox eulaAcceptCheckBox = (CheckBox) findViewById(R.id.eulaAcceptCheckBox);
            final CheckBox locationAcceptCheckBox = (CheckBox) findViewById(R.id.locationAcceptCheckBox);

            tosCard.setVisibility(View.VISIBLE);
            eulaAcceptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        acceptButton.setEnabled(true);
                    } else {
                        acceptButton.setEnabled(false);
                    }
                }

            });
            declineButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // 앱 종료
                    finish();
                }

            });
            acceptButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 약관 체크했는지 동의
                    Editor prefEditor = PreferenceManager
                            .getDefaultSharedPreferences(
                                    getApplicationContext()).edit();
                    prefEditor.putBoolean("tos_agreement", eulaAcceptCheckBox.isChecked());
                    prefEditor.putBoolean("location_use_agreement", locationAcceptCheckBox.isChecked());
                    prefEditor.commit();
                    tosCard.setVisibility(View.GONE);
                    checkInternet();
                }

            });
        }
    }

    private void checkInternet() {
        aq.ajax(getString(R.string.geojebus_api_url) + "/app/version.php?platform=android&app_version=" + BuildConfig.VERSION_NAME + "&debug=" + BuildConfig.DEBUG, new HttpJSONQueryCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, int status) {

                if (json != null) {
                    mAppQuery = json;
                } else {
                    mCommon.showErrorToast(SplashActivity.this, status);
                }

                checkAppVersion();
            }
        });
    }

    // 앱 버전 체크
    private void checkAppVersion() {
        try {
            if (!mAppQuery.getString("latest_app_version").equals(BuildConfig.VERSION_NAME)) {
                if (mAppQuery.getBoolean("app_update_force")) {
                    Toast.makeText(getApplicationContext(), "앱이 최신버전이 아닙니다.\n구글 플레이 스토어에서 업데이트 해 주시기 바랍니다.", Toast.LENGTH_LONG).show();
                    final String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "앱이 최신버전이 아닙니다.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.getMessage();
        }

        checkDatabase();
    }

    // 데이터 베이스 체크
    private void checkDatabase() {
        if (mDbHelper.isDbFileExist()) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            if (pref.getBoolean("auto_database_update", true)) {
                try {
                    String dbVer = mAppQuery.getString("db_version");
                    if (!dbVer.equals(mDbHelper.getBusDbVer())) {
                        new busDBDownload(SplashActivity.this).execute(dbVer);
                        return;
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }

            startApp();
        } else {
            DialogInterface.OnClickListener ok = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        new busDBDownload(SplashActivity.this).execute(mAppQuery.getString("db_version"));
                    } catch (Exception e) {
                        e.getMessage();
                        mCommon.showErrorToast(SplashActivity.this, -101);
                        finish();
                    }
                }
            };

            DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            };

            new AlertDialog.Builder(SplashActivity.this).setTitle("데이터베이스 다운로드")
                    .setMessage("거제버스 앱을 구동하기 위해서는 데이터베이스가 필요합니다.\n다운로드 하시겠습니까?")
                    .setPositiveButton("예", ok).setNegativeButton("아니오", cancel)
                    .show();
        }
    }

    private void startApp() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        Intent params = getIntent();
        Intent intent;
        try {
            if (params.getStringExtra("shortcut_type") != null) {
                if (params.getStringExtra("shortcut_type").equals("stop")) {
                    Cursor cursor = mDbHelper.StopBisQuery(params.getStringExtra("shortcut_num"), params.getStringExtra("shortcut_country"));
                    cursor.moveToFirst();
                    intent = new Intent(SplashActivity.this,
                            StopDetailActivity.class);
                    intent.putExtra("_id", cursor.getInt(cursor.getColumnIndex("_id")));
                    startActivity(intent);
                } else if (params.getStringExtra("shortcut_type").equals("route")) {
                    Cursor cursor = mDbHelper.RouteBisQuery(params.getStringExtra("shortcut_num"), params.getStringExtra("shortcut_country"));
                    cursor.moveToFirst();
                    intent = new Intent(SplashActivity.this,
                            RouteDetailActivity.class);
                    intent.putExtra("_id", cursor.getInt(cursor.getColumnIndex("_id")));
                    startActivity(intent);
                }
            } else if (Intent.ACTION_VIEW.equals(params.getAction())) {
                Uri uri = params.getData();
                String type = uri.getQueryParameter("kakao_type");
                String country = uri.getQueryParameter("kakao_country");
                String num = uri.getQueryParameter("kakao_num");
                if (type.equals("stop")) {
                    Cursor cursor = mDbHelper.StopBisQuery(num, country);
                    cursor.moveToFirst();
                    intent = new Intent(SplashActivity.this,
                            StopDetailActivity.class);
                    intent.putExtra("_id", cursor.getInt(cursor.getColumnIndex("_id")));
                    startActivity(intent);
                } else {
                    Cursor cursor = mDbHelper.RouteBisQuery(num, country);
                    cursor.moveToFirst();
                    intent = new Intent(SplashActivity.this,
                            RouteDetailActivity.class);
                    intent.putExtra("_id", cursor.getInt(cursor.getColumnIndex("_id")));
                    startActivity(intent);
                }
            } else {
                intent = new Intent(SplashActivity.this,
                        BaseActivity.class);
                startActivity(intent);
            }
        } catch (CursorIndexOutOfBoundsException e) {
            mCommon.showErrorToast(SplashActivity.this, 2);
        }
        finish();
    }

    public class busDBDownload extends AsyncTask<String, String, String> {
        Context mContext;
        private ProgressDialog mProgressDialog;

        public busDBDownload(Context context) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("DB를 다운로드 하는 중..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL(getString(R.string.geojebus_api_url) + "/database/android/" + args[0] + ".sqlite");
                URLConnection connection = url.openConnection();
                connection.connect();

                int lenghtOfFile = connection.getContentLength();

                File dir = new File(Environment.getDataDirectory()
                        .getAbsolutePath() + "/data/com.fct.geojebus/databases");
                File target = new File(Environment.getDataDirectory()
                        .getAbsolutePath()
                        + "/data/com.fct.geojebus/databases/bus_data.sqlite");

                if (!dir.exists())
                    dir.mkdir();

                if (target.exists())
                    target.delete();

                FileOutputStream f = new FileOutputStream(target);
                InputStream in = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;

                while ((len1 = in.read(buffer)) > 0) {
                    total += len1; // total = total + len1
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    f.write(buffer, 0, len1);
                }
                f.close();
            } catch (Exception e) {
                Log.d("Downloader", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            mDbHelper = BusDB.getInstance(getApplicationContext());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            checkDatabase();
        }
    }

}
