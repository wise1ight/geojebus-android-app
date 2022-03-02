/**
 * 안드로이드 거제버스 어플리케이션
 * <p/>
 * StopSearchFragment.java
 * 정류장 검색
 * <p/>
 * Copyright(C) 2015 KUvH. All Rights Reserved.
 */

package com.fct.geojebus.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.fct.geojebus.LargeMapActivity;
import com.fct.geojebus.R;
import com.fct.geojebus.StopDetailActivity;
import com.fct.geojebus.adapter.StopSearchAdapter;
import com.fct.geojebus.database.BusDB;

public class StopSearchFragment extends SearchFragment implements OnItemClickListener {
    private BusDB mDbHelper;
    private StopSearchAdapter mStopAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        getActivity().setTitle(getString(R.string.title_stop_search_fragment));

        mDbHelper = BusDB.getInstance(getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearch.setLogoText(getString(R.string.title_stop_search_fragment));

        Cursor cursor = mDbHelper.StopQuery("");
        mStopAdapter = new StopSearchAdapter(getActivity(), cursor);
        setListAdapter(mStopAdapter);

        setEmptyText(getString(R.string.stop_search_empty_text_1));
        getListView().setDivider(null);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_stop_search, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_srdstop:
                SharedPreferences pref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                if (pref.getBoolean("location_use_agreement", false)) {
                    Intent intent = new Intent(getActivity(), LargeMapActivity.class);
                    intent.putExtra("mode", "near");
                    startActivity(intent);
                } else {
                    //에러
                    Toast.makeText(getActivity(), getString(R.string.stop_search_location_use), Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_search:
                mSearch.revealFromMenuItem(R.id.action_search, getActivity());
                return true;
        }
        return false;
    }

    @Override
    protected void Search(String query) {
        if (query.equals("")) {
            getActivity().setTitle(getString(R.string.title_stop_search_fragment));
            setEmptyText(getString(R.string.stop_search_empty_text_1));
        } else {
            setEmptyText(getString(R.string.stop_search_empty_text_2));
            try {
                Cursor cursor = mDbHelper.StopQuery(query);
                mStopAdapter.changeCursor(cursor);
                mStopAdapter.notifyDataSetChanged();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mStopAdapter.getCursor();
        cursor.moveToPosition(position);

        Intent intent = new Intent(getActivity(), StopDetailActivity.class);
        intent.putExtra("_id", cursor.getInt(cursor.getColumnIndex("_id")));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}