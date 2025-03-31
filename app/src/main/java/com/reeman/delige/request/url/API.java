package com.reeman.delige.request.url;

public class API {

    public static String fetchPointAPI(String ip) {
        return "http://" + ip + "/reeman/position";
    }

    public static String fetchPathPointAPI(String ip) {
        return "http://" + ip + "/reeman/path_model";
    }


    public static String fetchRoutesAPI(String ip) {
        return "http://" + ip + "/reeman/navi_routes";
    }

    public static String fetchBirthdayMusicAPI(String ip) {
        return "http://" + ip + "/file_list/birthday";
    }

    public static String fetchCruiseMusicAPI(String ip) {
        return "http://" + ip + "/file_list/cruise";
    }

    public static String fetchDeliveryMealMusicAPI(String ip) {
        return "http://" + ip + "/file_list/delivery";
    }

    public static String downCruiseMusicAPI(String ip) {
        return "http://" + ip + "/file_down/cruise";
    }

    public static String downBirthdayMusicAPI(String ip) {
        return "http://" + ip + "/file_down/birthday";
    }

    public static String downDeliveryMusicAPI(String ip) {
        return "http://" + ip + "/file_down/delivery";
    }

    public static String savePathAPI(String ip) {
        return "http://" + ip + "/cmd/save_routes";
    }

    public static String taskRecordAPI(String hostname) {
        return "http://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/navigation/logs";
    }

    public static String taskListRecordAPI(String hostname) {
        return "http://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/navigation/logs/list";
    }

    public static String hardwareFaultAPI(String hostname) {
        return "http://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/fault/logs";
    }

    public static String heartbeatAPI(String hostname) {
        return "http://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname;
    }

    public static String batteryLogAPI(String hostname) {
        return "http://navi.rmbot.cn/openapispring/deliveryrobots/upload/" + hostname + "/battery/logs";
    }

    public static String notifyAPI() {
        return "http://navi.rmbot.cn/openapispring/notify2/upload";
    }

    public static String tokenAPI(){
        return "https://navi.rmbot.cn/openapispring/tokens";
    }
}