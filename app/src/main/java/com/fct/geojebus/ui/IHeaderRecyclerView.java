package com.fct.geojebus.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public interface IHeaderRecyclerView {
    void addHeaderView(int index, View view);

    void addOnScrollListener(RecyclerView.OnScrollListener l);

    int findFirstVisibleItemPosition();
}