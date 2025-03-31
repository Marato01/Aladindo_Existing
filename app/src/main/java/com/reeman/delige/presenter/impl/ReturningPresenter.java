package com.reeman.delige.presenter.impl;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseApplication;
import com.reeman.delige.base.BaseSetting;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.constants.Errors;
import com.reeman.delige.constants.State;
import com.reeman.delige.contract.ReturningContract;
import com.reeman.delige.dispatch.DispatchManager;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.FaultRecord;
import com.reeman.delige.request.model.Msg;
import com.reeman.delige.request.model.Response;
import com.reeman.delige.request.notifier.Notifier;
import com.reeman.delige.request.notifier.NotifyConstant;
import com.reeman.delige.request.url.API;
import com.reeman.delige.settings.ObstacleSetting;
import com.reeman.delige.settings.creator.SettingCreator;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.event.Event;
import com.reeman.ros.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.reeman.delige.base.BaseApplication.dispatchState;
import static com.reeman.delige.base.BaseApplication.getCallingQueue;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;
import static com.reeman.delige.base.BaseApplication.ros;


public class ReturningPresenter implements ReturningContract.Presenter {
    private final ReturningContract.View view;
    private CountDownTimer countDownTimer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    protected Runnable chargeRunnable;
    private ObstacleSetting obstacleSetting;
    private long lastObstaclePromptPlaybackTimeMills;
    private int currentMindOutIndex = 0;
    private int target;
    private Gson gson;
    private long lastDockFailedTimeMills;
    private String dispatchTargetPoint;

    public String getDispatchTargetPoint() {
        return dispatchTargetPoint;
    }

    public ReturningPresenter(ReturningContract.View view) {
        this.view = view;
    }

    private void resetCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onTaskPause(Context context) {
        ros.stopMove();
        handler.removeCallbacksAndMessages(null);
        if (ros.isNavigating()) ros.cancelNavigation();
        stopDockChargingPile();
        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
        startPauseCountDown();
        view.showTaskPauseView();
    }

    private void startPauseCountDown() {
        countDownTimer = new CountDownTimer(Constants.RETURN_COUNTDOWN, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                view.onCountDown(millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                view.onCountDownFinished();
            }
        };
        countDownTimer.start();
        ros.setState(State.PAUSE);
    }

    @Override
    public void onTaskCancel(Context context) {
        resetCountdown();
        onTaskFinished(0, null, null);
    }

    @Override
    public void onTaskContinue(Context context) {
        if (ros.isEmergencyStopDown()) {
            view.showEmergencyStopTurnOnView();
            return;
        }
        resetCountdown();
        ros.setState(State.RETURNING);
        DestHelper destHelper = DestHelper.getInstance();
        if (target == Constants.TYPE_GOTO_CHARGE) {
            navigationWithDispatch(destHelper.getChargePoint());
        } else if (target == Constants.TYPE_GOTO_PRODUCT_POINT) {
            navigationWithDispatch(destHelper.getProductPoint());
        } else {
            navigationWithDispatch(destHelper.getRecyclePoint());
        }
        view.showDeliveryView();
    }

    @Override
    public void startTask(Context context, int target, int reason) {
        SettingCreator<BaseSetting> typeAdapter = new SettingCreator<>();
        gson = new GsonBuilder()
                .registerTypeAdapter(ObstacleSetting.class, typeAdapter)
                .create();
        if (navigationMode == Mode.FIX_ROUTE){
            ros.positionAutoUploadControl(true);
            dispatchState = DispatchState.INIT;
        }
        ros.setState(State.RETURNING);
        ros.setNavSpeed("0.6");
        this.target = target;
        DestHelper destHelper = DestHelper.getInstance();
        if (target == Constants.TYPE_GOTO_CHARGE) {
            if (reason == Constants.CHARGE_REASON_USER_TRIGGER) {
                VoiceHelper.play("voice_okay_goto_charge");
            } else {
                VoiceHelper.play("voice_power_insufficient_and_goto_charge");
            }
            navigationWithDispatch(destHelper.getChargePoint());
        } else if (target == Constants.TYPE_GOTO_PRODUCT_POINT) {
            VoiceHelper.play("voice_okay_goto_product_point");
            navigationWithDispatch(destHelper.getProductPoint());
        } else {
            VoiceHelper.play("voice_okay_goto_recycling_point");
            navigationWithDispatch(destHelper.getRecyclePoint());
        }
        String obstacleSettingStr = SpManager.getInstance().getString(Constants.KEY_OBSTACLE_CONFIG, null);
        if (TextUtils.isEmpty(obstacleSettingStr)) {
            obstacleSetting = ObstacleSetting.getDefault();
        } else {
            obstacleSetting = gson.fromJson(obstacleSettingStr, ObstacleSetting.class);
        }
        view.showDeliveryView();
    }

