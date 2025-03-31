package com.reeman.delige.plugins;

import android.content.Context;

import com.reeman.delige.BuildConfig;
import com.tencent.bugly.beta.Beta;

public class Bugly {
    public static void initBugly(Context context) {
        Beta.autoCheckUpgrade = false;
        com.tencent.bugly.Bugly.init(context, BuildConfig.APP_BUGLY_ID, false);
    }
}
