/**
 * 안드로이드 거제버스 어플리케이션
 * <p/>
 * FavoriteFragment.java
 * 즐겨찾기 목록 구성
 * <p/>
 * Copyright(C) 2013 FCT. All Rights Reserved.
 */

package com.fct.geojebus.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.RouteDetailActivity;
import com.fct.geojebus.StopDetailActivity;
import com.fct.geojebus.adapter.FavoriteAdapter;
import com.fct.geojebus.database.BusDB;
import com.fct.geojebus.database.FavoriteConstants;
import com.fct.geojebus.database.FavoriteDB;
import com.fct.geojebus.util.Common;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class FavoriteFragment extends Fragment {
    private FavoriteDB mFavoriteDB;
    private BusDB mDbHelper;
    private Common mCommon;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private int id = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFavoriteDB = FavoriteDB.getInstance(getActivity());
        mDbHelper = BusDB.getInstance(getActivity());
        mCommon = new Common();

        getActivity().setTitle("나의버스");

        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));

        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        Cursor cursor = mFavoriteDB.selectFavorite();

        final FavoriteAdapter myItemAdapter = new FavoriteAdapter(getActivity(), cursor);
        myItemAdapter.setEventListener(new FavoriteAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                Cursor cursor = mFavoriteDB.selectFavorite();
                //아 제발 버그좀 고쳐지라고 ㅇ너링ㄴ머리ㅑㅇㅁ너리
                ((FavoriteAdapter) mAdapter).swapCursor(cursor);

                Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.recycler_view),
                        "즐겨찾기가 삭제되었습니다",
                        Snackbar.LENGTH_LONG);

                snackbar.setAction("실행취소", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myItemAdapter.undoLastRemoval();
                        Cursor cursor = mFavoriteDB.selectFavorite();
                        ((FavoriteAdapter) mAdapter).swapCursor(cursor);

                        emptyView();
                    }
                });
                snackbar.show();
                emptyView();
            }

            @Override
            public void onItemPinned(int position) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

                Cursor c = ((FavoriteAdapter) mAdapter).getItem(position);

                dialog.setTitle("즐겨찾기 수정");
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                final TextView titleTv = new TextView(getActivity());
                titleTv.setText("즐겨찾기 이름:");
                layout.addView(titleTv);

                final EditText titleBox = new EditText(getActivity());
                titleBox.setHint("항목 이름");
                titleBox.setText(c.getString(c.getColumnIndex(FavoriteConstants.FavoriteData.COL_NAME)));
                layout.addView(titleBox);

                dialog.setView(layout);

                id = c.getInt(c.getColumnIndex("_id"));

                dialog.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFavoriteDB.updateBySql(FavoriteConstants.FavoriteData.TABLE_NAME,
                                        FavoriteConstants.FavoriteData.COL_NAME + " = '" + titleBox.getEditableText().toString() + "'",
                                        FavoriteConstants.FavoriteData._ID + " = " + id);
                                Cursor cursor = mFavoriteDB.selectFavorite();
                                ((FavoriteAdapter) mAdapter).swapCursor(cursor);
                            }
                        });
                dialog.setNegativeButton("취소", null);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ((FavoriteAdapter) mAdapter).setPinnedToSwipeLeft(-1, false);
                    }
                });
                dialog.show();
                emptyView();
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                onItemViewClick(v, pinned);
            }
        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        //API 23에서는 뭔가 바뀜
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        //animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        if (!supportsViewElevation())
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        mEmptyView = (TextView) view.findViewById(R.id.favorite_empty);

        emptyView();
    }

    public void emptyView() {
        if (mAdapter.getItemCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Cursor cursor = mFavoriteDB.selectFavorite();
        ((FavoriteAdapter) mAdapter).swapCursor(cursor);
        emptyView();
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    private void onItemViewClick(View v, boolean pinned) {
        if (pinned) {
            ((FavoriteAdapter) mAdapter).setPinnedToSwipeLeft(-1, false);
        } else {
            Cursor cursor = ((FavoriteAdapter) mAdapter).getCursor();
            if (cursor.getCount() > 0) {
                HashMap<String, Object> data = new HashMap<>();
                try {
                    JSONObject json = new JSONObject(cursor.getString(cursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_VALUE)));
                    JSONArray names = json.names();
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.getString(i);
                        data.put(key, json.opt(key));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (cursor.getString(cursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_TYPE)).equals("stop")) {
                        Intent intent = new Intent(getActivity(),
                                StopDetailActivity.class);
                        Cursor c = mDbHelper.StopBisQuery(data.get("num").toString(), data.get("country").toString());
                        c.moveToFirst();
                        intent.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (cursor.getString(cursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_TYPE)).equals("route")) {
                        Intent intent = new Intent(getActivity(),
                                RouteDetailActivity.class);
                        Cursor c = mDbHelper.RouteBisQuery(data.get("num").toString(), data.get("country").toString());
                        c.moveToFirst();
                        intent.putExtra("_id", c.getInt(c.getColumnIndex("_id")));
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    mCommon.showErrorToast(getActivity(), 2);
                }
            } else {
                mCommon.showErrorToast(getActivity(), 2);
            }
        }
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

}