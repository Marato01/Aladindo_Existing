package com.reeman.delige.base;

import static com.reeman.delige.base.BaseApplication.activityStack;
import static com.reeman.delige.base.BaseApplication.addToCallingQueue;
import static com.reeman.delige.base.BaseApplication.dbRepository;
import static com.reeman.delige.base.BaseApplication.dispatchState;
import static com.reeman.delige.base.BaseApplication.getCallingQueue;
import static com.reeman.delige.base.BaseApplication.mApp;
import static com.reeman.delige.base.BaseApplication.mRobotInfo;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;

import static com.reeman.delige.base.BaseApplication.ros;
import static com.reeman.delige.presenter.impl.MainPresenter.REQUEST_CODE_FOR_TASK;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.reeman.delige.BuildConfig;
import com.reeman.delige.R;
import com.reeman.delige.activities.MainActivity;
import com.reeman.delige.activities.ReturningActivity;
import com.reeman.delige.activities.TaskExecutingActivity;
import com.reeman.delige.constants.BuglyConstants;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.constants.State;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.light.LightController;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.navigation.filter.ROSFilter;
import com.reeman.delige.repository.entities.DeliveryRecord;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.ChargeRecord;
import com.reeman.delige.request.model.Msg;
import com.reeman.delige.request.model.PathPoint;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.PointInfo;
import com.reeman.delige.request.notifier.Notifier;
import com.reeman.delige.request.notifier.NotifyConstant;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.LocaleUtil;
import com.reeman.delige.utils.PointUtil;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SoftKeyboardStateWatcher;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity implements
        SoftKeyboardStateWatcher.SoftKeyboardStateListener, View.OnClickListener {

    protected Map<Integer, View> views;
    private SoftKeyboardStateWatcher softKeyboardStateWatcher;
    public static final int REQUEST_CODE_FOR_CHARGE = 1001;
    public static final int REQUEST_CODE_FOR_PRODUCT_POINT = 1002;

    private static View mView;
    private static GifImageView chargingView;
    private static boolean isChargingViewShowing = false;
    protected static final Handler mHandler = new Handler(Looper.getMainLooper());
    protected static boolean isSelfCheck = false;
    private int relocCount = 0;
    private static long chargingViewShowTime = 0;


    public static final Runnable chargeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isChargingViewShowing
                    && ros.isCharging()
                    && !activityStack.isEmpty()
                    && activityStack.get(activityStack.size() - 1) instanceof MainActivity
                    && !EasyDialog.isShow()) {
                showChargingView(mApp);
                return;
            }
            mHandler.postDelayed(chargeRunnable, 10000);
        }
    };

    //是否应该响应电量低和定时任务事件
    protected boolean shouldResponse2TimeEvent() {
        return false;
    }

    public static void startup(Context context, Class<? extends Activity> clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    public static void startupAndClearStack(Context context, Class<? extends Activity> clazz) {
        Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        softKeyboardStateWatcher = new SoftKeyboardStateWatcher(getWindow().getDecorView());
        softKeyboardStateWatcher.addSoftKeyboardStateListener(this);
        Log.w("xuedong", this + " onCreate");
        activityStack.add(this);
        if (disableBottomNavigationBar()) {
            ScreenUtils.hideBottomUIMenu(this);
        } else {
            ScreenUtils.setImmersive(this);
        }
        int languageType = SpManager.getInstance().getInt(Constants.KEY_LANGUAGE_TYPE, Constants.DEFAULT_LANGUAGE_TYPE);
        if (languageType != -1 && languageType != LocaleUtil.getLocaleType()) {
            LocaleUtil.changeAppLanguage(getResources(), languageType);
            BuglyConstants.updateBuglyStrings(getResources());
        }
        initData();
        setContentView(getLayoutRes());
        initView();
    }


    private void initView() {
        views = new HashMap<>();
        initCustomView();
    }

    protected abstract int getLayoutRes();

    protected boolean disableBottomNavigationBar() {
        return false;
    }

    protected abstract void initCustomView();

    protected void initData() {
    }

    public <T extends View> T $(@IdRes int id) {
        View view = views.get(id);
        if (view != null) return (T) view;
        View targetView = findViewById(id);
        views.put(id, targetView);
        return (T) targetView;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("xuedong", this + " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        Log.w("xuedong", this + " onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        Log.w("xuedong", this + " onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("xuedong", this + " onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        views.clear();
        views = null;
        softKeyboardStateWatcher.removeSoftKeyboardStateListener(this);
        activityStack.remove(this);
        Log.w("xuedong", this + " onDestroy");
    }


    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {

    }

    @Override
    public void onSoftKeyboardClosed() {

    }

    protected void setOnClickListeners(int... ids) {
        for (int id : ids) {
            View view = views.get(id);
            if (view == null) {
                view = findViewById(id);
                views.put(id, view);
            }
            if (view != null) view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWaypointUpdateEvent(Event.OnWaypointUpdateEvent event) {
        onWaypointUpdate(event);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRobotInfoUpdate(RobotInfo robotInfo) {
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCoreDataEvent(Event.OnCoreDataEvent event) {
//        XLog.w("core_____" + event.rawData);
        if (ROSFilter.isCoreDataDiff(event.rawData)) {
            onCoreData(event);
        }
    }

    protected void onWaypointUpdate(Event.OnWaypointUpdateEvent event) {
        Observable
                .create(new ObservableOnSubscribe<List<Point>>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<Point>> emitter) throws Throwable {
                        try {
                            String hostIp = Event.getIpEvent().ipAddress;
                            retrofit2.Response<Map<String, List<Point>>> response;

                            response = ServiceFactory.getRobotService().fetchPoints(API.fetchPointAPI(hostIp)).execute();
                            Map<String, List<Point>> map = response.body();
                            if (map == null || map.size() == 0) {
                                throw new ConnectException();
                            }
                            List<Point> waypoints = map.get("waypoints");
                            XLog.w(waypoints);
                            //将餐位缓存下来
                            SpManager.getInstance().edit().putString(Constants.KEY_POINT_INFO, new Gson().toJson(waypoints)).apply();

                            PointUtil.checkPoints(emitter, waypoints);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<Point>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<Point> points) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void onCoreData(Event.OnCoreDataEvent event) {
        if (ros.getLevel() == 0) {
            ros.setLevel(event.battery);
            if (event.charger == 2 || event.charger == 3) {
                ros.setCharge(event.battery, 4);
            } else {
                ros.setLastChargeStartTime(System.currentTimeMillis());
                ros.setLastPowerLevel(event.battery);
            }
        }

        if (ROSFilter.isScramStateDiff(event.button)) {
            detailEmergencyStop(event.button);
        }
        if (ROSFilter.isAntiFallDiff(event.cliff) && (event.cliff == 1)) {
            onCustomDropEvent();
            com.reeman.ros.util.Log.w("触发防跌");
            dispatchState = DispatchState.IGNORE;
        }

        if (ROSFilter.isChargeStateDiff(event.charger)) {
            onCustomBatteryChange(event.battery, event.charger);
            if (ros.isCharging() && event.charger == 1) {
                hideChargingView();
                ros.setCharge(ROS.CS.NOT_CHARGE);
                if (ros.getState() == State.CHARGING) {
                    ros.setState(State.IDLE);
                }
                if (event.button == ROS.ES.SWITCH_UP) {
                    dispatchState = DispatchState.INIT;
                }
            } else if (ros.getCharge() != ROS.CS.NOT_CHARGE && event.charger == 1) {
                ros.setCharge(ROS.CS.NOT_CHARGE);
                if (event.button == ROS.ES.SWITCH_UP) {
                    dispatchState = DispatchState.INIT;
                }
                onCustomPowerDisconnected();
            } else if (!ros.isCharging() && (event.charger == 2 || event.charger == 3)) {
                ros.setCharge(ROS.CS.CHARGING);
                ros.setCharge(event.battery, 4);
                showChargingView(this);
                onCustomPowerConnected();
                uploadTaskRecord();
                LightController.getInstance().closeAll();
                dispatchState = DispatchState.IGNORE;
            } else if (ros.getCharge() != ROS.CS.DOCKING && event.charger == 8) {
                ros.setCharge(ROS.CS.DOCKING);

            } else if (ros.getCharge() != ROS.CS.CHARGE_FAILURE && event.charger > 8) {
                ros.setCharge(ROS.CS.CHARGE_FAILURE);
                XLog.w("充电对接失败" + event.rawData);
                onCustomDockFailed();
            }
            return;
        }
        if (ROSFilter.isPowerLevelDiff(event.battery)) {
            ros.setLevel(event.battery);
            onCustomBatteryChange(event.battery, event.charger);
        }
    }

    protected void detailEmergencyStop(int button) {
        if (ros.getEmergencyStop() == -1) {
            ros.setEmergencyStop(button);
            return;
        }
        if (button == ROS.ES.SWITCH_DOWN) {//急停开关按下
            mHandler.removeCallbacks(chargeRunnable);
            dispatchState = DispatchState.IGNORE;
            com.reeman.ros.util.Log.w("按下急停");
        } else if (button == ROS.ES.SWITCH_UP) {
            dispatchState = DispatchState.INIT;
            com.reeman.ros.util.Log.w("打开急停");
        }
        ros.setEmergencyStop(button);
        onCustomEmergencyStopStateChange(button);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEncounterObstacleEvent(Event.OnEncounterObstacleEvent event) {
        onCustomEncounterObstacle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallingEvent(RobotEvent.OnCallingEvent event) {
        XLog.w("收到任务" + event.target);

        if (ros.isEmergencyStopDown()) {
            VoiceHelper.play("voice_scram_stop_turn_on");
            return;
        }

        if (ros.isDocking()) return;

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = registerReceiver(null, intentFilter);
        int powerLevel = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int lowPower = SpManager.getInstance().getInt(Constants.KEY_LOW_POWER, Constants.DEFAULT_LOW_POWER);
        if (powerLevel < lowPower) return;

        if (!shouldResponse2CallingEvent()) return;

        if (event.target != null && !event.target.equals(ros.getCurrentDest()) && !event.target.equals(ros.getLastNavPoint())) {
            addToCallingQueue(event.target);
        }

        if (getCallingQueue() != null &&
                getCallingQueue().iterator().hasNext()) {
            onCustomResponseCallingEvent(getCallingQueue().iterator().next());
        }
    }

    protected void onCustomResponseCallingEvent(String next) {
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        Intent intent = new Intent(this, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, next);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_CALLING);
        this.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    protected boolean shouldResponse2CallingEvent() {
        return false;
    }

    //自定义遭遇障碍物
    protected void onCustomEncounterObstacle() {

    }

    //子类自定义充电处理逻辑，默认关闭充电对接提醒
    protected void onCustomPowerConnected() {
    }

    //子类自定义断电处理逻辑
    protected void onCustomPowerDisconnected() {
        LightController.getInstance().openAll();
        String hostname = Event.getOnHostnameEvent().hostname;
        if (TextUtils.isEmpty(hostname)) return;
        String path = API.batteryLogAPI(hostname);
        long timeMills = System.currentTimeMillis();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatusIntent = registerReceiver(null, intentFilter);
        int powerLevel = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        ServiceFactory
                .getRobotService()
                .reportChargeResult(path,
                        new ChargeRecord(
                                ros.getLastPowerLevel(),
                                powerLevel,
                                ros.getLastChargeStartTime(),
                                timeMills,
                                ros.getLastChargeType(),
                                0,
                                0,
                                0,
                                0,
                                timeMills,
                                BaseApplication.macAddress,
                                "v1.1",
                                BaseApplication.appVersion)
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Map<String, Object>>() {
                    @Override
                    public void accept(Map<String, Object> map) throws Throwable {
                        Log.w("xuedong", "充电记录上传成功");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        XLog.w("上传充电记录失败：" + throwable.getMessage());
                    }
                });
    }

    //子类自定义电量变化处理逻辑
    protected void onCustomBatteryChange(int level, int plug) {

    }

    //Android网络变化
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkStateChange(RobotEvent.OnNetworkEvent event) {
        onCustomNetworkStateChange(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPowerOffEvent(Event.OnPowerOffEvent event) {
        com.reeman.ros.util.Log.w("关机 :" + event.result);
        try {
            Runtime.getRuntime().exec("reboot -p");
        } catch (Exception e) {

        }
    }

    protected void onCustomNetworkStateChange(RobotEvent.OnNetworkEvent event) {

    }

    //子类自定义急停开关处理逻辑，默认关闭提示窗口
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {
        if (emergencyStopState == 0) {
            VoiceHelper.play("voice_scram_stop_turn_on");
        } else {
            VoiceHelper.play("voice_scram_stop_turn_off");
        }
    }

    private void uploadTaskRecord() {
        String hostname = Event.getOnHostnameEvent().hostname;
        if (TextUtils.isEmpty(hostname)) return;

        dbRepository.getAllDeliveryRecords()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<DeliveryRecord>>() {
                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<DeliveryRecord> deliveryRecords) {
                        try {
                            if (deliveryRecords != null && !deliveryRecords.isEmpty()) {
                                retrofit2.Response<Map<String, Object>> response = ServiceFactory.getRobotService().reportTaskListResult(API.taskListRecordAPI(hostname), deliveryRecords).execute();
                                int code = response.code();
                                if (code == 200) {
                                    dbRepository.deleteAllRecords();
                                }
                                com.reeman.ros.util.Log.w("上传配送数据结果" + code);
                            }
                        } catch (Exception e) {
                            com.reeman.ros.util.Log.e("上传配送数据失败", e.getMessage());
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        com.reeman.ros.util.Log.e("查询配送数据失败", e.getMessage());
                    }

                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMissPoseEvent(Event.OnMissPoseEvent event) {
        ros.setMissPositionResult(event.result);
        if (event.result == 0) {
            ros.setMissPoseUpload(false);
            return;
        }
        if (ros.isMissPoseUpload()) {
            ros.setMissPoseUpload(true);
            Notifier.notify(new Msg(NotifyConstant.LOCATE_NOTIFY, "定位状态", "定位丢失", Event.getOnHostnameEvent().hostname));
        }
    }

    protected boolean shouldResponse2MissPosition() {
        return false;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onPositionObtainedEvent(Event.OnPositionEvent event) {
        ros.setCurrentPosition(event.position);
        mRobotInfo.setCurrentPosition(event.position);
        if (!pointInfoQueue.isEmpty() && navigationMode == Mode.FIX_ROUTE) {
            double width = DispatchUtil.Companion.updateCurrentPointAndRoute(event.position, pointInfoQueue);
            if (width != -1) {
                ros.maxPlanDist(width);
            }
        }
        runOnUiThread(() -> onCustomPositionObtained(event.position));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRobotInfo(RobotInfo robotInfo) {
//        ToastUtils.showShortToast(robotInfo.getHostname()+"  :  "+robotInfo.getUpdateTime());
        DispatchUtil.Companion.updateRobotList(robotInfo);
    }

    protected void onCustomPositionObtained(double[] position) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBaseVal(Event.OnBaseValEvent event) {
//        Timber.tag("lineSpeed ::").w("%s", event.lineSpeed);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNaviResult(Event.OnNavResultEvent event) {
        if (event.state == ROS.NS.FAILURE
                || event.state == ROS.NS.CANCEL
                || event.state == ROS.NS.COMPLETE) {
            ros.setNavigating(false);
        }
        if (event.state == ROS.NS.RECEIVE) {
            onNavigationStartResult(event.code, event.name);
            return;
        }
        if (event.state == ROS.NS.COMPLETE) {
            onNavigationCompleteResult(event.code, event.name, event.mileage);
            return;
        }
        if (event.state == ROS.NS.CANCEL) {
            onNavigationCancelResult(event.code);
            return;
        }
        if (event.state == ROS.NS.RESUME) {
            onNavigationResumeResult(event.code);
        }
    }

    protected void onNavigationResumeResult(int code) {

    }

    protected void onNavigationCancelResult(int code) {
        ros.setNavigating(false);
    }

    protected void onNavigationCompleteResult(int code, String name, float mileage) {
        if (code == 0) ros.setNavigating(false);
    }

    protected void onNavigationStartResult(int code, String name) {
        if (code != 0) {
            ros.setNavigating(false);
            if (code == -4) {
                String chargePoint = DestHelper.getInstance().getChargePoint();
                if (!TextUtils.isEmpty(chargePoint) && chargePoint.equals(name)) {
                    onCustomChargingPileNotFound();
                } else {
                    onCustomPointNotFoundEvent(name);
                }
            }
        }
    }


    protected void onStartNavFailed(int code) {

    }

    //子类自定义目标点找不到处理逻辑
    protected void onCustomPointNotFoundEvent(String name) {

    }

    //子类自定义充电对接失败处理逻辑
    protected void onCustomDockFailed() {
    }


    //子类自定义找不到充电桩处理逻辑
    protected void onCustomChargingPileNotFound() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimeStamp(RobotEvent.OnTimeEvent event) {
        XLog.w(" 急停：" + Event.getCoreData().button + " 导航中：" + ros.isNavigating() + " 充电对接中：" + ros.isDocking());
        onCustomTimeStamp(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShutDown(Event.OnPowerOffEvent event) {
        shutdown();
    }

    /**
     * root生效
     */
    protected void shutdown() {
        try {
            Process process = Runtime.getRuntime().exec("reboot -p");
            String data = null;
            BufferedReader errorLine = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            BufferedReader inputLine = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String error = null;
            while ((error = errorLine.readLine()) != null && !error.equals("null")) {
                data += error + "\n";
            }
            String input = null;
            while ((input = inputLine.readLine()) != null && !input.equals("null")) {
                data += input + "\n";
            }
        } catch (Exception e) {

        }

    }

    //子类自定义低电、定时任务处理逻辑
    protected void onCustomTimeStamp(RobotEvent.OnTimeEvent event) {

        if (!shouldResponse2TimeEvent()) {
            return;
        }

        SharedPreferences instance = SpManager.getInstance();
        int lowPowerThreshold = instance.getInt(Constants.KEY_LOW_POWER, Constants.DEFAULT_LOW_POWER);
        long currentTimeMillis = System.currentTimeMillis();
        long lastChargeStartTime = ros.getLastChargeStartTime();
        long powerOnTime = ros.getPowerOnTime();

        /*
        开机5天以上,
        充电半小时以上且没有人点击充电界面,
        正在充电,
        在充电桩位置(角度小于60度,距离小于0.7)
         */
        if (
//                currentTimeMillis - powerOnTime > 1000 * 60 * 5
//                        && currentTimeMillis - lastChargeStartTime > 1000 * 60*2
                currentTimeMillis - powerOnTime > 1000 * 60 * 60 * 24 * 5
                        && chargingViewShowTime != 0
                        && currentTimeMillis - chargingViewShowTime > 1000 * 60 * 30
                        && ros.isCharging()) {
            String chargePointName = DestHelper.getInstance().getChargePoint();
            if (TextUtils.isEmpty(chargePointName)) return;
            double[] currentPosition = ros.getCurrentPosition();
            double[] chargePointCoordinate = DestHelper.getInstance().getChargePointCoordinate();
            if (Math.abs(chargePointCoordinate[2] - currentPosition[2]) < 1.04f
                    && Math.sqrt(Math.pow(chargePointCoordinate[0] - currentPosition[0], 2)) + Math.pow(chargePointCoordinate[1] - currentPosition[1], 2) < 0.7) {
                isSelfCheck = true;
                ros.getPosition();
                if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
                mHandler.postDelayed(() -> {
                    if (ros.getCurrentPosition() != null && ros.getCurrentPosition().length == 3) {
                        EasyDialog.getLoadingInstance(this).loading(getString(R.string.text_self_check));
                        ros.sysReboot();
                        ros.setPowerOnTime(currentTimeMillis);
                    }
                }, 1500);
            }
        } else if (ros.getLevel() <= lowPowerThreshold) {
            if (ros.isEmergencyStopDown()
                    || ros.isNavigating()
                    || ros.isDocking()
                    || ros.getMissPositionResult() == 1
                    || TextUtils.isEmpty(DestHelper.getInstance().getChargePoint())) return;

            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
            mHandler.postDelayed(() -> EasyDialog.getInstance(this).warnWithScheduledUpdateDetail(
                    getString(R.string.text_going_to_charge_for_low_power, Constants.RESPONSE_TIME),
                    R.string.text_start_right_now,
                    R.string.text_cancel,
                    new EasyDialog.OnViewClickListener() {
                        @Override
                        public void onViewClick(Dialog dialog, int id) {
                            dialog.dismiss();
                            if (id == R.id.btn_confirm) {
                                if (ros.isEmergencyStopDown()) {
                                    EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                                    return;
                                }
                                startGotoCharge();
                            }
                        }
                    },
                    new EasyDialog.OnTimeStampListener() {
                        @Override
                        public void onTimestamp(TextView title, TextView content, Button cancelBtn, Button neutralBtn, Button confirmBtn, int current) {
                            content.setText(getString(R.string.text_going_to_charge_for_low_power, Constants.RESPONSE_TIME - current));
                        }

                        @Override
                        public void onTimeOut(EasyDialog dialog) {
                            dialog.dismiss();
                            if (ros.isEmergencyStopDown()) {
                                EasyDialog.getInstance(BaseActivity.this).warnError(getString(R.string.voice_scram_stop_turn_on));
                                return;
                            }
                            startGotoCharge();
                        }
                    },
                    1000,
                    Constants.RESPONSE_TIME * 1000
            ), 1000);
        }
    }

    protected void startGotoCharge() {
        XLog.w("空闲状态触发低电");
        Intent intent = new Intent(this, ReturningActivity.class);
        intent.putExtra(Constants.TASK_TARGET, Constants.TYPE_GOTO_CHARGE);
        intent.putExtra(Constants.CHARGE_REASON, Constants.CHARGE_REASON_LOW_POWER);
        startActivityForResult(intent, REQUEST_CODE_FOR_CHARGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (dispatchState != DispatchState.IGNORE)
            dispatchState = DispatchState.INIT;
        if (pointInfoQueue.size() != 1) {
            pointInfoQueue.clear();
            Event.OnPositionEvent event = Event.getOnPositionEvent();
            PathPoint pathPoint = com.reeman.delige.dispatch.util.PointUtil.Companion.calculateNearestPoint(event.position);
            if (pathPoint != null) {
                pointInfoQueue.offer(new PointInfo(pathPoint.name, 1));
            }
        }
        if (resultCode == -1) {
            if (data == null) return;
            String voice = data.getStringExtra(Constants.TASK_RESULT_VOICE);
            if (!TextUtils.isEmpty(voice)) {
                VoiceHelper.play(voice);
            }
            String prompt = data.getStringExtra(Constants.TASK_RESULT_PROMPT);
            if (!TextUtils.isEmpty(prompt)) {
                mHandler.postDelayed(() -> EasyDialog.getInstance(this).warnError(prompt), 800);
            }
        } else if (resultCode == -2) {
            Iterator<String> iterator = getCallingQueue().iterator();
            if (iterator.hasNext()) {
                onCustomResponseCallingEvent(iterator.next());
            }
        } else {

            if (data == null) return;
            String page = data.getStringExtra("from_page");
//            mHandler.postDelayed(() -> {
//
//                if (!"task".equals(page)) return;
//                if (ros.isEmergencyStopDown()) return;
////                int chargePlug = ros.getCharge();
////                int currentPowerLevel = ros.getLevel();
////                if (chargePlug == 0 && currentPowerLevel < 15) {
////                    Intent intent = new Intent(this, ReturningActivity.class);
////                    intent.putExtra(Constants.TASK_TARGET, Constants.TYPE_GOTO_CHARGE);
////                    startActivityForResult(intent, REQUEST_CODE_FOR_CHARGE);
////                    return;
////                }
//                HashMap<Integer, String> map = new HashMap<>();
//                Random random = new Random();
//                List<? extends BaseItem> pathPoints = DestHelper.getInstance().getPoints();
////                List<Point> workingPoints = DestHelper.getInstance().getPoints();
//                map.put(1, pathPoints.get(random.nextInt(pathPoints.size())).name);
//                map.put(2, pathPoints.get(random.nextInt(pathPoints.size())).name);
//                map.put(3, pathPoints.get(random.nextInt(pathPoints.size())).name);
//                Intent intent = new Intent(this, TaskExecutingActivity.class);
//                intent.putExtra(Constants.TASK_TARGET, map);
//                intent.putExtra(Constants.TASK_MODE, Constants.MODE_DELIVERY_FOOD);
//                startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
//            }, 5000);
        }
    }

    public static void showChargingView(Context context) {
        try {
            if (isChargingViewShowing) return;
            WindowManager manager = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams para = new WindowManager.LayoutParams();
            para.height = WindowManager.LayoutParams.MATCH_PARENT;
            para.width = WindowManager.LayoutParams.MATCH_PARENT;
            para.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            para.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;

            if (mView == null) {
                mView = LayoutInflater.from(mApp).inflate(R.layout.layout_charging_view, null);
                mView.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                mView.setBackgroundColor(Color.BLACK);
                chargingView = mView.findViewById(R.id.gif_charging_view);
            }
            int deliveryAnimation = SpManager.getInstance().getInt(Constants.KEY_DELIVERY_ANIMATION, Constants.DEFAULT_DELIVERY_ANIMATION);
            Resources resources = context.getResources();
            int identifier = resources.getIdentifier("charging_animation_" + deliveryAnimation, "drawable", context.getPackageName());
            GifDrawable gifDrawable = new GifDrawable(resources, identifier);
            chargingView.setImageDrawable(gifDrawable);
            manager.addView(mView, para);
            gifDrawable.start();
            isChargingViewShowing = true;
            chargingViewShowTime = System.currentTimeMillis();
            mView.setOnClickListener(v -> {
                hideChargingView();
                mHandler.postDelayed(chargeRunnable, 10000);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hideChargingView() {
        try {
            if (!isChargingViewShowing) return;
            WindowManager manager = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
            ((GifDrawable) chargingView.getDrawable()).stop();
            manager.removeView(mView);
            isChargingViewShowing = false;
            chargingViewShowTime = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitPose(Event.OnInitPoseEvent event) {
        onCustomInitPose(event.currentPosition);
    }

    protected void onCustomInitPose(String currentPosition) {
        if (isSelfCheck && EasyDialog.isShow()) {
            if (relocCount++ < 3) {
                double[] position = ros.getCurrentPosition();
                ros.relocateByCoordinate(position);
                mHandler.postDelayed(() -> ros.cpuPerformance(), 100);
                mHandler.postDelayed(() -> ros.getPowerOnTime(), 150);
                mHandler.postDelayed(() -> ros.currentInfoControl(true), 200);
                double[] chargePointCoordinate = DestHelper.getInstance().getChargePointCoordinate();
                if (chargePointCoordinate == null || chargePointCoordinate.length == 0) {
                    EasyDialog.getInstance().dismiss();
                    isSelfCheck = false;
                    relocCount = 0;
                    return;
                }
                if (Math.abs(chargePointCoordinate[3] - position[2]) < 1.04f
                        && Math.sqrt(Math.pow(chargePointCoordinate[0] - position[0], 2)) + Math.pow(chargePointCoordinate[1] - position[1], 2) < 0.7) {
                    EasyDialog.getInstance().dismiss();
                    isSelfCheck = false;
                    relocCount = 0;
                } else {
                    ros.relocateByCoordinate(position);
                }
            } else {
                relocCount = 0;
                isSelfCheck = false;
                EasyDialog.getInstance().dismiss();
                Notifier.notify(new Msg(NotifyConstant.LOCATE_NOTIFY, "定位异常", "自动重启导航后定位失败", Event.getOnHostnameEvent().hostname));
                mHandler.postDelayed(() -> EasyDialog.getInstance(this).warnError(getString(R.string.text_check_position_error)), 300);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSensorState(Event.OnCheckSensorsEvent event) {
        if (event.rawData.equals(ros.getLastSensorState())) return;
        ros.setLastSensorState(event.rawData);
        if (event.rawData.contains("0")) {
            onSensorsError(event);
        }
    }


    protected void onSensorsError(Event.OnCheckSensorsEvent event) {
        Notifier.notify(new Msg(NotifyConstant.HARDWARE_NOTIFY, "传感器状态", "传感器异常:" + event.toString(), Event.getOnHostnameEvent().hostname));
    }

    protected void onCustomDropEvent() {
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.w("xuedong", "ACTION_DOWN");
                mHandler.removeCallbacks(chargeRunnable);
                break;
            case MotionEvent.ACTION_UP:
                Log.w("xuedong", "ACTION_UP");
                mHandler.postDelayed(chargeRunnable, 10000);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}