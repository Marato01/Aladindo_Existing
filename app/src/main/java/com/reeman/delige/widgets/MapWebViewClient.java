package com.reeman.delige.widgets;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MapWebViewClient extends WebViewClient {

    public MapWebViewClient(OnMapWebViewEventListener listener){
        this.onMapWebViewEventListener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (onMapWebViewEventListener != null) {
            onMapWebViewEventListener.onPageStart(view, url, favicon);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (onMapWebViewEventListener != null) {
            onMapWebViewEventListener.onPageFinished(view, url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (onMapWebViewEventListener != null) {
            onMapWebViewEventListener.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    private OnMapWebViewEventListener onMapWebViewEventListener;


    public interface OnMapWebViewEventListener {
        void onPageStart(WebView view, String url, Bitmap favicon);

        void onPageFinished(WebView view, String url);

        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
    }
}
