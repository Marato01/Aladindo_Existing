package com.reeman.delige.service;



import static com.reeman.delige.base.BaseApplication.ros;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.reeman.delige.base.BaseApplication;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.StateRecord;
import com.reeman.delige.request.url.API;
import com.reeman.delige.event.Event;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotService extends Service {

    Runnable task = () -> {
        String hostname = Event.getOnHostnameEvent().hostname;
        if (TextUtils.isEmpty(hostname)) return;
        try {
            int chargePlug = 0;
            if (ros.isCharging()){
                chargePlug = 4;
            }
            StateRecord record = new StateRecord(0,
                    ros.getLevel(),
                    chargePlug,
                    ros.getEmergencyStop(),
                    ros.getState().ordinal(),
                    Event.getOnHflsVersionEvent().softVersion,
                    BaseApplication.appVersion,
                    1,
                    "",
                    BaseApplication.macAddress,
                    System.currentTimeMillis(),
                    "v1.1",
                    "");
            Log.w("上传状态：", record.toString());
            ServiceFactory.getRobotService().heartbeat(API.heartbeatAPI(hostname), record).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    @Override
    public void onCreate() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(task, 10, 15, TimeUnit.SECONDS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
