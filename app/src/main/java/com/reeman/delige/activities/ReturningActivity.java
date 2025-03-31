package com.reeman.delige.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.reeman.delige.R;
import com.reeman.delige.base.BaseActivity;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.ReturningContract;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.presenter.impl.ReturningPresenter;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.utils.WIFIUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


import static com.reeman.delige.base.BaseApplication.dispatchState;
import static com.reeman.delige.base.BaseApplication.mRobotInfo;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;

import static com.reeman.delige.base.BaseApplication.ros;
import static com.reeman.delige.constants.Constants.TYPE_GOTO_CHARGE;
import static com.reeman.delige.constants.Constants.TYPE_GOTO_PRODUCT_POINT;
import static com.reeman.delige.constants.Constants.TYPE_GOTO_RECYCLING_POINT;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ReturningActivity extends BaseActivity implements ReturningContract.View {

    private ViewGroup root;
    private int deliveryViewId;
    private GifImageView deliveryView;
    private GifDrawable deliveryDrawable;
    private RelativeLayout taskPauseView;
    private int chargeReason;
    private int target;
    private TextView tvCountDown;
    private TextView tvTime;
    private TextView tvPower;
    private ImageView ivWiFi;
    private ReturningPresenter presenter;
    private boolean isFirstEnter = true;

    @Override
    protected boolean shouldResponse2CallingEvent() {
        return true;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_returning;
    }

    @Override
    protected void initData() {
        target = getIntent().getIntExtra(Constants.TASK_TARGET, -1);
        chargeReason = getIntent().getIntExtra(Constants.CHARGE_REASON, Constants.CHARGE_REASON_USER_TRIGGER);
        presenter = new ReturningPresenter(this);
    }

    @Override
    protected void initCustomView() {
        root = $(R.id.root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstEnter) return;
        isFirstEnter = false;
        presenter.startTask(this, target, chargeReason);
    }

    @Override
    protected void onPause() {
        super.onPause();
        View firstChild = root.getChildAt(0);
        if (firstChild != null && firstChild == deliveryView) {
            deliveryDrawable.stop();
        }
    }

    @Override
    public void showDeliveryView() {
        initDeliveryView();
        showView(deliveryView);
    }

    @Override
    public void showTaskFinishView(int result, String prompt, String voice) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TASK_RESULT_PROMPT, prompt);
        intent.putExtra(Constants.TASK_RESULT_VOICE, voice);
        setResult(result, intent);
        finish();
    }

    @Override
    public void showTaskPauseView() {
        initTaskPauseView();
        showView(taskPauseView);
    }

    @Override
    public void onCountDownFinished() {
        presenter.onTaskContinue(this);
    }

    @Override
    public void onCountDown(long mills) {
        XLog.w("暂停倒计时：" + mills);
        SpannableString str = new SpannableString(getString(R.string.text_task_continued_in_minutes, mills));
        int length = String.valueOf(mills).length();
        int start = str.toString().split("\\d")[0].length();
        str.setSpan(new ForegroundColorSpan(Color.BLUE), start, start + length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        str.setSpan(new AbsoluteSizeSpan(40), start, start + length, 0);
        str.setSpan(new StyleSpan(Typeface.BOLD), start, start + length, 0);
        str.setSpan(new ScaleXSpan(1.1f), start, start + length, 0);
        tvCountDown.setText(str);
    }

    void initTaskPauseView() {
        if (taskPauseView == null) {
            taskPauseView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.layout_task_pause_view, null);
            TextView tvTaskMode = taskPauseView.findViewById(R.id.tv_task_mode_in_pause_view);
            int id = 0;
            if (target == TYPE_GOTO_PRODUCT_POINT) {
                id = R.string.text_going_to_product_point;
            } else if (target == TYPE_GOTO_CHARGE) {
                id = R.string.text_going_to_charging_pile;
            } else if (target == TYPE_GOTO_RECYCLING_POINT) {
                id = R.string.text_going_to_recycling_point;
            }
            tvTaskMode.setText(getString(id));
            TextView tvReturn = taskPauseView.findViewById(R.id.tv_return_in_pause_view);
            tvReturn.setText(R.string.text_continue_task);
            tvReturn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_continue_task, 0, 0);
            TextView tvCancelTask = taskPauseView.findViewById(R.id.tv_cancel_task_in_pause_view);
            tvCountDown = taskPauseView.findViewById(R.id.tv_count_down_in_pause_view);
            LinearLayout llPickMealInAdvance = taskPauseView.findViewById(R.id.ll_pick_meal_in_advance);
            llPickMealInAdvance.setVisibility(View.GONE);
            TextView tvContinueTask = taskPauseView.findViewById(R.id.tv_continue_task_in_pause_view);
            tvContinueTask.setVisibility(View.GONE);
            tvTime = taskPauseView.findViewById(R.id.tv_time);
            ivWiFi = taskPauseView.findViewById(R.id.iv_wifi);
            tvPower = taskPauseView.findViewById(R.id.tv_power);
            refreshTime();

            refreshPowerState(ros.getLevel());
            refreshNetworkState();
            tvReturn.setOnClickListener(this);
            tvCancelTask.setOnClickListener(this);
        }
        tvCountDown.setText("");
    }


    private void refreshTime() {
        if (tvTime == null) return;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        tvTime.setText(simpleDateFormat.format(new Date()));
    }


    void initDeliveryView() {
        try {
            if (deliveryView == null) {
                deliveryView = new GifImageView(this);
                deliveryViewId = View.generateViewId();
                deliveryView.setId(deliveryViewId);
                deliveryView.setOnClickListener(this);
                int targetDeliveryAnimation = SpManager.getInstance().getInt(Constants.KEY_DELIVERY_ANIMATION, Constants.DEFAULT_DELIVERY_ANIMATION);
                int identifier = getResources().getIdentifier("delivery_animation_" + targetDeliveryAnimation, "drawable", getPackageName());
                deliveryDrawable = new GifDrawable(getResources(), identifier);
                deliveryView.setImageDrawable(deliveryDrawable);
                deliveryView.setBackgroundColor(Color.BLACK);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showView(View view) {
        View firstChild = root.getChildAt(0);
        if (firstChild != null && firstChild == deliveryView) {
            deliveryDrawable.stop();
        }
        root.removeAllViews();
        root.addView(view);
        if (view == deliveryView) {
            ((GifImageView) view).setImageDrawable(deliveryDrawable);
            deliveryDrawable.start();
        }
    }

    private void refreshNetworkState() {
        if (ivWiFi == null) return;
        if (WIFIUtils.isNetworkConnected(this)) {
            ivWiFi.setImageResource(R.drawable.icon_wifi_on);
            return;
        }
        ivWiFi.setImageResource(R.drawable.icon_wifi_off);
    }

    private void refreshPowerState(int level) {
        if (tvPower == null) return;
        tvPower.setText(level + "%");
        if (level < 20) {
            tvPower.setTextColor(getResources().getColor(R.color.warning));
        } else {
            tvPower.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == deliveryViewId) {
            onTaskPause();
            return;
        }
        switch (id) {
            case R.id.tv_cancel_task_in_pause_view:
                presenter.onTaskCancel(this);
                break;
            case R.id.tv_return_in_pause_view:
                onTaskContinue();
                break;
        }
    }

    private void onTaskContinue() {
        presenter.onTaskContinue(this);
    }

    private void onTaskPause() {
        presenter.onTaskPause(this);
    }

    @Override
    protected void onNavigationCancelResult(int code) {
        super.onNavigationCancelResult(code);
        presenter.onNavigationCancelResult(this, code);
    }

    @Override
    protected void onNavigationCompleteResult(int code, String name, float mileage) {
        super.onNavigationCompleteResult(code, name, mileage);
        presenter.onNavigationCompleteResult(this, code, name, mileage);
    }

    @Override
    protected void onNavigationStartResult(int code, String name) {
        super.onNavigationStartResult(code, name);
        presenter.onNavigationStartResult(this, code, name);
    }

    @Override
    protected void onSensorsError(Event.OnCheckSensorsEvent event) {
        presenter.onSensorsError(this, event);
    }


    @Override
    protected void onCustomPointNotFoundEvent(String name) {
        presenter.onPointNotFound(this);
    }

    @Override
    protected void onCustomChargingPileNotFound() {
        presenter.onChargingPileNotFound(this);
    }

    @Override
    protected void onCustomPowerConnected() {
        presenter.onTaskFinished(0, null, null);
    }

    @Override
    protected void onCustomDockFailed() {
        View firstChild = root.getChildAt(0);
        if (firstChild != null && taskPauseView != null && firstChild == taskPauseView) {
            return;
        }
        presenter.onDockFailed(this);
    }

    @Override
    protected void onCustomDropEvent() {
        presenter.onDropEvent(this);
    }

    @Override
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {
        if (emergencyStopState == 0) {
            presenter.onEmergencyStopTurnOn(this, emergencyStopState);
        } else {
            presenter.onEmergencyStopTurnOff();
        }
    }

    @Override
    public void showDropView() {
        showTaskPauseView();
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.newCustomInstance(this, R.layout.layout_drop_dialog).emergencyStopOn();
        tvCountDown.setText(getString(R.string.text_recovery_falling_state));
    }

    @Override
    public void showEmergencyStopTurnOnView() {
        showTaskPauseView();
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.newCustomInstance(this, R.layout.layout_emergency_stop_dialog).emergencyStopOn();
        tvCountDown.setText(getString(R.string.text_turn_off_emergency_stop_to_continue_task));
    }

    @Override
    public void showEmergencyStopTurnOffView() {
        tvCountDown.setText("");
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
    }

    @Override
    protected void onNavigationResumeResult(int code) {
        super.onNavigationResumeResult(code);
        if (code == -1) {
            ros.navigationByPoint(presenter.getDispatchTargetPoint());
        }
    }

    @Override
    protected void onCustomBatteryChange(int level, int plug) {
        refreshPowerState(level);
    }

    @Override
    protected void onCustomTimeStamp(RobotEvent.OnTimeEvent event) {
        refreshTime();
    }

    @Override
    protected void onCustomNetworkStateChange(RobotEvent.OnNetworkEvent event) {
        if (ivWiFi == null) return;
        NetworkInfo networkInfo = event.networkIntent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
            ivWiFi.setImageResource(R.drawable.icon_wifi_on);
            return;
        }
        ivWiFi.setImageResource(R.drawable.icon_wifi_off);
    }

    @Override
    protected void onCustomEncounterObstacle() {
        presenter.onEncounterObstacle();
    }

    @Override
    protected void onCustomResponseCallingEvent(String next) {
        presenter.onCustomResponseCallingEvent();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMissPoseEvent(Event.OnMissPoseEvent event) {
        if (event.result == 1)
            presenter.onMissPose(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRoutePointEvent(Event.OnRoutePointEvent event) {
        if (navigationMode == Mode.FIX_ROUTE)
            DispatchUtil.Companion.updateRoutePoint(event.points, pointInfoQueue);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpecialAreaEvent(Event.OnSpecialAreaEvent event) {
        if (navigationMode == Mode.FIX_ROUTE) {
            boolean pause = DispatchUtil.Companion.updateSpecialArea(event.name, mRobotInfo);
            if (pause) {
                pauseByDispatch(R.string.text_pause_by_special_area);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDispatchResumeEvent(Event.OnDispatchResumeEvent event) {
        resumeByDispatch();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDispatchPauseEvent(Event.OnDispatchPauseEvent event) {
        if (DispatchUtil.Companion.canPause()) {
            pauseByDispatch(R.string.text_pause_by_cross);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPlanDijEvent(Event.OnGetPlanDijEvent event) {
        presenter.onGetPlanDij(event.points);
    }

    @Override
    public void onCustomPositionObtained(double[] position) {
        presenter.onPositionObtained(this,position);
    }

    private long lastPauseTime;
    private long lastResumeTime;


    @Override
    public void pauseByDispatch(int id) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastPauseTime > 10_000 && !VoiceHelper.isPlaying()) {
            VoiceHelper.play("voice_line_up");
            lastPauseTime = currentTimeMillis;
        }
        ros.pauseNavigation();
        dispatchState = DispatchState.WAITING;
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        EasyDialog.getLoadingInstance(this).loading(getString(id));
    }

    private void resumeByDispatch() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastResumeTime > 10_000 && !VoiceHelper.isPlaying()) {
            VoiceHelper.play("voice_robot_will_pass");
            lastResumeTime = currentTimeMillis;
        }
        if (ros.isNavigating()) {
            ros.resumeNavigation();
        } else {
            ros.navigationByPoint(presenter.getDispatchTargetPoint());
        }
        dispatchState = DispatchState.INIT;
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
    }
}
