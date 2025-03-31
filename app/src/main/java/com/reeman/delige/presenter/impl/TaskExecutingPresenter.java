package com.reeman.delige.presenter.impl;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseApplication;
import com.reeman.delige.base.BaseSetting;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.constants.Errors;
import com.reeman.delige.constants.State;
import com.reeman.delige.contract.TaskExecutingContract;
import com.reeman.delige.dispatch.DispatchManager;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.boardcast.LANBroadcaster;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.light.LightController;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.repository.entities.DeliveryRecord;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.FaultRecord;
import com.reeman.delige.request.model.Msg;
import com.reeman.delige.request.model.Response;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.request.notifier.Notifier;
import com.reeman.delige.request.notifier.NotifyConstant;
import com.reeman.delige.request.url.API;
import com.reeman.delige.settings.BirthdayModeSetting;
import com.reeman.delige.settings.CruiseModeSetting;
import com.reeman.delige.settings.DeliveryMealSetting;
import com.reeman.delige.settings.MultiDeliverySetting;
import com.reeman.delige.settings.ObstacleSetting;
import com.reeman.delige.settings.RecycleModeSetting;
import com.reeman.delige.settings.creator.SettingCreator;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.MediaPlayerHelper;
import com.reeman.delige.utils.PackageUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.utils.WIFIUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;
import com.reeman.ros.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.reeman.delige.base.BaseApplication.dbRepository;
import static com.reeman.delige.base.BaseApplication.dispatchState;
import static com.reeman.delige.base.BaseApplication.getCallingQueue;
import static com.reeman.delige.base.BaseApplication.mRobotInfo;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;

import static com.reeman.delige.base.BaseApplication.ros;


public class TaskExecutingPresenter implements TaskExecutingContract.Presenter {

    private final TaskExecutingContract.View view;

    private Gson gson;
    private CountDownTimer pauseCountDownTimer;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private ObstacleSetting obstacleSetting;
    private CruiseModeSetting cruiseModeSetting;
    private RecycleModeSetting recycleModeSetting;
    private DeliveryMealSetting deliveryMealSetting;
    private BirthdayModeSetting birthdayModeSetting;
    private MultiDeliverySetting multiDeliverySetting;

    private double mileage;
    private int pickMealTime;
    private long taskStartTime;
    private int taskMode = -1;
    private boolean lowCharge = false;
    private int lastTaskMode = -1;
    private boolean isPauseCountdown = false;
    private int currentLoopIndex = 0;
    private int currentMindOutIndex = 0;
    private long lastObstaclePromptPlaybackTimeMills;

    private Route route;
    private Route backupRoute;
    private double currentX;
    private double currentY;
    private double currentZ;
    private int cancelCount = 0;
    private float currentRouteAngle;
    private boolean reverse = false;
    private boolean isPathStart = false;
    private boolean isTurningPoint = false;

    private Runnable loopRunnable;
    private Runnable chargeRunnable;
    private Runnable retryRunnable;

    private TreeMap<Integer, String> level2Table;
    private TreeMap<Integer, String> originTable;
    private TreeMap<Integer, String> failedDeliveryMap;

    private TreeMap<Integer, List<String>> multiDeliveryTable;
    private TreeMap<Integer, List<String>> originMultiDeliveryTable;
    private TreeMap<Integer, List<String>> failedMultiDeliveryTable;

    private String target;
    private String failedTarget = "";
    private long lastDockFailedTimeMills;
    private String dispatchTargetPoint;

    public String getDispatchTargetPoint() {
        return dispatchTargetPoint;
    }

    public boolean isLowCharge() {
        return lowCharge;
    }

    public TaskExecutingPresenter(TaskExecutingContract.View view) {
        this.view = view;
    }

    public void setTaskMode(int taskMode) {
        this.taskMode = taskMode;
    }

    public int getTaskMode() {
        return taskMode;
    }

    public int getLastTaskMode() {
        return lastTaskMode;
    }

    @Override
    public void navigateToPoint(String point) {
        navigationWithDispatch(point);
    }


