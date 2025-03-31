package com.reeman.delige.contract;

import android.content.Context;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.request.model.MapVO;
import com.reeman.delige.view.IView;

import java.util.List;

public interface BasicSettingContract {

    interface Presenter extends IPresenter {

        void relocate(Context context);

        void tryListen(Context context, String dir, String prompt, String type, android.view.View btnTryListen, android.view.View btnSave);

        void onSwitchMap(Context context);
    }

    interface View extends IView {

        void showRelocatingView();

        void onSynthesizeStart(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeEnd(android.view.View btnTryListen, android.view.View btnSave);

        void onSynthesizeError(String message, android.view.View btnTryListen, android.view.View btnSave);

        void onMapListLoaded(List<MapVO> list);

        void onMapListLoadedFailed(Throwable throwable);
    }
}
