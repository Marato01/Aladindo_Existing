package com.reeman.delige.utils;

import android.app.Activity;
import android.view.View;

import static com.reeman.delige.base.BaseApplication.mApp;

/**
 * @ClassName: ScreenUtils.java
 * @Author: XueDong(1123988589 @ qq.com)
 * @Date: 2022/1/9 15:02
 * @Description: 屏幕设置工具类
 */
public class ScreenUtils {

    private static final int SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED = 0x00004000;


    /**
     * 沉浸式，上划可以显示导航栏，过一段时间自动消失
     *
     * @param activity
     */
    public static void setImmersive(Activity activity) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomUIMenu(Activity activity) {
        // 隐藏虚拟按键，并且全屏
        // for new api versions.
        View decorView = activity.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static float getScreenWidth() {
        return mApp.getResources().getDisplayMetrics().widthPixels;
    }

    public static float getScreenHeight() {
        return mApp.getResources().getDisplayMetrics().heightPixels;
    }

}
