package com.reeman.delige.activities;


import static com.reeman.delige.base.BaseApplication.ros;

import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.dispatch.config.EspConfigurer;
import com.reeman.delige.dispatch.config.MulticastReceiver;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.ExpandableLayout;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiMachineConfigActivity extends BaseActivity {

    private Button btnSend;
    private Button btnStop;
    private TextView tvReceiveMacAddress;
    private TextView tvCurrentMacAddress;
    private LinearLayout llReceiveData;
    private boolean isInQuickMode = false;
    private Button btnGetMacAddress;
    private Button btnBroadcastMacAddress;
    private Button btnSaveMacAddress;
    private Button btnEnterTransparentTransmission;
    private Button btnExitTransparentTransmission;
    private MulticastReceiver multicastReceiver;
    private EspConfigurer espConfigurer;
    private ScheduledExecutorService executorService;
    private ScheduledExecutorService macAddressExecutorService;
    private final Gson mGson = new Gson();
    private final HashSet<String> set = new HashSet<>();
    private final Map<String, TextView> receivedData = new HashMap<>();
    private final StringBuilder currentReceiveMacAddress = new StringBuilder();
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_multi_machine_config;
    }

    @Override
    protected void initCustomView() {
        Button btnQuickConfig = $(R.id.btn_quick_config);
        Button btnExit = $(R.id.btn_exit);

        ExpandableLayout elMore = $(R.id.el_more);

        btnGetMacAddress = elMore.findViewById(R.id.btn_get_mac_address);
        btnBroadcastMacAddress = elMore.findViewById(R.id.btn_broadcast_mac);
        btnSaveMacAddress = elMore.findViewById(R.id.btn_save_mac_address);
        btnEnterTransparentTransmission = elMore.findViewById(R.id.btn_enter_transparent_transmission);
        btnSend = elMore.findViewById(R.id.btn_send);
        btnStop = elMore.findViewById(R.id.btn_stop);
        btnExitTransparentTransmission = elMore.findViewById(R.id.btn_exit_transparent_transmission);

        tvCurrentMacAddress = $(R.id.tv_current_mac_address);
        tvReceiveMacAddress = $(R.id.tv_receive_mac_address);
        llReceiveData = $(R.id.tv_receive_data);

        btnGetMacAddress.setOnClickListener(this);
        btnBroadcastMacAddress.setOnClickListener(this);
        btnSaveMacAddress.setOnClickListener(this);
        btnEnterTransparentTransmission.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnExitTransparentTransmission.setOnClickListener(this);
        btnQuickConfig.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        ros.positionAutoUploadControl(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            espConfigurer = new EspConfigurer();
            espConfigurer.start();
            multicastReceiver = new MulticastReceiver();
            multicastReceiver.start();
            onExitTransmission();
        } catch (Exception e) {
            showSerialPortOpenFailedPrompt();
            return;
        }
        espConfigurer.getMacAddress();
    }

    @Override
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {
    }

    private void onExitTransmission() {
        btnGetMacAddress.setEnabled(true);
        btnBroadcastMacAddress.setEnabled(true);
        btnSaveMacAddress.setEnabled(true);
        btnEnterTransparentTransmission.setEnabled(true);
        btnSend.setEnabled(false);
        btnStop.setEnabled(false);
        btnExitTransparentTransmission.setEnabled(false);
    }

    private void onEnterTransmission() {
        btnGetMacAddress.setEnabled(false);
        btnBroadcastMacAddress.setEnabled(false);
        btnSaveMacAddress.setEnabled(false);
        btnEnterTransparentTransmission.setEnabled(false);
        btnSend.setEnabled(true);
        btnStop.setEnabled(true);
        btnExitTransparentTransmission.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (espConfigurer != null) {
            espConfigurer.exitTransmission();
            espConfigurer.stop();
        }
        if (multicastReceiver != null) multicastReceiver.stop();
        if (executorService != null) executorService.shutdownNow();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsg(EspConfigurer.MsgEvent event) {
        String msg = event.msg;
        if (event.type == 1) {
            tvCurrentMacAddress.setText(msg);

        } else {
            RobotInfo robotInfo = mGson.fromJson(event.msg, RobotInfo.class);
            if (receivedData.containsKey(robotInfo.getHostname())) {
                TextView textView = receivedData.get(robotInfo.getHostname());
                textView.setText(robotInfo.getHostname() + " " + mSimpleDateFormat.format(new Date()));
            } else {
                TextView newTextView = createNewTextView(robotInfo.getHostname());
                receivedData.put(robotInfo.getHostname(), newTextView);
                llReceiveData.addView(newTextView);
            }
        }
    }

    private TextView createNewTextView(String hostname) {
        TextView textView = new TextView(this);
        textView.setTextSize(24);
        textView.setText(hostname + " " + mSimpleDateFormat.format(new Date()));
        return textView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onObtainMacAddress(MulticastReceiver.MacAddressEvent event) {
        Log.w("xuedong", "收到: " + event.msg);

        String trim = event.msg.trim();
        if (!trim.equals(espConfigurer.getCurrentMacAddress())) {
            set.add(trim);
        }

        currentReceiveMacAddress.delete(0, currentReceiveMacAddress.length());
        for (String s : set) {
            currentReceiveMacAddress.append("received: ").append(s).append("\n");
        }

        tvReceiveMacAddress.setText(currentReceiveMacAddress);

        if (!isInQuickMode) return;

        if (!TextUtils.isEmpty(currentReceiveMacAddress) && EasyDialog.isShow()) {
            EasyDialog.getInstance().updateMessage(currentReceiveMacAddress);
        }
    }

    private void showSerialPortOpenFailedPrompt() {
        EasyDialog.getInstance(this).warn(getString(R.string.text_serial_port_open_failed), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                dialog.dismiss();
                finish();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_mac_address:
                tvCurrentMacAddress.setText("");
                mHandler.postDelayed(() -> espConfigurer.getMacAddress(), 300);
                break;
            case R.id.btn_broadcast_mac:
                String currentMacAddress = espConfigurer.getCurrentMacAddress();
                if (TextUtils.isEmpty(currentMacAddress)) {
                    ToastUtils.showShortToast(getString(R.string.text_empty_current_mac_address));
                    return;
                }
                multicastReceiver.send(currentMacAddress);
                break;
            case R.id.btn_save_mac_address:
                espConfigurer.setMacAddress(set);
                onExitTransmission();
                ToastUtils.showShortToast(getString(R.string.text_pair_complete));
                break;
            case R.id.btn_enter_transparent_transmission:
                espConfigurer.enterTransmission();
                onEnterTransmission();
                break;
            case R.id.btn_send:
                if (executorService == null) {
                    executorService = Executors.newSingleThreadScheduledExecutor();
                    executorService.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            if (espConfigurer == null) return;
                            espConfigurer.sendData("{\"h\": \"" + Event.getOnHostnameEvent().hostname + "\"}");
                        }
                    }, 3000, 1000, TimeUnit.MILLISECONDS);
                    btnSend.setEnabled(false);
                    btnStop.setEnabled(true);
                }
                break;
            case R.id.btn_stop:
                if (executorService != null) {
                    executorService.shutdownNow();
                    executorService = null;
                    btnSend.setEnabled(true);
                    btnStop.setEnabled(false);
                }
                break;
            case R.id.btn_exit_transparent_transmission:
                if (executorService != null) {
                    executorService.shutdownNow();
                    executorService = null;
                }
                espConfigurer.exitTransmission();
                onExitTransmission();
                break;
            case R.id.btn_quick_config:
                quickConfig();
                break;
            case R.id.btn_exit:
                finish();
                break;
        }
    }


    public void quickConfig() {
        if (espConfigurer == null) return;
        String currentMacAddress = espConfigurer.getCurrentMacAddress();
        if (TextUtils.isEmpty(currentMacAddress)) {
            Toast.makeText(this, getText(R.string.text_empty_current_mac_address), Toast.LENGTH_SHORT).show();
            return;
        }

        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();

        if (macAddressExecutorService == null) {
            espConfigurer.exitTransmission();
            macAddressExecutorService = Executors.newSingleThreadScheduledExecutor();
            EasyDialog.getWaitingInstance(this).waiting(getString(R.string.text_save_and_exit), getString(R.string.text_exit_only), getString(R.string.text_receiving_mac_address), new EasyDialog.OnViewClickListener() {
                @Override
                public void onViewClick(Dialog dialog, int id) {
                    dialog.dismiss();
                    macAddressExecutorService.shutdownNow();
                    macAddressExecutorService = null;
                    isInQuickMode = false;

                    if (id == R.id.btn_confirm) {
                        EasyDialog.getLoadingInstance(MultiMachineConfigActivity.this).loading(getString(R.string.text_is_pairing));
                        espConfigurer.setMacAddress(set);
                        set.clear();
                        mHandler.postDelayed(() -> {
                            espConfigurer.enterTransmission();
                            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                            Toast.makeText(MultiMachineConfigActivity.this, getText(R.string.text_pair_complete), Toast.LENGTH_SHORT).show();
                            onEnterTransmission();
                        }, 1500);
                    } else if (id == R.id.btn_cancel) {
                        set.clear();
                        onExitTransmission();
                    }
                }
            });
            macAddressExecutorService.scheduleWithFixedDelay(() -> multicastReceiver.send(currentMacAddress), 300, 1000, TimeUnit.MILLISECONDS);
            isInQuickMode = true;
        }
    }
}