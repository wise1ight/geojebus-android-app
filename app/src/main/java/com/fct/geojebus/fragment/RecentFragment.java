package com.fct.geojebus.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fct.geojebus.R;
import com.fct.geojebus.RouteDetailActivity;
import com.fct.geojebus.StopDetailActivity;
import com.fct.geojebus.adapter.RecentListAdapter;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.database.RecentConstants;
import com.fct.geojebus.database.RecentDB;
import com.fct.geojebus.util.Common;

public class RecentFragment extends Fragment {
    private BusDB mDbHelper;
    private Common mCommon;
    private RecentDB mRecentDB;
    private ListView mRecentListView;
    private RecentListAdapter mRecentAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, null);
        mDbHelper = BusDB.getInstance(getActivity());
        mCommon = new Common();
        mRecentDB = RecentDB.getInstance(getActivity());
        mRecentListView = (ListView) view.findViewById(R.id.recent_list);

        Cursor cursor = mRecentDB.selectRecent();
        mRecentAdapter = new RecentListAdapter(getActivity(), cursor);

        TextView recentempty = (TextView) view.findViewById(R.id.recent_empty);
        mRecentListView.setEmptyView(recentempty);
        mRecentListView.setAdapter(mRecentAdapter);
        mRecentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) mRecentAdapter.getItem(position);

                try {
                    if (cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_TYPE)).equals("stop")) {
                        Intent intent = new Intent(getActivity(),
                                StopDetailActivity.class);
                        Cursor c = mDbHelper.StopBisQuery(cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_NUM)), cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_COUNTRY)));
                        c.moveToFirst();
                        intent.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_TYPE)).equals("route")) {
                        Intent intent = new Intent(getActivity(),
                                RouteDetailActivity.class);
                        Cursor c = mDbHelper.RouteBisQuery(cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_NUM)), cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_COUNTRY)));
                        c.moveToFirst();
                        intent.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    mCommon.showErrorToast(getActivity(), 2);
                }
            }
        });
        mRecentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mRecentAdapter.getItem(position);
                mRecentDB.deleteRecent(cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_TYPE)), cursor.getInt(cursor.getColumnIndex(RecentConstants.RecentData.COL_COUNTRY)), cursor.getInt(cursor.getColumnIndex(RecentConstants.RecentData.COL_NUM)));
                Toast.makeText(getActivity(), "최근 조회기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                Cursor c = mRecentDB.selectRecent();
                mRecentAdapter.changeCursor(c);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        Cursor cursor = mRecentDB.selectRecent();
        if (mRecentAdapter == null) {
            mRecentAdapter = new RecentListAdapter(getActivity(), cursor);
        } else {
            mRecentAdapter.changeCursor(cursor);
        }

        mRecentListView.setAdapter(mRecentAdapter);
        super.onResume();
    }
}