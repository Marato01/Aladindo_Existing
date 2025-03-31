package com.reeman.delige.constants;

import com.reeman.delige.BuildConfig;
import com.reeman.delige.request.model.Point;

public class Constants {
    public static final long RETURN_COUNTDOWN = 15000;
    public static final String KEY_IS_OPERATION_GUIDED = "KEY_IS_OPERATION_GUIDED";
    public static final int RESPONSE_TIME = 15;
    public static final String CHARGE_REASON = "CHARGE_REASON";
    public static final int CHARGE_REASON_USER_TRIGGER = 1;
    public static final int CHARGE_REASON_LOW_POWER = 2;
    public static final String TASK_RESULT_PROMPT = "TASK_RESULT_PROMPT";
    public static final String TASK_RESULT_VOICE = "TASK_RESULT_VOICE";
    public static final String KEY_DISPLAY_DURING_DELIVERY = "KEY_DISPLAY_DURING_DELIVERY";
    public static final int DEFAULT_DISPLAY_DURING_DELIVERY = 0;
    public static final String KEY_START_UP_TIMESTAMP = "KEY_START_UP_TIMESTAMP";
    public static final int DEFAULT_TABLE_LAYER = 3;
    public static final String KEY_TABLE_LAYER = "KEY_TABLE_LAYER";
    public static final String KEY_DATA_SYNC_TYPE = "KEY_DATA_SYNC_TYPE";
    public static final int DEFAULT_DELIVERY_ANIMATION = 0;
    public static final String KEY_DELIVERY_ANIMATION = "KEY_DELIVERY_ANIMATION";
    public static final String KEY_SETTING_PASSWORD_CONTROL = "KEY_SETTING_PASSWORD_CONTROL";
    public static final int KEY_DEFAULT_SETTING_PASSWORD_CONTROL = 0;
    public static final String KEY_SETTING_PASSWORD = "666777";
    //返回模式
    public static int TYPE_GOTO_PRODUCT_POINT = 0;
    public static int TYPE_GOTO_CHARGE = 1;
    public static int TYPE_GOTO_RECYCLING_POINT = 2;

    public static final String TASK_TARGET = "TARGET";
    public static final String TASK_MODE = "MODE";
    public static final String KEY_LOW_POWER = "KEY_LOW_POWER";
    public static final String KEY_SCREEN_BRIGHTNESS = "KEY_SCREEN_BRIGHTNESS";
    public static final int DEFAULT_LOW_POWER = 20;
    public static final int DEFAULT_SCREEN_BRIGHTNESS = 100;

    //几种模式sp的名称
    public static final String KEY_DELIVERY_MODE_CONFIG = "KEY_DELIVERY_MODE_CONFIG";
    public static final String KEY_CRUISE_MODE_CONFIG = "KEY_CRUISE_MODE_CONFIG";
    public static final String KEY_RECYCLE_MODE_CONFIG = "KEY_RECYCLE_MODE_CONFIG";
    public static final String KEY_BIRTHDAY_MODE_CONFIG = "KEY_BIRTHDAY_MODE_CONFIG";
    public static final String KEY_MULTI_DELIVERY_MODE_CONFIG = "KEY_MULTI_DELIVERY_MODE_CONFIG";
    public static final String KEY_OBSTACLE_CONFIG = "KEY_OBSTACLE_CONFIG";

    //障碍物
    public static final String KEY_DEFAULT_OBSTACLE_ASSETS_PREFIX = "/deligo/assets/obstacle";

    //巡航模式文件
    public static final String KEY_DEFAULT_CRUISE_MODE_ASSETS_PREFIX = "/deligo/assets/cruise";
    public static final String KEY_CRUISE_MODE_BACKGROUND_MUSIC_PATH = "/deligo/background/cruise";
    public static final String KEY_CRUISE_MODE_LOOP_BROADCAST = "loop_broadcast";

