package com.reeman.delige.request;

import com.reeman.delige.plugins.RetrofitClient;
import com.reeman.delige.request.service.RobotService;

public class ServiceFactory {
    private static RobotService robotService;

    public static RobotService getRobotService() {
        if (robotService == null) {
            robotService = RetrofitClient.getClient().create(RobotService.class);
        }
        return robotService;
    }
}
