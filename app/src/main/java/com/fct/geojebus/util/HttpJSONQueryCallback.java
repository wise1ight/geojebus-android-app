package com.fct.geojebus.util;

import android.os.Handler;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpJSONQueryCallback<T> implements Runnable {

    private static int NETWORK_POOL = 4;
    private static ExecutorService fetchExe;
    protected JSONObject result;
    private String url;
    private int error = -101;
    private Handler mHandler;

    public static void execute(Runnable runn) {
        if (fetchExe == null) {
            fetchExe = Executors.newFixedThreadPool(NETWORK_POOL);
        }

        fetchExe.execute(runn);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

            conn.setReadTimeout(15000);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = br.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            br.close();

            result = (JSONObject) new JSONTokener(response.toString()).nextValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                callback();
            }
        });
    }

    public HttpJSONQueryCallback url(String url) {
        this.url = url;
        return this;
    }

    void callback() {
        callback(url, result, error);
    }

    public void callback(String url, JSONObject object, int status) {

    }

    public void async() {
        mHandler = new Handler();
        execute(this);
    }

}