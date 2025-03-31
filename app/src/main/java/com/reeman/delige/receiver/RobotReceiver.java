package com.reeman.delige.receiver;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.reeman.delige.event.RobotEvent;

import org.greenrobot.eventbus.EventBus;

public class RobotReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.w("xuedong", action);
        switch (action) {
            case Intent.ACTION_TIME_TICK:
                EventBus.getDefault().post(RobotEvent.getOnTimeEvent());
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                EventBus.getDefault().post(RobotEvent.getNetworkEvent(intent));
                break;
        }
    }

    public static class RobotIntentFilter extends IntentFilter {
        public RobotIntentFilter(){
            addAction(Intent.ACTION_TIME_TICK);
            addAction("android.net.conn.CONNECTIVITY_CHANGE");
        }
    }

}
