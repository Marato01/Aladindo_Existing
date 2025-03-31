package com.reeman.delige.settings;

import com.reeman.delige.base.BaseSetting;

import java.util.ArrayList;
import java.util.List;

public class RecycleModeSetting extends BaseSetting {

    public float runningSpeed;

    public int pauseTime;

    //播报间隔
    public int broadcastInterval;

    public boolean enableLoopBroadcast;

    //循环播报内容
    public List<Integer> targetLoopBroadcastPrompts;

    //循环播报内容
    public List<String> loopBroadcastPrompts;

    //循环播报内容
    public List<String> loopBroadcastPromptAudioList;

    public boolean enablePlaceRecyclablesPrompt;

    //放置回收物提醒
    public List<Integer> targetPlaceRecyclablePrompt;

    public List<String> placeRecyclablePrompts;

    public List<String> placeRecyclablePromptAudioList;

    public boolean enableRecycleCompletePrompt;

    //回收完毕提示内容
    public List<Integer> targetRecycleCompletePrompts;

    public List<String> recycleCompletePrompts;

    public List<String> recycleCompletePromptAudioList;

    public RecycleModeSetting(float runningSpeed, int pauseTime, int broadcastInterval, boolean enableLoopBroadcast, List<Integer> targetLoopBroadcastPrompts, List<String> loopBroadcastPrompts, List<String> loopBroadcastPromptAudioList, boolean enablePlaceRecyclablesPrompt, List<Integer> targetPlaceRecyclablePrompt, List<String> placeRecyclablePrompts, List<String> placeRecyclablePromptAudioList, boolean enableRecycleCompletePrompt, List<Integer> targetRecycleCompletePrompts, List<String> recycleCompletePrompts, List<String> recycleCompletePromptAudioList) {
        this.runningSpeed = runningSpeed;
        this.pauseTime = pauseTime;
        this.broadcastInterval = broadcastInterval;
        this.enableLoopBroadcast = enableLoopBroadcast;
        this.targetLoopBroadcastPrompts = targetLoopBroadcastPrompts;
        this.loopBroadcastPrompts = loopBroadcastPrompts;
        this.loopBroadcastPromptAudioList = loopBroadcastPromptAudioList;
        this.enablePlaceRecyclablesPrompt = enablePlaceRecyclablesPrompt;
        this.targetPlaceRecyclablePrompt = targetPlaceRecyclablePrompt;
        this.placeRecyclablePrompts = placeRecyclablePrompts;
        this.placeRecyclablePromptAudioList = placeRecyclablePromptAudioList;
        this.enableRecycleCompletePrompt = enableRecycleCompletePrompt;
        this.targetRecycleCompletePrompts = targetRecycleCompletePrompts;
        this.recycleCompletePrompts = recycleCompletePrompts;
        this.recycleCompletePromptAudioList = recycleCompletePromptAudioList;
    }

    public static RecycleModeSetting getDefault() {
        return new RecycleModeSetting(0.4f, 30, 10,
                false, new ArrayList<Integer>(), new ArrayList<>(), new ArrayList<>(),
                true, new ArrayList<Integer>(), new ArrayList<>(), new ArrayList<>(), true,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }


    @Override
    public String toString() {
        return "RecycleModeSetting{" +
                "runningSpeed=" + runningSpeed +
                ", pauseTime=" + pauseTime +
                ", broadcastInterval=" + broadcastInterval +
                ", enableLoopBroadcast=" + enableLoopBroadcast +
                ", targetLoopBroadcastPrompts=" + targetLoopBroadcastPrompts +
                ", loopBroadcastPrompts=" + loopBroadcastPrompts +
                ", loopBroadcastPromptAudioList=" + loopBroadcastPromptAudioList +
                ", enablePlaceRecyclablesPrompt=" + enablePlaceRecyclablesPrompt +
                ", targetPlaceRecyclablePrompt=" + targetPlaceRecyclablePrompt +
                ", placeRecyclablePrompts=" + placeRecyclablePrompts +
                ", placeRecyclablePromptAudioList=" + placeRecyclablePromptAudioList +
                ", enableRecycleCompletePrompt=" + enableRecycleCompletePrompt +
                ", targetRecycleCompletePrompts=" + targetRecycleCompletePrompts +
                ", recycleCompletePrompts=" + recycleCompletePrompts +
                ", recycleCompletePromptAudioList=" + recycleCompletePromptAudioList +
                '}';
    }
}
