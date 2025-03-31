package com.reeman.delige.contract;

import android.content.Context;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.view.IView;
import com.reeman.delige.event.Event;

import java.util.List;

public interface ReturningContract {
    interface Presenter extends IPresenter {

        void onTaskPause(Context context);

        void onTaskCancel(Context context);

        void onTaskContinue(Context context);

        void startTask(Context context, int target, int reason);

//        void onNavRes(Context context, String res);

        void onDockFailed(Context context);

        void onEmergencyStopTurnOn(Context context, int emergencyStopState);

        void onEmergencyStopTurnOff();

        void onEncounterObstacle();

        void onPointNotFound(Context context);

        void onChargingPileNotFound(Context context);

        void onCustomResponseCallingEvent();

        void onDropEvent(Context context);

        void onNavigationCancelResult(Context context,int code);

        void onNavigationCompleteResult(Context context,int code, String name,float mileage);

        void onNavigationStartResult(Context context,int code, String name);

        void onSensorsError(Context context,Event.OnCheckSensorsEvent event);

        void onGetPlanDij(List<String> point);
        void onMissPose(Context context);
        void onPositionObtained(Context context,double[] position);
    }

    interface View extends IView {

        void showTaskPauseView();

        void onCountDownFinished();

        void onCountDown(long mills);

        void showDeliveryView();

        void showTaskFinishView(int result, String prompt, String voice);

        void showEmergencyStopTurnOnView();

        void showEmergencyStopTurnOffView();

        void showDropView();

        void pauseByDispatch(int id);
    }
}