    private void handleNavSuccessResult(Context context, String dest) {
        DestHelper destHelper = DestHelper.getInstance();
        if (!TextUtils.isEmpty(destHelper.getRecyclePoint())
                && destHelper.getRecyclePoint().equals(dest)) {
            pauseAllAudio();
            VoiceHelper.play("voice_arrived_at_recycling_point");
            uploadTaskRecord(context);
            onTaskFinished(0, null, null);
            return;
        }
        if (!TextUtils.isEmpty(destHelper.getProductPoint())
                && destHelper.getProductPoint().equals(dest)) {
            pauseAllAudio();
            VoiceHelper.play("voice_arrived_at_product_point");
            uploadTaskRecord(context);
            onTaskFinished(0, null, null);
            return;
        }
        if (!TextUtils.isEmpty(destHelper.getChargePoint())
                && destHelper.getChargePoint().equals(dest)) {
            ros.setCharge(ROS.CS.DOCKING);
            VoiceHelper.play("voice_start_docking_charging_pile");
            return;
        }
        if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            onArrivedAtTargetTable(TextUtils.isEmpty(dest) ? ros.getLastNavPoint() : dest);
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            onArrivedBirthdayTable(dest);
        } else if (taskMode == Constants.MODE_RECYCLE_2) {
            onArriveRecyclePoint(dest);
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            onArriveMultiDeliveryTarget(dest);
        } else if (taskMode == Constants.MODE_CALLING) {
            onArriveCallingTarget(dest);
        } else {
            //路线还有点位
            try {
                if (!isPathStart) isPathStart = true;
                if (TextUtils.isEmpty(dest)) return;
                String[] split = dest.split(",");
                if (split.length != 3) return;
                currentX = Float.parseFloat(split[0]);
                currentY = Float.parseFloat(split[1]);

                if (Math.abs(currentX - ros.getLastNavX()) > 0.01 || Math.abs(currentY - ros.getLastNavY()) > 0.01) {
                    XLog.w("误差过大，不处理导航结果");
                    return;
                }

                route.pointList.remove(0);
                if (route.pointList.isEmpty()) {
                    route = Route.clone(backupRoute, reverse);
                    route.pointList.remove(0);
                    reverse = !reverse;
                    ros.turn(180);
                    retryRunnable = () -> gotoNextRoutePoint(currentX, currentY);
                    mHandler.postDelayed(retryRunnable, 2500);
                    return;
                }
                gotoNextRoutePoint(currentX, currentY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onArriveCallingTarget(String dest) {
        pauseAllAudio();
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.remove(dest);
        view.showArrivedAtCallingTable(dest);
        startCountDownTimer(false);
    }

    private void onArriveMultiDeliveryTarget(String dest) {
        pauseAllAudio();
        LightController instance = LightController.getInstance();
        instance.closeAll();
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.remove(dest);
        List<Integer> layers = view.showArrivedAtMultiDeliveryTargetTable(originMultiDeliveryTable, multiDeliveryTable, dest);
        int sum = 0;
        for (int i = 0; i < layers.size(); i++) {
            Integer layer = layers.get(i);
            sum += Math.pow(2, layer);
            mHandler.postDelayed(() -> instance.blink(layer), i * 100);
        }
        playMultiDeliveryArrivalPrompt(sum);
        startCountDownTimer(false);
    }

    private void playDeliveryArrivalPrompt(int sum) {
        try {
            if (deliveryMealSetting.enableDeliveryArrivalPrompt && deliveryMealSetting.targetPromptForDeliveryArrival != null && !deliveryMealSetting.targetPromptForDeliveryArrival.isEmpty()
                    && deliveryMealSetting.deliveryArrivalPromptAudioList != null && !deliveryMealSetting.deliveryArrivalPromptAudioList.isEmpty()) {
                VoiceHelper.playFile(deliveryMealSetting.deliveryArrivalPromptAudioList.get(deliveryMealSetting.targetPromptForDeliveryArrival.get(0)), () -> VoiceHelper.play("voice_arrived_and_pick_up_your_meal_" + sum + "_partial"));
            } else {
                VoiceHelper.play("voice_arrived_and_pick_up_your_meal_" + sum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void playMultiDeliveryArrivalPrompt(int sum) {
        try {
            if (multiDeliverySetting.enableDeliveryArrivalPrompt && multiDeliverySetting.targetPromptForDeliveryArrival != null && !multiDeliverySetting.targetPromptForDeliveryArrival.isEmpty()
                    && multiDeliverySetting.deliveryArrivalPromptAudioList != null && !multiDeliverySetting.deliveryArrivalPromptAudioList.isEmpty()) {
                VoiceHelper.playFile(multiDeliverySetting.deliveryArrivalPromptAudioList.get(multiDeliverySetting.targetPromptForDeliveryArrival.get(0)), () -> VoiceHelper.play("voice_arrived_and_pick_up_your_meal_" + sum + "_partial"));
            } else {
                VoiceHelper.play("voice_arrived_and_pick_up_your_meal_" + sum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onArrivedAtTargetTable(String dest) {
        pauseAllAudio();
        LightController instance = LightController.getInstance();
        instance.closeAll();
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.remove(dest);
        List<Integer> layers = view.showArrivedAtTargetTable(originTable, dest);
        int sum = 0;
        for (int i = 0; i < layers.size(); i++) {
            Integer layer = layers.get(i);
            sum += Math.pow(2, layer - 1);
            mHandler.postDelayed(() -> instance.blink(layer - 1), i * 100);
            level2Table.remove(layer);
        }
        playDeliveryArrivalPrompt(sum);
        startCountDownTimer(false);
    }

    private void onArriveRecyclePoint(String dest) {
        pauseAllAudio();
        playPlaceRecyclables();
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.remove(dest);
        for (Integer key : level2Table.keySet()) {
            if (dest.equals(level2Table.get(key))) {
                level2Table.remove(key);
                break;
            }
        }
        startCountDownTimer(false);
        view.showTaskPauseView(level2Table, multiDeliveryTable);
    }

    private void gotoNextRoutePoint(double x1, double y1) {
        List<Double> floats = route.pointList.get(0);
        currentRouteAngle = calcAngle(x1, floats.get(0), y1, floats.get(1));
        if (route.pointList.size() >= 2) {
            List<Double> floats1 = route.pointList.get(1);

            float calcNextRouteAngle = calcAngle(floats.get(0), floats1.get(0), floats.get(1), floats1.get(1));

            if (calcNextRouteAngle < 0) calcNextRouteAngle += 360;

            float calcCurrentRouteAngle = currentRouteAngle;

            if (calcCurrentRouteAngle < 0) calcCurrentRouteAngle += 360;

            float abs = Math.abs(calcNextRouteAngle - calcCurrentRouteAngle);

            isTurningPoint = Math.min(abs, 360 - abs) > 60;
        }
        ros.navigationByCoordinates(floats.get(0), floats.get(1), currentRouteAngle);
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

    public void onTaskFinished(int result, String prompt, String voice) {
        ros.setState(State.IDLE);
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.clear();
        ros.setLastNavPoint("");
        ros.setCurrentDest("");
        ros.positionAutoUploadControl(false);
        view.showTaskFinishedView(result, prompt, voice);
    }

    private void onArrivedBirthdayTable(String dest) {
        this.target = null;
        LinkedHashSet<String> taskQueue = getCallingQueue();
        if (taskQueue != null) taskQueue.remove(dest);
        if (birthdayModeSetting.backgroundMusicPlayTime == 0) playBirthdayBackgroundMusic();
        if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.decreaseVolume();
        LightController instance = LightController.getInstance();
        instance.closeAll();
        mHandler.postDelayed(() -> instance.blink(0), 100);
        playBirthdayArrived();
        view.showArrivedAtBirthdayTable(dest);
        startCountDownTimer(false);
    }

    private void initObstaclePromptSettings() {
        String obstacleSettingStr = SpManager.getInstance().getString(Constants.KEY_OBSTACLE_CONFIG, null);
        if (TextUtils.isEmpty(obstacleSettingStr)) {
            obstacleSetting = ObstacleSetting.getDefault();
        } else {
            obstacleSetting = gson.fromJson(obstacleSettingStr, ObstacleSetting.class);
        }
    }

    @Override
    public void startTask(Context context, Intent intent) {
        SettingCreator<BaseSetting> typeAdapter = new SettingCreator<>();
        gson = new GsonBuilder()
                .registerTypeAdapter(BirthdayModeSetting.class, typeAdapter)
                .registerTypeAdapter(CruiseModeSetting.class, typeAdapter)
                .registerTypeAdapter(DeliveryMealSetting.class, typeAdapter)
                .registerTypeAdapter(MultiDeliverySetting.class, typeAdapter)
                .registerTypeAdapter(RecycleModeSetting.class, typeAdapter)
                .registerTypeAdapter(ObstacleSetting.class, typeAdapter)
                .create();
        if (navigationMode == Mode.FIX_ROUTE) {
            ros.positionAutoUploadControl(true);
            dispatchState = DispatchState.INIT;
        }
        if (taskMode == Constants.MODE_CALLING) {
            VoiceHelper.play("voice_receive_calling");
            target = intent.getStringExtra(Constants.TASK_TARGET);
            startTask(context, taskMode, target);
            view.showDeliveryView(target);
            return;
        }
        VoiceHelper.play("voice_task_start");
        if (taskMode == Constants.MODE_DELIVERY_FOOD || taskMode == Constants.MODE_RECYCLE_2) {
            level2Table = new TreeMap<>((Map<Integer, String>) intent.getSerializableExtra(Constants.TASK_TARGET));
            startTask(context, taskMode, level2Table);
            view.showDeliveryView(level2Table.firstEntry().getValue());
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            multiDeliveryTable = new TreeMap<>((Map<Integer, List<String>>) intent.getSerializableExtra(Constants.TASK_TARGET));
            startMultiDeliveryTask(context, taskMode, multiDeliveryTable);
            for (Map.Entry<Integer, List<String>> entry : multiDeliveryTable.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    view.showDeliveryView(entry.getValue().get(0));
                    break;
                }
            }
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            target = intent.getStringExtra(Constants.TASK_TARGET);
            startTask(context, taskMode, target);
            view.showDeliveryView(target);
        } else if (taskMode == Constants.MODE_CRUISE || taskMode == Constants.MODE_RECYCLE) {
            startTask(context, taskMode, (Route) (intent.getSerializableExtra(Constants.TASK_TARGET)));
            view.showDeliveryView(null);
        }
    }

    @Override
    public void startTask(Context context, int taskMode, TreeMap<Integer, String> level2Table) {
        this.taskMode = taskMode;
        this.level2Table = level2Table;
        this.originTable = new TreeMap<>(level2Table);
        this.mileage = 0;
        this.pickMealTime = 0;
        this.taskStartTime = System.currentTimeMillis();
        LightController.getInstance().openAll();
        initObstaclePromptSettings();
        if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            String deliverySettingStr = SpManager.getInstance().getString(Constants.KEY_DELIVERY_MODE_CONFIG, null);
            if (TextUtils.isEmpty(deliverySettingStr)) {
                deliveryMealSetting = DeliveryMealSetting.getDefault();
            } else {
                deliveryMealSetting = gson.fromJson(deliverySettingStr, DeliveryMealSetting.class);
            }
            XLog.w("开始普通配送任务: " + deliveryMealSetting.toString());
            ros.setNavSpeed(deliveryMealSetting.runningSpeed + "");
        } else if (taskMode == Constants.MODE_RECYCLE_2) {
            String recycleSettingStr = SpManager.getInstance().getString(Constants.KEY_RECYCLE_MODE_CONFIG, null);
            if (TextUtils.isEmpty(recycleSettingStr)) {
                recycleModeSetting = RecycleModeSetting.getDefault();
            } else {
                recycleModeSetting = gson.fromJson(recycleSettingStr, RecycleModeSetting.class);
            }
            XLog.w("开始回收任务: " + recycleModeSetting.toString());
            ros.setNavSpeed(recycleModeSetting.runningSpeed + "");
        }
        initLoopBroadcast();
        startTask(context, true, false);
    }

    @Override
    public void startMultiDeliveryTask(Context context, int taskMode, TreeMap<Integer, List<String>> multiDeliveryTable) {
        this.mileage = 0;
        this.pickMealTime = 0;
        this.taskStartTime = System.currentTimeMillis();
        this.taskMode = taskMode;
        this.multiDeliveryTable = multiDeliveryTable;
        this.originMultiDeliveryTable = new TreeMap<>();
        for (Map.Entry<Integer, List<String>> integerListEntry : this.multiDeliveryTable.entrySet()) {
            ArrayList<String> value = new ArrayList<>(integerListEntry.getValue());
            this.originMultiDeliveryTable.put(integerListEntry.getKey(), value);
        }
        LightController.getInstance().openAll();
        initObstaclePromptSettings();
        String multiDeliveryStr = SpManager.getInstance().getString(Constants.KEY_MULTI_DELIVERY_MODE_CONFIG, null);
        if (TextUtils.isEmpty(multiDeliveryStr)) {
            multiDeliverySetting = MultiDeliverySetting.getDefault();
        } else {
            multiDeliverySetting = gson.fromJson(multiDeliveryStr, MultiDeliverySetting.class);
        }
        XLog.w("开始随机送模式: " + multiDeliverySetting.toString());
        ros.setNavSpeed(multiDeliverySetting.runningSpeed + "");
        initLoopBroadcast();
        startTask(context, true, false);
    }

    public String startTask(Context context, boolean isStartTask, boolean isResumeFromPause) {
        String target = null;
        DestHelper destHelper = DestHelper.getInstance();
        if (taskMode == Constants.MODE_RECYCLE_2) {
            //回收模式
            if (level2Table.isEmpty()) {
                LightController.getInstance().openAll();
                playRecycleComplete(this::initRecycleLoopBroadcast);

                LinkedHashSet<String> taskQueue = getCallingQueue();
                if (taskQueue != null && taskQueue.iterator().hasNext()) {
                    return convertToCallingMode(taskQueue);
                }
                ros.setState(State.RETURNING);
                target = destHelper.getRecyclePoint();
                if (TextUtils.isEmpty(target)) {
                    onPointNotFound(context, 1);
                    return "";
                }
                navigationWithDispatch(target);
                return target;
            }
            ros.setState(State.RECYCLING);
            ros.setState(State.RECYCLING);
            LightController.getInstance().openAll();
            target = level2Table.firstEntry().getValue();
            if (!isStartTask) {
                playRecycleComplete(this::initRecycleLoopBroadcast);
            }
            navigationWithDispatch(target);
        } else if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            if (level2Table.isEmpty()) {
                LightController.getInstance().openAll();
                if (!isResumeFromPause) {
                    VoiceHelper.play("voice_going_to_target_table");
                }

                LinkedHashSet<String> taskQueue = getCallingQueue();
                if (taskQueue != null && taskQueue.iterator().hasNext()) {
                    return convertToCallingMode(taskQueue);
                }

                ros.setState(State.RETURNING);
                target = destHelper.getProductPoint();
                if (TextUtils.isEmpty(target)) {
                    onPointNotFound(context, 0);
                    return "";
                }
                navigationWithDispatch(target);
                return target;
            }
            ros.setState(State.DELIVERY);
            LightController.getInstance().openAll();
            target = level2Table.firstEntry().getValue();
            if (!isStartTask) {
                if (isResumeFromPause) {
                    playDeliveryBackgroundMusic();
                } else {
                    VoiceHelper.play("voice_going_to_target_table", () -> {
                        if (ros.getState() == State.DELIVERY) {
                            playDeliveryBackgroundMusic();
                        }
                    });
                }
            }
            navigationWithDispatch(target);
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            if (multiDeliveryTable.isEmpty()) {
                LightController.getInstance().openAll();
                if (!isResumeFromPause) {
                    VoiceHelper.play("voice_going_to_target_table");
                }

                LinkedHashSet<String> taskQueue = getCallingQueue();
                if (taskQueue != null && taskQueue.iterator().hasNext()) {
                    return convertToCallingMode(taskQueue);
                }

                ros.setState(State.RETURNING);
                target = destHelper.getProductPoint();
                if (TextUtils.isEmpty(target)) {
                    onPointNotFound(context, 0);
                    return "";
                }
                navigationWithDispatch(target);
                return target;
            }
            ros.setState(State.DELIVERY);
            LightController.getInstance().openAll();
            for (Map.Entry<Integer, List<String>> entry : multiDeliveryTable.entrySet()) {
                if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    target = entry.getValue().get(0);
                    break;
                }
            }
            if (!isStartTask) {
                if (isResumeFromPause) {
                    playMultiDeliveryBackgroundMusic();
                } else {
                    VoiceHelper.play("voice_going_to_target_table", () -> {
                        if (ros.getState() == State.DELIVERY) {
                            playMultiDeliveryBackgroundMusic();
                        }
                    });
                }
            }
            navigationWithDispatch(target);
        }
        return target;
    }

    private String convertToCallingMode(LinkedHashSet<String> taskQueue) {
        String target;
        lastTaskMode = taskMode;
        taskMode = Constants.MODE_CALLING;
        target = taskQueue.iterator().next();
        ros.setState(State.CALLING);
        navigationWithDispatch(target);
        return target;
    }

    @Override
    public void startTask(Context context, int taskMode, Route route) {
        this.taskMode = taskMode;
        this.route = route;
        LightController.getInstance().openAll();
        backupRoute = Route.clone(route, reverse);
        reverse = true;

        if (taskMode == Constants.MODE_CRUISE) {
            String cruiseModeSettingStr = SpManager.getInstance().getString(Constants.KEY_CRUISE_MODE_CONFIG, null);
            if (TextUtils.isEmpty(cruiseModeSettingStr)) {
                cruiseModeSetting = CruiseModeSetting.getDefault();
            } else {
                cruiseModeSetting = gson.fromJson(cruiseModeSettingStr, CruiseModeSetting.class);
            }
            XLog.w("开始巡航任务: " + cruiseModeSetting.toString());
            ros.setState(State.CRUISING);
            ros.setNavSpeed(cruiseModeSetting.runningSpeed + "");
        } else if (taskMode == Constants.MODE_RECYCLE) {
            String recycleSettingStr = SpManager.getInstance().getString(Constants.KEY_RECYCLE_MODE_CONFIG, null);
            if (TextUtils.isEmpty(recycleSettingStr)) {
                recycleModeSetting = RecycleModeSetting.getDefault();
            } else {
                recycleModeSetting = gson.fromJson(recycleSettingStr, RecycleModeSetting.class);
            }
            XLog.w("开始回收任务: " + recycleModeSetting.toString());
            ros.setState(State.RECYCLING);
            ros.setNavSpeed(recycleModeSetting.runningSpeed + "");
        }
        if (!route.pointList.isEmpty()) {
            ros.positionAutoUploadControl(true);
            mHandler.postDelayed(() -> {
                gotoNextRoutePoint(currentX, currentY);
                ros.setState(State.CRUISING);
            }, 400);
        }
        initLoopBroadcast();
    }

    @Override
    public void startTask(Context context, int taskMode, String target) {
        this.taskMode = taskMode;
        this.target = target;
        this.mileage = 0;
        this.pickMealTime = 0;
        this.taskStartTime = System.currentTimeMillis();
        LightController.getInstance().openAll();
        initObstaclePromptSettings();
        if (taskMode == Constants.MODE_BIRTHDAY) {
            String birthdayModeSettingStr = SpManager.getInstance().getString(Constants.KEY_BIRTHDAY_MODE_CONFIG, null);
            if (TextUtils.isEmpty(birthdayModeSettingStr)) {
                birthdayModeSetting = BirthdayModeSetting.getDefault();
            } else {
                birthdayModeSetting = gson.fromJson(birthdayModeSettingStr, BirthdayModeSetting.class);
            }
            XLog.w("开始生日配送任务:" + birthdayModeSetting.toString());
            ros.setState(State.BIRTHDAY);
            ros.setNavSpeed(birthdayModeSetting.runningSpeed + "");
            if (birthdayModeSetting.backgroundMusicPlayTime == 1) initLoopBroadcast();
        } else {
            XLog.w("开始呼叫任务");
            ros.setState(State.CALLING);
        }
        navigationWithDispatch(target);
    }

    private void initLoopBroadcast() {
        if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            playDeliveryBackgroundMusic();
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            playMultiDeliveryBackgroundMusic();
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            playBirthdayBackgroundMusic();
        } else if (taskMode == Constants.MODE_CRUISE) {
            playCruiseBackgroundMusic();
            if (!cruiseModeSetting.enableLoopBroadcast) return;
            loopRunnable = new Runnable() {
                @Override
                public void run() {
                    if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.decreaseVolume();
                    try {
                        List<Integer> targetLoopBroadcastPromptList = cruiseModeSetting.targetLoopBroadcastPromptList;
                        if (targetLoopBroadcastPromptList != null && !targetLoopBroadcastPromptList.isEmpty()) {
                            if (cruiseModeSetting.loopBroadcastPromptAudioList != null && !cruiseModeSetting.loopBroadcastPromptAudioList.isEmpty()) {
                                VoiceHelper.playFile(cruiseModeSetting.loopBroadcastPromptAudioList.get(targetLoopBroadcastPromptList.get(currentLoopIndex++ % targetLoopBroadcastPromptList.size())), new VoiceHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete() {
                                        if (MediaPlayerHelper.isPlaying())
                                            MediaPlayerHelper.resumeVolume();
                                        mHandler.postDelayed(loopRunnable, cruiseModeSetting.broadcastInterval * 1000);
                                    }
                                });
                            }
                        } else {
                            VoiceHelper.play("voice_default_cruise_broadcast", new VoiceHelper.OnCompleteListener() {
                                @Override
                                public void onComplete() {
                                    if (MediaPlayerHelper.isPlaying())
                                        MediaPlayerHelper.resumeVolume();
                                    mHandler.postDelayed(loopRunnable, cruiseModeSetting.broadcastInterval * 1000);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            initCruiseLoopBroadcast();
        } else if (taskMode == Constants.MODE_RECYCLE_2) {
            if (!recycleModeSetting.enableLoopBroadcast) return;
            loopRunnable = new Runnable() {
                @Override
                public void run() {
                    if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.decreaseVolume();
                    try {
                        List<Integer> targetLoopBroadcastPrompts = recycleModeSetting.targetLoopBroadcastPrompts;
                        if (targetLoopBroadcastPrompts != null && !targetLoopBroadcastPrompts.isEmpty()) {
                            if (recycleModeSetting.loopBroadcastPromptAudioList != null && !recycleModeSetting.loopBroadcastPromptAudioList.isEmpty()) {
                                VoiceHelper.playFile(recycleModeSetting.loopBroadcastPromptAudioList.get(targetLoopBroadcastPrompts.get(currentLoopIndex++ % targetLoopBroadcastPrompts.size())), new VoiceHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete() {
                                        if (MediaPlayerHelper.isPlaying())
                                            MediaPlayerHelper.resumeVolume();
                                        mHandler.postDelayed(loopRunnable, recycleModeSetting.broadcastInterval * 1000);
                                    }
                                });
                            }
                        } else {
                            VoiceHelper.play("voice_default_recycle_broadcast", new VoiceHelper.OnCompleteListener() {
                                @Override
                                public void onComplete() {
                                    if (MediaPlayerHelper.isPlaying())
                                        MediaPlayerHelper.resumeVolume();
                                    mHandler.postDelayed(loopRunnable, recycleModeSetting.broadcastInterval * 1000);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            initRecycleLoopBroadcast();
        } else if (taskMode == Constants.MODE_RECYCLE) {
            if (!recycleModeSetting.enableLoopBroadcast) return;
            loopRunnable = new Runnable() {
                @Override
                public void run() {
                    if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.decreaseVolume();
                    try {
                        List<Integer> targetLoopBroadcastPrompts = recycleModeSetting.targetLoopBroadcastPrompts;
                        if (targetLoopBroadcastPrompts != null && !targetLoopBroadcastPrompts.isEmpty()) {
                            if (recycleModeSetting.loopBroadcastPromptAudioList != null && !recycleModeSetting.loopBroadcastPromptAudioList.isEmpty()) {
                                VoiceHelper.playFile(recycleModeSetting.loopBroadcastPromptAudioList.get(targetLoopBroadcastPrompts.get(currentLoopIndex++ % targetLoopBroadcastPrompts.size())), new VoiceHelper.OnCompleteListener() {
                                    @Override
                                    public void onComplete() {
                                        if (MediaPlayerHelper.isPlaying())
                                            MediaPlayerHelper.resumeVolume();
                                        mHandler.postDelayed(loopRunnable, recycleModeSetting.broadcastInterval * 1000);
                                    }
                                });
                            }
                        } else {
                            VoiceHelper.play("voice_default_recycle_broadcast", new VoiceHelper.OnCompleteListener() {
                                @Override
                                public void onComplete() {
                                    if (MediaPlayerHelper.isPlaying())
                                        MediaPlayerHelper.resumeVolume();
                                    mHandler.postDelayed(loopRunnable, recycleModeSetting.broadcastInterval * 1000);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            initRecycleLoopBroadcast();
        }
    }

    private void playDeliveryBackgroundMusic() {
        if (!deliveryMealSetting.enableBackgroundMusic) return;
        if (deliveryMealSetting.backgroundMusicFileName != null && deliveryMealSetting.backgroundMusicPath != null) {
            MediaPlayerHelper.playFile(deliveryMealSetting.backgroundMusicPath, true);
        }
    }

    private void playMultiDeliveryBackgroundMusic() {
        if (!multiDeliverySetting.enableBackgroundMusic) return;
        if (multiDeliverySetting.backgroundMusicFileName != null && multiDeliverySetting.backgroundMusicPath != null) {
            MediaPlayerHelper.playFile(multiDeliverySetting.backgroundMusicPath, true);
        }
    }

    private void playCruiseBackgroundMusic() {
        if (!cruiseModeSetting.enableBackgroundMusic) return;
        if (cruiseModeSetting.backgroundMusicFileName != null) {
            if (cruiseModeSetting.backgroundMusicPath != null) {
                MediaPlayerHelper.playFile(cruiseModeSetting.backgroundMusicPath, true);
            } else if (Constants.DEFAULT_CRUISE_BACKGROUND_MUSIC.equals(cruiseModeSetting.backgroundMusicFileName)) {
                MediaPlayerHelper.playDefaultMusic(Constants.DEFAULT_CRUISE_BACKGROUND_MUSIC, true, null);
            } else {
                MediaPlayerHelper.play(cruiseModeSetting.backgroundMusicFileName.substring(0, cruiseModeSetting.backgroundMusicFileName.lastIndexOf(".")), true);
            }
        }
    }

    private void playBirthdayBackgroundMusic() {
        if (!birthdayModeSetting.enableBackgroundMusic) return;
        if (birthdayModeSetting.backgroundMusicFileName != null) {
            if (birthdayModeSetting.backgroundMusicPath != null) {
                MediaPlayerHelper.playFile(birthdayModeSetting.backgroundMusicPath, true);
            } else if (Constants.DEFAULT_BIRTHDAY_BACKGROUND_MUSIC.equals(birthdayModeSetting.backgroundMusicFileName)) {
                MediaPlayerHelper.playDefaultMusic(Constants.DEFAULT_BIRTHDAY_BACKGROUND_MUSIC, true, null);
            } else {
                MediaPlayerHelper.play(birthdayModeSetting.backgroundMusicFileName.substring(0, birthdayModeSetting.backgroundMusicFileName.lastIndexOf(".")), true);
            }
        }
    }

    private void initRecycleLoopBroadcast() {
        if (loopRunnable == null) return;
        mHandler.postDelayed(loopRunnable, recycleModeSetting.broadcastInterval * 1000);
    }

    private void initCruiseLoopBroadcast() {
        if (loopRunnable == null) return;
        mHandler.postDelayed(loopRunnable, cruiseModeSetting.broadcastInterval * 1000);
    }

    @Override
    public void onTaskTerminated(Context context) {
        resetCountdown();
        if (checkEmergencyStopState()) return;
        ros.setState(State.RETURNING);
        mHandler.removeCallbacks(loopRunnable);

        clearRemainingTask();

        LightController.getInstance().openAll();
        DestHelper destHelper = DestHelper.getInstance();
        if (taskMode == Constants.MODE_RECYCLE_2 || (taskMode == Constants.MODE_CALLING && lastTaskMode == Constants.MODE_RECYCLE_2)) {
            String recyclePoint = destHelper.getRecyclePoint();
            if (TextUtils.isEmpty(recyclePoint)) {
                onPointNotFound(context, 1);
                return;
            }
            navigationWithDispatch(recyclePoint);
            VoiceHelper.play("voice_okay_goto_recycling_point");
            view.showDeliveryView(recyclePoint);
        } else {
            String productPoint = destHelper.getProductPoint();
            if (TextUtils.isEmpty(productPoint)) {
                onPointNotFound(context, 0);
                return;
            }
            navigationWithDispatch(productPoint);
            VoiceHelper.play("voice_okay_goto_product_point");
            view.showDeliveryView(productPoint);
        }

        //返航，任务模式从呼叫变更为原始任务类型
        if (lastTaskMode != -1) {
            taskMode = lastTaskMode;
            lastTaskMode = -1;
        }
    }

    private void clearRemainingTask() {
        if (level2Table != null) {
            if (!level2Table.isEmpty()) {
                failedDeliveryMap = new TreeMap<>(level2Table);
            }
            level2Table.clear();
        }
        if (multiDeliveryTable != null) {
            if (!multiDeliveryTable.isEmpty()) {
                failedMultiDeliveryTable = new TreeMap<>();
                for (Map.Entry<Integer, List<String>> integerListEntry : this.multiDeliveryTable.entrySet()) {
                    ArrayList<String> value = new ArrayList<>(integerListEntry.getValue());
                    failedMultiDeliveryTable.put(integerListEntry.getKey(), value);
                }
            }
            multiDeliveryTable.clear();
        }
        if (target != null) {
            failedTarget = target;
            target = null;
        }
        if (route != null) {
            route = null;
        }
        if (getCallingQueue() != null) {
            getCallingQueue().clear();
        }
    }

    private void pauseAllAudio() {
        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
        if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.pause();
        mHandler.removeCallbacks(loopRunnable);
    }

    @Override
    public void onCancelTask(Context context) {
        resetCountdown();
        pauseAllAudio();
        clearRemainingTask();
        uploadTaskRecord(context);
        onTaskFinished(0, null, null);
    }

    private void uploadTaskRecord(Context context) {
        String hostname = Event.getOnHostnameEvent().hostname;
        if (TextUtils.isEmpty(hostname)) return;
        long uploadTime = System.currentTimeMillis();
        DeliveryRecord record;

        if (taskMode == Constants.MODE_CALLING) {
            taskMode = lastTaskMode;
        }

        Set<String> set = new HashSet<>();
        if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            if (failedDeliveryMap != null && !failedDeliveryMap.isEmpty()) {
                set.addAll(failedDeliveryMap.values());
            }
            record = new DeliveryRecord(1, originTable.size(), new HashSet<>(originTable.values()).size(), taskStartTime, uploadTime, uploadTime, mileage, pickMealTime, set.isEmpty() ? 0 : 1, set.toString(), WIFIUtils.getMacAddress(context), "v1.1", PackageUtils.getVersion(context));
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            record = new DeliveryRecord(2, 1, 1, taskStartTime, uploadTime, uploadTime, mileage, pickMealTime, TextUtils.isEmpty(failedTarget) ? 0 : 1, failedTarget, WIFIUtils.getMacAddress(context), "v1.1", PackageUtils.getVersion(context));
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            if (failedMultiDeliveryTable != null && !failedMultiDeliveryTable.isEmpty()) {
                for (List<String> value : failedMultiDeliveryTable.values()) {
                    set.addAll(value);
                }
            }
            Set<String> originTableSet = new HashSet<>();
            for (List<String> value : originMultiDeliveryTable.values()) {
                originTableSet.addAll(value);
            }
            record = new DeliveryRecord(4, originMultiDeliveryTable.size(), originTableSet.size(), taskStartTime, uploadTime, uploadTime, mileage, pickMealTime, set.isEmpty() ? 0 : 1, set.toString(), WIFIUtils.getMacAddress(context), "v1.1", PackageUtils.getVersion(context));
        } else if (taskMode == Constants.MODE_RECYCLE_2) {
            if (failedDeliveryMap != null && !failedDeliveryMap.isEmpty()) {
                set.addAll(failedDeliveryMap.values());
            }
            record = new DeliveryRecord(3, originTable.size(), new HashSet<>(originTable.values()).size(), taskStartTime, uploadTime, uploadTime, mileage, pickMealTime, set.isEmpty() ? 0 : 1, set.toString(), WIFIUtils.getMacAddress(context), "v1.1", PackageUtils.getVersion(context));
        } else {
            return;
        }

        XLog.w("上传任务记录" + gson.toJson(record));
        ServiceFactory
                .getRobotService()
                .reportTaskResult(API.taskRecordAPI(hostname), record)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Response>() {
                    @Override
                    public void accept(Response response) throws Throwable {
                        int code = response.code;
                        if (code == 0) {
                            com.reeman.ros.util.Log.w("上传任务成功");
                        } else {
                            dbRepository.addDeliveryRecord(record);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        Log.e("上传任务失败", throwable.getMessage());
                        dbRepository.addDeliveryRecord(record);
                    }
                });
    }

    private void resetCountdown() {
        if (pauseCountDownTimer != null) {
            pauseCountDownTimer.cancel();
            pauseCountDownTimer = null;
        }
    }

    @Override
    public void onDrop(Context context) {
        ros.stopMove();
        mHandler.removeCallbacksAndMessages(null);
        if (ros.isNavigating()) ros.cancelNavigation();
        stopDockChargingPile();
        ros.setState(State.PAUSE);
        this.isPauseCountdown = true;
        resetCountdown();
        pauseAllAudio();
        Notifier.notify(new Msg(NotifyConstant.TASK_NOTIFY, "防跌状态", "机器人任务过程中检测到跌落风险，请检查机器定位是否正确", Event.getOnHostnameEvent().hostname));
        view.showDropView(level2Table, multiDeliveryTable);
    }

    @Override
    public void onNavigationCancelResult(Context context, int code) {

    }

    @Override
    public void onNavigationCompleteResult(Context context, int code, String name, float mileage) {
        if (code == 0) {
            this.mileage += mileage;
            ros.setCurrentDest(name);
            handleNavSuccessResult(context, name);
        } else {
            XLog.w("导航失败，旋转重试");
            //巡航模式和回收模式按路线走的过程中导航失败，不旋转
            if (taskMode == Constants.MODE_CRUISE || taskMode == Constants.MODE_RECYCLE) {
                if (route != null && route.pointList != null && !route.pointList.isEmpty()) {
                    List<Double> coordinates = route.pointList.get(0);
                    ros.navigationByCoordinates(coordinates.get(0), coordinates.get(1), currentRouteAngle);
                    return;
                }
            }
            VoiceHelper.play("voice_mind_out_0");
            DestHelper destHelper = DestHelper.getInstance();
            String productPoint = destHelper.getProductPoint();
            String recyclePoint = destHelper.getRecyclePoint();
            String chargePoint = destHelper.getChargePoint();
            if (!TextUtils.isEmpty(destHelper.getChargePoint())
                    && destHelper.getChargePoint().equals(ros.getLastNavPoint())) {
                navigationWithDispatch(destHelper.getChargePoint());
                return;
            }
            if (taskMode == Constants.MODE_BIRTHDAY) {
                if (target == null) {
                    navigationWithDispatch(destHelper.getProductPoint());
                } else {
                    navigationWithDispatch(ros.getLastNavPoint());
                }
            } else if (taskMode == Constants.MODE_DELIVERY_FOOD) {
                if (level2Table != null && !level2Table.isEmpty()) {
                    navigationWithDispatch(ros.getLastNavPoint());
                } else {
                    if (TextUtils.isEmpty(productPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                    navigationWithDispatch(productPoint);
                }
            } else if (taskMode == Constants.MODE_RECYCLE) {
                if (!TextUtils.isEmpty(recyclePoint) && recyclePoint.equals(ros.getLastNavPoint())) {
                    navigationWithDispatch(recyclePoint);
                }
            } else if (taskMode == Constants.MODE_CRUISE) {
                if (!TextUtils.isEmpty(productPoint) && productPoint.equals(ros.getLastNavPoint())) {
                    navigationWithDispatch(productPoint);
                }
            } else if (taskMode == Constants.MODE_RECYCLE_2) {
                if (level2Table != null && !level2Table.isEmpty()) {
                    navigationWithDispatch(ros.getLastNavPoint());
                } else {
                    if (TextUtils.isEmpty(recyclePoint)) {
                        onPointNotFound(context, 1);
                        return;
                    }
                    navigationWithDispatch(recyclePoint);
                }
            } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
                if (multiDeliveryTable != null && !multiDeliveryTable.isEmpty()) {
                    for (Map.Entry<Integer, List<String>> entry : multiDeliveryTable.entrySet()) {
                        List<String> value = entry.getValue();
                        if (value != null && !value.isEmpty()) {
                            navigationWithDispatch(value.get(0));
                            break;
                        }
                    }
                } else {
                    if (TextUtils.isEmpty(productPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                    navigationWithDispatch(productPoint);
                }
            } else if (taskMode == Constants.MODE_CALLING) {
                if (target != null && getCallingQueue() != null && getCallingQueue().iterator().hasNext()) {
                    target = getCallingQueue().iterator().next();
                    navigationWithDispatch(target);
                } else {
                    if (TextUtils.isEmpty(productPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                    navigationWithDispatch(productPoint);
                }
            }
        }
    }

    @Override
    public void onNavigationStartResult(Context context, int code, String name) {
        if (code == 0) {
            if (ros.getState() == State.PAUSE) ros.cancelNavigation();
            return;
        }
        pauseAllAudio();
        String navigationStartError = Errors.getNavigationStartError(context, code);
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "发起导航失败", "导航结果:" + navigationStartError, Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, navigationStartError, "");
    }

    @Override
    public void onSensorsError(Context context, Event.OnCheckSensorsEvent event) {
        String hardwareError = Errors.getSensorError(context, event);
        if (hardwareError == null) return;
        stopDockChargingPile();
        ros.cancelNavigation();
        uploadHardwareError(event, hardwareError);
        Log.w("传感器异常");
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "传感器状态", "导航结果上报传感器异常:" + event.toString(), Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, hardwareError, "");
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
    public void onSpecialArea(String name) {
//        if (TextUtils.isEmpty(name)){
//            ros.setNavSpeed();
//        }
        if (navigationMode == Mode.FIX_ROUTE) {
            boolean pause = DispatchUtil.Companion.updateSpecialArea(name, mRobotInfo);
            if (pause) {
                view.pauseByDispatch(R.string.text_pause_by_special_area);
            }
        }
    }

    @Override
    public void onMissPose(Context context) {
        stopDockChargingPile();
        ros.cancelNavigation();
        pauseAllAudio();
        dispatchState = DispatchState.IGNORE;
        Log.w("定位异常");
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "定位异常", "定位异常", Event.getOnHostnameEvent().hostname));
        onTaskFinished(-1, context.getString(R.string.voice_miss_pose_task_finish), "voice_miss_pose_task_finish");
    }

    @Override
    public void onEmergencyStopTurnOn(Context context) {
        VoiceHelper.play("voice_scram_stop_turn_on");
        ros.stopMove();
        mHandler.removeCallbacksAndMessages(null);
        if (ros.isNavigating()) ros.cancelNavigation();
        stopDockChargingPile();
        ros.setState(State.PAUSE);
        this.isPauseCountdown = true;
        resetCountdown();
        pauseAllAudio();
        Notifier.notify(new Msg(NotifyConstant.TASK_NOTIFY, "急停状态", "任务过程中急停开关被按下", Event.getOnHostnameEvent().hostname));
        view.showEmergencyOnView(level2Table, multiDeliveryTable);
    }

    private void stopDockChargingPile() {
        if (ros.isDocking()) {
            mHandler.removeCallbacks(chargeRunnable);
            ros.cancelCharge();
            ros.setDockFailCount(0);
        }
    }

    @Override
    public void onEmergencyStopTurnOff() {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        VoiceHelper.play("voice_scram_stop_turn_off");
        view.showEmergencyStopTurnOffView();
    }

    @Override
    public void onTouch(Context context) {
        if (ros.isEmergencyStopDown()) return;
        if (pauseCountDownTimer == null) return;
        pauseCountDownTimer.cancel();
        pauseCountDownTimer = null;
        XLog.w("按下触摸键继续任务");
        onTaskResume(context, this.isPauseCountdown);
    }

    @Override
    public void onTimeStamp(Context context) {
        if (taskMode != Constants.MODE_CRUISE)
            return;

        //暂停中或者已经低电，不处理
        if (pauseCountDownTimer != null || isLowCharge()) return;

        //充电对接中或者急停
        if (ros.isDocking() || ros.isEmergencyStopDown())
            return;

        int currentPower = Event.getCoreData().battery;
        int lowPower = SpManager.getInstance().getInt(Constants.KEY_LOW_POWER, Constants.DEFAULT_LOW_POWER);

        //电量低
        if (currentPower <= lowPower) {
            XLog.w("任务中触发低电");
            target = null;
            lowCharge = true;
            route = null;
            mHandler.removeCallbacks(retryRunnable);
            ros.cancelNavigation();
            pauseAllAudio();
            VoiceHelper.play("voice_power_insufficient_and_goto_charge");
            ros.setState(State.RETURNING);
            String chargePoint = DestHelper.getInstance().getChargePoint();
            if (TextUtils.isEmpty(chargePoint)) {
                onChargingPileNotFound(context);
                return;
            }
            navigationWithDispatch(chargePoint);
        }
    }

    @Override
    public void onEncounterObstacle() {
        if (obstacleSetting == null || !obstacleSetting.enableObstaclePrompt
                || taskMode == Constants.MODE_CRUISE
                || taskMode == Constants.MODE_RECYCLE
                || taskMode == Constants.MODE_RECYCLE_2
                || System.currentTimeMillis() - lastObstaclePromptPlaybackTimeMills < 3000
                || VoiceHelper.isPlaying())
            return;
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始定时器
     */
    private void startCountDownTimer(boolean isPauseCountdown) {
        this.isPauseCountdown = isPauseCountdown;
        if (pauseCountDownTimer == null) {
            int count;
            if (isPauseCountdown) {
                count = 15000;
            } else {
                if (taskMode == Constants.MODE_BIRTHDAY) {
                    count = birthdayModeSetting.pauseTime * 1000;
                } else if (taskMode == Constants.MODE_DELIVERY_FOOD) {
                    count = deliveryMealSetting.pauseTime * 1000;
                } else if (taskMode == Constants.MODE_RECYCLE_2) {
                    count = recycleModeSetting.pauseTime * 1000;
                } else if (taskMode == Constants.MODE_RECYCLE) {
                    count = recycleModeSetting.pauseTime * 1000;
                } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
                    count = multiDeliverySetting.pauseTime * 1000;
                } else {
                    count = 30 * 1000;
                }
            }
            pauseCountDownTimer = new CountDownTimer(count, 1000) {
                @Override
                public void onTick(long mills) {
                    addPickMealCountdownTimer(isPauseCountdown);
                    view.onPauseCountDown(isPauseCountdown, mills / 1000);
                }

                @Override
                public void onFinish() {
                    addPickMealCountdownTimer(isPauseCountdown);
                    pauseCountDownTimer = null;
                    view.onCountDownFinished(isPauseCountdown);
                }
            };
        }
        pauseCountDownTimer.start();
        ros.setState(State.PAUSE);
    }

    private void addPickMealCountdownTimer(boolean isPauseCountdown) {
        if (!isPauseCountdown && (taskMode == Constants.MODE_BIRTHDAY ||
                taskMode == Constants.MODE_DELIVERY_FOOD ||
                taskMode == Constants.MODE_MULTI_DELIVERY ||
                taskMode == Constants.MODE_RECYCLE_2)) {
            pickMealTime++;
        }
    }

    @Override
    public void onPositionObtained(Context context, double[] position) {
        if (
                navigationMode == Mode.FIX_ROUTE
                        && ros.getState() != State.PAUSE
                        && ros.getState() != State.CHARGING
                        && ros.getState() != State.IDLE
        ) {
            if (
                    dispatchState == DispatchState.INIT
                            && DispatchUtil.Companion.isCloseToTargetPoint()
            ) {
                if (DispatchUtil.Companion.isTargetPointOccupied()) {
                    view.pauseByDispatch(R.string.text_target_point_occupied);
                }
            }
            return;
        }
        currentX = position[0];
        currentY = position[1];
        currentZ = position[2];

        State state = ros.getState();
        if (state == State.CRUISING && isPathStart) {
            if (route != null && route.pointList != null && !route.pointList.isEmpty()) {
                //转折点或者最后一个点必须到达
                if (isTurningPoint || route.pointList.size() == 1) {
                    return;
                }

                //其它点可以提前上报到达
                List<Double> point1 = route.pointList.get(0);
                double calcDistance = Math.sqrt(Math.pow(currentX - point1.get(0), 2) + Math.pow(currentY - point1.get(1), 2));
                if (calcDistance < 0.3) {
                    XLog.w(point1.get(0) + " " + point1.get(1) + "提前上报到达");
                    route.pointList.remove(0);
                    gotoNextRoutePoint(currentX, currentY);
                }
            }
        }
    }

    @Override
    public void onCustomResponseCallingEvent(String next) {
        pauseAllAudio();
        VoiceHelper.play("voice_receive_calling");
        if (taskMode != Constants.MODE_CALLING) {
            lastTaskMode = taskMode;
        }
        taskMode = Constants.MODE_CALLING;
        target = next;
        ros.setState(State.CALLING);
        mHandler.removeCallbacks(retryRunnable);
        ros.cancelNavigation();
        mHandler.postDelayed(() -> {
            if (ros.getState() == State.CALLING) {
                navigationWithDispatch(target);
            }
        }, 3000);
        view.showDeliveryView(target);
    }

    @Override
    public void onGlobalPathEvent(Double[] path) {
        if (taskMode != Constants.MODE_CRUISE && taskMode != Constants.MODE_RECYCLE)
            return;

        if (ros.getState() == State.PAUSE) return;

        if (!isPathStart || route == null) return;

//        String[] s = path.split(" ");

        if (path.length <= 2) return;

        if (route.pointList.isEmpty()) return;

        List<Double> floats = route.pointList.get(0);

        //当前位置与下一个目标点形成的直线的角度
        double targetAngle = calcAngle(currentX, floats.get(0), currentY, floats.get(1));

        Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                        List<Pair<Double, Double>> basePoints = getPoint(currentX, currentY, path);

                        if (basePoints.isEmpty()) return;

                        int count = 0;
                        for (Pair<Double, Double> temp : basePoints) {
                            count++;
                            if (Math.sqrt(Math.pow(currentX - temp.first, 2) + Math.pow(currentY - temp.second, 2)) < 0.5) {
                                continue;
                            }

                            double currentAngle = calcAngle(currentX, temp.first, currentY, temp.second);

                            double angle = Math.abs(currentAngle - targetAngle);

                            double result = Math.min(angle, 360 - angle);

//                            if (result > 30) {
//                                XLog.w("当前第" + count + "个点，" + "当前角度:" + currentAngle + " 路径的角度：" + currentRouteAngle + " 角度相差: " + result + " 取消导航");
//                                emitter.onNext(false);
//                                emitter.onComplete();
//                                return;
//                            }
                        }
                        emitter.onNext(true);
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Boolean canNavigation) {
                        if (!canNavigation) {
                            ros.cancelNavigation();
                            retryRunnable = () -> {
                                if (ros.isEmergencyStopDown())
                                    return;
                                ros.navigationByCoordinates(floats.get(0), floats.get(1), currentRouteAngle);
                            };
                            if (++cancelCount >= 3) {
                                double currentAngle = currentZ > 0 ? currentZ : currentZ + 360;
                                double nextRouteAngle = currentRouteAngle > 0 ? currentRouteAngle : currentRouteAngle + 360;
                                double diffAngle = nextRouteAngle - currentAngle;
                                cancelCount = 0;
                                if (Math.abs(diffAngle) > 10) {
                                    ros.turn(diffAngle);
                                    mHandler.postDelayed(retryRunnable, 3000);
                                    return;
                                }
                            }
                            mHandler.postDelayed(retryRunnable, 1000);
                        } else {
                            cancelCount = 0;
                            XLog.w("执行导航");
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public List<Pair<Double, Double>> getPoint(double currentX, double currentY, Double[] s) {
        double minDistance = Integer.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i <= s.length - 2; i += 2) {
            double dis = Math.sqrt(Math.pow(currentX - s[i], 2) + Math.pow(currentY - s[i + 1], 2));
            if (minDistance > dis) {
                minDistance = dis;
                minIndex = i;
            }
        }

        XLog.w("从" + minIndex + "开始取点");

        List<Pair<Double, Double>> list = new ArrayList<>();

        int length = Math.min(s.length, minIndex + 10);
        for (int i = minIndex; i <= length - 2; i += 2) {
            list.add(new Pair<>(s[i], s[i + 1]));
        }
        return list;
    }

    public static float calcAngle(double x1, double x2, double y1, double y2) {
        double xDiff = x2 - x1;
        double yDiff = y2 - y1;
        return (float) (Math.atan2(yDiff, xDiff) * (180 / Math.PI));
    }

    @Override
    public void onTaskPause(boolean isPauseCountdown) {
        view.showTaskPauseView(level2Table, multiDeliveryTable);
        ros.stopMove();
        ros.cancelNavigation();
        mHandler.removeCallbacks(retryRunnable);
        stopDockChargingPile();
        startCountDownTimer(isPauseCountdown);
        pauseAllAudio();
        if (taskMode == Constants.MODE_RECYCLE || taskMode == Constants.MODE_RECYCLE_2) {
            playPlaceRecyclables();
        }
    }

    @Override
    public void onPickMealInAdvance() {
        resetCountdown();
        if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            onArrivedAtTargetTable(ros.getLastNavPoint());
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            onArriveMultiDeliveryTarget(ros.getLastNavPoint());
        }
    }

    @Override
    public void onTaskContinue(Context context) {
        resetCountdown();
        onTaskResume(context, isPauseCountdown);
    }

    private boolean checkEmergencyStopState() {
        if (ros.isEmergencyStopDown()) {
            view.showEmergencyOnView(level2Table, multiDeliveryTable);
            return true;
        }
        return false;
    }

    @Override
    public void onNextTable(Context context) {
        resetCountdown();
        onTaskResume(context, false);
    }

    @Override
    public void onCompleteTask(Context context) {
        resetCountdown();
        onTaskResume(context, false);
    }

    @Override
    public void onTaskResume(Context context, boolean isResumeFromPause) {
        if (checkEmergencyStopState()) return;
        String targetPoint = null;
        DestHelper destHelper = DestHelper.getInstance();
        if (taskMode == Constants.MODE_DELIVERY_FOOD || taskMode == Constants.MODE_MULTI_DELIVERY || taskMode == Constants.MODE_RECYCLE_2) {
            targetPoint = startTask(context, false, isResumeFromPause);
            if (targetPoint.equals("")) return;
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            if (target == null) {
                LightController.getInstance().openAll();
                if (MediaPlayerHelper.isPlaying()) MediaPlayerHelper.pause();
                if (!isResumeFromPause) {
                    playBirthdayPickMealComplete();
                }

                LinkedHashSet<String> taskQueue = getCallingQueue();
                if (taskQueue != null && taskQueue.iterator().hasNext()) {
                    taskMode = Constants.MODE_CALLING;
                    target = taskQueue.iterator().next();
                    targetPoint = target;
                    ros.setState(State.CALLING);
                    navigationWithDispatch(target);
                } else {
                    ros.setState(State.RETURNING);
                    targetPoint = destHelper.getProductPoint();
                    if (TextUtils.isEmpty(targetPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                    navigationWithDispatch(targetPoint);
                }
            } else {
                targetPoint = target;
                navigationWithDispatch(targetPoint);
                ros.setState(State.BIRTHDAY);
                if (birthdayModeSetting.backgroundMusicPlayTime == 1) playBirthdayBackgroundMusic();
            }
        } else if (taskMode == Constants.MODE_CRUISE) {
            if (isLowCharge()) {
                ros.setState(State.RETURNING);
                String chargePoint = destHelper.getChargePoint();
                if (TextUtils.isEmpty(chargePoint)) {
                    onChargingPileNotFound(context);
                    return;
                }
                navigationWithDispatch(chargePoint);
                view.showDeliveryView(null);
                return;
            }
            if (route == null) {

                LinkedHashSet<String> taskQueue = getCallingQueue();
                if (taskQueue != null && taskQueue.iterator().hasNext()) {
                    lastTaskMode = taskMode;
                    taskMode = Constants.MODE_CALLING;
                    target = taskQueue.iterator().next();
                    ros.setState(State.CALLING);
                    navigateToPoint(target);
                } else {
                    ros.setState(State.RETURNING);
                    targetPoint = destHelper.getProductPoint();
                    if (TextUtils.isEmpty(targetPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                    navigateToPoint(targetPoint);
                }
            } else {
                if (route.pointList != null && !route.pointList.isEmpty()) {
                    ros.setState(State.CRUISING);
                    List<Double> coordinates = route.pointList.get(0);
                    ros.navigationByCoordinates(coordinates.get(0), coordinates.get(1), currentRouteAngle);
                    playCruiseBackgroundMusic();
                    initCruiseLoopBroadcast();
                }
            }
        } else if (taskMode == Constants.MODE_CALLING) {
            Iterator<String> iterator = getCallingQueue().iterator();
            if (getCallingQueue() != null && iterator.hasNext()) {
                targetPoint = iterator.next();
                target = targetPoint;
                ros.setState(State.CALLING);
                navigationWithDispatch(targetPoint);
            } else if (lastTaskMode == Constants.MODE_CRUISE) {
                lastTaskMode = -1;
                taskMode = Constants.MODE_CRUISE;
                if (route == null) {
                    ros.setState(State.RETURNING);
                    targetPoint = destHelper.getProductPoint();
                    if (TextUtils.isEmpty(targetPoint)) {
                        onPointNotFound(context, 0);
                        return;
                    }
                } else {
                    isPathStart = false;
                    playCruiseBackgroundMusic();
                    initCruiseLoopBroadcast();
                    ros.setState(State.CRUISING);
                    route = Route.clone(backupRoute, false);
                    reverse = true;
                    gotoNextRoutePoint(currentX, currentY);
                }
            } else if (lastTaskMode == Constants.MODE_RECYCLE_2) {
                lastTaskMode = -1;
                taskMode = Constants.MODE_RECYCLE_2;
                targetPoint = destHelper.getProductPoint();
                if (TextUtils.isEmpty(targetPoint)) {
                    onPointNotFound(context, 0);
                    return;
                }
                ros.setState(State.RETURNING);
                navigationWithDispatch(targetPoint);
            } else if (lastTaskMode == -1) {
                targetPoint = destHelper.getProductPoint();
                if (TextUtils.isEmpty(targetPoint)) {
                    onPointNotFound(context, 0);
                    return;
                }
                ros.setState(State.RETURNING);
                navigationWithDispatch(targetPoint);
            } else {
                taskMode = lastTaskMode;
                lastTaskMode = -1;
                targetPoint = destHelper.getProductPoint();
                if (TextUtils.isEmpty(targetPoint)) {
                    onPointNotFound(context, 0);
                    return;
                }
                ros.setState(State.RETURNING);
                navigationWithDispatch(targetPoint);
            }
        }
        view.showDeliveryView(targetPoint);
    }

    @Override
    public void onPointNotFound(Context context) {
        pauseAllAudio();
        clearRemainingTask();
        uploadTaskRecord(context);
        onTaskFinished(-1, context.getString(R.string.voice_not_found_target_point), "voice_not_found_target_point");
    }

    /**
     * @param context
     * @param pointType 0:出品点
     *                  1:回收点
     */
    private void onPointNotFound(Context context, int pointType) {
        pauseAllAudio();
        clearRemainingTask();
        uploadTaskRecord(context);
        if (pointType == 0) {
            onTaskFinished(-1, context.getString(R.string.voice_not_found_product_point), "voice_not_found_target_point");
        } else {
            onTaskFinished(-1, context.getString(R.string.voice_not_found_recycle_point), "voice_not_found_target_point");
        }
    }

    @Override
    public void onChargingPileNotFound(Context context) {
        pauseAllAudio();
        onTaskFinished(-1, context.getString(R.string.voice_not_found_charging_pile), "voice_not_found_charging_pile");
    }

    @Override
    public void onDockFailed(Context context) {
        if (ros.getDockFailCount() < 2) {
            VoiceHelper.play("voice_retry_charge");
            chargeRunnable = new Runnable() {
                @Override
                public void run() {
                    if (ros.isEmergencyStopDown())
                        return;
                    ros.setDockFailCount(ros.getDockFailCount() + 1);
                    String chargePoint = DestHelper.getInstance().getChargePoint();
                    if (TextUtils.isEmpty(chargePoint)) {
                        onChargingPileNotFound(context);
                        return;
                    }
                    navigationWithDispatch(chargePoint);
                }
            };
            mHandler.postDelayed(chargeRunnable, 4000);
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

    private void playRecycleComplete(VoiceHelper.OnCompleteListener listener) {
        if (!recycleModeSetting.enableRecycleCompletePrompt) return;
        try {
            if (recycleModeSetting.targetRecycleCompletePrompts != null && !recycleModeSetting.targetRecycleCompletePrompts.isEmpty()
                    && recycleModeSetting.recycleCompletePromptAudioList != null && !recycleModeSetting.recycleCompletePromptAudioList.isEmpty()) {
                VoiceHelper.playFile(recycleModeSetting.recycleCompletePromptAudioList.get(recycleModeSetting.targetRecycleCompletePrompts.get(0)), listener);
            } else {
                VoiceHelper.play("voice_default_recycle_complete", listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playPlaceRecyclables() {
        if (!recycleModeSetting.enablePlaceRecyclablesPrompt) return;
        try {
            if (recycleModeSetting.targetPlaceRecyclablePrompt != null && !recycleModeSetting.targetPlaceRecyclablePrompt.isEmpty()
                    && recycleModeSetting.placeRecyclablePromptAudioList != null && !recycleModeSetting.placeRecyclablePromptAudioList.isEmpty()) {
                VoiceHelper.playFile(recycleModeSetting.placeRecyclablePromptAudioList.get(recycleModeSetting.targetPlaceRecyclablePrompt.get(0)));
            } else {
                VoiceHelper.play("voice_default_recycle_place_recyclables");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playBirthdayArrived() {
        if (!birthdayModeSetting.enablePickMealPrompt) return;
        try {
            if (birthdayModeSetting.targetPickMealPrompt != null && !birthdayModeSetting.targetPickMealPrompt.isEmpty()
                    && birthdayModeSetting.pickMealPromptAudioList != null && !birthdayModeSetting.pickMealPromptAudioList.isEmpty()) {
                VoiceHelper.playFile(birthdayModeSetting.pickMealPromptAudioList.get(birthdayModeSetting.targetPickMealPrompt.get(0)), () -> {
                    if (MediaPlayerHelper.isPlaying()) {
                        MediaPlayerHelper.resumeVolume();
                    }
                });
            } else {
                VoiceHelper.play("voice_default_birthday_pick_meal", () -> {
                    if (MediaPlayerHelper.isPlaying()) {
                        MediaPlayerHelper.resumeVolume();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playBirthdayPickMealComplete() {
        if (!birthdayModeSetting.enablePickMealCompletePrompt) return;
        try {
            if (birthdayModeSetting.targetPickMealCompletePrompt != null && !birthdayModeSetting.targetPickMealCompletePrompt.isEmpty()
                    && birthdayModeSetting.pickMealCompletePromptAudioList != null && !birthdayModeSetting.pickMealCompletePromptAudioList.isEmpty()) {
                VoiceHelper.playFile(birthdayModeSetting.pickMealCompletePromptAudioList.get(birthdayModeSetting.targetPickMealCompletePrompt.get(0)));
            } else {
                VoiceHelper.play("voice_default_birthday_pick_meal_complete");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
