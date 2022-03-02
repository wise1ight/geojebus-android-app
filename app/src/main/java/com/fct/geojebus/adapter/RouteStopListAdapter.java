package com.fct.geojebus.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.StopDetailActivity;
import com.fct.geojebus.model.RouteBusItem;
import com.fct.geojebus.model.RouteStopItem;

import java.util.List;

public class RouteStopListAdapter extends RecyclerView.Adapter<RouteStopListAdapter.ViewHolder> {
    private Context mContext;
    private List<RouteStopItem> mItem;
    private List<RouteBusItem> mBusItem;

    public RouteStopListAdapter(Context context, List<RouteStopItem> itemList) {
        mContext = context;
        mItem = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_route_stop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        holder.setClickListener(new ViewHolder.ClickListener() {

            @Override
            public void onClick(View v, boolean isLongClick) {
                if (isLongClick) {
                } else {
                    Intent intent = new Intent(mContext, StopDetailActivity.class);
                    intent.putExtra("_id", mItem.get(pos).getStopId());
                    mContext.startActivity(intent);
                }
            }

        });
        holder.title.setText(mItem.get(position).getStopName());
        holder.summary.setText(mItem.get(position).getStopArs());
        if (position == 0) {
            holder.vertical_line.setVisibility(View.GONE);
            holder.vertical_line_top.setVisibility(View.GONE);
            holder.vertical_line_bottom.setVisibility(View.VISIBLE);
            holder.list_stop_icon.setImageResource(R.drawable.blue_marker);
        } else if (position == (mItem.size() - 1)) {
            holder.vertical_line.setVisibility(View.GONE);
            holder.vertical_line_top.setVisibility(View.VISIBLE);
            holder.vertical_line_bottom.setVisibility(View.GONE);
            holder.list_stop_icon.setImageResource(R.drawable.pink_marker);
        } else {
            holder.vertical_line.setVisibility(View.VISIBLE);
            holder.vertical_line_top.setVisibility(View.GONE);
            holder.vertical_line_bottom.setVisibility(View.GONE);
            holder.list_stop_icon.setImageResource(R.drawable.grey_circle);
        }

        //그리고 버스를 추가하자
        //일단 이 차일드 뷰의 정류장 num값은 mItem.get(position).getStopNum();
        //차량 정보와 대조
        holder.bus_list_layout.removeAllViews();
        if (mBusItem != null) {
            for (int i = 0; i < mBusItem.size(); i++) {
                if (mBusItem.get(i).getStopBis() == mItem.get(position).getStopNum()) {
                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    LinearLayout item_route_bus = (LinearLayout) inflater.inflate(R.layout.item_route_bus, null);
                    ((TextView) item_route_bus.findViewById(R.id.plate)).setText(mBusItem.get(i).getVehicleNum());
                    holder.bus_list_layout.addView(item_route_bus);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public void sendBusData(List<RouteBusItem> busList) {
        mBusItem = busList;
        this.notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView title;
        public TextView summary;
        public View vertical_line;
        public View vertical_line_top;
        public View vertical_line_bottom;
        public ImageView list_stop_icon;
        public LinearLayout bus_list_layout;
        private ClickListener clickListener;

        public ViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            summary = (TextView) view.findViewById(R.id.summary);
            vertical_line = view.findViewById(R.id.vertical_line);
            vertical_line_top = view.findViewById(R.id.vertical_line_top);
            vertical_line_bottom = view.findViewById(R.id.vertical_line_bottom);
            list_stop_icon = (ImageView) view.findViewById(R.id.list_stop_icon);
            bus_list_layout = (LinearLayout) view.findViewById(R.id.bus_list_layout);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
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
}
