package com.reeman.delige.settings;

import com.reeman.delige.base.BaseSetting;
import com.reeman.delige.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class CruiseModeSetting extends BaseSetting {

    public float runningSpeed;

    public int broadcastInterval;

    public boolean enableBackgroundMusic;

    public String backgroundMusicFileName;

    public String backgroundMusicPath;

    public boolean enableLoopBroadcast;

    public List<Integer> targetLoopBroadcastPromptList;

    public List<String> loopBroadcastPromptList;

    public List<String> loopBroadcastPromptAudioList;

    public CruiseModeSetting(float runningSpeed, int broadcastInterval, boolean enableBackgroundMusic, String backgroundMusicFileName, String backgroundMusicPath, boolean enableLoopBroadcast, List<Integer> targetLoopBroadcastPromptList, List<String> loopBroadcastPromptList, List<String> loopBroadcastPromptAudioList) {
        this.runningSpeed = runningSpeed;
        this.broadcastInterval = broadcastInterval;
        this.enableBackgroundMusic = enableBackgroundMusic;
        this.backgroundMusicFileName = backgroundMusicFileName;
        this.backgroundMusicPath = backgroundMusicPath;
        this.enableLoopBroadcast = enableLoopBroadcast;
        this.targetLoopBroadcastPromptList = targetLoopBroadcastPromptList;
        this.loopBroadcastPromptList = loopBroadcastPromptList;
        this.loopBroadcastPromptAudioList = loopBroadcastPromptAudioList;
    }

    public static CruiseModeSetting getDefault() {
        return new CruiseModeSetting(0.4f,
                10,
                true,
                Constants.DEFAULT_CRUISE_BACKGROUND_MUSIC,
                null,
                true,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());
    }


    @Override
    public String toString() {
        return "CruiseModeSetting{" +
                "runningSpeed=" + runningSpeed +
                ", broadcastInterval=" + broadcastInterval +
                ", enableBackgroundMusic=" + enableBackgroundMusic +
                ", backgroundMusicFileName='" + backgroundMusicFileName + '\'' +
                ", backgroundMusicPath='" + backgroundMusicPath + '\'' +
                ", enableLoopBroadcast=" + enableLoopBroadcast +
                ", targetLoopBroadcastPromptList=" + targetLoopBroadcastPromptList +
                ", loopBroadcastPromptList=" + loopBroadcastPromptList +
                ", loopBroadcastPromptAudioList=" + loopBroadcastPromptAudioList +
                '}';
    }
}
