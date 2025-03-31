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
import com.reeman.delige.constants.State;
import com.reeman.delige.contract.TaskExecutingContract;
import com.reeman.delige.dispatch.DispatchState;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.presenter.impl.TaskExecutingPresenter;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.utils.WIFIUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.reeman.delige.base.BaseApplication.dispatchState;
import static com.reeman.delige.base.BaseApplication.getCallingQueue;
import static com.reeman.delige.base.BaseApplication.mRobotInfo;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;

import static com.reeman.delige.base.BaseApplication.ros;


public class TaskExecutingActivity extends BaseActivity implements TaskExecutingContract.View {

    private int tableLayer = 3;
    private int deliveryViewId;
    private boolean isFirstEnter = true;
    private ViewGroup root;
    private LinearLayout deliveryView;
    private RelativeLayout taskPauseView;
    private TextView tvCountDown;
    private ImageView ivWiFi;
    private TextView tvTime;
    private TextView tvPower;
    private TextView tvTaskMode;
    private ViewGroup arrivedView;
    private GifImageView indicatorOne;
    private GifImageView indicatorTwo;
    private GifImageView indicatorThree;
    private GifImageView indicatorFour;
    private TextView tvLayerTwo;
    private TextView tvLayerOne;
    private TextView tvLayerThree;
    private TextView tvLayerFour;
    private GifDrawable deliveryDrawable;
    private TextView tvNextTableCountDown;
    private TextView tvCurrentTable;
    private ViewGroup arriveAtBirthdayTableView;
    private TextView btnComplete;
    private View llPickMealInAdvance;
    private View currentShowingView;
    private LinearLayout llReturn;
    private GifImageView gifImageView;
    private TextView tvTargetTable;
    private TextView tvTaskState;
    private TaskExecutingPresenter taskExecutingPresenter;

    @Override
    protected boolean shouldResponse2CallingEvent() {
        return true;
    }

    @Override
    protected boolean disableBottomNavigationBar() {
        return true;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_task_executing;
    }

    @Override
    protected void initData() {
        taskExecutingPresenter = new TaskExecutingPresenter(this);
        int intExtra = getIntent().getIntExtra(Constants.TASK_MODE, -1);
        taskExecutingPresenter.setTaskMode(intExtra);
    }

