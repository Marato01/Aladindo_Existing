package com.reeman.delige.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kyleduo.switchbutton.SwitchButton;
import com.reeman.delige.R;
import com.reeman.delige.SplashActivity;
import com.reeman.delige.adapter.WifiItemAdapter;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.WiFiConnectContract;
import com.reeman.delige.presenter.impl.WiFiConnectPresenter;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.WifiAuthDialog;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.reeman.delige.base.BaseApplication.activityStack;
import static com.reeman.delige.base.BaseApplication.ros;

public class WiFiConnectActivity extends BaseActivity implements
        WiFiConnectContract.View, SwipeRefreshLayout.OnRefreshListener,
        WifiItemAdapter.OnItemClickListener, View.OnClickListener, WifiAuthDialog.OnViewClickListener {

    private SwitchButton swButton;
    private TextView tvRos;
    private TextView tvAndroid;
    private SwipeRefreshLayout refreshLayout;
    private WifiBroadcastReceiver receiver;
    private WiFiConnectContract.Presenter presenter;
    private TextView tvWiFiStatus;
    private WifiItemAdapter adapter;
    private WifiManager wifiManager;
    private WifiAuthDialog wifiAuthDialog;
    private String ssid, pwd;
    private Map<String, String> wifiMap = null;

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_wifi_connect;
    }

    @Override
    protected void initData() {
        presenter = new WiFiConnectPresenter(this);
        if (activityStack != null && activityStack.size() >= 2) {
            Activity activity = activityStack.get(activityStack.size() - 2);
            if (activity instanceof LanguageSelectActivity) {
                VoiceHelper.play("voice_please_connect_wifi");
            } else if (activity instanceof SplashActivity) {
                VoiceHelper.play("voice_please_connect_wifi");
            }
        }
    }


    @Override
    protected void initCustomView() {
        setOnClickListeners(R.id.tv_back);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        swButton = $(R.id.switch_wifi_status);
        swButton.setChecked(wifiManager.isWifiEnabled());
        swButton.setOnCheckedChangeListener((buttonView, isChecked) -> wifiManager.setWifiEnabled(isChecked));
        tvWiFiStatus = $(R.id.tv_wifi_status);
        tvRos = $(R.id.tv_ros_wifi_name);
        tvAndroid = $(R.id.tv_android_wifi_name);

        //下拉刷新
        refreshLayout = $(R.id.refresh_layout);
        Resources resources = getResources();
        refreshLayout.setColorSchemeColors(
                resources.getColor(R.color.purple_700),
                resources.getColor(R.color.purple_500),
                resources.getColor(R.color.purple_200));
        refreshLayout.setOnRefreshListener(this);

        //WIFI列表
        RecyclerView rvWiFiList = $(R.id.rv_wifi_list);
        rvWiFiList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable_divider, getTheme());
        decor.setDrawable(drawable);
        rvWiFiList.addItemDecoration(decor);
        adapter = new WifiItemAdapter();
        adapter.setOnItemClickListener(this);
        rvWiFiList.setAdapter(adapter);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        ros.getHostIP();
        registerNetworkReceiver();
        String wpStr = SpManager.getInstance().getString(Constants.WIFI_PASSWORD, "");
        if (!"".equals(wpStr)) {
            wifiMap = new Gson().fromJson(wpStr, new TypeToken<Map<String, String>>() {
            }.getType());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostIpEvent(Event.OnIpEvent event) {
        tvRos.setText(getString(R.string.text_current_ros_wifi, TextUtils.isEmpty(event.wifiName) ? getString(R.string.text_not_connected) : event.wifiName));
    }

    private void registerNetworkReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        receiver = new WifiBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onItemClick(ScanResult scanResult) {
        if (wifiAuthDialog == null) {
            wifiAuthDialog = new WifiAuthDialog(this);
            wifiAuthDialog.setOnViewClickListener(this);
        }
        wifiAuthDialog.setName(scanResult.SSID);
        wifiAuthDialog.setHidden(scanResult);
        String passwd;
        TextInputEditText wifiPassword = wifiAuthDialog.getWifiPassword();
        if ((wifiMap != null && (passwd = wifiMap.get(scanResult.SSID)) != null)) {
            wifiPassword.setText(passwd);
            wifiPassword.setSelection(passwd.length());
        } else {
            wifiPassword.setText("");
        }
        wifiPassword.requestFocus();
        wifiAuthDialog.show();
    }

    @Override
    public void onRefresh() {
        presenter.onRefresh(this);
    }

    @Override
    public void showRefreshFailedView() {
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void showStartRefreshView() {
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void showConnectingView(String prompt) {
        EasyDialog.getLoadingInstance(this).loading(prompt);
    }

    @Override
    public void showConnectTimeOutView(String prompt) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        ToastUtils.showShortToast(prompt);
    }


    @Override
    public void onConnectSuccess() {
        if (wifiMap == null) wifiMap = new HashMap<>();
        wifiMap.put(ssid, pwd);
        SpManager.getInstance().edit().putString(Constants.WIFI_PASSWORD, new Gson().toJson(wifiMap)).apply();
        ToastUtils.showShortToast(getString(R.string.voice_wifi_connect_success));
        VoiceHelper.play("voice_wifi_connect_success", () -> {
            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
            boolean isNetworkGuide = SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false);
            if (isNetworkGuide) {
                finish();
            } else {
                SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_NETWORK_GUIDE, true).apply();
                BaseActivity.startupAndClearStack(WiFiConnectActivity.this, MainActivity.class);
            }
        });
    }


    @Override
    public void onConnectFailed() {
        ToastUtils.showShortToast(getString(R.string.text_wifi_connect_failed));
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_back) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWiFiEvent(Event.OnWiFiEvent event) {
        presenter.onWiFiEvent(this, event.isConnect);
    }

    @Override
    public void onViewClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cancel:
                if (wifiAuthDialog != null)
                    wifiAuthDialog.dismiss();
                break;
            case R.id.btn_login:
                ssid = wifiAuthDialog.getWifiName().getText().toString();
                pwd = wifiAuthDialog.getWifiPassword().getText().toString();
                if (TextUtils.isEmpty(ssid)) {
                    ToastUtils.showShortToast(getString(R.string.text_wifi_name_can_not_be_empty));
                    return;
                }
                wifiAuthDialog.dismiss();
                presenter.auth(this, ssid, pwd, wifiAuthDialog.getHidden());
                break;
        }
    }

    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        tvAndroid.setText("");
                        Log.w("network", "DISCONNECTED");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                        presenter.onAndroidConnected(WiFiConnectActivity.this);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        tvAndroid.setText(getString(R.string.text_current_android_wifi, wifiInfo.getSSID().replace("\"", "")));
                        Log.w("network", "CONNECTED");
                    } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                        Log.w("network", "CONNECTING");
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED:
                            ToastUtils.showShortToast(getString(R.string.text_closed));
                            refreshLayout.setRefreshing(false);
                            swButton.setEnabled(true);
                            swButton.setChecked(false);
                            tvWiFiStatus.setText(getString(R.string.text_closed));
                            adapter.setResult(null);
                            break;
                        case WifiManager.WIFI_STATE_DISABLING:
                            swButton.setEnabled(false);
                            tvWiFiStatus.setText(R.string.text_wifi_disabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLING:
                            swButton.setEnabled(false);
                            tvWiFiStatus.setText(R.string.text_wifi_enabling);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            ToastUtils.showShortToast(getString(R.string.text_opened));
                            swButton.setEnabled(true);
                            tvWiFiStatus.setText(getString(R.string.text_opened));
                            presenter.startScanWiFi(wifiManager);
                            break;
                    }
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
                    int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    Log.w("network :", error + "");
                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        Log.w("network", "密码错误");
                        if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                        } else if (state == NetworkInfo.DetailedState.SCANNING) {
                        }
                    }
                    break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    //ToastUtils.showShortToast(getString(R.string.text_already_update));
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    Collections.sort(scanResults, (r1, r2) -> r2.level - r1.level);
                    adapter.setResult(scanResults);
                    refreshLayout.setRefreshing(false);
                    break;
            }
        }
    }

    @Override
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {

    }

    @Override
    public void onSoftKeyboardClosed() {
        ScreenUtils.hideBottomUIMenu(this);
    }
}