package com.fct.geojebus.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.database.RecentConstants;

/**
 * Created by 현욱 on 2015-08-03.
 */
public class RecentListAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private Context mContext;

    public RecentListAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        ImageView type = (ImageView) convertView.findViewById(R.id.list_icon);
        TextView title = (TextView) convertView.findViewById(android.R.id.text1);
        title.setText(cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_NAME)));
        if (cursor.getString(cursor.getColumnIndex(RecentConstants.RecentData.COL_TYPE)).equals("stop")) {
            type.setImageResource(R.drawable.ic_map_marker_black_48dp);
        } else {
            type.setImageResource(R.drawable.ic_dots_vertical_black_48dp);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_recent, parent, false);
        return view;
    }

}