    @Override
    protected void initCustomView() {
        root = $(R.id.root);
        initDeliveryView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirstEnter) return;
        isFirstEnter = false;
        tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
        taskExecutingPresenter.startTask(this, getIntent());
    }

    public void showArrivedAtView() {
        initTaskArrivedView();
        showView(arrivedView);
    }

    @Override
    public void showTaskPauseView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable) {
        initTaskPauseView(level2Table, multiDeliveryTable);
        showView(taskPauseView);
    }

    public void showArrivedAtBirthdayTableView() {
        initArriveAtBirthdayTableView();
        showView(arriveAtBirthdayTableView);
    }

    void initTaskArrivedView() {
        if (arrivedView == null) {
            int tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
            arrivedView = (ViewGroup) LayoutInflater.from(this).inflate(tableLayer == 3 ? R.layout.layout_arrive_at_target_table_three_layers : R.layout.layout_arrive_at_target_table_four_layers, null);
            arrivedView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            indicatorOne = arrivedView.findViewById(R.id.iv_indicator_one);
            indicatorTwo = arrivedView.findViewById(R.id.iv_indicator_two);
            indicatorThree = arrivedView.findViewById(R.id.iv_indicator_three);
            indicatorFour = arrivedView.findViewById(R.id.iv_indicator_four);
            tvLayerOne = arrivedView.findViewById(R.id.tv_layer_one);
            tvLayerTwo = arrivedView.findViewById(R.id.tv_layer_two);
            tvLayerThree = arrivedView.findViewById(R.id.tv_layer_three);
            tvLayerFour = arrivedView.findViewById(R.id.tv_layer_four);
            tvCurrentTable = arrivedView.findViewById(R.id.tv_current_table);
            tvNextTableCountDown = arrivedView.findViewById(R.id.tv_next_table_countdown);
            TextView returnToProductPoint = arrivedView.findViewById(R.id.tv_return_to_product_point);
            returnToProductPoint.setOnClickListener(this);
            TextView tvCancelDelivery = arrivedView.findViewById(R.id.tv_cancel_delivery);
            tvCancelDelivery.setOnClickListener(this);
            TextView tvGotoNext = arrivedView.findViewById(R.id.tv_go_to_next);
            tvGotoNext.setOnClickListener(this);
        }
    }

    void initArriveAtBirthdayTableView() {
        if (arriveAtBirthdayTableView == null) {
            int tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
            arriveAtBirthdayTableView = (ViewGroup) LayoutInflater.from(this).inflate(tableLayer == 3 ? R.layout.layout_arrive_at_birthday_table_three_layers : R.layout.layout_arrive_at_birthday_table_four_layers, null);
            arriveAtBirthdayTableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            indicatorOne = arriveAtBirthdayTableView.findViewById(R.id.iv_indicator_one);
            tvLayerOne = arriveAtBirthdayTableView.findViewById(R.id.tv_layer_one);
            tvCurrentTable = arriveAtBirthdayTableView.findViewById(R.id.tv_current_table);
            btnComplete = arriveAtBirthdayTableView.findViewById(R.id.btn_complete);
            btnComplete.setOnClickListener(this);
            tvCurrentTable = arriveAtBirthdayTableView.findViewById(R.id.tv_current_table);
        }
    }

    void initDeliveryView() {
        int displayContent = SpManager.getInstance().getInt(Constants.KEY_DISPLAY_DURING_DELIVERY, Constants.DEFAULT_DISPLAY_DURING_DELIVERY);
        int taskMode = taskExecutingPresenter.getTaskMode();
        if (displayContent == 0 || taskMode == Constants.MODE_CRUISE) {
            try {
                if (deliveryView == null) {
                    int targetDeliveryAnimation = SpManager.getInstance().getInt(Constants.KEY_DELIVERY_ANIMATION, Constants.DEFAULT_DELIVERY_ANIMATION);
                    deliveryView = new LinearLayout(this);
                    deliveryView.setBackgroundColor(Color.BLACK);
                    deliveryView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    gifImageView = new GifImageView(this);
                    gifImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    int identifier = getResources().getIdentifier("delivery_animation_" + targetDeliveryAnimation, "drawable", getPackageName());
                    deliveryDrawable = new GifDrawable(getResources(), identifier);
                    gifImageView.setImageDrawable(deliveryDrawable);
                    deliveryView.addView(gifImageView);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            int tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
            deliveryView = (LinearLayout) LayoutInflater.from(this).inflate(tableLayer == 3 ? R.layout.layout_delivery_view_three_layers : R.layout.layout_delivery_view_four_layers, null);
            deliveryView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            tvTargetTable = deliveryView.findViewById(R.id.tv_target_table);
        }
        deliveryViewId = View.generateViewId();
        deliveryView.setId(deliveryViewId);
        mHandler.postDelayed(() -> deliveryView.setOnClickListener(this), 1000);
//        deliveryView.setOnClickListener(this);
    }

    void initTaskPauseView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable) {
        if (taskPauseView == null) {
            taskPauseView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.layout_task_pause_view, null);
            tvTaskMode = taskPauseView.findViewById(R.id.tv_task_mode_in_pause_view);
            tvTaskState = taskPauseView.findViewById(R.id.tv_task_state_in_pause_view);
            TextView tvReturn = taskPauseView.findViewById(R.id.tv_return_in_pause_view);
            llReturn = taskPauseView.findViewById(R.id.ll_return);
            TextView tvCancelTask = taskPauseView.findViewById(R.id.tv_cancel_task_in_pause_view);
            tvCountDown = taskPauseView.findViewById(R.id.tv_count_down_in_pause_view);
            llPickMealInAdvance = taskPauseView.findViewById(R.id.ll_pick_meal_in_advance);
            TextView tvPickMealInAdvance = taskPauseView.findViewById(R.id.tv_pick_meal_in_advance_in_pause_view);
            TextView tvContinueTask = taskPauseView.findViewById(R.id.tv_continue_task_in_pause_view);
            tvTime = taskPauseView.findViewById(R.id.tv_time);
            ivWiFi = taskPauseView.findViewById(R.id.iv_wifi);
            tvPower = taskPauseView.findViewById(R.id.tv_power);
            refreshTime();
            refreshPowerState(ros.getLevel());
            refreshNetworkState();
            tvReturn.setOnClickListener(this);
            tvPickMealInAdvance.setOnClickListener(this);
            tvCancelTask.setOnClickListener(this);
            tvContinueTask.setOnClickListener(this);
        }
        int taskMode = taskExecutingPresenter.getTaskMode();
        int id;
        if (taskMode == Constants.MODE_RECYCLE || taskMode == Constants.MODE_RECYCLE_2) {
            id = R.string.text_recycle_mode_in_progress;
        } else if (taskMode == Constants.MODE_CRUISE) {
            id = R.string.text_cruise_mode_in_progress;
        } else if (taskMode == Constants.MODE_DELIVERY_FOOD) {
            id = R.string.text_delivery_mode_in_progress;
        } else if (taskMode == Constants.MODE_BIRTHDAY) {
            id = R.string.text_birthday_mode_in_progress;
        } else if (taskMode == Constants.MODE_MULTI_DELIVERY) {
            id = R.string.text_multi_delivery_mode_in_progress;
        } else {
            LinkedHashSet<String> taskQueue = getCallingQueue();
            if (taskMode == Constants.MODE_CALLING && taskQueue != null && taskQueue.iterator().hasNext()) {
                id = R.string.text_response_to_calling;
            } else if (taskMode == Constants.MODE_CALLING && taskExecutingPresenter.getLastTaskMode() == Constants.MODE_RECYCLE_2) {
                id = R.string.text_goto_recycling_point;
            } else {
                id = R.string.text_goto_product_point;
            }
        }
        tvTaskMode.setText(getString(id));
        tvTaskState.setText(getString(R.string.text_task_is_pause));
        tvCountDown.setText("");
        if ((taskMode == Constants.MODE_DELIVERY_FOOD && level2Table != null && !level2Table.isEmpty()) ||
                (taskMode == Constants.MODE_MULTI_DELIVERY && multiDeliveryTable != null && !multiDeliveryTable.isEmpty())) {
            llPickMealInAdvance.setVisibility(View.VISIBLE);
        } else {
            llPickMealInAdvance.setVisibility(View.GONE);
        }

        if (taskMode == Constants.MODE_CRUISE && taskExecutingPresenter.isLowCharge()) {
            llReturn.setVisibility(View.GONE);
        } else {
            llReturn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCustomEmergencyStopStateChange(int emergencyStopState) {
        if (emergencyStopState == 0) {
            taskExecutingPresenter.onEmergencyStopTurnOn(this);
        } else {
            taskExecutingPresenter.onEmergencyStopTurnOff();
        }
    }

    @Override
    protected void onCustomDropEvent() {
        taskExecutingPresenter.onDrop(this);
    }

    @Override
    public void showEmergencyStopTurnOffView() {
        tvCountDown.setText("");
    }

    private void refreshTime() {
        if (tvTime == null) return;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        tvTime.setText(simpleDateFormat.format(new Date()));
    }

    private void refreshNetworkState() {
        if (ivWiFi == null) return;
        if (WIFIUtils.isNetworkConnected(this)) {
            ivWiFi.setImageResource(R.drawable.icon_wifi_on);
            return;
        }
        ivWiFi.setImageResource(R.drawable.icon_wifi_off);
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

    private void refreshPowerState(int level) {
        if (tvPower == null) return;
        tvPower.setText(level + "%");
        if (level < 20) {
            tvPower.setTextColor(getResources().getColor(R.color.warning));
        } else {
            tvPower.setTextColor(Color.WHITE);
        }
    }


    public void showView(View view) {
        View firstChild = root.getChildAt(0);
        if (firstChild != null) {
            if (deliveryDrawable != null) {
                deliveryDrawable.stop();
            }
            if (firstChild == arrivedView) {
                ((GifDrawable) indicatorOne.getDrawable()).stop();
                ((GifDrawable) indicatorTwo.getDrawable()).stop();
                ((GifDrawable) indicatorThree.getDrawable()).stop();
            }
            if (firstChild == arriveAtBirthdayTableView) {
                ((GifDrawable) indicatorOne.getDrawable()).stop();
            }
        }
        root.removeAllViews();
        root.addView(view);
        currentShowingView = view;
        if (deliveryDrawable != null) {
            gifImageView.setImageDrawable(deliveryDrawable);
            deliveryDrawable.start();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == deliveryViewId) {
            onTaskPause(true);
            return;
        }
        switch (id) {
            case R.id.tv_pick_meal_in_advance_in_pause_view:
                onPickMealInAdvanceBtnClick();
                break;
            case R.id.tv_continue_task_in_pause_view:
                onContinueTaskBtnClick();
                break;
            case R.id.tv_return_in_pause_view:
            case R.id.tv_return_to_product_point:
                onReturnBtnClick();
                break;
            case R.id.tv_cancel_task_in_pause_view:
            case R.id.tv_cancel_delivery:
                onCancelTaskBtnClick();
                break;
            case R.id.btn_complete:
                onCompleteTaskBtnClick();
                break;
            case R.id.tv_go_to_next:
                onNextTableBtnClick();
                break;
        }
    }

    private void onPickMealInAdvanceBtnClick() {
        taskExecutingPresenter.onPickMealInAdvance();
    }

    private void onNextTableBtnClick() {
        taskExecutingPresenter.onNextTable(this);
    }

    private void onCompleteTaskBtnClick() {
        taskExecutingPresenter.onCompleteTask(this);
    }

    private void onContinueTaskBtnClick() {
        taskExecutingPresenter.onTaskContinue(this);
    }

    @Override
    protected void onCustomBatteryChange(int level, int plug) {
        refreshPowerState(level);
    }


    @Override
    public void onTaskPause(boolean isPauseCountdown) {
        taskExecutingPresenter.onTaskPause(isPauseCountdown);
    }

    /**
     * 点击返航
     */
    private void onReturnBtnClick() {
        taskExecutingPresenter.onTaskTerminated(this);
    }

    /**
     * 点击取消任务
     */
    private void onCancelTaskBtnClick() {
        taskExecutingPresenter.onCancelTask(this);
    }

    /**
     * 显示返程界面
     *
     * @param target
     */
    @Override
    public void showDeliveryView(String target) {
        if (tvTargetTable != null) {
            if (target != null) {
                if (target.length() <= 3) {
                    tvTargetTable.setTextSize(100);
                } else if (target.length() <= 5) {
                    tvTargetTable.setTextSize(80);
                } else {
                    tvTargetTable.setTextSize(60);
                }
                tvTargetTable.setText(target);
            }
        }
        showView(deliveryView);
    }


    /**
     * 任务结束
     *
     * @param result
     * @param prompt
     * @param voice
     */
    @Override
    public void showTaskFinishedView(int result, String prompt, String voice) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TASK_RESULT_PROMPT, prompt);
        intent.putExtra(Constants.TASK_RESULT_VOICE, voice);
        intent.putExtra("from_page", "task");
        setResult(result, intent);
        finish();
    }

    @Override
    public void showArrivedAtBirthdayTable(String dest) {
        showArrivedAtBirthdayTableView();
        tvCurrentTable.setText(getString(R.string.text_arrive_at_target_point, dest));
        tvLayerOne.setText(dest);
        ((GifDrawable) indicatorOne.getDrawable()).start();
    }

    @Override
    public void showDropView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable) {
        if (currentShowingView == taskPauseView) {
            tvCountDown.setText(getString(R.string.text_recovery_falling_state));
        } else if (currentShowingView == arrivedView) {
            tvNextTableCountDown.setText(getString(R.string.text_recovery_falling_state));
        } else if (currentShowingView == arriveAtBirthdayTableView) {
            btnComplete.setText(R.string.text_complete);
        } else {
            showTaskPauseView(level2Table, multiDeliveryTable);
        }
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        mHandler.postDelayed(() -> EasyDialog.newCustomInstance(this, R.layout.layout_drop_dialog).emergencyStopOn(), 500);
    }

    @Override
    public void showEmergencyOnView(TreeMap<Integer, String> level2Table, TreeMap<Integer, List<String>> multiDeliveryTable) {
        if (currentShowingView == taskPauseView) {
            tvCountDown.setText(getString(R.string.text_turn_off_emergency_stop_to_continue_task));
        } else if (currentShowingView == arrivedView) {
            tvNextTableCountDown.setText(getString(R.string.text_turn_off_emergency_stop_to_continue_task));
        } else if (currentShowingView == arriveAtBirthdayTableView) {
            btnComplete.setText(R.string.text_complete);
        } else {
            showTaskPauseView(level2Table, multiDeliveryTable);
        }
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
        mHandler.postDelayed(() -> EasyDialog.newCustomInstance(this, R.layout.layout_emergency_stop_dialog).emergencyStopOn(), 500);
    }

    @Override
    public List<Integer> showArrivedAtTargetTable(Map<Integer, String> originTable, String dest) {
        showArrivedAtView();
        tvCurrentTable.setText(getString(R.string.text_please_pick_up_meal, dest));
        List<TextView> layers = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        List<GifImageView> indicators = tableLayer == 3 ? Arrays.asList(indicatorOne, indicatorTwo, indicatorThree) : Arrays.asList(indicatorOne, indicatorTwo, indicatorThree, indicatorFour);
        String table;
        TextView layer;
        GifImageView indicator;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            table = originTable.get(i + 1);
            layer = layers.get(i);
            if (layer == null) continue;
            indicator = indicators.get(i);
            if (table != null) {
                layer.setText(table);
            } else {
                layer.setText("");
            }
            if (dest.equals(table)) {
                list.add(i + 1);
                layer.setBackgroundResource(R.drawable.bg_layer_selected);
                ((GifDrawable) indicator.getDrawable()).start();
                indicator.setVisibility(View.VISIBLE);
            } else {
                layer.setBackgroundResource(R.drawable.bg_layer_normal);
                ((GifDrawable) indicator.getDrawable()).stop();
                indicator.setVisibility(View.INVISIBLE);
            }
        }
        return list;
    }


    @Override
    public List<Integer> showArrivedAtMultiDeliveryTargetTable(TreeMap<Integer, List<String>> originDeliveryTable, TreeMap<Integer, List<String>> multiDeliveryTable, String dest) {
        showArrivedAtView();
        tvCurrentTable.setText(getString(R.string.text_please_pick_up_meal, dest));
        List<TextView> layers = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        List<GifImageView> indicators = tableLayer == 3 ? Arrays.asList(indicatorOne, indicatorTwo, indicatorThree) : Arrays.asList(indicatorOne, indicatorTwo, indicatorThree, indicatorFour);
        List<String> tables;
        TextView layer;
        GifImageView indicator;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            tables = originDeliveryTable.get(i);
            layer = layers.get(i);
            if (layer == null) continue;
            indicator = indicators.get(i);
            String tablePrompt = tables == null ? "" : tables.toString().replace("[", "").replace("]", "");
            layer.setText(tablePrompt);
            if (tables != null && tables.contains(dest)) {
                list.add(i);
                List<String> strings = multiDeliveryTable.get(i);
                if (strings != null) {
                    strings.remove(dest);
                    if (strings.isEmpty()) {
                        multiDeliveryTable.remove(i);
                    }
                }
                layer.setBackgroundResource(R.drawable.bg_layer_selected);
                ((GifDrawable) indicator.getDrawable()).start();
                indicator.setVisibility(View.VISIBLE);
            } else {
                layer.setBackgroundResource(R.drawable.bg_layer_normal);
                ((GifDrawable) indicator.getDrawable()).stop();
                indicator.setVisibility(View.INVISIBLE);
            }
        }
        return list;
    }

    @Override
    public void onPauseCountDown(boolean isPauseCountdown, long seconds) {
        int taskMode = taskExecutingPresenter.getTaskMode();
        if (seconds > 1000) {
            if (isPauseCountdown || taskMode == Constants.MODE_RECYCLE_2 || taskMode == Constants.MODE_CALLING) {
                //暂停的话设置暂停倒计时
                tvCountDown.setText("");
            } else if (taskMode == Constants.MODE_DELIVERY_FOOD || taskMode == Constants.MODE_MULTI_DELIVERY) {
                //配送模式设置下一桌倒计时
                tvNextTableCountDown.setText("");
            } else if (taskMode == Constants.MODE_BIRTHDAY) {
                //生日模式设置结束倒计时
                btnComplete.setText(getString(R.string.text_complete));
            }
        } else {
            SpannableString str = new SpannableString(getString(R.string.text_task_continued_in_minutes, seconds));
            int length = String.valueOf(seconds).length();
            int start = str.toString().split("\\d")[0].length();
            str.setSpan(new ForegroundColorSpan(Color.BLUE), start, start + length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            str.setSpan(new AbsoluteSizeSpan(40), start, start + length, 0);
            str.setSpan(new StyleSpan(Typeface.BOLD), start, start + length, 0);
            str.setSpan(new ScaleXSpan(1.1f), start, start + length, 0);
            if (isPauseCountdown || taskMode == Constants.MODE_RECYCLE_2 || taskMode == Constants.MODE_CALLING) {
                //暂停的话设置暂停倒计时
                tvCountDown.setText(str);
            } else if (taskMode == Constants.MODE_DELIVERY_FOOD || taskMode == Constants.MODE_MULTI_DELIVERY) {
                //配送模式设置下一桌倒计时
                tvNextTableCountDown.setText(str);
            } else if (taskMode == Constants.MODE_BIRTHDAY) {
                //生日模式设置结束倒计时
                btnComplete.setText(getString(R.string.text_complete_countdown, seconds));
            }
        }
    }

    @Override
    protected void onCustomEncounterObstacle() {
        taskExecutingPresenter.onEncounterObstacle();
    }

    @Override
    protected void onCustomPointNotFoundEvent(String name) {
        taskExecutingPresenter.onPointNotFound(this);
    }

    @Override
    protected void onCustomChargingPileNotFound() {
        taskExecutingPresenter.onChargingPileNotFound(this);
    }

    @Override
    protected void onCustomPowerConnected() {
        taskExecutingPresenter.onTaskFinished(0, null, null);
    }

    @Override
    public void onCountDownFinished(boolean isPauseCountdown) {
        taskExecutingPresenter.onTaskResume(this, isPauseCountdown);
    }

    @Override
    protected void onNavigationCancelResult(int code) {
        super.onNavigationCancelResult(code);
        taskExecutingPresenter.onNavigationCancelResult(this, code);
    }

    @Override
    protected void onNavigationCompleteResult(int code, String name, float mileage) {
        super.onNavigationCompleteResult(code, name, mileage);
        taskExecutingPresenter.onNavigationCompleteResult(this, code, name, mileage);
    }

    @Override
    protected void onNavigationResumeResult(int code) {
        super.onNavigationResumeResult(code);
        if (code == -1) {
            ros.navigationByPoint(taskExecutingPresenter.getDispatchTargetPoint());
        }
    }

    @Override
    protected void onNavigationStartResult(int code, String name) {
        super.onNavigationStartResult(code, name);
        taskExecutingPresenter.onNavigationStartResult(this, code, name);
    }

    @Override
    protected void onSensorsError(Event.OnCheckSensorsEvent event) {
        taskExecutingPresenter.onSensorsError(this, event);
    }


    @Override
    protected void onCustomTimeStamp(RobotEvent.OnTimeEvent event) {
        refreshTime();
        taskExecutingPresenter.onTimeStamp(this);
    }

    @Override
    protected void onCustomDockFailed() {
        taskExecutingPresenter.onDockFailed(this);
    }

    @Override
    protected void onCustomPositionObtained(double[] position) {
        taskExecutingPresenter.onPositionObtained(this, position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGlobalPathObtained(Event.OnGlobalPathEvent event) {
        taskExecutingPresenter.onGlobalPathEvent(event.arrays.toArray(new Double[0]));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomTouchEvent(RobotEvent.OnTouchEvent event) {
        taskExecutingPresenter.onTouch(this);
    }

    @Override
    protected void onCustomResponseCallingEvent(String next) {
        State state = ros.getState();
        if ((state == State.CRUISING || state == State.RETURNING) && !taskExecutingPresenter.isLowCharge()) {
            taskExecutingPresenter.onCustomResponseCallingEvent(next);
        }
    }

    @Override
    public void showArrivedAtCallingTable(String dest) {
        showTaskPauseView(null, null);
        tvTaskMode.setText(getString(R.string.text_arrive_at_target_point, dest));
        tvTaskState.setText(getString(R.string.text_wait_for_operation));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMissPoseEvent(Event.OnMissPoseEvent event) {
        if (event.result == 1)
            taskExecutingPresenter.onMissPose(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRoutePointEvent(Event.OnRoutePointEvent event) {
        if (navigationMode == Mode.FIX_ROUTE)
            DispatchUtil.Companion.updateRoutePoint(event.points, pointInfoQueue);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpecialAreaEvent(Event.OnSpecialAreaEvent event) {
        taskExecutingPresenter.onSpecialArea(event.name);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetPlanDijEvent(Event.OnGetPlanDijEvent event) {
        taskExecutingPresenter.onGetPlanDij(event.points);
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

    @Override
    public void resumeByDispatch() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastResumeTime > 10_000 && !VoiceHelper.isPlaying()) {
            VoiceHelper.play("voice_robot_will_pass");
            lastResumeTime = currentTimeMillis;
        }
        if (ros.isNavigating()) {
            ros.resumeNavigation();
        } else {
            ros.navigationByPoint(taskExecutingPresenter.getDispatchTargetPoint());
        }
        dispatchState = DispatchState.INIT;
        if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();
    }
}