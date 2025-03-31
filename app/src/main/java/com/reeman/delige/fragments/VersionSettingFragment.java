package com.reeman.delige.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.room.util.FileUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.reeman.delige.BuildConfig;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseFragment;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.plugins.RetrofitClient;
import com.reeman.delige.utils.FileUtils;
import com.reeman.delige.utils.PackageUtils;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;
import com.tencent.bugly.beta.Beta;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.reeman.delige.base.BaseApplication.ros;

public class VersionSettingFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvNavigationVersion;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_version_setting;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView tvAppVersion = root.findViewById(R.id.tv_app_version);
        tvAppVersion.setText(PackageUtils.getVersion(requireContext()));
        Button btnCheckForUpdate = root.findViewById(R.id.btn_check_update);
        btnCheckForUpdate.setOnClickListener(this);
        tvNavigationVersion = root.findViewById(R.id.tv_navigation_version);
        tvNavigationVersion.setOnClickListener(this);


        Button btnUpload = root.findViewById(R.id.btn_click_to_upload);
        btnUpload.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        ros.getHostVersion();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_check_update) {
            Beta.checkUpgrade();
        } else if (id == R.id.tv_navigation_version) {
            ros.getHostVersion();
        } else if (id == R.id.btn_click_to_upload) {
            uploadLogs();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostVersionObtained(Event.OnVersionEvent event) {
        tvNavigationVersion.setText(event.version);
    }

    protected void uploadLogs() {
        if (TextUtils.isEmpty(Event.getOnHostnameEvent().hostname)) return;
        File root = new File(Environment.getExternalStorageDirectory() + "/" + BuildConfig.APP_LOG_DIR);
        if (!root.exists()) return;
        File[] files = root.listFiles();
        if (files == null || files.length == 0) return;
        EasyDialog.getLoadingInstance(requireContext()).loading(getString(R.string.text_uploading));
        JsonArray jsonArray = new JsonArray();
        MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
        JsonObject singleParam;
        for (File file : files) {
            if (file.exists()) {
                File tempFile = new File(file.getParentFile().getAbsolutePath(), "temp-" + file.getName());
                FileUtils.fileCopy(file, tempFile);
                singleParam = new JsonObject();
                singleParam.addProperty("project", BuildConfig.APP_LOG_DIR);
                singleParam.addProperty("device", Event.getOnHostnameEvent().hostname);
                singleParam.addProperty("log", file.getName());
                singleParam.addProperty("file", file.getName());
                jsonArray.add(singleParam);
                body.addFormDataPart("uploadfiles", file.getName(), RequestBody.create(MediaType.parse("file/file"), tempFile));
            }
        }
        JsonObject params = new JsonObject();
        params.addProperty("files", jsonArray.toString());
        RequestBody requestBody = body.addFormDataPart("information", params.toString()).build();
        Request request = new Request.Builder()
                .url("http://navi.rmbot.cn/logfile/uploadfiles")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = RetrofitClient.getOkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                File[] files1 = root.listFiles();
                for (File file : files1) {
                    if (file.getName().startsWith("temp"))
                        file.delete();
                }
                handler.postDelayed(() -> {
                    if (EasyDialog.isShow()) {
                        EasyDialog.getInstance().dismiss();
                    }
                    ToastUtils.showShortToast(getString(R.string.text_upload_failed, e.getMessage()));
                }, 500);
                Log.w("日志上报：", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                File[] files1 = root.listFiles();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String todayFileName = sdf.format(new Date());
                for (File file : files1) {
                    if (file.getName().startsWith("temp"))
                        file.delete();
                    if (todayFileName.equals(file.getName())) continue;
                    file.delete();
                }
                handler.postDelayed(() -> {
                    if (EasyDialog.isShow()) {
                        EasyDialog.getInstance().dismiss();
                    }
                    ToastUtils.showShortToast(getString(R.string.text_upload_success));
                }, 500);
            }
        });
    }

}
