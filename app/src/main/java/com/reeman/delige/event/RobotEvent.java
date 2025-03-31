package com.reeman.delige.event;

import android.content.Intent;
import android.text.TextUtils;

public class RobotEvent {

    private static final OnNetworkEvent onNetworkEvent = new OnNetworkEvent();
    private static final OnTimeEvent onTimeEvent = new OnTimeEvent();
    private static final OnTouchEvent onTouchEvent = new OnTouchEvent();
    private static final OnCallingEvent onCallingEvent = new OnCallingEvent();

    public static OnTimeEvent getOnTimeEvent() {
        return onTimeEvent;
    }


    public static OnNetworkEvent getNetworkEvent(Intent intent) {
        onNetworkEvent.networkIntent = intent;
        return onNetworkEvent;
    }


    public static Object getOnTouchEvent() {
        return onTouchEvent;
    }



    public static OnCallingEvent getOnCallingEvent(String s) {
        onCallingEvent.target = s;
        return onCallingEvent;
    }

    public static class BaseEvent {
        public String rawData;
    }



    public static class OnTimeEvent {
    }



    public static class OnNetworkEvent {
        public Intent networkIntent;
    }


    public static class OnDefaultEvent {
    }

    public static class OnTouchEvent {

    }



    public static class OnCallingEvent {
        public String target;
    }

}
