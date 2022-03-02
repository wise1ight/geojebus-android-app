package com.fct.geojebus.database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.fct.geojebus.SplashActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BusDBDownload extends AsyncTask<String, String, String> {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    Context mContext;
    private ProgressDialog mProgressDialog;

    public BusDBDownload(Context context) {
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
            URL url = new URL("http://api.geojebus.kr/database/android/20150719-000626.sqlite");
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
        Intent mStartActivity = new Intent(mContext, SplashActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(mContext,
                mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                mPendingIntent);
        System.exit(0);
    }
}