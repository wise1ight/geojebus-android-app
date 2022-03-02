package com.fct.geojebus;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.GoogleMap;

import net.daum.adam.publisher.AdView;
import net.daum.adam.publisher.impl.AdError;

public class DetailBaseActivity extends AppCompatActivity implements ObservableScrollViewCallbacks, FloatingActionsMenu.OnFloatingActionsMenuUpdateListener, SwipeRefreshLayout.OnRefreshListener {
    protected ObservableRecyclerView mRecyclerView;
    protected GoogleMap mGoogleMap;
    protected SwipeRefreshLayout refreshLayout;
    protected FloatingActionsMenu mFab;
    protected View mHeader;
    protected int mFlexibleSpaceImageHeight;
    protected int mIntersectionHeight;
    protected View mHeaderBar;
    protected View mListBackgroundView;
    protected int mActionBarSize;
    private RelativeLayout mSemiBgLayout;
    private AdView adView = null;
    private View mMap;
    private View mHeaderBackground;
    private int mPrevScrollY;
    private boolean mGapIsChanging;
    private boolean mGapHidden;
    private boolean mReady;
    private Handler mHandler;
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            onRefresh();
        }
    };

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_base);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_height);
        mActionBarSize = getActionBarSize();
        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);

        mSemiBgLayout = (RelativeLayout) findViewById(R.id.semi_background);

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.scroll);
        mRecyclerView.setScrollViewCallbacks(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);

        mFab = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        // 지도 설정
        mMap = findViewById(R.id.map);
        mHeader = findViewById(R.id.header);
        mHeaderBar = findViewById(R.id.header_bar);
        mHeaderBackground = findViewById(R.id.header_background);
        mListBackgroundView = findViewById(R.id.list_background);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        //새로 수정한 부분
        refreshLayout.setColorSchemeResources(R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5);
        refreshLayout.setProgressViewOffset(false, 0, getActionBarSize());
        refreshLayout.setOnRefreshListener(this);

        setTitle(null);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final ObservableRecyclerView scrollable = mRecyclerView;
        ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
            @Override
            public void run() {
                mReady = true;
                updateViews(scrollable.getCurrentScrollY(), false);
            }
        });

        mFab.setOnFloatingActionsMenuUpdateListener(this);
        mSemiBgLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // ignore all touch events
                if (mFab.isExpanded()) {
                    mFab.collapse();
                    return true;
                }
                return false;
            }
        });


        adView = (AdView) findViewById(R.id.adview);

        adView.setOnAdFailedListener(new AdView.OnAdFailedListener() {
            @Override
            public void OnAdFailed(AdError error, String message) {
                adView.setVisibility(View.GONE);
            }
        });

        adView.setOnAdLoadedListener(new AdView.OnAdLoadedListener() {
            @Override
            public void OnAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        adView.setAnimationType(AdView.AnimationType.FLIP_HORIZONTAL);
        adView.setVisibility(View.VISIBLE);

        mHandler = new Handler();
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateViews(scrollY, true);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    protected void updateViews(int scrollY, boolean animated) {
        if (!mReady) {
            return;
        }

        mMap.setTranslationY(-scrollY / 2);

        mHeader.setTranslationY(getHeaderTranslationY(scrollY));

        final int headerHeight = mHeaderBar.getHeight();
        boolean scrollUp = mPrevScrollY < scrollY;
        if (scrollUp) {
            if (mFlexibleSpaceImageHeight - headerHeight - mActionBarSize <= scrollY) {
                changeHeaderBackgroundHeightAnimated(false, animated);
            }
        } else {
            if (scrollY <= mFlexibleSpaceImageHeight - headerHeight - mActionBarSize) {
                changeHeaderBackgroundHeightAnimated(true, animated);
            }
        }
        mPrevScrollY = scrollY;

        mListBackgroundView.setTranslationY(mHeader.getTranslationY());
    }

    protected float getHeaderTranslationY(int scrollY) {
        final int headerHeight = mHeaderBar.getHeight();
        int headerTranslationY = mActionBarSize - mIntersectionHeight;
        if (0 <= -scrollY + mFlexibleSpaceImageHeight - headerHeight - mActionBarSize + mIntersectionHeight) {
            headerTranslationY = -scrollY + mFlexibleSpaceImageHeight - headerHeight;
        }
        return headerTranslationY;
    }

    private void changeHeaderBackgroundHeightAnimated(boolean shouldShowGap, boolean animated) {
        if (mGapIsChanging) {
            return;
        }
        final int heightOnGapShown = mHeaderBar.getHeight();
        final int heightOnGapHidden = mHeaderBar.getHeight() + mActionBarSize;
        final float from = mHeaderBackground.getLayoutParams().height;
        final float to;
        if (shouldShowGap) {
            if (!mGapHidden) {
                // Already shown
                return;
            }
            to = heightOnGapShown;
        } else {
            if (mGapHidden) {
                // Already hidden
                return;
            }
            to = heightOnGapHidden;
        }
        if (animated) {
            mHeaderBackground.animate().cancel();
            ValueAnimator a = ValueAnimator.ofFloat(from, to);
            a.setDuration(100);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    changeHeaderBackgroundHeight(height, to, heightOnGapHidden);
                }
            });
            a.start();
        } else {
            changeHeaderBackgroundHeight(to, to, heightOnGapHidden);
        }
    }

    private void changeHeaderBackgroundHeight(float height, float to, float heightOnGapHidden) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeaderBackground.getLayoutParams();
        lp.height = (int) height;
        lp.topMargin = (int) (mHeaderBar.getHeight() - height);
        mHeaderBackground.requestLayout();
        mGapIsChanging = (height != to);
        if (!mGapIsChanging) {
            mGapHidden = (height == heightOnGapHidden);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (!pref.getString("bis_frequency", "30").equals("-1")) {
            mHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public void onMenuExpanded() {
        Animation in = AnimationUtils.loadAnimation(DetailBaseActivity.this, android.R.anim.fade_in);
        mSemiBgLayout.startAnimation(in);
        mSemiBgLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMenuCollapsed() {
        Animation out = AnimationUtils.loadAnimation(DetailBaseActivity.this, android.R.anim.fade_out);
        mSemiBgLayout.startAnimation(out);
        mSemiBgLayout.setVisibility(View.GONE);
    }

    public void onBackPressed() {
        if (mFab.isExpanded()) {
            mFab.collapse();
        } else {
            finish();
        }
    }

    @Override
    public void onRefresh() {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (!pref.getString("bis_frequency", "30").equals("-1")) {
            mHandler.removeCallbacks(refreshRunnable);
            mHandler.postDelayed(refreshRunnable, Integer.parseInt(pref.getString("bis_frequency", "30")) * 1000);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }
}
