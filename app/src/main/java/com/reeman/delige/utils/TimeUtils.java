package com.reeman.delige.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: TimeUtils.java
 * @Author: XueDong(1123988589 @ qq.com)
 * @Date: 2022/1/9 15:03
 * @Description: 时间处理工具类
 */
public class TimeUtils {

    static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());


    public static String formatHourAndMinute(Date startTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(startTime);
    }

    public static String formatTime(long timestamp) {
        long hours = TimeUnit.MILLISECONDS.toHours(timestamp);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d", seconds);
        }
    }

    public static String formatTimeHourMinSec(long timestamp) {
        long hours = TimeUnit.MILLISECONDS.toHours(timestamp);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static long convertToTimestamp(String timeString) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(timeString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }



    public static String format(Date date) {
        return formatter.format(date);
    }

}
