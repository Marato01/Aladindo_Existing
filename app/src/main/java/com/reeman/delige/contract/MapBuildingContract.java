package com.reeman.delige.contract;


import android.content.Context;

import com.reeman.delige.activities.MapBuildingActivity;
import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.view.IView;

public interface MapBuildingContract {
    interface Presenter extends IPresenter {

        void changeToConstructMap();

        void changeToNavMode() ;

        void saveMap();

        void getCurrentPosition();

        void exitWithoutSaving();

        void markPoint(Context context, double[] position, String currentMarkPointName);

        void reloadPoints(Context context);

        void startDrawPath(Context context);

        void onPositionLoaded(double[] position);

        void abandonPath();

        void savePath(Context context);

        void deletePoint();
    }


    interface View extends IView {

        void onReloadPointSuccess();

        void onReloadPointFailed();

        void onPathSaveSuccess();

        void onPathSaveFailed(String message);
    }
}
