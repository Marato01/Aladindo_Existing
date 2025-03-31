package com.reeman.delige.dispatch;

import android.os.Handler;
import android.os.Looper;

import com.reeman.delige.dispatch.communication.EspHelper;
import com.reeman.delige.dispatch.model.RobotInfo;


public class DispatchServer {
    private static DispatchServer sINSTANCE;
    private final EspHelper espHelper;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private DispatchServer() {
        espHelper = new EspHelper();

    }

    public static DispatchServer getInstance() {
        if (sINSTANCE == null) {
            sINSTANCE = new DispatchServer();
        }
        return sINSTANCE;
    }

    public void start() throws Exception {
        espHelper.start();
//        handler.postDelayed(espHelper::exitTransmission, 500);
        handler.postDelayed(espHelper::enterBoardCast,500);
        handler.postDelayed(() -> {
            espHelper.enterTransmission();
            espHelper.setReady(true);
        }, 1000);
    }


    public void stop() {
//        espHelper.exitTransmission();
        espHelper.stop();
        sINSTANCE = null;
    }


    public boolean isReady() {
        return espHelper.getReady();
    }

    public void publishMessage(RobotInfo msg) {
        espHelper.send(msg);
    }


}
