package com.reeman.delige.contract;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.view.IView;

public interface WiFiConnectContract {
    interface Presenter extends IPresenter {

        void startScanWiFi(WifiManager manager);

        void onRefresh(Context context);

        void auth(Context context,String name, String passwd, ScanResult hidden);

        void onWiFiEvent(Context context,boolean isConnect);

        void onAndroidConnected(Context context);

    }

    interface View extends IView {

        void showRefreshFailedView();

        void showStartRefreshView();

        void showConnectingView(String prompt);

        void showConnectTimeOutView(String prompt);

        void onConnectSuccess();

        void onConnectFailed();

    }
}
