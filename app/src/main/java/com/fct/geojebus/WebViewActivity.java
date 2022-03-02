package com.fct.geojebus;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.fct.geojebus.database.BusDB;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {
    private BusDB mDbHelper;
    private String mMode;
    private ObservableWebView mWebView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mWebView = (ObservableWebView) findViewById(R.id.webView);
        mWebView.setScrollViewCallbacks(this);
        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.setWebChromeClient(new WebChromeClientClass());
        mWebView.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        mMode = intent.getStringExtra("mode");
        if (mMode.equals("roadview")) {
            mDbHelper = BusDB.getInstance(this);
            Cursor cursor = mDbHelper.selectBusStop(intent.getIntExtra("id", 0));
            cursor.moveToFirst();

            setTitle("로드뷰");

            mWebView.loadUrl(getString(R.string.geojebus_api_url) + "/businfo/roadview.php?st_country="
                    + cursor.getString(cursor.getColumnIndex("st_country")) + "&st_num="
                    + cursor.getString(cursor.getColumnIndex("st_num")));
            cursor.close();
        } else if (mMode.equals("report")) {
            setTitle("수정요청");
            mWebView.loadUrl("http://www.geojebus.kr/report?act=dispBoardWrite&m=1&layout=none");
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }

    protected int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }

    private boolean toolbarIsShown() {
        return mToolbar.getTranslationY() == 0;
    }

    private boolean toolbarIsHidden() {
        return mToolbar.getTranslationY() == -mToolbar.getHeight();
    }

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-mToolbar.getHeight());
    }

    private void moveToolbar(float toTranslationY) {
        if (mToolbar.getTranslationY() == toTranslationY) {
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(mToolbar.getTranslationY(), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                mToolbar.setTranslationY(translationY);
                mWebView.setTranslationY(translationY);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
                lp.height = (int) -translationY + getScreenHeight() - lp.topMargin;
                mWebView.requestLayout();
            }
        });
        animator.start();
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            view.loadUrl("file:///android_asset/network_error.html");
        }
    }

    private class WebChromeClientClass extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            new AlertDialog.Builder(WebViewActivity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int select) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }
    }
}
