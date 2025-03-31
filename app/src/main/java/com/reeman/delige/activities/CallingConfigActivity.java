package com.reeman.delige.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.aill.androidserialport.ByteUtil;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.utils.ByteUtils;
import com.reeman.delige.utils.FileMapUtils;
import com.reeman.delige.utils.PackageUtils;
import com.reeman.delige.utils.SerialPortParser;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CallingConfigActivity extends BaseActivity {
    private EditText etKeyNumber;
    private SerialPortParser instance;
    private ProgressDialog progressDialog;
    private boolean configMode = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Pattern mPattern = Pattern.compile("AA55");

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_calling_config;
    }

    @Override
    protected void initCustomView() {
        etKeyNumber = findViewById(R.id.et_key_number);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(this::startListen, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListen();
    }

    private void stopListen() {
        if (instance != null) {
            instance.stop();
        }
    }

    private void startListen() {
        File file = new File("/sys/bus/usb/devices/1-1.2/1-1.2:1.0");
        File[] files = file.listFiles();
        if (!file.exists() || files == null || files.length == 0) {
            showNotFoundSerialDevice();
            return;
        }
        File target = null;
        for (File temp : files) {
            if (temp.getName().startsWith("ttyUSB")) {
                target = temp;
                break;
            }
        }
        if (target == null) {
            showNotFoundSerialDevice();
            return;
        }
        try {
            instance = new SerialPortParser(new File("/dev/" + target.getName()), 115200, new SerialPortParser.OnDataResultListener() {
                private final StringBuilder sb = new StringBuilder();

                @Override
                public void onDataResult(byte[] bytes, int len) {
                    sb.append(byteArr2HexString(bytes, len));
                    while (sb.length() != 0) {
                        if (sb.length() < 4) break;
                        Matcher matcher = mPattern.matcher(sb);
                        if (matcher.find()) {
                            try {
                                int start = matcher.start();
                                int startIndex = start + 4;

                                if (startIndex + 2 >= sb.length())
                                    break;

                                String dataSize = sb.substring(startIndex, startIndex + 2);
                                int intSize = ByteUtil.hexStringToInt(dataSize);

                                int dataLastIndex = startIndex + intSize * 2 + 2;

                                if (dataLastIndex + 2 > sb.length())
                                    break;

                                String dataHexSum = sb.substring(startIndex, dataLastIndex);
                                String checkSum = sb.substring(dataLastIndex, dataLastIndex + 2);
                                if (checkSum.equals(ByteUtils.checksum(dataHexSum))) {
                                    String key = sb.substring(dataLastIndex - 2, dataLastIndex);
                                    if (configMode) {
                                        bind(key, etKeyNumber.getText().toString());
                                    } else {
                                        keyPress(key);
                                    }
                                    sb.delete(0, dataLastIndex + 2);
                                } else if (matcher.find()) {
                                    sb.delete(0, matcher.start());
                                } else {
                                    sb.delete(0, sb.length());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                sb.delete(0, sb.length());
                            }
                        } else {
                            sb.delete(0, sb.length());
                        }
                    }
                }
            });
            instance.start();
        } catch (Exception e) {
            showOpenSerialDeviceFailed();
        }
    }

    private void keyPress(String key) {
        try {
            String table = FileMapUtils.get(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(CallingConfigActivity.this) + File.separator + "key-map.txt", key);
            if (!TextUtils.isEmpty(table)) {
                runOnUiThread(() -> ToastUtils.showShortToast(getString(R.string.text_key_pressed, table)));
            } else {
                runOnUiThread(() -> ToastUtils.showShortToast(getString(R.string.text_please_bind_key_first)));
            }
        } catch (Exception e) {
            runOnUiThread(() -> ToastUtils.showShortToast(getString(R.string.text_get_key_failed, e.getMessage())));
        }
    }

    public static String byteArr2HexString(byte[] inBytArr, int len) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < len; ++i) {
            strBuilder.append(String.format("%02X", inBytArr[i]).toUpperCase());
        }
        return strBuilder.toString();
    }

    private void showNotFoundSerialDevice() {
        EasyDialog.getInstance(this).warn(getString(R.string.text_not_found_serial_device), (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
    }

    private void showOpenSerialDeviceFailed() {
        EasyDialog.getInstance(this).warn(getString(R.string.text_open_serial_device_failed), (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
    }

    public void startBind(View view) {
        String s = etKeyNumber.getText().toString();
        if (TextUtils.isEmpty(s)) {
            EasyDialog.getInstance(this).warnError(getString(R.string.text_please_input_key_number));
            return;
        }
        enterConfigMode();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.text_waiting_for_key_pressed));
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_cancel), (dialog, which) -> {
            dialog.dismiss();
            exitConfigMode();
        });
        progressDialog.show();
    }

    private void exitConfigMode() {
        Log.w("xuedong", "退出配置模式");
        configMode = false;
    }

    private void enterConfigMode() {
        Log.w("xuedong", "进入配置模式");
        configMode = true;
    }

    private void bind(String key, String num) {
        try {
            String name = Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(CallingConfigActivity.this) + File.separator + "key-map.txt";
            if (TextUtils.isEmpty(FileMapUtils.get(name, key))) {
                FileMapUtils.put(name, key, num);
            } else {
                FileMapUtils.replace(name, key, num);
            }
            exitConfigMode();
            runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                ToastUtils.showShortToast(getString(R.string.text_bind_success));
            });
        } catch (Exception e) {
            exitConfigMode();
            runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                ToastUtils.showShortToast(getString(R.string.text_bind_failed, e.getMessage()));
            });
        }
    }

    public void deleteAll(View view) {
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    FileMapUtils.clear(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName(CallingConfigActivity.this) + File.separator + "key-map.txt");
                    emitter.onNext(true);
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean o) throws Throwable {
                        ToastUtils.showShortToast(getString(R.string.text_delete_success));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        ToastUtils.showShortToast(getString(R.string.text_delete_failed));
                    }
                });
    }

    public void exit(View view) {
        finish();
    }
}