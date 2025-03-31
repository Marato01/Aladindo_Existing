package com.reeman.delige.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.reeman.delige.R;
import com.reeman.delige.activities.WiFiConnectActivity;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.base.BaseFragment;
import com.reeman.delige.utils.WIFIUtils;
import com.reeman.delige.widgets.ExpandableLayout;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.reeman.delige.base.BaseApplication.ros;

public class NetSettingFragment extends BaseFragment implements ExpandableLayout.OnExpandListener, View.OnClickListener {

    private TextView tvAndroidWlanName;
    private TextView tvAndroidWlanIp;
    private TextView tvNavigationWlanName;
    private TextView tvNavigationWlanIp;
    private ExpandableLayout expandableLayout;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_net_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expandableLayout = root.findViewById(R.id.el_wlan_status);
        expandableLayout.setOnExpandListener(this);
        tvAndroidWlanName = root.findViewById(R.id.tv_android_wlan_name);
        tvAndroidWlanIp = root.findViewById(R.id.tv_android_wlan_ip);
        tvNavigationWlanName = root.findViewById(R.id.tv_navigation_wlan_name);
        tvNavigationWlanIp = root.findViewById(R.id.tv_navigation_wlan_ip);
        Button btnSwitchNetwork = root.findViewById(R.id.btn_switch_network);
        btnSwitchNetwork.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNetworkState();
        expandableLayout.show();
    }

    private void refreshNetworkState() {
        String connectWifiSSID = WIFIUtils.getConnectWifiSSID(requireContext());
        tvAndroidWlanName.setText(connectWifiSSID);
        tvAndroidWlanIp.setText(WIFIUtils.getIpAddress(requireContext()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostIpLoaded(Event.OnIpEvent event) {
        tvNavigationWlanIp.setText(event.ipAddress);
        tvNavigationWlanName.setText(TextUtils.isEmpty(event.wifiName) ? getString(R.string.text_not_connected) : event.wifiName);
    }

    @Override
    public void onExpand(ExpandableLayout expandableLayout, boolean isExpand) {
        ImageButton ibExpandIndicator = expandableLayout.getHeaderLayout().findViewById(R.id.ib_expand_indicator);
        ibExpandIndicator.animate().rotation(isExpand ? 90 : 0).setDuration(200).start();

        if (isExpand) {
            refreshNetworkState();
            ros.getHostIP();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_switch_network) {
            BaseActivity.startup(requireContext(), WiFiConnectActivity.class);
        }
    }
}
