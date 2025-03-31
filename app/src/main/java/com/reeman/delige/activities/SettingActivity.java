package com.reeman.delige.activities;

import static com.reeman.delige.base.BaseApplication.ros;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.delige.BuildConfig;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.fragments.BasicSettingFragment;
import com.reeman.delige.fragments.LanguageSettingFragment;
import com.reeman.delige.fragments.ModeSettingFragment;
import com.reeman.delige.fragments.NetSettingFragment;
import com.reeman.delige.fragments.VersionSettingFragment;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.utils.ClickRestrict;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SettingActivity extends BaseActivity {

    public static final int PAGE_BASIC_SETTING = 0;
    public static final int PAGE_LANGUAGE_SETTING = 1;
    public static final int PAGE_NETWORK_SETTING = 2;
    public static final int PAGE_DELIVERY_MODE_SETTING = 3;
    public static final int PAGE_VERSION_SETTING = 4;

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();
    private int currentPage = -1;
    private List<TextView> list;
    private Fragment fragment;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_setting;
    }


    @Override
    protected boolean shouldResponse2TimeEvent() {
        return true;
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
        ros.getCurrentMap();
    }

    @Override
    protected void initCustomView() {
        setOnClickListeners(
                R.id.tv_back,
                R.id.tv_basic_setting,
                R.id.tv_language_setting,
                R.id.tv_wifi_setting,
                R.id.tv_delivery_mode_setting,
                R.id.tv_version_setting);

        if (BuildConfig.APP_FORCE_USE_ZH) {
            $(R.id.tv_language_setting).setVisibility(View.GONE);
        }

        list = Arrays.asList($(R.id.tv_basic_setting), $(R.id.tv_language_setting), $(R.id.tv_wifi_setting), $(R.id.tv_delivery_mode_setting), $(R.id.tv_version_setting));
        switchPage(PAGE_BASIC_SETTING);
    }

    public void switchPage(int page) {
        if (this.currentPage == page) return;
        if (ClickRestrict.restrictFrequency(500)) return;
        this.currentPage = page;
        for (int i = 0; i < list.size(); i++) {
            TextView textView = list.get(i);
            if (i == page) {
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundResource(R.drawable.bg_setting_banner_active);
            } else {
                textView.setTextColor(Color.parseColor("#777777"));
                textView.setBackgroundResource(R.drawable.bg_setting_banner);
            }
        }

        fragment = fragmentMap.get(page);
        if (fragment == null) {
            switch (page) {
                case PAGE_LANGUAGE_SETTING:
                    fragment = new LanguageSettingFragment();
                    break;
                case PAGE_NETWORK_SETTING:
                    fragment = new NetSettingFragment();
                    break;
                case PAGE_DELIVERY_MODE_SETTING:
                    fragment = new ModeSettingFragment();
                    break;
                case PAGE_VERSION_SETTING:
                    fragment = new VersionSettingFragment();
                    break;
                default:
                    fragment = new BasicSettingFragment();
            }
            fragmentMap.put(page, fragment);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_setting_container, fragment)
                .commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_basic_setting:
                switchPage(PAGE_BASIC_SETTING);
                break;
            case R.id.tv_language_setting:
                switchPage(PAGE_LANGUAGE_SETTING);
                break;
            case R.id.tv_wifi_setting:
                switchPage(PAGE_NETWORK_SETTING);
                break;
            case R.id.tv_delivery_mode_setting:
                switchPage(PAGE_DELIVERY_MODE_SETTING);
                break;
            case R.id.tv_version_setting:
                switchPage(PAGE_VERSION_SETTING);
                break;
            case R.id.tv_back:
                finish();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMap(Event.OnMapEvent event){
        XLog.w("map ::"+event.map);
    }

    @Override
    protected void onCustomTimeStamp(RobotEvent.OnTimeEvent event) {
        if (EasyDialog.isShow()) return;
        super.onCustomTimeStamp(event);
    }

    @Override
    protected void onCustomInitPose(String currentPosition) {
        if (currentPage == PAGE_BASIC_SETTING && this.fragment != null) {
            BasicSettingFragment fragment = (BasicSettingFragment) this.fragment;
            double[] lastRelocateCoordinate = fragment.getLastRelocateCoordinate();
                    
            if (lastRelocateCoordinate == null) {
                return;
            }

            fragment.setLastRelocateCoordinate(null);
            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();

            
            String pointInfo = SpManager.getInstance().getString(Constants.KEY_POINT_INFO, null);

            if (TextUtils.isEmpty(pointInfo) || TextUtils.isEmpty(currentPosition)) {
                ToastUtils.showShortToast(getString(R.string.text_locate_finish));
                return;
            }

            String[] currPos = currentPosition.split(" ");
            List<Point> waypoints = new Gson().fromJson(pointInfo, new TypeToken<List<Point>>() {
            }.getType());
            Point chargePoint = null;
            for (Point waypoint : waypoints) {
                if (ROS.PT.CHARGE.equals(waypoint.type)){
                    chargePoint = waypoint;
                    break;
                }
            }
            if (chargePoint != null
                    && Math.abs(chargePoint.pose.theta - Float.parseFloat(currPos[2])) <1.04f
                    && Math.sqrt(Math.pow(chargePoint.pose.x - Float.parseFloat(currPos[0]), 2) + Math.pow(chargePoint.pose.y - Float.parseFloat(currPos[1]), 2)) < 0.7) {
                ToastUtils.showLongToast(getString(R.string.text_locate_finish));
            } else {
                fragment.onRelocateBtnClick();
                ToastUtils.showLongToast(getString(R.string.text_relocate_failed));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fragmentMap != null) {
            fragmentMap.clear();
        }
    }
}