    //生日模式文件
    public static final String KEY_DEFAULT_BIRTHDAY_MODE_ASSETS_PREFIX = "/deligo/assets/birthday";
    public static final String KEY_BIRTHDAY_MODE_PICK_MEAL_PROMPT = "pick_meal_prompt";
    public static final String KEY_BIRTHDAY_MODE_PICK_MEAL_COMPLETE_PROMPT = "pick_meal_complete_prompt";
    public static final String KEY_BIRTHDAY_MODE_BACKGROUND_MUSIC_PATH = "/deligo/background/birthday";

    //配送模式文件
    public static final String KEY_DELIVERY_MODE_BACKGROUND_MUSIC_PATH = "/deligo/background/delivery_meal";
    public static final String KEY_DEFAULT_DELIVERY_MODE_ASSETS_PREFIX = "/deligo/assets/delivery";
    public static final String KEY_DELIVERY_MODE_DELIVERY_ARRIVAL_PROMPT = "delivery_arrival";


    public static String KEY_MULTI_DELIVERY_MODE_BACKGROUND_MUSIC_PATH = "/deligo/background/multi_delivery";
    public static final String KEY_DEFAULT_MULTI_DELIVERY_MODE_ASSETS_PREFIX = "/deligo/assets/multi_delivery";
    public static final String KEY_MULTI_DELIVERY_MODE_DELIVERY_ARRIVAL_PROMPT = "multi_delivery_arrival";

    //回收模式文件
    public static final String KEY_DEFAULT_RECYCLE_MODE_ASSETS_PREFIX = "/deligo/assets/recycle";
    public static final String KEY_RECYCLE_MODE_PLACE_RECYCLABLES_PROMPT = "place_recyclables";
    public static final String KEY_RECYCLE_MODE_RECYCLE_COMPLETE_PROMPT = "recycle_complete";
    public static final String KEY_RECYCLE_MODE_LOOP_BROADCAST = "loop_broadcast";


    //配送模式
    public static int MODE_DELIVERY_FOOD = 1;
    public static int MODE_CRUISE = 2;
    public static int MODE_RECYCLE = 3;
    public static int MODE_BIRTHDAY = 4;
    public static int MODE_RECYCLE_2 = 5;
    public static int MODE_MULTI_DELIVERY = 6;
    public static int MODE_CALLING = 7;

    //默认背景音乐名称
    public static final String DEFAULT_BIRTHDAY_BACKGROUND_MUSIC = "happy_birthday_1.wav";
    public static final String DEFAULT_CRUISE_BACKGROUND_MUSIC = "cruise_music_1.mp3";

    public static final String KEY_DELIGO_SP_NAME = "deligo_sp";
    public static final String KEY_IS_LANGUAGE_CHOSEN = "KEY_IS_LANGUAGE_CHOSEN";
    public static final String KEY_IS_NETWORK_GUIDE = "KEY_IS_NETWORK_GUIDE";
    public static final int DEFAULT_LANGUAGE_TYPE = BuildConfig.APP_FORCE_USE_ZH ? 1 : -1;
    public static final String KEY_LANGUAGE_TYPE = "KEY_LANGUAGE_TYPE";
    public static final String KEY_MEDIA_VOLUME = "KEY_SYS_VOLUME";
    public static final int DEFAULT_MEDIA_VOLUME = 12;
    public static final String KEY_IS_MAP_BUILDING_GUIDE = "KEY_IS_MAP_BUILDING_GUIDE";
    public static final String KEY_CURRENT_DELIVERY_MODE = "KEY_CURRENT_DELIVERY_MODE";
    public static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    public static final String KEY_ROUTE_INFO = "KEY_ROUTE_INFO";
    public static final String KEY_POINT_INFO = "KEY_POINT_INFO";
    public static final String KEY_POINT_COLUMN = "KEY_POINT_COLUMN";
    public static final int DEFAULT_DELIVERY_MODE = 1;
    public static final int DEFAULT_POINT_COLUMN = 4;

    public static final int MAX_AUDIO_FILE_COUNT = 5;

    public static final String WIFI_PASSWORD = "WIFI_PASSWORD";

    //导航模式
    public static final String KEY_NAVIGATION_MODE = "KEY_NAVIGATION_MODE";

    public static final String KEY_PATH_INFO = "KEY_PATH_INFO";

}
