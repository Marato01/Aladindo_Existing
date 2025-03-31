package com.reeman.delige.settings;

import com.reeman.delige.base.BaseSetting;

import java.util.ArrayList;
import java.util.List;

public class DeliveryMealSetting extends BaseSetting {

    public float runningSpeed;

    public int pauseTime;

    public boolean enableBackgroundMusic;

    public String backgroundMusicFileName;

    public String backgroundMusicPath;

    //回收完毕提示内容
    public boolean enableDeliveryArrivalPrompt;

    public List<Integer> targetPromptForDeliveryArrival;

    public List<String> deliveryArrivalPrompts;

    public List<String> deliveryArrivalPromptAudioList;

    public DeliveryMealSetting(float runningSpeed, int pauseTime, boolean enableBackgroundMusic, String backgroundMusicFileName, String backgroundMusicPath, boolean enableDeliveryArrivalPrompt, List<Integer> targetPromptForDeliveryArrival, List<String> deliveryArrivalPrompts, List<String> deliveryArrivalPromptAudioList) {
        this.runningSpeed = runningSpeed;
        this.pauseTime = pauseTime;
        this.enableBackgroundMusic = enableBackgroundMusic;
        this.backgroundMusicFileName = backgroundMusicFileName;
        this.backgroundMusicPath = backgroundMusicPath;
        this.enableDeliveryArrivalPrompt = enableDeliveryArrivalPrompt;
        this.targetPromptForDeliveryArrival = targetPromptForDeliveryArrival;
        this.deliveryArrivalPrompts = deliveryArrivalPrompts;
        this.deliveryArrivalPromptAudioList = deliveryArrivalPromptAudioList;
    }

    public static DeliveryMealSetting getDefault() {
        return new DeliveryMealSetting(0.4f, 30, false, null, null, true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public String toString() {
        return "DeliveryMealSetting{" +
                "runningSpeed=" + runningSpeed +
                ", pauseTime=" + pauseTime +
                ", enableBackgroundMusic=" + enableBackgroundMusic +
                ", backgroundMusicFileName='" + backgroundMusicFileName + '\'' +
                ", backgroundMusicPath='" + backgroundMusicPath + '\'' +
                ", enableDeliveryArrivalPrompt=" + enableDeliveryArrivalPrompt +
                ", targetPromptForDeliveryArrival=" + targetPromptForDeliveryArrival +
                ", deliveryArrivalPrompts=" + deliveryArrivalPrompts +
                ", deliveryArrivalPromptAudioList=" + deliveryArrivalPromptAudioList +
                '}';
    }
}
