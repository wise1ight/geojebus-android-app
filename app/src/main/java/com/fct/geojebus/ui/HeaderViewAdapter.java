package com.fct.geojebus.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class HeaderViewAdapter extends RecyclerView.Adapter<HeaderViewAdapter.ViewHolder> {
    ArrayList<Header> headers = new ArrayList<>();
    int viewTypeCount;

    public void addHeaderView(int index, View header) {
        Header h = new Header();
        h.view = header;
        h.viewType = viewTypeCount++;
        headers.add(index, h);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return headers.get(position).viewType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (Header h : headers) {
            if (h.viewType == viewType)
                return new ViewHolder(h.view);
        }
        throw new AssertionError("unexpected viewtype");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return headers.size();
    }

    private static class Header {
        View view;
        int viewType;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements GridRecyclerView.SpanningViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public int getSpanSize(int spanCount) {
            return spanCount;
        }
    }
}