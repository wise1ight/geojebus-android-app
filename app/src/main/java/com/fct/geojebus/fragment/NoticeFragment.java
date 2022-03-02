package com.fct.geojebus.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fct.geojebus.R;

/**
 * Created by 현욱 on 2015-08-07.
 */
public class NoticeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, null);
        WebView mWebView = (WebView) view.findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.loadUrl("http://www.geojebus.kr/notice?m=1&layout=none");
        return view;
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
}
