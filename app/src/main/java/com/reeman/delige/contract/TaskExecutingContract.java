package com.reeman.delige.contract;

import android.content.Context;
import android.content.Intent;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.view.IView;
import com.reeman.delige.event.Event;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface TaskExecutingContract {
    interface Presenter extends IPresenter {

        void startTask(Context context, int taskMode, TreeMap<Integer, String> level2Table);

        void startTask(Context context, int taskMode, String target);

        void startTask(Context context, int taskMode, Route route);

        void startMultiDeliveryTask(Context context, int taskMode, TreeMap<Integer, List<String>> multiDeliveryTable);

        void navigateToPoint(String point);

//        void onNavRes(Context context, String raw);

        void onCancelTask(Context context);

        void onTaskTerminated(Context context);

        void onTaskPause(boolean isPauseCountdown);

        void onTaskResume(Context context, boolean isPauseCountdown);

        void onEmergencyStopTurnOn(Context context);

        void onEmergencyStopTurnOff();

        void onTaskContinue(Context context);

        void onNextTable(Context context);

        void onCompleteTask(Context context);

        void onPickMealInAdvance();

        void onEncounterObstacle();

        void onTimeStamp(Context context);

        void onTouch(Context context);

        void onPointNotFound(Context context);

        void onChargingPileNotFound(Context context);

        void onDockFailed(Context context);

        void onPositionObtained(Context context,double[] position);

        void onGlobalPathEvent(Double[] path);

        void onCustomResponseCallingEvent(String next);

        void startTask(Context context, Intent intent);

        void onDrop(Context context);

        void onNavigationCancelResult(Context context,int code);

        void onNavigationCompleteResult(Context context,int code, String name,float mileage);

        void onNavigationStartResult(Context context,int code, String name);

        void onSensorsError(Context context, Event.OnCheckSensorsEvent event);

        void onGetPlanDij(List<String> point);

        void onSpecialArea(String name);

        void onMissPose(Context context);
    }

    interface View extends IView {

        void onTaskPause(boolean isPauseCountdown);

        void showTaskPauseView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable);

        void onPauseCountDown(boolean isPauseCountdown, long seconds);

        void onCountDownFinished(boolean isPauseCountdown);

        void showTaskFinishedView(int result, String prompt, String voice);

        void showDeliveryView(String target);

        List<Integer> showArrivedAtTargetTable(Map<Integer, String> originTable, String dest);

        void showArrivedAtBirthdayTable(String dest);

        void showEmergencyOnView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable);

        List<Integer> showArrivedAtMultiDeliveryTargetTable(TreeMap<Integer, List<String>> originDeliveryTable, TreeMap<Integer, List<String>> multiDeliveryTable, String dest);

        void showArrivedAtCallingTable(String dest);

        void showEmergencyStopTurnOffView();

        void showDropView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable);

        void pauseByDispatch(int id);

        void resumeByDispatch();
    }
}
