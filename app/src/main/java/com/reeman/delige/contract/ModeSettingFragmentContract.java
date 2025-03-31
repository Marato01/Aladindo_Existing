package com.reeman.delige.contract;

import android.content.Context;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.view.IView;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;

public interface ModeSettingFragmentContract {

    interface Presenter extends IPresenter {
        void tryListen(Context context, String dir, String prompt, String type, android.view.View btnTryListen, android.view.View btnSave);

        void loadBackgroundMusic(Context context, int type);
    }

    interface View extends IView {

        void onSynthesizeStart(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeEnd(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeError(String message, android.view.View btnTryListen, android.view.View btnSave);

        void onMusicListLoaded(int type, @NonNull List<String> music);

        void onMusicListFailed(int type, Throwable e);
    }
}
