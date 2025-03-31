package com.reeman.delige.utils;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import com.reeman.delige.constants.Constants;

import java.io.IOException;
import java.util.Locale;

import static com.reeman.delige.base.BaseApplication.mApp;


public class MediaPlayerHelper {

    private static MediaPlayer mediaPlayer;

    public static void play(String name) {
        play(name, false, () -> {
        });
    }

    public static void play(String name, boolean loop) {
        play(name, loop, () -> {
        });
    }

    public static void play(String name, boolean loop, OnCompleteListener listener) {
        String path;
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        if (languageType != -1) {
            path = LocaleUtil.getAssetsPathByLanguage(languageType);
        } else {
            path = Locale.getDefault().getLanguage() + "/";
        }
        AssetFileDescriptor assetFileDescriptor;
        try {
            assetFileDescriptor = mApp.getAssets().openFd(path + name + ".wav");
            playAssetsFile(assetFileDescriptor, loop, listener);
        } catch (Exception e) {
            try {
                assetFileDescriptor = mApp.getAssets().openFd(path + name + ".mp3");
                playAssetsFile(assetFileDescriptor, loop, listener);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public static void pause() {
        try {
            mediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playFile(String path) {
        playFile(path, false, null);
    }

    public static void playFile(String path, boolean loop) {
        playFile(path, loop, null);
    }

    public static void playAssetsFile(AssetFileDescriptor assetFileDescriptor, boolean loop) {
        playAssetsFile(assetFileDescriptor, loop, null);
    }

    public static void playAssetsFile(AssetFileDescriptor assetFileDescriptor, boolean loop, OnCompleteListener listener) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            mediaPlayer.setLooping(loop);
            mediaPlayer.setOnCompletionListener(mp -> {
                if (listener == null) return;
                listener.onComplete();
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (listener == null) return;
                listener.onComplete();
            }, 1500);
        }
    }

    public static void playDefaultMusic(String name, boolean loop, OnCompleteListener listener) {
        AssetFileDescriptor assetFileDescriptor;
        try {
            assetFileDescriptor = mApp.getAssets().openFd("zh/" + name);
            playAssetsFile(assetFileDescriptor, loop, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playFile(String path, boolean loop, OnCompleteListener listener) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
            mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.setLooping(loop);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (listener != null) listener.onComplete();
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (listener == null) return;
                listener.onComplete();
            }, 1500);
        }
    }

    public static void decreaseVolume() {
        int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        volume -= 5;
        if (volume < 0) {
            volume = 0;
        }
        mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
    }

    public static void resumeVolume() {
        int volume = SpManager.getInstance().getInt(Constants.KEY_MEDIA_VOLUME, Constants.DEFAULT_MEDIA_VOLUME);
        mediaPlayer.setVolume(volume / 15.0f, volume / 15.0f);
    }

    public interface OnCompleteListener {
        void onComplete();
    }
}
