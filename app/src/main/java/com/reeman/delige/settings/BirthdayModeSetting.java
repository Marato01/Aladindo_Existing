package com.reeman.delige.settings;

import com.reeman.delige.base.BaseSetting;
import com.reeman.delige.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class BirthdayModeSetting extends BaseSetting {

    public float runningSpeed;

    public int pauseTime;

    public boolean enablePickMealPrompt;
    public List<Integer> targetPickMealPrompt;
    public List<String> pickMealPrompt;
    public List<String> pickMealPromptAudioList;

    public boolean enablePickMealCompletePrompt;
    public List<Integer> targetPickMealCompletePrompt;
    public List<String> pickMealCompletePrompts;
    public List<String> pickMealCompletePromptAudioList;

    public boolean enableBackgroundMusic;
    public String backgroundMusicPath;
    public String backgroundMusicFileName;
    public int backgroundMusicPlayTime;

    public BirthdayModeSetting(float runningSpeed, int pauseTime, boolean enablePickMealPrompt, List<Integer> targetPickMealPrompt, List<String> pickMealPrompt, List<String> pickMealPromptAudioList, boolean pickMealCompletePrompt, List<Integer> targetPickMealCompletePrompt, List<String> pickMealCompletePrompts, List<String> pickMealCompletePromptAudioList, String backgroundMusicPath, String backgroundMusicFileName, boolean enableBackgroundMusic, int backgroundMusicPlayTime) {
        this.runningSpeed = runningSpeed;
        this.pauseTime = pauseTime;
        this.enablePickMealPrompt = enablePickMealPrompt;
        this.targetPickMealPrompt = targetPickMealPrompt;
        this.pickMealPrompt = pickMealPrompt;
        this.pickMealPromptAudioList = pickMealPromptAudioList;
        this.enablePickMealCompletePrompt = pickMealCompletePrompt;
        this.targetPickMealCompletePrompt = targetPickMealCompletePrompt;
        this.pickMealCompletePrompts = pickMealCompletePrompts;
        this.pickMealCompletePromptAudioList = pickMealCompletePromptAudioList;
        this.backgroundMusicPath = backgroundMusicPath;
        this.backgroundMusicFileName = backgroundMusicFileName;
        this.enableBackgroundMusic = enableBackgroundMusic;
        this.backgroundMusicPlayTime = backgroundMusicPlayTime;
    }

    public static BirthdayModeSetting getDefault() {
        return new BirthdayModeSetting(
                0.4f,
                30,
                true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                true, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                null,
                Constants.DEFAULT_BIRTHDAY_BACKGROUND_MUSIC,
                true,
                1
        );
    }

    @Override
    public String toString() {
        return "BirthdayModeSetting{" +
                "runningSpeed=" + runningSpeed +
                ", pauseTime=" + pauseTime +
                ", enablePickMealPrompt=" + enablePickMealPrompt +
                ", targetPickMealPrompt=" + targetPickMealPrompt +
                ", pickMealPrompt=" + pickMealPrompt +
                ", pickMealPromptAudioList=" + pickMealPromptAudioList +
                ", enablePickMealCompletePrompt=" + enablePickMealCompletePrompt +
                ", targetPickMealCompletePrompt=" + targetPickMealCompletePrompt +
                ", pickMealCompletePrompts=" + pickMealCompletePrompts +
                ", pickMealCompletePromptAudioList=" + pickMealCompletePromptAudioList +
                ", enableBackgroundMusic=" + enableBackgroundMusic +
                ", backgroundMusicPath='" + backgroundMusicPath + '\'' +
                ", backgroundMusicFileName='" + backgroundMusicFileName + '\'' +
                ", backgroundMusicPlayTime=" + backgroundMusicPlayTime +
                '}';
    }
}
