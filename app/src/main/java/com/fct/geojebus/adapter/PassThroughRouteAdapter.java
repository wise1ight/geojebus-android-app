package com.fct.geojebus.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.RouteDetailActivity;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.model.PassThroughRoute;

import java.util.ArrayList;
import java.util.HashMap;

public class PassThroughRouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW = 2;
    HashMap<String, Bitmap> map = new HashMap<>();
    private Context mContext;
    private ArrayList<PassThroughRoute> mItems;
    private BusDB mDbHelper;

    public PassThroughRouteAdapter(Context context, ArrayList<PassThroughRoute> items) {
        mContext = context;
        mItems = items;
        mDbHelper = BusDB.getInstance(context);
    }

    @Override
    public int getItemCount() {
        return mItems.size() > 0 ? mItems.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.size() == 0) {
            return EMPTY_VIEW;
        }

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).routeLabel.setText(mItems.get(position).getName());
            ((ItemViewHolder) viewHolder).detailContents.setText(mItems.get(position).getExtra());
            ((ItemViewHolder) viewHolder).listIcon.setImageBitmap(makeBitmapWithText(mItems.get(position).getType()));
            final int pos = position;
            ((ItemViewHolder) viewHolder).setClickListener(new ItemViewHolder.ClickListener() {

                @Override
                public void onClick(View v, boolean isLongClick) {
                    if (isLongClick) {
                    } else {
                        Intent intent = new Intent(mContext, RouteDetailActivity.class);
                        intent.putExtra("_id", mItems.get(pos).getId());
                        mContext.startActivity(intent);
                    }
                }

            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_wrapper_list_header, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passthrough_route, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(v);
            return viewHolder;
        }
    }

    public Bitmap makeBitmapWithText(String type) {
        if (map.containsKey(type)) {
            return map.get(type);
        } else {
            Cursor cursor = mDbHelper.selectType(type);
            cursor.moveToFirst();
            String _txt = cursor.getString(cursor.getColumnIndex("ty_name"));
            String color = cursor.getString(cursor.getColumnIndex("ty_color"));
            Bitmap textBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(textBitmap);
            canvas.drawColor(Color.parseColor(color));
            Typeface typeface = Typeface.create("", Typeface.BOLD);
            Paint textPaint = new Paint();
            textPaint.setTextSize(68f); //(int)(14 * scale)
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
            textPaint.setTypeface(typeface);
            Rect bounds = new Rect();
            textPaint.getTextBounds(_txt, 0, _txt.length(), bounds);
            int x = (textBitmap.getWidth() - bounds.width()) / 2;
            int y = (textBitmap.getHeight() + bounds.height()) / 2;
            canvas.drawText(_txt, x, y, textPaint);
            cursor.close();
            map.put(type, textBitmap);
            return textBitmap;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView routeLabel;
        TextView detailContents;
        ImageView listIcon;

        private ClickListener clickListener;

        public ItemViewHolder(View v) {
            super(v);
            routeLabel = (TextView) v.findViewById(R.id.routeLabel);
            detailContents = (TextView) v.findViewById(R.id.detailContents);
            listIcon = (ImageView) v.findViewById(R.id.list_icon);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, false);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onClick(v, true);
            return false;
        }

        public interface ClickListener {
            void onClick(View v, boolean isLongClick);
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }

}