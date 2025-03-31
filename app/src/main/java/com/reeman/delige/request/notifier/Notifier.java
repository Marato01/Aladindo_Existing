package com.reeman.delige.request.notifier;

import com.google.gson.Gson;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.Msg;
import com.reeman.delige.request.url.API;
import com.reeman.delige.request.service.RobotService;
import com.reeman.delige.utils.AESUtil;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class Notifier {

    public static final String key = "07RMbeite55afNO1";

    public static void notify(Msg msg) {
        try {
            RobotService robotService = ServiceFactory.getRobotService();
            Map<String, String> map = new HashMap<>();
            map.put("device", AESUtil.encrypt(key, new Gson().toJson(msg)));
            map.put("key", key);
            robotService.notify(API.notifyAPI(), map).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(map1 -> {

                    }, throwable -> {
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
