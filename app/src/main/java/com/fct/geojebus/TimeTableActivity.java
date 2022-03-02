package com.fct.geojebus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimeTableActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TableLayout tablelayout;
    private ArrayList<String> stopList;

    private String[] departTime;
    private String[] stopGapTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();
        String rt_depart_time = intent.getStringExtra("rt_depart_time");
        String rt_stop_gap = intent.getStringExtra("rt_stop_gap");
        departTime = rt_depart_time.split(",");
        stopGapTime = rt_stop_gap.split(",");
        stopList = intent.getStringArrayListExtra("stop_list");

        ArrayList<String> departTimeList = new ArrayList<String>();

        SimpleDateFormat origformat = new SimpleDateFormat("HH:mm:ss",
                Locale.KOREA);
        SimpleDateFormat newformat = new SimpleDateFormat("HH시 mm분",
                Locale.KOREA);
        for (int i = 0; i < departTime.length; i++) {
            try {
                departTimeList.add(newformat.format(origformat
                        .parse(departTime[i])));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.toolbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        DepartTimeSpinnerAdapter mAdapter = new DepartTimeSpinnerAdapter();
        mAdapter.addItems(departTimeList);

        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
        spinner.setAdapter(mAdapter);
        spinner.setOnItemSelectedListener(this);

        tablelayout = (TableLayout) findViewById(R.id.TimeTableLayout);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        tablelayout.removeAllViews();

        TableRow tr_head = new TableRow(TimeTableActivity.this);
        tr_head.setBackgroundColor(Color.GRAY);
        tr_head.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView label_count = new TextView(TimeTableActivity.this);
        label_count.setText("순번");
        label_count.setTextColor(Color.WHITE);
        label_count.setPadding(5, 5, 5, 5);
        label_count.setGravity(Gravity.CENTER);
        tr_head.addView(label_count);

        TextView label_stop = new TextView(TimeTableActivity.this);
        label_stop.setText("정류장");
        label_stop.setTextColor(Color.WHITE);
        label_stop.setPadding(5, 5, 5, 5);
        label_stop.setGravity(Gravity.CENTER);
        tr_head.addView(label_stop);

        TextView label_dtime = new TextView(TimeTableActivity.this);
        label_dtime.setText("도착 시간");
        label_dtime.setTextColor(Color.WHITE);
        label_dtime.setPadding(5, 5, 5, 5);
        label_dtime.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.span = 6;
        tr_head.addView(label_dtime, params);

        tablelayout.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

        SimpleDateFormat rawformat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        SimpleDateFormat printformat = new SimpleDateFormat("HH시 mm분", Locale.KOREA);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(rawformat.parse(departTime[position]));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < stopList.size(); i++) {
            TableRow tr = new TableRow(TimeTableActivity.this);
            if (i % 2 != 0)
                tr.setBackgroundColor(Color.GRAY);
            else
                tr.setBackgroundColor(Color.WHITE);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView labelCOUNT = new TextView(TimeTableActivity.this);
            labelCOUNT.setText(String.valueOf(i + 1));
            labelCOUNT.setPadding(2, 0, 5, 0);
            if (i % 2 != 0)
                labelCOUNT.setTextColor(Color.WHITE);
            labelCOUNT.setGravity(Gravity.CENTER);
            tr.addView(labelCOUNT);

            TextView labelStopName = new TextView(TimeTableActivity.this);
            labelStopName.setText(stopList.get(i));
            labelStopName.setPadding(2, 0, 5, 0);
            if (i % 2 != 0)
                labelStopName.setTextColor(Color.WHITE);
            labelStopName.setGravity(Gravity.CENTER);
            tr.addView(labelStopName);

            TextView labelDTIME = new TextView(TimeTableActivity.this);
            labelDTIME.setText(printformat.format(cal.getTime()));
            if (i % 2 != 0)
                labelDTIME.setTextColor(Color.WHITE);
            labelDTIME.setGravity(Gravity.CENTER);
            tr.addView(labelDTIME, params);

            tablelayout.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            if (i < stopList.size() - 1) {
                String[] time = stopGapTime[i].split(":");
                cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                cal.add(Calendar.MINUTE, Integer.parseInt(time[1]));
                cal.add(Calendar.SECOND, Integer.parseInt(time[2]));
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class DepartTimeSpinnerAdapter extends BaseAdapter {
        private List<String> mItems = new ArrayList<>();

        public void clear() {
            mItems.clear();
        }

        public void addItem(String obj) {
            mItems.add(obj);
        }

        public void addItems(List<String> list) {
            mItems.addAll(list);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
                convertView = getLayoutInflater().inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
                convertView.setTag("DROPDOWN");
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
                convertView = getLayoutInflater().inflate(R.layout.
                        toolbar_spinner_item, parent, false);
                convertView.setTag("NON_DROPDOWN");
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(getTitle(position));
            return convertView;
        }

        private String getTitle(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position) : "";
        }
    }
}
