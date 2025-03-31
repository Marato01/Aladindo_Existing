package com.reeman.delige.widgets;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class WebViewHolder {

    private static WebViewHolder instance;
    private static WebView mWebView;


    private WebViewHolder(Context context) {
        if (mWebView == null) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mWebView = new WebView(context) {

                @Override
                protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
                    return false;
                }

                @Override
                public void scrollTo(int x, int y) {
                    super.scrollTo(0, 0);
                }
            };
            mWebView.setLayoutParams(params);
            WebSettings mWebSettings = mWebView.getSettings();
            mWebSettings.setSupportZoom(true);
            mWebSettings.setLoadWithOverviewMode(true);
            mWebSettings.setUseWideViewPort(true);
            mWebSettings.setDefaultTextEncodingName("UTF-8");
            mWebSettings.setLoadsImagesAutomatically(true);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setDatabaseEnabled(true);
            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            String appCachePath = context.getCacheDir().getAbsolutePath();
            mWebSettings.setAppCachePath(appCachePath);
            mWebSettings.setSupportMultipleWindows(false);
            mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        }
    }

    public static WebView getView(Context context) {
        if (instance == null) {
            instance = new WebViewHolder(context);
        }
        return mWebView;
    }

    public static void onResume() {
        mWebView.onResume();
    }

    public static void load(String url) {
        mWebView.loadUrl(url);
    }

    public static void onPause() {
        mWebView.onPause();
        mWebView.clearFormData();
        mWebView.clearHistory();
        mWebView.clearCache(true);
    }
}
