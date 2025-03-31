package com.reeman.delige.presenter.impl;

import static com.reeman.delige.base.BaseApplication.ros;

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
import com.reeman.delige.contract.BasicSettingContract;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.MapVO;
import com.reeman.delige.request.service.RobotService;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.LocaleUtil;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class BasicSettingPresenter implements BasicSettingContract.Presenter {
    private final BasicSettingContract.View view;

    public BasicSettingPresenter(BasicSettingContract.View view) {
        this.view = view;

    }

    @Override
    public void relocate(Context context) {
            ros.relocateByCoordinate(DestHelper.getInstance().getChargePointCoordinate());
        view.showRelocatingView();
    }

    private String lastTryListenText;
    private String lastGeneratedFile;

    public String getLastTryListenText() {
        return lastTryListenText;
    }

    public String getLastGeneratedFile() {
        return lastGeneratedFile;
    }

    @Override
    public void onSwitchMap(Context context) {
        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_loading_map_list));
        RobotService robotService = ServiceFactory.getRobotService();
        String ipAddress = Event.getIpEvent().ipAddress;
        robotService.getMapList("http://" + ipAddress + "/reeman/map_list")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<MapVO>>() {
                    @Override
                    public void accept(List<MapVO> mapListResponse) throws Throwable {
                        if (mapListResponse == null || mapListResponse.size() == 0) {
                            //SpManager.getInstance().edit().remove(Constants.KEY_MAP_INFO).apply();
                            view.onMapListLoaded(Collections.emptyList());
                            return;
                        }
                        //SpManager.getInstance().edit().putString(Constants.KEY_MAP_INFO, new Gson().toJson(mapListResponse.maps)).apply();
                        List<MapVO> mapVOList = new ArrayList<>();
                        for (int i = 0; i < mapListResponse.size(); i++) {
                            mapVOList.add(new MapVO(mapListResponse.get(i).name,mapListResponse.get(i).alias,false));
                        }
                        view.onMapListLoaded(mapVOList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        view.onMapListLoadedFailed(throwable);
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
                        if (!reason.equals(ResultReason.SynthesizingAudioCompleted)) {
                            emitter.onError(new RuntimeException(reason.toString()));
                            return;
                        }
                        byte[] audioData = result.getAudioData();
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
