package com.reeman.delige.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.service.DispatchService;
import com.reeman.delige.light.LightController;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.navigation.ROSController;
import com.reeman.delige.plugins.Bugly;
import com.reeman.delige.receiver.RobotReceiver;
import com.reeman.delige.repository.DbRepository;
import com.reeman.delige.repository.db.AppDataBase;
import com.reeman.delige.request.model.PointInfo;
import com.reeman.delige.service.RobotService;
import com.reeman.delige.utils.PackageUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.WIFIUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BaseApplication extends Application {

    public static List<Activity> activityStack = new ArrayList<>();
    public static Context mApp;
    public static String appVersion;
    public static String macAddress;
    public static DbRepository dbRepository;
    private static LinkedHashSet<String> callingQueue;
    public static Mode navigationMode = Mode.AUTO_ROUTE;
    public static boolean shouldRefreshPoints = false;
    public static boolean isFirstEnter = true;
    public static ROSController ros;
    public static volatile RobotInfo mRobotInfo = new RobotInfo();
    public static volatile DispatchState dispatchState = DispatchState.INIT;
    public static volatile ConcurrentLinkedQueue<PointInfo> pointInfoQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mApp = this;

        appVersion = PackageUtils.getVersion(this);

        macAddress = WIFIUtils.getMacAddress(this);

        try {
            LightController.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //sp存储工具类
        SpManager.init(this, Constants.KEY_DELIGO_SP_NAME);

        //吐司工具类
        ToastUtils.init(this);

        //日志
//        Log.init(this);

        //在线更新， bug上报
        Bugly.initBugly(this);

        dbRepository = DbRepository.getInstance(AppDataBase.getInstance(this));

        registerReceiver(new RobotReceiver(), new RobotReceiver.RobotIntentFilter());

        startService(new Intent(this, RobotService.class));

        startService(new Intent(this, DispatchService.class));

        String mode = SpManager.getInstance().getString(Constants.KEY_NAVIGATION_MODE, Mode.AUTO_ROUTE.name());
        navigationMode = mode.equals(Mode.AUTO_ROUTE.name()) ? Mode.AUTO_ROUTE : Mode.FIX_ROUTE;
    }

    public static LinkedHashSet<String> getCallingQueue() {
        return callingQueue;
    }

    public static void addToCallingQueue(String task) {
        if (callingQueue == null) {
            callingQueue = new LinkedHashSet<>();
        }
        callingQueue.add(task);
    }

}
