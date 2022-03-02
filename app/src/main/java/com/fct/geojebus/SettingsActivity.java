package com.fct.geojebus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.database.RecentDB;
import com.fct.geojebus.ui.AppCompatPreferenceActivity;
import com.fct.geojebus.util.HttpJSONQuery;
import com.fct.geojebus.util.HttpJSONQueryCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    private BusDB mDbHelper;
    private RecentDB mRecentDB;
    private HttpJSONQuery aq;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return !isXLargeTablet(context);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.activity_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDbHelper = BusDB.getInstance(getApplicationContext());
        mRecentDB = RecentDB.getInstance(SettingsActivity.this);
        aq = new HttpJSONQuery();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            //return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        // Add 'notifications' preferences, and a corresponding header.
        //PreferenceCategory fakeHeader = new PreferenceCategory(this);
        //fakeHeader.setTitle(R.string.pref_header_notifications);
        //getPreferenceScreen().addPreference(fakeHeader);
        //addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        //fakeHeader = new PreferenceCategory(this);
        //fakeHeader.setTitle(R.string.pref_header_data_sync);
        //getPreferenceScreen().addPreference(fakeHeader);
        //addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("bis_frequency"));
        //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        //bindPreferenceSummaryToValue(findPreference("sync_frequency"));

        final Preference databaseVersion = findPreference("database_version");
        databaseVersion.setSummary(mDbHelper.getBusDbVer());
        final Preference databaseUpdate = findPreference("database_update");

        aq.ajax(getString(R.string.geojebus_api_url) + "/app/version.php?platform=android&app_version=" + BuildConfig.VERSION_NAME + "&debug=" + BuildConfig.DEBUG, new HttpJSONQueryCallback<JSONObject>() {

            @Override
            public void callback(String url, JSONObject json, int status) {

                if (json != null) {
                    try {
                        String db_version = json.getString("db_version");
                        if (db_version.equals(mDbHelper.getBusDbVer())) {
                            databaseUpdate.setSummary("최신버전 입니다.");
                            databaseUpdate.setEnabled(false);
                        } else {
                            databaseUpdate.setSummary(db_version);
                            databaseUpdate.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.getMessage();
                        databaseUpdate.setSummary("버전 데이터를 가져올 수 없음.");
                        databaseUpdate.setEnabled(false);
                    }

                } else {
                    databaseUpdate.setSummary("버전 데이터를 가져올 수 없음.");
                    databaseUpdate.setEnabled(false);
                }
            }
        });

        databaseUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new busDBDownload(SettingsActivity.this).execute(preference.getSummary().toString());
                return true;
            }
        });

        final Preference recentRemove = findPreference("recent_remove");
        recentRemove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mRecentDB.deleteAllRecent();
                Toast.makeText(SettingsActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
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
            Intent mStartActivity = new Intent(mContext, SplashActivity.class);
            PendingIntent mPendingIntent = PendingIntent.getActivity(mContext,
                    0, mStartActivity,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) mContext
                    .getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                    mPendingIntent);
            System.exit(0);
        }
    }
}
