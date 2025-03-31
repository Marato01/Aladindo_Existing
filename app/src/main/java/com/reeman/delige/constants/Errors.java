package com.reeman.delige.constants;

import android.content.Context;

import com.reeman.delige.R;
import com.reeman.delige.event.Event;

import java.util.HashMap;
import java.util.Map;

public class Errors {


    public static String getSensorError(Context context, Event.OnCheckSensorsEvent event) {
        StringBuilder stringBuilder = new StringBuilder();
        if (event.lidarWarn)
            stringBuilder.append(context.getString(R.string.exception_laser_error));
        if (event.imuWarn) stringBuilder.append(context.getString(R.string.exception_imu_error));
        if (event.odomWarn)
            stringBuilder.append(context.getString(R.string.exception_speedometer_error));
//        if (event.cam3DWarn) stringBuilder.append(context.getString(R.string.exception_3d_error));
        if (stringBuilder.length() < 1) return null;
        return stringBuilder.toString();
    }

    public static int getFaultReason(Event.OnCheckSensorsEvent event) {
        if (event.imuWarn) return 4;
        if (event.lidarWarn) return 1;
        if (event.odomWarn) return 2;
        if (event.cam3DWarn) return 7;
        return 0;
    }

    public static String getNavigationStartError(Context context, int code) {
        switch (code) {
            case -1:
                return context.getString(R.string.text_docking);
            case -2:
                return context.getString(R.string.voice_scram_stop_turn_on);
            case -3:
                return context.getString(R.string.text_ac_charging);
            case -4:
                return context.getString(R.string.voice_not_found_target_point);
            case -5:
                return context.getString(R.string.text_agv_dock_failed);
            case -6:
                return context.getString(R.string.voice_location_exception);
            case -7:
            case -8:
                return context.getString(R.string.text_cannot_arrive_this_target);
            case -9:
                return context.getString(R.string.voice_read_file_exception);
        }
        return "";
    }
}
