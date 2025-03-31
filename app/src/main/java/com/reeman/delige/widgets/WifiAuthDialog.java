package com.reeman.delige.widgets;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.reeman.delige.R;

public class WifiAuthDialog extends BaseDialog implements View.OnClickListener {

    private TextInputEditText wifiName;
    private TextInputEditText wifiPassword;
    private Button cancelBtn;
    private Button loginBtn;

    public void setName(String name) {
        wifiName.setText(name);
        wifiName.setSelection(name.length());
    }

    public void setPassword(String password) {
        wifiPassword.setText(password);
        wifiName.setSelection(wifiPassword.length());
    }

    public TextInputEditText getWifiName() {
        return wifiName;
    }

    public TextInputEditText getWifiPassword() {
        return wifiPassword;
    }

    public Button getLoginBtn() {
        return loginBtn;
    }

    public Button getCancelBtn() {
        return cancelBtn;
    }

    public WifiAuthDialog(@NonNull Context context) {
        super(context);
        init(context);

    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.layout_wifi_auth, null);
        wifiName = root.findViewById(R.id.et_wifi_name);
        wifiPassword = root.findViewById(R.id.et_wifi_password);
        cancelBtn = root.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
        loginBtn = root.findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);

        setTitle(R.string.text_wifi_auth);
        setContentView(root);
    }

    private OnViewClickListener onViewClickListener;

    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onViewClickListener != null) onViewClickListener.onViewClick(v);
    }

    public void setHidden(ScanResult scanResult) {
        this.wifiName.setTag(scanResult);
    }

    public ScanResult getHidden() {
        return (ScanResult) this.wifiName.getTag();
    }

    public interface OnViewClickListener {
        void onViewClick(View v);
    }

}
