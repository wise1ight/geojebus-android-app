package com.fct.geojebus.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.RouteDetailActivity;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.model.StopArrive;

import java.util.ArrayList;

public class StopArriveAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EMPTY_VIEW = 2;
    private Context mContext;
    private ArrayList<StopArrive> mItems;
    private int lastPosition = -1;
    private BusDB mDbHelper;

    public StopArriveAdapter(Context context, ArrayList<StopArrive> items) {
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
            setAnimation(((ItemViewHolder) viewHolder).cardView, position);
            ((ItemViewHolder) viewHolder).route_card_title.setText(mItems.get(position).getRouteName());
            if (mItems.get(position).getRouteBis().equals("null")) {
                ((ItemViewHolder) viewHolder).route_card_header.setBackgroundColor(Color.parseColor("#9e9e9e"));
                ((ItemViewHolder) viewHolder).route_card_summary.setText("상세정보 미제공");
            } else {
                Cursor cursor = mDbHelper.selectBusRouteAdditional(mItems.get(position).getRouteCountry(), mItems.get(position).getRouteBis());
                cursor.moveToFirst();
                Cursor tCursor = mDbHelper.selectType(cursor.getString(cursor.getColumnIndex("rt_type")));
                tCursor.moveToFirst();
                ((ItemViewHolder) viewHolder).route_card_header.setBackgroundColor(Color.parseColor(tCursor.getString(tCursor.getColumnIndex("ty_color"))));
                ((ItemViewHolder) viewHolder).route_card_summary.setText(tCursor.getString(tCursor.getColumnIndex("ty_name")));
                cursor.close();
                tCursor.close();
            }
            String wt;
            if (mItems.get(position).getWaitTime() < 1) {
                wt = "잠시 후";
            } else {
                wt = Integer.toString(mItems.get(position).getWaitTime()) + " 분";
            }
            ((ItemViewHolder) viewHolder).arrive_time_tv.setText(wt);
            if (mItems.get(position).getVehicleNum().equals("null")) {
                ((ItemViewHolder) viewHolder).vehicle_num_layout.setVisibility(View.GONE);
                ((ItemViewHolder) viewHolder).card_touch.setOnClickListener(null);
            } else {
                ((ItemViewHolder) viewHolder).vehicle_num_layout.setVisibility(View.VISIBLE);
                ((ItemViewHolder) viewHolder).vehicle_num_tv.setText("71자 " + mItems.get(position).getVehicleNum().substring(2, 6));
                final int pos = position;
                ((ItemViewHolder) viewHolder).card_touch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext,
                                RouteDetailActivity.class);
                        Cursor c = mDbHelper.RouteBisQuery(mItems.get(pos).getRouteBis(), mItems.get(pos).getRouteCountry());
                        c.moveToFirst();
                        intent.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);
                        c.close();
                    }
                });
            }
            ((ItemViewHolder) viewHolder).current_pos_tv.setText(Integer.toString(mItems.get(position).getPositionNum()) + " 전");
            ((ItemViewHolder) viewHolder).current_spos_tv.setText(mItems.get(position).getPosition());
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card_slide_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == EMPTY_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty, parent, false);
            EmptyViewHolder evh = new EmptyViewHolder(v);
            return evh;
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_arrive_bus_card, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(v);
            return viewHolder;
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card_touch;
        RelativeLayout route_card_header;
        RelativeLayout vehicle_num_layout;
        TextView route_card_title;
        TextView route_card_summary;
        TextView arrive_time_tv;
        TextView vehicle_num_tv;
        TextView current_pos_tv;
        TextView current_spos_tv;
        CardView cardView;

        public ItemViewHolder(View v) {
            super(v);
            card_touch = (LinearLayout) v.findViewById(R.id.card_touch);
            route_card_header = (RelativeLayout) v.findViewById(R.id.route_card_header);
            vehicle_num_layout = (RelativeLayout) v.findViewById(R.id.vehicle_num_layout);
            route_card_title = (TextView) v.findViewById(R.id.route_card_title);
            route_card_summary = (TextView) v.findViewById(R.id.route_card_summary);
            arrive_time_tv = (TextView) v.findViewById(R.id.arrive_time_tv);
            vehicle_num_tv = (TextView) v.findViewById(R.id.vehicle_num_tv);
            current_pos_tv = (TextView) v.findViewById(R.id.current_pos_tv);
            current_spos_tv = (TextView) v.findViewById(R.id.current_spos_tv);
            cardView = (CardView) v.findViewById(R.id.bus_arrive_card);
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);
        }
    }
}