    private void handleNavSuccessResult(Context context, String dest) {
        DestHelper destHelper = DestHelper.getInstance();
        if (!TextUtils.isEmpty(destHelper.getRecyclePoint())
                && destHelper.getRecyclePoint().equals(dest)) {
            VoiceHelper.play("voice_arrived_at_recycling_point");
            onTaskFinished(0, null, null);
            return;
        }
        if (!TextUtils.isEmpty(destHelper.getProductPoint())
                && destHelper.getProductPoint().equals(dest)) {
            VoiceHelper.play("voice_arrived_at_product_point");
            onTaskFinished(0, null, null);
            return;
        }
        if (!TextUtils.isEmpty(destHelper.getChargePoint())
                && destHelper.getChargePoint().equals(dest)) {
            ros.setCharge(ROS.CS.DOCKING);
            VoiceHelper.play("voice_start_docking_charging_pile");
        }
    }

    public void onTaskFinished(int result, String prompt, String voice) {
        ros.setState(State.IDLE);
        ros.setLastNavPoint("");
        ros.setCurrentDest("");
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.clear();
        view.showTaskFinishView(result, prompt, voice);
    }


    private void uploadHardwareError(Event.OnCheckSensorsEvent event, String hardwareError) {
        String hostname = Event.getOnHostnameEvent().hostname;
        if (TextUtils.isEmpty(hostname)) return;
        long mills = System.currentTimeMillis();
        FaultRecord faultRecord = new FaultRecord(Errors.getFaultReason(event), hardwareError, BaseApplication.macAddress, "v1.1", BaseApplication.appVersion, Event.getVersionEvent().version, mills, mills);
        String url = API.hardwareFaultAPI(hostname);
        ServiceFactory.getRobotService()
                .reportHardwareError(url, faultRecord)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response>() {
                    @Override
                    public void accept(Response response) throws Throwable {
                        XLog.w("上传硬件异常成功" + response.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        XLog.w("上传硬件异常失败：" + throwable.getMessage());
                    }
                });
    }

    @Override
    public void onDockFailed(Context context) {
        if (ros.getDockFailCount() < 2) {
            VoiceHelper.play("voice_retry_charge");
            chargeRunnable = new Runnable() {
                @Override
                public void run() {
                    if (ros.isEmergencyStopDown()) return;
                    ros.setDockFailCount(ros.getDockFailCount() + 1);
                    navigationWithDispatch(DestHelper.getInstance().getChargePoint());
                }
            };
            handler.postDelayed(chargeRunnable, 4000);
            return;
        }
        dispatchState = DispatchState.INIT;
        ros.cancelCharge();
        ros.setDockFailCount(0);
        ros.setNavigating(false);
        if (System.currentTimeMillis() - lastDockFailedTimeMills > 30 * 60 * 1000) {
            Notifier.notify(new Msg(NotifyConstant.CHARGE_NOTIFY, "充电状态", "充电对接失败", Event.getOnHostnameEvent().hostname));
            lastDockFailedTimeMills = System.currentTimeMillis();
        }
        onTaskFinished(-1, context.getString(R.string.voice_charge_failed), "voice_charge_failed");
    }

    @Override
    public void onDropEvent(Context context) {
        resetCountdown();
        handler.removeCallbacksAndMessages(null);
        if (ros.isNavigating()) ros.cancelNavigation();
        stopDockChargingPile();
        ros.setState(State.PAUSE);

        Notifier.notify(new Msg(NotifyConstant.TASK_NOTIFY, "防跌状态", "机器人任务过程中检测到跌落风险，请检查机器定位是否正确", Event.getOnHostnameEvent().hostname));
        view.showDropView();
    }

    @Override
    public void onNavigationCancelResult(Context context, int code) {

    }

    @Override
    public void onNavigationCompleteResult(Context context, int code, String name, float mileage) {
        if (code == 0) {
            handleNavSuccessResult(context, name);
        } else {
            DestHelper destHelper = DestHelper.getInstance();
            if (target == Constants.TYPE_GOTO_CHARGE) {
                navigationWithDispatch(destHelper.getChargePoint());
            } else if (target == Constants.TYPE_GOTO_PRODUCT_POINT) {
                navigationWithDispatch(destHelper.getProductPoint());
            } else {
                navigationWithDispatch(destHelper.getRecyclePoint());
            }
        }
    }

