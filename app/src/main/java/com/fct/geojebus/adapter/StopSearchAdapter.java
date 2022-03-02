package com.fct.geojebus.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.database.BusDB;

import java.util.HashMap;

/**
 * Created by 현욱 on 2015-07-19.
 */
public class StopSearchAdapter extends CursorAdapter {
    HashMap<String, Bitmap> map = new HashMap<>();
    private BusDB mDbHelper;

    public StopSearchAdapter(Context context, Cursor c) {
        super(context, c);
        mDbHelper = BusDB.getInstance(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_item, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv1 = (TextView) view.findViewById(R.id.label);
        TextView tv2 = (TextView) view.findViewById(R.id.bottomtext);
        ImageView iv1 = (ImageView) view.findViewById(R.id.list_icon);

        tv1.setText(cursor.getString(cursor.getColumnIndex("st_name")));
        tv2.setText(cursor.getString(cursor.getColumnIndex("st_ars")));
        iv1.setImageBitmap(countryLogo(cursor.getString(cursor.getColumnIndex("st_country"))));
    }

    public Bitmap countryLogo(String country) {
        if (map.containsKey(country)) {
            return map.get(country);
        } else {
            Cursor cCursor = mDbHelper.selectCountry(country);
            cCursor.moveToFirst();
            byte[] decodedString = Base64.decode(cCursor.getString(cCursor.getColumnIndex("ct_image")), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            cCursor.close();
            map.put(country, decodedByte);
            return decodedByte;
        }
    }
}
