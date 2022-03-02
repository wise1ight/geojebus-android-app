package com.fct.geojebus.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.provider.BaseColumns;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.fct.geojebus.R;
import com.fct.geojebus.database.FavoriteConstants;
import com.fct.geojebus.database.FavoriteDB;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.viewHolder>
        implements DraggableItemAdapter<FavoriteAdapter.viewHolder>,
        SwipeableItemAdapter<FavoriteAdapter.viewHolder> {

    private FavoriteDB mFavoriteDB;
    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;
    private EventListener mEventListener;
    private View.OnClickListener mItemViewOnClickListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;
    private int mPinnedPosition = -1;
    private int mLastRemovedPosition = -1;
    private ContentValues mLastRemovedData;
    public FavoriteAdapter(Context context, Cursor cursor) {
        mFavoriteDB = FavoriteDB.getInstance(context);
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
        mItemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };

        setHasStableIds(true);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(v, true); // true --- pinned
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }

    public Cursor getItem(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            try {
                notifyDataSetChanged();
            } catch (Exception e) {

            }

        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.item_favorite, parent, false);
        return new viewHolder(v);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        mCursor.moveToPosition(position);
        holder.mTextView.setText(mCursor.getString(mCursor.getColumnIndex("item_name")));
        if (mCursor.getString(mCursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_TYPE)).equals("stop")) {
            holder.mIconView.setImageResource(R.drawable.ic_map_marker_black_48dp);
        } else {
            holder.mIconView.setImageResource(R.drawable.ic_dots_vertical_black_48dp);
        }

        holder.mContainer.setBackgroundResource(R.drawable.bg_swipe_item_neutral);
        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(
                isPinnedToSwipeLeft(position) ? Swipeable.OUTSIDE_OF_THE_WINDOW_RIGHT : 0);
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    public void undoLastRemoval() {
        if (mLastRemovedData != null) {

            mFavoriteDB.execUndo(mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;
        }
    }

    @Override
    public void onMoveItem(int from, int to) {
        if (from != to) {
            int targetOrderNum;
            int startTo;

            if (from < to) {
                startTo = to + 1;
                mCursor.moveToPosition(to);
                targetOrderNum = mCursor.getInt(mCursor
                        .getColumnIndex(FavoriteConstants.FavoriteData.ORDER)) + 1;
            } else {
                if (to > 0) {
                    startTo = to;
                    mCursor.moveToPosition(to - 1);
                    targetOrderNum = mCursor.getInt(mCursor
                            .getColumnIndex(FavoriteConstants.FavoriteData.ORDER)) + 1;
                } else {
                    startTo = 0;
                    mCursor.moveToPosition(0);
                    targetOrderNum = mCursor.getInt(mCursor
                            .getColumnIndex(FavoriteConstants.FavoriteData.ORDER));
                }
            }

            mCursor.moveToPosition(from);
            int fromId = mCursor.getInt(mCursor
                    .getColumnIndex(BaseColumns._ID));
            mFavoriteDB.updateOrderNum(FavoriteConstants.FavoriteData.TABLE_NAME, fromId,
                    targetOrderNum);

            if (mCursor.getCount() > startTo) {

                String value = FavoriteConstants.FavoriteData.ORDER + "="
                        + FavoriteConstants.FavoriteData.ORDER + "+1";
                StringBuilder sb = new StringBuilder();
                mCursor.moveToPosition(startTo);
                do {
                    int id = mCursor.getInt(mCursor
                            .getColumnIndex(BaseColumns._ID));
                    if (id != fromId)
                        sb.append("," + id);
                } while (mCursor.moveToNext());

                if (sb.length() > 0) {
                    String selection = BaseColumns._ID + " in ("
                            + sb.substring(1) + ")";
                    mFavoriteDB.updateBySql(FavoriteConstants.FavoriteData.TABLE_NAME, value,
                            selection);
                }
            }
        } else {
            return;
        }

        Cursor cursor = mFavoriteDB.selectFavorite();
        swapCursor(cursor);

        mLastRemovedPosition = -1;

        notifyDataSetChanged();
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    @Override
    public boolean onCheckCanStartDrag(viewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    public boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(viewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public int onGetSwipeReactionType(viewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
        } else {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSetSwipeBackground(viewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(viewHolder holder, int position, int result) {
        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                return new SwipeRightResultAction(this, position);
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                if (isPinnedToSwipeLeft(position)) {
                    return new UnpinResultAction(this, position);
                } else {
                    return new SwipeLeftResultAction(this, position);
                }
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    private boolean isPinnedToSwipeLeft(int position) {
        return position == mPinnedPosition;
    }

    public void setPinnedToSwipeLeft(int position, boolean pin) {
        if (pin)
            mPinnedPosition = position;
        else
            mPinnedPosition = -1;
        notifyDataSetChanged();
    }

    private interface Draggable extends DraggableItemConstants {
    }

    private interface Swipeable extends SwipeableItemConstants {
    }

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemPinned(int position);

        void onItemViewClicked(View v, boolean pinned);
    }

    public static class viewHolder extends AbstractDraggableSwipeableItemViewHolder {
        public FrameLayout mContainer;
        public View mDragHandle;
        public TextView mTextView;
        public ImageView mIconView;

        public viewHolder(View v) {
            super(v);
            mContainer = (FrameLayout) v.findViewById(R.id.container);
            mDragHandle = v.findViewById(R.id.drag_handle);
            mTextView = (TextView) v.findViewById(android.R.id.text1);
            mIconView = (ImageView) v.findViewById(R.id.favorite_icon);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    //삭제
    private class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private final int mPosition;
        private FavoriteAdapter mAdapter;

        SwipeLeftResultAction(FavoriteAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            if (mCursor.moveToPosition(mPosition)) {
                mLastRemovedData = new ContentValues();
                mLastRemovedData.put("item_type", mCursor.getString(mCursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_TYPE)));
                mLastRemovedData.put("item_name", mCursor.getString(mCursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_NAME)));
                mLastRemovedData.put("item_value", mCursor.getString(mCursor.getColumnIndex(FavoriteConstants.FavoriteData.COL_VALUE)));
                mLastRemovedData.put("item_order", mCursor.getInt(mCursor.getColumnIndex(FavoriteConstants.FavoriteData.ORDER)));
                mFavoriteDB.deleteFavoriteByID(mCursor.getInt(mCursor
                        .getColumnIndexOrThrow(BaseColumns._ID)));
            }
            mLastRemovedPosition = mPosition;

            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    //편집
    private class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private final int mPosition;
        private FavoriteAdapter mAdapter;
        private boolean mSetPinned;

        SwipeRightResultAction(FavoriteAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            if (mPosition != mPinnedPosition) {
                mSetPinned = true;
                mPinnedPosition = mPosition;
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mSetPinned && mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemPinned(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private class UnpinResultAction extends SwipeResultActionDefault {
        private final int mPosition;
        private FavoriteAdapter mAdapter;

        UnpinResultAction(FavoriteAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            if (mPosition == mPinnedPosition) {
                mPinnedPosition = -1;
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}
