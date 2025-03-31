package com.reeman.delige.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.reeman.delige.R;
import com.reeman.delige.SplashActivity;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.constants.BuglyConstants;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.utils.LocaleUtil;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.VolumeAdjustDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.reeman.delige.base.BaseApplication.activityStack;
import static com.reeman.delige.base.BaseApplication.ros;

public class LanguageSelectActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button btnConfirm;
    private int originLanguageType;
    private List<String> languages;
    private ArrayAdapter<String> adapter;
    private int currentLanguageType = 0;

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_language_select;
    }

    @Override
    protected void initData() {
        currentLanguageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        if (currentLanguageType == -1) {
            currentLanguageType = LocaleUtil.getLocaleType();
        }
        originLanguageType = currentLanguageType;

        playHello();

        languages = new ArrayList<>();
        Collections.addAll(languages, getResources().getStringArray(R.array.languages));
    }

    @Override
    protected void initCustomView() {
        //语言列表
        ListView languageListView = $(R.id.language_list_view);
        adapter = new ArrayAdapter<String>(this, R.layout.layout_language_item, R.id.tv_language_name, languages) {

            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View root = super.getView(position, convertView, parent);
                ImageView ivLanguageState = root.findViewById(R.id.iv_language_state);
                if (position == currentLanguageType) {
                    ivLanguageState.setImageResource(R.drawable.icon_language_checked);
                } else {
                    ivLanguageState.setImageResource(R.drawable.icon_language_normal);
                }
                return root;
            }
        };
        languageListView.setAdapter(adapter);
        languageListView.setOnItemClickListener(this);
        mHandler.postDelayed(() -> languageListView.smoothScrollToPosition(currentLanguageType), 500);

        //返回按钮
        ImageButton ibBack = $(R.id.ib_back);
        if (activityStack != null && activityStack.size() >= 2 && activityStack.get(activityStack.size() - 2) instanceof SettingActivity) {
            ibBack.setOnClickListener((v) -> {
                if (currentLanguageType != originLanguageType) {
                    LocaleUtil.changeAppLanguage(getResources(), originLanguageType);
                }
                finish();
            });
        }else {
            ibBack.setOnClickListener((v) -> {
                ros.unInit();
                for (Activity activity : activityStack) {
                    activity.finish();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            });
        }

        ImageView ivAdjustVolume = $(R.id.iv_adjust_volume);
        ivAdjustVolume.setOnClickListener(v -> onAdjustVolume());

        //确认按钮
        btnConfirm = $(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        if (activityStack != null && activityStack.size() >= 2 && activityStack.get(activityStack.size() - 2) instanceof SplashActivity) {
            btnConfirm.setText(getString(R.string.text_next_step));
            ivAdjustVolume.setVisibility(View.VISIBLE);
        } else {
            btnConfirm.setText(getString(R.string.text_confirm));
            ivAdjustVolume.setVisibility(View.INVISIBLE);
        }
    }

    private void onAdjustVolume() {
        new VolumeAdjustDialog(this).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentLanguageType = position;
        //更新语言
        LocaleUtil.changeAppLanguage(getResources(), currentLanguageType);
        //更新确认按钮
        if (activityStack != null && activityStack.size() == 1) {
            btnConfirm.setText(getString(R.string.text_next_step));
        } else {
            btnConfirm.setText(getString(R.string.text_confirm));
        }
        playHello();
        adapter.notifyDataSetChanged();
    }

    private void playHello() {
        try {
            String language = LocaleUtil.getAssetsPathByLanguage(currentLanguageType);
            VoiceHelper.playAssetsFile(getAssets().openFd(language + "voice_hello.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        SpManager.getInstance().edit().putInt(Constants.KEY_LANGUAGE_TYPE, currentLanguageType).apply();
        BuglyConstants.updateBuglyStrings(getResources());
        if (activityStack != null && activityStack.size() == 1) {
            SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_LANGUAGE_CHOSEN, true).apply();
            BaseActivity.startup(this, WiFiConnectActivity.class);
        } else if (originLanguageType != currentLanguageType) {
            restartApp();
        } else {
            finish();
        }
    }

    private void restartApp() {
        EasyDialog.getInstance(this).warn(getString(R.string.text_restart_for_configuration_change), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                if (id == R.id.btn_confirm) {
                    dialog.dismiss();
                    SharedPreferences.Editor edit = SpManager.getInstance().edit();
                    edit.remove(Constants.KEY_BIRTHDAY_MODE_CONFIG);
                    edit.remove(Constants.KEY_CRUISE_MODE_CONFIG);
                    edit.remove(Constants.KEY_RECYCLE_MODE_CONFIG);
                    edit.remove(Constants.KEY_DELIVERY_MODE_CONFIG);
                    edit.remove(Constants.KEY_OBSTACLE_CONFIG);
                    edit.remove(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG);
                    edit.apply();
                    deleteDir(new File(getFilesDir().getAbsolutePath() + "/deligo/assets"));
//                    Event.reset();
//                    controller.stopListen();
                    Intent intent = new Intent(LanguageSelectActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }
}