package com.fct.geojebus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.model.RouteDetailItem;

import java.util.List;

public class RouteDetailAdapter extends RecyclerView.Adapter<RouteDetailAdapter.ViewHolder> {

    private List<RouteDetailItem> mItem;

    public RouteDetailAdapter(List<RouteDetailItem> itemList) {
        mItem = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_route_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageResource(mItem.get(position).getIcon());
        holder.summary.setText(mItem.get(position).getSummary());
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView icon;
        public TextView summary;

        public ViewHolder(View view) {
            super(view);

            icon = (ImageView) view.findViewById(R.id.icon);
            summary = (TextView) view.findViewById(R.id.label);
        }
    }
}
