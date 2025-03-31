package com.reeman.delige.navigation.filter;

import android.text.TextUtils;

public class ROSFilter {

    private static String coreData;
    private static String hflsVersion;
    private static int chargeState = -1;
    private static int scramState = -1;
    private static int powerLevel = -1;
    private static int antiFallState = -1;
    private static String wheelData;
    private static String missPoseData;

    public static boolean isAntiFallDiff(int data){
        if (antiFallState == -1) {
            antiFallState = data;
            return true;
        }
        if (antiFallState == data) {
            return false;
        }
        antiFallState = data;
        return true;
    }

    public static boolean isPowerLevelDiff(int data){
        if (powerLevel == -1) {
            powerLevel = data;
            return true;
        }
        if (powerLevel == data) {
            return false;
        }
        powerLevel = data;
        return true;
    }

    public static boolean isChargeStateDiff(int data) {
        if (chargeState == -1) {
            chargeState = data;
            return true;
        }
        if (chargeState == data) {
            return false;
        }
        chargeState = data;
        return true;
    }

    public static boolean isScramStateDiff(int data) {
        if (scramState == -1) {
            scramState = data;
            return true;
        }
        if (scramState == data) {
            return false;
        }
        scramState = data;
        return true;
    }

    public static boolean isCoreDataDiff(String data) {
        if (TextUtils.isEmpty(coreData)) {
            coreData = data;
            return true;
        }
        if (coreData.equals(data)) {
            return false;
        }
        coreData = data;
        return true;
    }


    public static boolean isHlfsVersionDiff(String data) {
        if (TextUtils.isEmpty(hflsVersion)) {
            hflsVersion = data;
            return true;
        }
        if (hflsVersion.equals(data)) {
            return false;
        }
        hflsVersion = data;
        return true;
    }

    public static boolean isWheelDataDiff(String data) {
        if (TextUtils.isEmpty(wheelData)) {
            wheelData = data;
            return true;
        }
        if (wheelData.equals(data)) {
            return false;
        }
        wheelData = data;
        return true;
    }

    public static boolean isMissPoseDiff(String data) {
        if (TextUtils.isEmpty(missPoseData)) {
            missPoseData = data;
            return true;
        }
        if (missPoseData.equals(data)) {
            return false;
        }
        missPoseData = data;
        return true;
    }
}
