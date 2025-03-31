package com.reeman.delige.dispatch;

import com.reeman.delige.dispatch.model.RobotInfo;

public class DispatchManager {
    private static boolean started;

    public static void start() throws Exception {
        DispatchServer.getInstance().start();
        started = true;
    }

    public static void stop() {
        DispatchServer.getInstance().stop();
        started = false;
    }

    public static boolean isStarted() {
        return started;
    }

    public static void publishMessage(RobotInfo robotMsg) {
        DispatchServer instance = DispatchServer.getInstance();
        if (!instance.isReady()) return;
        instance.publishMessage(robotMsg);
    }
}
