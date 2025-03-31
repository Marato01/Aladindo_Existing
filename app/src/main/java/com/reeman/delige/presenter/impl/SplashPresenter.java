package com.reeman.delige.presenter.impl;

import android.app.Activity;

import com.reeman.delige.BuildConfig;
import com.reeman.delige.activities.LanguageSelectActivity;
import com.reeman.delige.activities.MainActivity;
import com.reeman.delige.activities.WiFiConnectActivity;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.SplashContract;
import com.reeman.delige.utils.SpManager;

public class SplashPresenter implements SplashContract.Presenter {

    private final SplashContract.View view;

    public SplashPresenter(SplashContract.View view) {
        this.view = view;
    }

    @Override
    public void startup(Activity context) {
//        if (BuildConfig.APP_FORCE_USE_ZH) {
//            if (SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false)) {
                BaseActivity.startup(context, MainActivity.class);
//            } else {
//                BaseActivity.startup(context, WiFiConnectActivity.class);
//            }
//            context.finish();
//        } else {
//            boolean isLanguageChosen = SpManager.getInstance().getBoolean(Constants.KEY_IS_LANGUAGE_CHOSEN, false);
//            if (isLanguageChosen) {
//                if (SpManager.getInstance().getBoolean(Constants.KEY_IS_NETWORK_GUIDE, false)) {
//                    BaseActivity.startup(context, MainActivity.class);
//                    context.finish();
//                    return;
//                }
//                BaseActivity.startup(context, WiFiConnectActivity.class);
//            } else {
//                //没有选择语言 跳转语言选择界面
//                BaseActivity.startup(context, LanguageSelectActivity.class);
//            }
//            context.finish();
//        }
    }
}
