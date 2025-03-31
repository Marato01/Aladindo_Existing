package com.reeman.delige.presenter.impl;

import android.content.Context;
import android.view.View;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.reeman.delige.R;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.ModeSettingFragmentContract;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.service.RobotService;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.LocaleUtil;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_BIRTHDAY;
import static com.reeman.delige.widgets.BackgroundMusicChooseDialog.BACKGROUND_MUSIC_TYPE_CRUISE;

public class ModeSettingFragmentPresenter implements ModeSettingFragmentContract.Presenter {

    private final ModeSettingFragmentContract.View view;
    private String lastTryListenText;
    private String lastGeneratedFile;

    public String getLastTryListenText() {
        return lastTryListenText;
    }

    public String getLastGeneratedFile() {
        return lastGeneratedFile;
    }

    public ModeSettingFragmentPresenter(ModeSettingFragmentContract.View view) {
        this.view = view;
    }

    @Override
    public void loadBackgroundMusic(Context context, int type) {
        String ipAddress = Event.getIpEvent().ipAddress;
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_loading_background_music));
        String path;
        if (type == BACKGROUND_MUSIC_TYPE_BIRTHDAY) {
            path = API.fetchBirthdayMusicAPI(ipAddress);
        } else if (type == BACKGROUND_MUSIC_TYPE_CRUISE) {
            path =  API.fetchCruiseMusicAPI(ipAddress);
        } else {
            path =  API.fetchDeliveryMealMusicAPI(ipAddress);
        }
        RobotService robotService = ServiceFactory.getRobotService();
        robotService.fetchMusic(path)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<String> music) {
                        view.onMusicListLoaded(type, music);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onMusicListFailed(type, e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @Override
    public void tryListen(Context context, String dir, String text, String type, View btnTryListen, View btnSave) {
        this.lastTryListenText = text;
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        emitter.onNext(1);
                        int localeType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
                        if (localeType == -1) localeType = LocaleUtil.getLocaleType();
                        String language = LocaleUtil.getLanguage(localeType);
                        String voice = LocaleUtil.getVoice(localeType);
                        SpeechConfig speechConfig = SpeechConfig.fromSubscription("", "eastasia");
                        speechConfig.setSpeechSynthesisLanguage(language);
                        speechConfig.setSpeechSynthesisVoiceName(voice);
                        speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Riff24Khz16BitMonoPcm);
                        AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();
                        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
                        SpeechSynthesisResult result = synthesizer.SpeakText(text);
                        ResultReason reason = result.getReason();

                        byte[] audioData = result.getAudioData();

                        if (audioData == null || audioData.length == 0) {
                            emitter.onError(new RuntimeException(context.getString(R.string.text_try_listen_failed)));
                            return;
                        }

                        if (!reason.equals(ResultReason.SynthesizingAudioCompleted)) {
                            emitter.onError(new RuntimeException(reason.toString()));
                            return;
                        }

                        try {
                            File root = new File(context.getFilesDir() + dir + "/" + language + "/" + type);
                            if (!root.exists()) root.mkdirs();
                            File targetFile = new File(root, System.currentTimeMillis() + ".wav");
                            lastGeneratedFile = targetFile.getAbsolutePath();
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                            bufferedOutputStream.write(audioData, 0, audioData.length);
                            bufferedOutputStream.flush();
                            bufferedOutputStream.close();
                            emitter.onNext(2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Integer o) {
                        if (o == 1) {
                            view.onSynthesizeStart(btnTryListen, btnSave);
                        } else {
                            view.onSynthesizeEnd(btnTryListen, btnSave);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onSynthesizeError(e.getMessage(), btnTryListen, btnSave);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