    @Override
    public void onNavigationStartResult(Context context, int code, String name) {
        if (code == 0) {
            if (ros.getState() == State.PAUSE) ros.cancelNavigation();
            return;
        }
        String navigationStartError = Errors.getNavigationStartError(context, code);
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "发起导航失败", "导航结果:" + navigationStartError, Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, navigationStartError, "");
    }

    @Override
    public void onSensorsError(Context context, Event.OnCheckSensorsEvent event) {
        String hardwareError = Errors.getSensorError(context, event);
        if (hardwareError == null)return;
        stopDockChargingPile();
        ros.cancelNavigation();
        uploadHardwareError(event, hardwareError);
        Log.w("传感器异常");
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "传感器状态", "导航结果上报传感器异常:" + event.toString(), Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, hardwareError, null);
    }

    @Override
    public void onEmergencyStopTurnOn(Context context, int emergencyStopState) {
        VoiceHelper.play("voice_scram_stop_turn_on");
        resetCountdown();
        handler.removeCallbacksAndMessages(null);
        if (ros.isNavigating()) ros.cancelNavigation();
        stopDockChargingPile();
        ros.setState(State.PAUSE);
        Notifier.notify(new Msg(NotifyConstant.TASK_NOTIFY, "急停状态", "任务过程中急停开关被按下", Event.getOnHostnameEvent().hostname));
        view.showEmergencyStopTurnOnView();
    }

    private void stopDockChargingPile() {
        if (ros.isDocking()) {
            handler.removeCallbacks(chargeRunnable);
            ros.cancelCharge();
            ros.setDockFailCount(0);
        }
    }

    @Override
    public void onEmergencyStopTurnOff() {
        VoiceHelper.play("voice_scram_stop_turn_off");
        view.showEmergencyStopTurnOffView();
    }

    @Override
    public void onChargingPileNotFound(Context context) {
        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
        onTaskFinished(-1, context.getString(R.string.voice_not_found_charging_pile), "voice_not_found_charging_pile");
    }


    @Override
    public void onPointNotFound(Context context) {
        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
        onTaskFinished(-1, context.getString(R.string.voice_not_found_target_point), "voice_not_found_target_point");
    }

    @Override
    public void onCustomResponseCallingEvent() {
        if (target != Constants.TYPE_GOTO_CHARGE) {
            resetCountdown();
            view.showTaskFinishView(-2, "", "");
        }
    }

    @Override
    public void onEncounterObstacle() {
        if (!obstacleSetting.enableObstaclePrompt
                || System.currentTimeMillis() - lastObstaclePromptPlaybackTimeMills < 5000
                || VoiceHelper.isPlaying())
            return;
        List<Integer> targetObstaclePrompts = obstacleSetting.targetObstaclePrompts;
        if (targetObstaclePrompts != null && !targetObstaclePrompts.isEmpty()) {
            if (obstacleSetting.obstaclePromptAudioList != null && !obstacleSetting.obstaclePromptAudioList.isEmpty()) {
                int currentPlayback = currentMindOutIndex++ % targetObstaclePrompts.size();
                VoiceHelper.playFile(obstacleSetting.obstaclePromptAudioList.get(targetObstaclePrompts.get(currentPlayback)));
            }
        } else {
            VoiceHelper.play("voice_mind_out_" + (currentMindOutIndex++ % 3));
        }
        lastObstaclePromptPlaybackTimeMills = System.currentTimeMillis();
    }

    @Override
    public void onGetPlanDij(List<String> point) {
        if (ros.getState() == State.PAUSE) return;
        ArrayList<RobotInfo> robotList = DispatchUtil.Companion.getRobotList();
        if (point == null || point.isEmpty() || robotList.isEmpty()) {
            ros.navigationByPoint(dispatchTargetPoint);
        } else {
            DispatchUtil.Companion.updateRoutePoint(point, pointInfoQueue);
            List<String> newPointList;
            if (point.size() > 2) {
                newPointList = point.subList(0, 3);
            } else {
                newPointList = new ArrayList<>(point);
            }
            if (DispatchUtil.Companion.checkPause(newPointList, robotList, true)) {
                view.pauseByDispatch(R.string.text_pause_by_cross);
            } else {
                ros.navigationByPoint(dispatchTargetPoint);

            }
        }
    }

    @Override
    public void onMissPose(Context context) {
        stopDockChargingPile();
        ros.cancelNavigation();
        dispatchState = DispatchState.IGNORE;
        Log.w("定位异常");
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "定位异常", "定位异常", Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, context.getString(R.string.voice_miss_pose_task_finish), "voice_miss_pose_task_finish");
    }

    @Override
    public void onPositionObtained(Context context, double[] position) {
        if (
                navigationMode == Mode.FIX_ROUTE
                && ros.getState() != State.PAUSE
                && ros.getState() != State.CHARGING
                && ros.getState() != State.IDLE) {
            if (
                    dispatchState == DispatchState.INIT
                            && DispatchUtil.Companion.isCloseToTargetPoint()
            ) {
                if (DispatchUtil.Companion.isTargetPointOccupied()) {
                    view.pauseByDispatch(R.string.text_target_point_occupied);
                }
            }
        }
    }

    public void navigationWithDispatch(String point) {
        if (ros.getState() == State.PAUSE)
            ros.setState(State.DELIVERY);
        if (
                navigationMode == Mode.FIX_ROUTE
                        && DispatchManager.isStarted()
                        && !ros.isInSpecialArea()
        ) {
            ros.getDefinedPlan(point);
            dispatchTargetPoint = point;
        } else {
            ros.navigationByPoint(point);
        }
    }
}
