package com.reeman.delige.activities;

import static com.reeman.delige.base.BaseApplication.activityStack;
import static com.reeman.delige.base.BaseApplication.mRobotInfo;
import static com.reeman.delige.base.BaseApplication.navigationMode;
import static com.reeman.delige.base.BaseApplication.pointInfoQueue;
import static com.reeman.delige.base.BaseApplication.ros;
import static com.reeman.delige.base.BaseApplication.shouldRefreshPoints;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.delige.R;
import com.reeman.delige.adapter.LayerAdapter;
import com.reeman.delige.adapter.RouteListAdapter;
import com.reeman.delige.adapter.TableGroupAdapter;
import com.reeman.delige.adapter.TableNumberAdapter;
import com.reeman.delige.base.BaseActivity;
import com.reeman.delige.calling.CallingHelper;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.MainContract;
import com.reeman.delige.dispatch.DispatchManager;
import com.reeman.delige.dispatch.boardcast.LANBroadcaster;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.delige.dispatch.util.PointUtil;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.light.LightController;
import com.reeman.delige.navigation.Mode;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.presenter.impl.MainPresenter;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.PathPoint;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.PointInfo;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.utils.ClickHelper;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.ScreenUtils;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.utils.WIFIUtils;
import com.reeman.delige.widgets.DeliveryModeSelectDialog;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.widgets.EmptyView;
import com.reeman.delige.widgets.GuideHelper;
import com.reeman.delige.widgets.GuideItem;
import com.reeman.delige.widgets.LayoutIdData;
import com.reeman.delige.widgets.TooltipHelper;
import com.reeman.delige.event.Event;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends BaseActivity implements ClickHelper.OnFastClickListener,
        TableNumberAdapter.OnTableNumberClickListener,
        DeliveryModeSelectDialog.OnDeliveryModeSelectListener, MainContract.View,
        TableGroupAdapter.OnTableGroupItemClickListener, SwipeRefreshLayout.OnRefreshListener,
        EmptyView.OnRefreshBtnClickListener, EasyDialog.OnViewClickListener {

    private TooltipHelper tooltipHelper;
    private ClickHelper clickHelper;
    private MainPresenter mainPresenter;
    private RecyclerView rvTableGroup;
    private TableGroupAdapter tableGroupAdapter;
    private GridView gvTableNumber;
    private TableNumberAdapter tableNumberAdapter;
    private SwipeRefreshLayout tableViewRefreshLayout;
    private LinearLayoutManager layoutManager;
    private EmptyView noDataView;

    private int tableLayer = 3;
    private int currentLayer = 0;
    private int displayColumn;
    private int currentModeIndex;
    private LinearLayout centerContent;
    private LinearLayout tableView;
    private RecyclerView rvRouteList;
    private RouteListAdapter routeListAdapter;
    private SwipeRefreshLayout routeViewRefreshLayout;
    private LinearLayout refreshRoot;
    private DrawerLayout drawerLayout;
    private boolean isFirstEnter = true;
    private boolean isNoPointPromptShowing = false;
    private LayerAdapter layerAdapter;
    private TextView tvLayerOne;
    private TextView tvLayerTwo;
    private TextView tvLayerThree;
    private TextView tvLayerFour;
    private ImageView ivBirthdayIcon;
    private ImageView ivCancelSelectOne;
    private ImageView ivCancelSelectTwo;
    private ImageView ivCancelSelectThree;
    private ImageView ivCancelSelectFour;
    private HashMap<Integer, List<String>> multiDeliveryData;
    private NestedScrollView scrollView;
    private String lastRelocateTarget = null;
    private ImageView ivRobotCount;
    private int lastRobotCount = -1;
    private DeliveryModeSelectDialog mDeliveryModeSelectDialog;
    private int positionUpdateCount;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            ScreenUtils.hideBottomUIMenu(this);
        }
    }

    @Override
    protected boolean shouldResponse2TimeEvent() {
        return true;
    }

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
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        clickHelper = new ClickHelper(this);
        mainPresenter = new MainPresenter(this);
        if (ros.isCharging()) {
            mainPresenter.closeAllLights();
        } else {
            mainPresenter.openAllLights();
        }

        currentModeIndex = SpManager.getInstance().getInt(Constants.KEY_CURRENT_DELIVERY_MODE, Constants.DEFAULT_DELIVERY_MODE);
        displayColumn = SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN);
    }

    @Override
    protected void initCustomView() {
        setOnClickListeners(
                R.id.tv_hostname,
                R.id.view_tooltip_click,
                R.id.robot_outline,
                R.id.btn_start,
                R.id.tv_goto_setting,
                R.id.tv_map_deploy,
                R.id.tv_goto_charge,
                R.id.tv_goto_product_point);
        ivRobotCount = $(R.id.iv_robot_count);

        centerContent = $(R.id.ll_center_content);
        drawerLayout = $(R.id.dl_root);
        if (currentModeIndex == Constants.MODE_CRUISE) {
            //初始化餐位界面
            initRouteView();
        } else {
            //初始化路线界面
            initTableView();
        }
    }

    /**
     * 初始化路线列表界面
     */
    private void initRouteView() {
        if (routeViewRefreshLayout != null) return;
        routeViewRefreshLayout = (SwipeRefreshLayout) LayoutInflater.from(this).inflate(R.layout.layout_center_content_routes, $(R.id.ll_center_content), false);
        routeViewRefreshLayout.setOnRefreshListener(this);
        rvRouteList = routeViewRefreshLayout.findViewById(R.id.rv_route_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvRouteList.setLayoutManager(layoutManager);
        DividerItemDecoration decor = new DividerItemDecoration(this, layoutManager.getOrientation());
        rvRouteList.addItemDecoration(decor);
        routeListAdapter = new RouteListAdapter();
        rvRouteList.setAdapter(routeListAdapter);
        centerContent.addView(routeViewRefreshLayout);
    }

    /**
     * 初始化餐位号，分组view
     */
    private void initTableView() {
        if (tableView != null) return;

        tableView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_center_content_points, $(R.id.ll_center_content), false);

        //刷新
        tableViewRefreshLayout = tableView.findViewById(R.id.refresh_layout);
        refreshRoot = tableView.findViewById(R.id.ll_refresh_root);
        tableViewRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.purple_200), getResources().getColor(R.color.purple_500), getResources().getColor(R.color.purple_700));
        tableViewRefreshLayout.setOnRefreshListener(this);

        //桌号
        gvTableNumber = tableView.findViewById(R.id.gv_table_number);
        gvTableNumber.setNumColumns(displayColumn);
        gvTableNumber.setHorizontalSpacing(15);
        gvTableNumber.setVerticalSpacing(8);
        tableNumberAdapter = new TableNumberAdapter(this);
        gvTableNumber.setAdapter(tableNumberAdapter);

        //餐位分组
        tableView.findViewById(R.id.iv_pre_btn).setOnClickListener(this);
        tableView.findViewById(R.id.iv_next_btn).setOnClickListener(this);
        rvTableGroup = tableView.findViewById(R.id.rv_table_group);
        tableGroupAdapter = new TableGroupAdapter();
        tableGroupAdapter.setOnTableGroupItemClickListener(this);
        rvTableGroup.setAdapter(tableGroupAdapter);
        layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return true;
            }
        };
        rvTableGroup.setLayoutManager(layoutManager);
        centerContent.addView(tableView);
    }

    @Override
    protected void onCustomPositionObtained(double[] position) {
        if (navigationMode == Mode.FIX_ROUTE) {
            ArrayList<RobotInfo> robotList = DispatchUtil.Companion.getRobotList();
            int size = robotList.size();
            if (lastRobotCount != size) {
                lastRobotCount = size;
                int identifier = getResources().getIdentifier("ic_robot_count_" + size, "drawable", getPackageName());
                ivRobotCount.setImageResource(identifier);
            }
            if (++positionUpdateCount >5){
                positionUpdateCount = 0;
                PathPoint pathPoint = PointUtil.Companion.calculateNearestPoint(position);
                if (pathPoint == null){
                    if (!pointInfoQueue.isEmpty() ){
                        pointInfoQueue.clear();
                    }
                }else {
                    if (pointInfoQueue.size() !=1){
                        pointInfoQueue.clear();
                        pointInfoQueue.add(new PointInfo(pathPoint.name,1));
                    }else if (pointInfoQueue.size() == 1 && pointInfoQueue.peek() != null&& !pointInfoQueue.peek().name.equals(pathPoint.name)) {
                        pointInfoQueue.clear();
                        pointInfoQueue.add(new PointInfo(pathPoint.name,1));
                    }
                }
            }
//            PathPoint pathPoint = PointUtil.Companion.calculateNearestPoint(position);
//            if (pathPoint == null) {
//                if (!pointQueue.isEmpty()) {
//                    pointQueue.clear();
//                }
//            } else {
//                if (pointQueue.size() != 1 || !Objects.equals(pointQueue.peek(), pathPoint.name)) {
//                    pointQueue.clear();
//                    pointQueue.add(pathPoint.name);
//                }
//            }
        }
    }

    @Override
    protected void onCustomNetworkStateChange(RobotEvent.OnNetworkEvent event) {
        String connectWifiSSID = WIFIUtils.getConnectWifiSSID(this);
        ((TextView) $(R.id.tv_android_wifi)).setText(getString(R.string.text_current_android_connection, connectWifiSSID));
        ((TextView) $(R.id.tv_android_host_ip)).setText(getString(R.string.text_current_android_connection, WIFIUtils.getIpAddress(this)));
        NetworkInfo networkInfo = event.networkIntent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
            ((ImageView) $(R.id.iv_wifi)).setImageResource(R.drawable.icon_wifi_on);
            return;
        }
        ((ImageView) $(R.id.iv_wifi)).setImageResource(R.drawable.icon_wifi_off);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isFirstEnter = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentTime();

        refreshPowerState(ros.getLevel());

        refreshNetworkState();

        tooltipHelper = TooltipHelper.create(this, $(R.id.view_tooltip_anchor),
                $(R.id.dl_root),
                getResources().getStringArray(R.array.deliveryMode)[currentModeIndex - 1]);
        tooltipHelper.show();


        CallingHelper instance = CallingHelper.getInstance();
        if (!instance.isStart()) {
            try {
                instance.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ros.getHostIP();
        ros.getHostName();
        mHandler.postDelayed(() -> ros.positionAutoUploadControl(true), 800);
        if (isFirstEnter) {
            ros.requestPowerOnTime();
            mHandler.postDelayed(chargeRunnable, 10000);
        }
        tableLayer = SpManager.getInstance().getInt(Constants.KEY_TABLE_LAYER, Constants.DEFAULT_TABLE_LAYER);
    }


    private void refreshNetworkState() {
        String connectWifiSSID = WIFIUtils.getConnectWifiSSID(this);
        ((TextView) $(R.id.tv_android_wifi)).setText(getString(R.string.text_current_android_connection, connectWifiSSID));
        ((TextView) $(R.id.tv_android_host_ip)).setText(getString(R.string.text_current_android_connection, WIFIUtils.getIpAddress(this)));
        if (WIFIUtils.isNetworkConnected(this)) {
            ((ImageView) $(R.id.iv_wifi)).setImageResource(R.drawable.icon_wifi_on);
            return;
        }
        ((ImageView) $(R.id.iv_wifi)).setImageResource(R.drawable.icon_wifi_off);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (VoiceHelper.isPlaying()) VoiceHelper.pause();
        tooltipHelper.dismiss();
    }

    private void refreshPowerState(int level) {
        TextView tvPower = $(R.id.tv_power);
        tvPower.setText(level + "%");
        if (level < 20) {
            tvPower.setTextColor(getResources().getColor(R.color.warning));
        } else {
            tvPower.setTextColor(Color.WHITE);
        }
    }

    @Override
    protected void onCustomBatteryChange(int level, int plug) {
        refreshPowerState(level);
    }

    @Override
    protected void onCustomTimeStamp(RobotEvent.OnTimeEvent event) {
        refreshCurrentTime();
        super.onCustomTimeStamp(event);
    }

    private void refreshCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        ((TextView) $(R.id.tv_time)).setText(simpleDateFormat.format(new Date()));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostIpLoaded(Event.OnIpEvent event) {
        ((TextView) $(R.id.tv_nav_host_ip)).setText(getString(R.string.text_current_host_ip, event.ipAddress));
        ((TextView) $(R.id.tv_nav_wifi)).setText(getString(R.string.text_current_nav_wifi, TextUtils.isEmpty(event.wifiName) ? getString(R.string.text_not_connected) : event.wifiName));
        ros.autoUploadLogs(event.ipAddress);
        mainPresenter.startDeployGuide(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostnameLoaded(Event.OnHostnameEvent event) {
        if (!DispatchManager.isStarted()) {
            try {
                DispatchManager.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        if (!LANBroadcaster.isStart()){
//            LANBroadcaster.getInstance(mApp);
//        }
        mRobotInfo.setHostname(event.hostname);
        ((TextView) $(R.id.tv_hostname)).setText(event.hostname);
        ((TextView) $(R.id.tv_nav_host_name)).setText(getString(R.string.text_current_hostname, event.hostname));
    }

    @Override
    public void showGuideDeployDialog() {
        EasyDialog
                .getInstance(this)
                .confirm(getString(R.string.voice_start_map_building_procedure),
                        new EasyDialog.OnViewClickListener() {
                            @Override
                            public void onViewClick(Dialog dialog, int id) {
                                dialog.dismiss();
                                if (id == R.id.btn_confirm) {
                                    startActivity(new Intent(MainActivity.this, MapBuildingActivity.class));
                                } else if (id == R.id.btn_cancel) {
                                    SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_MAP_BUILDING_GUIDE, true).apply();
                                    mHandler.postDelayed(() -> mainPresenter.startOperationGuide(MainActivity.this), 300);
                                }
                            }
                        });
    }

    @Override
    public void showOperationGuideView() {
        EasyDialog
                .getInstance(this)
                .confirm(getString(R.string.voice_not_guide_for_novice),
                        new EasyDialog.OnViewClickListener() {
                            @Override
                            public void onViewClick(Dialog dialog, int id) {
                                dialog.dismiss();
                                SpManager.getInstance().edit().putBoolean(Constants.KEY_IS_OPERATION_GUIDED, true).apply();
                                if (id == R.id.btn_confirm) {
                                    createGuideViews();
                                } else if (id == R.id.btn_cancel) {
                                    showAllGuidanceCompleteView();
                                }
                            }
                        });
    }

    private void createGuideViews() {
        String nextStep = getString(R.string.text_next_step);
        List<GuideItem> guideViews = Arrays.asList(
                new GuideItem($(R.id.view_tooltip_click), "voice_guide_working_mode", getString(R.string.voice_guide_working_mode), nextStep, LayoutIdData.RIGHT, 480, 240),
                new GuideItem($(R.id.sv_container), "voice_guide_robot_level", getString(R.string.voice_guide_robot_level), nextStep, LayoutIdData.RIGHT, 400, 240),
                new GuideItem($(R.id.ll_center_content), "voice_guide_table_number", getString(R.string.voice_guide_table_number), nextStep, LayoutIdData.LEFT, 300, 400),
                new GuideItem($(R.id.btn_start), "voice_guide_start_btn", getString(R.string.voice_guide_start_btn), nextStep, LayoutIdData.TOP, 600, 240, Gravity.END | Gravity.CENTER_VERTICAL),
                new GuideItem($(R.id.ll_drawer), "voice_guide_hide_drawer", getString(R.string.voice_guide_hide_drawer), nextStep, LayoutIdData.LEFT, 400, 280, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM));

        GuideHelper.startGuide(this, guideViews, new GuideHelper.OnEventListener() {
            @Override
            public void onComplete() {
                drawerLayout.closeDrawer(Gravity.RIGHT);
                VoiceHelper.play("voice_congratulate_finish_action_guide");
                EasyDialog
                        .getInstance(MainActivity.this)
                        .warn(getString(R.string.voice_congratulate_finish_action_guide),
                                (dialog, id) -> {
                                    if (id == R.id.btn_confirm) {
                                        dialog.dismiss();
                                        showAllGuidanceCompleteView();
                                    }
                                });
            }

            @Override
            public void onGuideItemStart(int currentGuide) {
                if (currentGuide == guideViews.size() - 1) {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                }
            }
        });
    }

    @Override
    public void showAllGuidanceCompleteView() {
        refreshByCurrentMode(isFirstEnter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.view_tooltip_click:
                onDeliveryModeBtnClick();
                break;
            case R.id.tv_hostname:
                onHostnameBtnClick();
                break;
            case R.id.btn_start:
                onStartBtnClick();
                break;
            case R.id.iv_pre_btn:
                scrollTo(tableGroupAdapter.getSelectedIndex() - 1);
                break;
            case R.id.iv_next_btn:
                scrollTo(tableGroupAdapter.getSelectedIndex() + 1);
                break;
            case R.id.tv_layer_one:
            case R.id.tv_layer_two:
            case R.id.tv_layer_three:
            case R.id.tv_layer_four:
                onLayerClick(v, id);
                break;
            case R.id.iv_cancel_selected_one:
            case R.id.iv_cancel_selected_two:
            case R.id.iv_cancel_selected_three:
            case R.id.iv_cancel_selected_four:
                onCancelSelectClick(v, id);
                break;
            case R.id.tv_goto_charge:
                onGotoCharge();
                break;
            case R.id.tv_goto_product_point:
                onGotoProductPoint();
                break;
            case R.id.tv_map_deploy:
                onMapDeploy();
                break;
            case R.id.tv_goto_setting:
                onGotoSetting();
                break;
        }
    }

    private void onMapDeploy() {
        drawerLayout.closeDrawer(Gravity.RIGHT);
        Intent intent = new Intent(this, MapBuildingActivity.class);
        startActivity(intent);
    }

    private void onGotoSetting() {
        drawerLayout.closeDrawer(Gravity.RIGHT);
        int settingPassword = SpManager.getInstance().getInt(Constants.KEY_SETTING_PASSWORD_CONTROL, Constants.KEY_DEFAULT_SETTING_PASSWORD_CONTROL);
        if (settingPassword == 1) {
            EasyDialog.newCustomInstance(this, R.layout.layout_input_setting_password).showInputPasswordDialog(this);
        } else {
            BaseActivity.startup(this, SettingActivity.class);
        }
    }

    @Override
    public void onViewClick(Dialog dialog, int id) {
        if (id == R.id.btn_confirm) {
            EditText editText = (EditText) EasyDialog.getInstance().getView(R.id.et_password);
            String str = editText.getText().toString();
            if (TextUtils.equals(str, Constants.KEY_SETTING_PASSWORD)) {
                dialog.dismiss();
                mHandler.postDelayed(() -> BaseActivity.startup(this, SettingActivity.class), 300);
            } else {
                ToastUtils.showShortToast(getString(R.string.text_password_error));
            }
        } else {
            dialog.dismiss();
        }
    }

    private void onGotoProductPoint() {
        drawerLayout.closeDrawer(Gravity.RIGHT);
        if (ros.isEmergencyStopDown()) {
            VoiceHelper.play("voice_scram_stop_turn_on");
            return;
        }
        if (Event.getCoreData().charger == 3) {
            VoiceHelper.play("voice_charging_and_can_not_move");
            return;
        }
        if (TextUtils.isEmpty(DestHelper.getInstance().getProductPoint())){
            VoiceHelper.play("voice_not_found_product_point");
            EasyDialog.getInstance(this).warnError(getString(R.string.voice_not_found_product_point));
            return;
        }
        Intent intent = new Intent(this, ReturningActivity.class);
        intent.putExtra(Constants.TASK_TARGET, Constants.TYPE_GOTO_PRODUCT_POINT);
        startActivityForResult(intent, REQUEST_CODE_FOR_PRODUCT_POINT);
    }

    private void onGotoCharge() {
        drawerLayout.closeDrawer(Gravity.RIGHT);
        if (ros.isEmergencyStopDown()) {
            VoiceHelper.play("voice_scram_stop_turn_on");
            return;
        }
        if (ros.isCharging()) {
            ToastUtils.showShortToast(getString(R.string.text_is_charging));
            return;
        }
        if (TextUtils.isEmpty(DestHelper.getInstance().getChargePoint())){
            VoiceHelper.play("voice_not_found_charging_pile");
            EasyDialog.getInstance(this).warnError(getString(R.string.voice_not_found_charging_pile));
            return;
        }
        Intent intent = new Intent(this, ReturningActivity.class);
        intent.putExtra(Constants.TASK_TARGET, Constants.TYPE_GOTO_CHARGE);
        startActivityForResult(intent, REQUEST_CODE_FOR_CHARGE);
    }

    /**
     * 开始按钮被点击
     */
    private void onStartBtnClick() {
        if (ros.isEmergencyStopDown()) {
            EasyDialog.getInstance(this).warnError(getString(R.string.voice_scram_stop_turn_on));
            VoiceHelper.play("voice_scram_stop_turn_on");
            return;
        }
        if (Event.getCoreData().charger == 3) {
            VoiceHelper.play("voice_charging_and_can_not_move");
            EasyDialog.getInstance(this).warnError(getString(R.string.voice_charging_and_can_not_move));
            return;
        }
        if (currentModeIndex == Constants.MODE_DELIVERY_FOOD) {
            List<TextView> layers = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
            HashMap<Integer, String> level2TableMap = new HashMap<>();
            for (int i = 0; i < layers.size(); i++) {
                TextView textView = layers.get(i);
                if (textView == null) continue;
                String target = textView.getText().toString();
                if (!TextUtils.isEmpty(target)) {
                    level2TableMap.put(i + 1, target);
                }
            }
            if (level2TableMap.isEmpty()) {
                ToastUtils.showShortToast(getString(R.string.text_no_delivery_task));
                return;
            }
            mainPresenter.startDeliveryFoodTask(this, level2TableMap);
        } else if (currentModeIndex == Constants.MODE_BIRTHDAY) {
            String target = tvLayerOne.getText().toString();
            if (TextUtils.isEmpty(target)) {
                ToastUtils.showShortToast(getString(R.string.text_no_delivery_task));
                return;
            }
            mainPresenter.startBirthdayTask(this, target);
        } else if (currentModeIndex == Constants.MODE_RECYCLE_2) {
            List<String> recycleTables = tableNumberAdapter.getRecycleTables();
            if (recycleTables == null || recycleTables.isEmpty()) {
                return;
            }
            HashMap<Integer, String> map = new HashMap<>();
            for (int i = 0; i < recycleTables.size(); i++) {
                map.put(i + 1, recycleTables.get(i));
            }
            mainPresenter.startRecycle2Task(this, map);
        } else if (currentModeIndex == Constants.MODE_CRUISE) {
            Route currentRoute = routeListAdapter.getCurrentRoute();
            if (currentRoute == null) {
                VoiceHelper.play("voice_please_choose_cruise_route");
                ToastUtils.showShortToast(getString(R.string.voice_please_choose_cruise_route));
                return;
            }
            mainPresenter.startCruiseTask(this, currentRoute);
        } else if (currentModeIndex == Constants.MODE_RECYCLE) {
            Route currentRoute = routeListAdapter.getCurrentRoute();
            if (currentRoute == null) {
                VoiceHelper.play("voice_please_choose_recycle_route");
                ToastUtils.showShortToast(getString(R.string.voice_please_choose_recycle_route));
                return;
            }
            mainPresenter.startRecycleTask(this, currentRoute);
        } else if (currentModeIndex == Constants.MODE_MULTI_DELIVERY) {
            if (multiDeliveryData == null) {
                ToastUtils.showShortToast(getString(R.string.text_no_delivery_task));
                return;
            }
            List<String> oneLayerTables = multiDeliveryData.get(0);
            List<String> twoLayerTables = multiDeliveryData.get(1);
            List<String> threeLayerTables = multiDeliveryData.get(2);
            List<String> fourLayerTables = multiDeliveryData.get(3);
            if (oneLayerTables == null || oneLayerTables.isEmpty()) {
                multiDeliveryData.remove(0);
            }
            if (twoLayerTables == null || twoLayerTables.isEmpty()) {
                multiDeliveryData.remove(1);
            }
            if (threeLayerTables == null || threeLayerTables.isEmpty()) {
                multiDeliveryData.remove(2);
            }
            if (fourLayerTables == null || fourLayerTables.isEmpty()) {
                multiDeliveryData.remove(3);
            }
            if (multiDeliveryData.isEmpty()) {
                ToastUtils.showShortToast(getString(R.string.text_no_delivery_task));
                return;
            }
            mainPresenter.startMultiDeliveryTask(this, multiDeliveryData);
        }
    }

    /**
     * 当点击配送层
     *
     * @param v
     * @param id
     */
    private void onLayerClick(View v, int id) {
        if (currentModeIndex == Constants.MODE_CRUISE || currentModeIndex == Constants.MODE_RECYCLE)
            return;

        if (currentModeIndex == Constants.MODE_BIRTHDAY) {
            String s = ((TextView) v).getText().toString();
            if (TextUtils.isEmpty(s)) return;
            int tableGroup = mainPresenter.getTableGroupByTableName(s);
            tableGroupAdapter.setSelectedIndex(tableGroup);
            generateTable(tableGroup, DestHelper.getInstance().getPoints());
            scrollTo(tableGroup);
            return;
        }

        List<TextView> textViews = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        List<Integer> ids = tableLayer == 3 ? Arrays.asList(R.id.tv_layer_one, R.id.tv_layer_two, R.id.tv_layer_three) : Arrays.asList(R.id.tv_layer_one, R.id.tv_layer_two, R.id.tv_layer_three, R.id.tv_layer_four);

        if (currentModeIndex == Constants.MODE_MULTI_DELIVERY) {
            int index = ids.indexOf(id);
            if (currentLayer == index) return;
            tableNumberAdapter.setSelect(multiDeliveryData == null ? null : multiDeliveryData.get(index));
            textViews.get(currentLayer).setBackgroundResource(R.drawable.bg_layer_normal);
            textViews.get(index).setBackgroundResource(R.drawable.bg_layer_selected);
            currentLayer = index;
            tableGroupAdapter.setSelectedIndex(0);
            generateTable(0, DestHelper.getInstance().getPoints());
            scrollTo(0);
            return;
        }

        for (int i = 0; i < textViews.size(); i++) {
            TextView textView = textViews.get(i);
            if (textView == null) continue;
            Integer resId = ids.get(i);
            if (id == resId) {
                if (currentLayer != i) {
                    currentLayer = i;
                    textView.setBackgroundResource(R.drawable.bg_layer_selected);
                    String str = ((TextView) v).getText().toString();
                    if (TextUtils.isEmpty(str)) continue;
                    int tableGroup = mainPresenter.getTableGroupByTableName(str);
//                    tableGroupAdapter.setSelectedIndex(tableGroup);
                    generateTable(tableGroup, DestHelper.getInstance().getPoints());
                    scrollTo(tableGroup);
                }
            } else {
                textView.setBackgroundResource(R.drawable.bg_layer_normal);
            }
        }
    }

    /**
     * 取消当前选择的桌号
     *
     * @param v
     * @param id
     */
    private void onCancelSelectClick(View v, int id) {
        v.setVisibility(View.INVISIBLE);
        List<ImageView> list = tableLayer == 3 ? Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree) : Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree, ivCancelSelectFour);
        List<TextView> layers = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        for (int i = 0; i < list.size(); i++) {
            ImageView imageView = list.get(i);
            if (imageView == null) continue;
            if (imageView.getId() == id) {
                TextView textView = layers.get(i);
                textView.setText("");
                if (multiDeliveryData != null) {
                    List<String> strings = multiDeliveryData.get(i);
                    if (strings != null && !strings.isEmpty()) {
                        strings.clear();
                        if (currentLayer == i) {
                            tableNumberAdapter.resetSelected();
                            tableNumberAdapter.notifyDataSetChanged();
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 快速点击主机名退出app
     */
    private void onHostnameBtnClick() {
        clickHelper.fastClick();
    }

    /**
     * 选择配送模式
     */
    private void onDeliveryModeBtnClick() {
        if (mDeliveryModeSelectDialog != null && mDeliveryModeSelectDialog.isShowing())
            mDeliveryModeSelectDialog.dismiss();

        mDeliveryModeSelectDialog = new DeliveryModeSelectDialog(this,
                SpManager.getInstance().getInt(Constants.KEY_CURRENT_DELIVERY_MODE, Constants.DEFAULT_DELIVERY_MODE), this);
        mDeliveryModeSelectDialog.show();
    }

    @Override
    public void onFastClick() {
        EasyDialog.getInstance(this).confirm(getString(R.string.text_exit_app), (dialog, id) -> {
            if (id == R.id.btn_confirm) {
                DispatchManager.stop();
                ros.unInit();
                LightController.getInstance().release();
                for (Activity activity : activityStack) {
                    activity.finish();
                }
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
            dialog.dismiss();
        });
    }

    /**
     * 配送模式被选中
     *
     * @param mode
     */
    @Override
    public void onDeliveryModeSelect(int mode) {
        if (currentModeIndex == mode) return;
        currentModeIndex = mode;
        SpManager.getInstance().edit().putInt(Constants.KEY_CURRENT_DELIVERY_MODE, mode).apply();
        if (tooltipHelper != null) {
            tooltipHelper.dismiss();
        }
        tooltipHelper = TooltipHelper.create(this, $(R.id.view_tooltip_anchor),
                $(R.id.dl_root),
                getResources().getStringArray(R.array.deliveryMode)[mode - 1]);
        tooltipHelper.show();
        refreshByCurrentMode(false);
    }


    /**
     * 根据配送模式更新当前界面
     *
     * @param isManualRefresh
     */
    private void refreshByCurrentMode(boolean isManualRefresh) {
        scrollView = $(R.id.sv_container);
        scrollView.removeAllViews();
        if (currentModeIndex == Constants.MODE_RECYCLE_2) {
            scrollView.setClipChildren(true);
            scrollView.setClipToPadding(true);
            RecyclerView rvRecycleTables = new RecyclerView(this);
            rvRecycleTables.setPadding(40, 0, 0, 0);
            rvRecycleTables.setLayoutManager(new LinearLayoutManager(this));
            rvRecycleTables.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull @NotNull Rect outRect, @NonNull @NotNull View view, @NonNull @NotNull RecyclerView parent, @NonNull @NotNull RecyclerView.State state) {
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = 0;
                    } else {
                        outRect.top = 40;
                    }
                }
            });
            layerAdapter = new LayerAdapter();
            rvRecycleTables.setAdapter(layerAdapter);
            scrollView.addView(rvRecycleTables);
            if (tableView == null) initTableView();
            fetchPoints(isManualRefresh);
        } else {
            scrollView.setClipChildren(false);
            scrollView.setClipToPadding(false);

            $(R.id.robot_outline).setBackgroundResource(tableLayer == 3 ? R.drawable.bg_robot_outline_three_layers : R.drawable.bg_robot_outline_four_layers);
            LinearLayout fixedLayerContainer = (LinearLayout) LayoutInflater.from(this).inflate(tableLayer == 3 ? R.layout.layout_layers_three : R.layout.layout_layers_four, null, false);
            ivCancelSelectOne = fixedLayerContainer.findViewById(R.id.iv_cancel_selected_one);
            ivCancelSelectOne.setOnClickListener(this);
            ivCancelSelectTwo = fixedLayerContainer.findViewById(R.id.iv_cancel_selected_two);
            ivCancelSelectTwo.setOnClickListener(this);
            ivCancelSelectThree = fixedLayerContainer.findViewById(R.id.iv_cancel_selected_three);
            ivCancelSelectThree.setOnClickListener(this);
            ivCancelSelectFour = fixedLayerContainer.findViewById(R.id.iv_cancel_selected_four);
            if (ivCancelSelectFour != null) ivCancelSelectFour.setOnClickListener(this);

            tvLayerOne = fixedLayerContainer.findViewById(R.id.tv_layer_one);
            tvLayerOne.setOnClickListener(this);
            tvLayerTwo = fixedLayerContainer.findViewById(R.id.tv_layer_two);
            tvLayerTwo.setOnClickListener(this);
            tvLayerThree = fixedLayerContainer.findViewById(R.id.tv_layer_three);
            tvLayerThree.setOnClickListener(this);
            tvLayerFour = fixedLayerContainer.findViewById(R.id.tv_layer_four);
            if (tvLayerFour != null) tvLayerFour.setOnClickListener(this);

            ivBirthdayIcon = fixedLayerContainer.findViewById(R.id.iv_birthday_icon);
            scrollView.addView(fixedLayerContainer);
            currentLayer = 0;

            tvLayerOne.setText("");
            tvLayerTwo.setText("");
            tvLayerThree.setText("");
            if (tvLayerFour != null) tvLayerFour.setText("");

            ivCancelSelectOne.setVisibility(View.INVISIBLE);
            ivCancelSelectTwo.setVisibility(View.INVISIBLE);
            ivCancelSelectThree.setVisibility(View.INVISIBLE);
            if (ivCancelSelectFour != null) ivCancelSelectFour.setVisibility(View.INVISIBLE);

            if (currentModeIndex == Constants.MODE_BIRTHDAY) {
                tvLayerOne.setBackgroundResource(R.drawable.bg_layer_selected);
                tvLayerTwo.setBackgroundResource(R.drawable.bg_layer_disabled);
                tvLayerThree.setBackgroundResource(R.drawable.bg_layer_disabled);
                if (tvLayerFour != null)
                    tvLayerFour.setBackgroundResource(R.drawable.bg_layer_disabled);
                ivBirthdayIcon.setVisibility(View.VISIBLE);
                if (tableView == null) initTableView();
                fetchPoints(isManualRefresh);
            } else if (currentModeIndex == Constants.MODE_DELIVERY_FOOD || currentModeIndex == Constants.MODE_MULTI_DELIVERY) {
                tvLayerOne.setBackgroundResource(R.drawable.bg_layer_selected);
                tvLayerTwo.setBackgroundResource(R.drawable.bg_layer_normal);
                tvLayerThree.setBackgroundResource(R.drawable.bg_layer_normal);
                if (tvLayerFour != null)
                    tvLayerFour.setBackgroundResource(R.drawable.bg_layer_normal);
                ivBirthdayIcon.setVisibility(View.INVISIBLE);
                if (tableView == null) initTableView();
                if (multiDeliveryData != null) multiDeliveryData.clear();
                fetchPoints(isManualRefresh);
            } else {
                tvLayerOne.setBackgroundResource(R.drawable.bg_layer_disabled);
                tvLayerTwo.setBackgroundResource(R.drawable.bg_layer_disabled);
                tvLayerThree.setBackgroundResource(R.drawable.bg_layer_disabled);
                if (tvLayerFour != null)
                    tvLayerFour.setBackgroundResource(R.drawable.bg_layer_disabled);
                ivBirthdayIcon.setVisibility(View.INVISIBLE);
                if (routeViewRefreshLayout == null) initRouteView();
                fetchRoutes(isManualRefresh);
            }
        }
    }

    /**
     * 加载路线
     */
    private void fetchRoutes(boolean isManualRefresh) {
        startRefresh();
        mainPresenter.fetchRoutes(this, isManualRefresh);
    }

    /**
     * 开始刷新
     */
    private void startRefresh() {
        if (currentModeIndex == Constants.MODE_CRUISE) {
            routeViewRefreshLayout.setRefreshing(true);
        } else {
            tableViewRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * 加载点位
     */
    private void fetchPoints(boolean isManualRefresh) {
        startRefresh();
        if (shouldRefreshPoints) {
            SpManager.getInstance().edit().remove(Constants.KEY_POINT_INFO).apply();
            DestHelper destHelper = DestHelper.getInstance();
            destHelper.setPoints(null);
            destHelper.setChargePointCoordinate(null);
            destHelper.setChargePoint(null);
            destHelper.setRecyclePoint(null);
            destHelper.setProductPoint(null);
            destHelper.setRoutes(null);
        }
        if (navigationMode == Mode.AUTO_ROUTE) {
            mainPresenter.fetchPoints(this, isManualRefresh);
        } else {
            mainPresenter.fetchFixPoints(this, isManualRefresh);
        }
    }

    /**
     * 缺少充电桩或出品点
     *
     * @param list
     * @param isChargingPileMarked
     */
    @Override
    public void onLackOfRequiredPoint(List<? extends BaseItem> list, boolean isChargingPileMarked) {
        tableViewRefreshLayout.setRefreshing(false);
        drawerLayout.openDrawer(Gravity.RIGHT);
        if (!isNoPointPromptShowing) {
            GuideItem guideItem = new GuideItem($(R.id.tv_map_deploy), "voice_not_found_point_required", getString(R.string.voice_not_found_point_required), getString(R.string.text_i_see), LayoutIdData.LEFT, 480, 240);
            GuideHelper.createGuideItem(this, 1, guideItem, () -> isNoPointPromptShowing = false, null);
            isNoPointPromptShowing = true;
        }

        tableNumberAdapter.resetSelected();
        if (list.isEmpty()) {
            onEmptyDataLoaded(true);
        } else {
            tableGroupAdapter.setItemCount((int) Math.ceil(list.size() / (4.0 * SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN))));
            tableGroupAdapter.setSelectedIndex(0);
            generateTable(0, list);
        }
        if (isChargingPileMarked)
            showRelocatePrompt();
    }

    /**
     * 根据页数和餐位信息生成餐位列表
     *
     * @param page   页数
     * @param points 所有点位
     */
    private void generateTable(int page, List<? extends BaseItem> points) {
        if (points == null || points.isEmpty()) return;
        centerContent.removeAllViews();
        refreshRoot.removeAllViews();
        int columnPoint = SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN);
        gvTableNumber.setNumColumns(columnPoint);
        int min = page * 4 * columnPoint;
        int max = Math.min(points.size(), (page + 1) * 4 * columnPoint);
        List<? extends BaseItem> currentPagePoints = points.subList(min, max);
        tableNumberAdapter.setList(currentPagePoints);
        refreshRoot.addView(gvTableNumber);
        centerContent.addView(tableView);
    }

    private void scrollTo(int position) {
        if (position < 0) position = 0;
        if (position >= tableGroupAdapter.getItemCount())
            position = tableGroupAdapter.getItemCount() - 1;
        if (position == tableGroupAdapter.getSelectedIndex()) return;
        XLog.w("position ::" + position);
        tableGroupAdapter.setSelectedIndex(position);
        generateTable(position, DestHelper.getInstance().getPoints());
        rvTableGroup.smoothScrollToPosition(position);
    }

    /**
     * 点位加载失败，网络加载失败并且没有缓存
     */
    @Override
    public void onLoadFailed(boolean isPoint) {
        stopRefresh();
        ToastUtils.showShortToast(getString(R.string.text_point_loaded_failed));
        centerContent.removeAllViews();
        if (currentModeIndex == Constants.MODE_CRUISE) {
            routeListAdapter.setList(null, -1);
            centerContent.addView(routeViewRefreshLayout);
        } else {
            tableNumberAdapter.resetSelected();
            showNoDataView(getString(R.string.text_point_loaded_failed));
            centerContent.addView(tableView);
        }
        showRelocatePrompt();
    }

    /**
     * 点位数据为空
     */
    @Override
    public void onEmptyDataLoaded(boolean isPoint) {
        stopRefresh();
        String prompt = getString(R.string.text_loaded_success_with_no_data);
        ToastUtils.showShortToast(prompt);
        centerContent.removeAllViews();
        if (currentModeIndex == Constants.MODE_CRUISE) {
            routeListAdapter.setList(null, -1);
            centerContent.addView(routeViewRefreshLayout);
        } else {
            tableNumberAdapter.resetSelected();
            showNoDataView(prompt);
            centerContent.addView(tableView);
        }
        showRelocatePrompt();
    }

    /**
     * 显示没有数据View
     *
     * @param prompt
     */
    private void showNoDataView(String prompt) {
        if (noDataView == null) {
            noDataView = new EmptyView(this, this);
        }
        noDataView.updatePrompt(prompt);
        refreshRoot.removeAllViews();
        refreshRoot.addView(noDataView);
    }

    private void stopRefresh() {
        if (currentModeIndex == Constants.MODE_CRUISE) {
            routeViewRefreshLayout.setRefreshing(false);
        } else {
            tableViewRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 点位加载成功
     *
     * @param points
     */
    @Override
    public void onDataLoadSuccess(List<? extends BaseItem> points, boolean isPoint, boolean isManualRefresh) {
        if (isPoint) {
            //点位加载完成
            tableViewRefreshLayout.setRefreshing(false);
            currentLayer = 0;
            tableGroupAdapter.setItemCount((int) Math.ceil(points.size() / (4.0 * SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN))));
            tableGroupAdapter.setSelectedIndex(0);
            tableNumberAdapter.resetSelected();
            scrollTo(0);
            generateTable(0, points);
        } else {
            //路线加载完成
            centerContent.removeAllViews();
            routeViewRefreshLayout.setRefreshing(false);
            routeListAdapter.setList(points, 0);
            centerContent.addView(routeViewRefreshLayout);
        }

        showRelocatePrompt();
    }

    private void showRelocatePrompt() {
        if (ros.isRelocating()) return;
        ros.setRelocating(true);
        showRelocatePromptDialog();
    }

    private void showRelocatePromptDialog() {
        lastRelocateTarget = null;
        String chargePoint = DestHelper.getInstance().getChargePoint();
        if (TextUtils.isEmpty(chargePoint)) return;
        mHandler.postDelayed(() -> EasyDialog.getInstance(this).confirmCareful(getString(R.string.text_relocate_prompt), getString(R.string.text_relocate), getString(R.string.text_cancel), new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                if (id == R.id.btn_confirm) {
                    if (!ros.isCharging()) {
                        ToastUtils.showShortToast(getString(R.string.voice_please_dock_charging_pile));
                        return;
                    }
                    lastRelocateTarget = chargePoint;
                    dialog.dismiss();
                    mHandler.postDelayed(() -> {
                        EasyDialog.getLoadingInstance(MainActivity.this).loading(getString(R.string.text_relocating));
                        ros.relocateByPointName(lastRelocateTarget);
                    }, 300);
                } else {
                    lastRelocateTarget = null;
                    dialog.dismiss();
                }
            }
        }), 500);
    }

    public void onCustomPointNotFoundEvent(String name) {
        lastRelocateTarget = null;
        if (EasyDialog.isShow()) {
            EasyDialog.getInstance().dismiss();
        }
        mHandler.postDelayed(() -> EasyDialog.getInstance(this).warnError(getString(R.string.voice_not_found_target_point)), 1000);
    }

    @Override
    protected void onCustomInitPose(String currentPosition) {
        super.onCustomInitPose(currentPosition);

        if (lastRelocateTarget != null) {
            lastRelocateTarget = null;

            if (EasyDialog.isShow()) EasyDialog.getInstance().dismiss();

            String pointInfo = SpManager.getInstance().getString(Constants.KEY_POINT_INFO, null);

            if (TextUtils.isEmpty(pointInfo) || TextUtils.isEmpty(currentPosition)) {
                ToastUtils.showShortToast(getString(R.string.text_locate_finish));
                return;
            }

            String[] currPos = currentPosition.split(" ");
            List<Point> waypoints = new Gson().fromJson(pointInfo, new TypeToken<List<Point>>() {
            }.getType());
            Point chargePoint = null;
            for (Point waypoint : waypoints) {
                if (ROS.PT.CHARGE.equals(waypoint.type)) {
                    chargePoint = waypoint;
                    break;
                }
            }
            if (chargePoint != null
                    && Math.abs(chargePoint.pose.theta - Float.parseFloat(currPos[2])) < 1.04f
                    && Math.sqrt(Math.pow(chargePoint.pose.x - Float.parseFloat(currPos[0]), 2) + Math.pow(chargePoint.pose.y - Float.parseFloat(currPos[1]), 2)) < 0.7) {
                ToastUtils.showLongToast(getString(R.string.text_locate_finish));
            } else {
                showRelocatePromptDialog();
                ToastUtils.showLongToast(getString(R.string.text_relocate_failed));
            }
        }

    }

    @Override
    public void onTableGroupItemClick(int tableGroup, View view) {
        generateTable(tableGroup, DestHelper.getInstance().getPoints());
    }


    /**
     * 数据加载失败时点击刷新按钮
     */
    @Override
    public void onRefreshBtnClick() {
        refreshByCurrentMode(true);
    }

    /**
     * 开始刷新
     */
    @Override
    public void onRefresh() {
        refreshByCurrentMode(true);
    }

    /**
     * 配送餐位被点击
     *
     * @param tableName
     */
    @Override
    public void onTableNumberClick(String tableName) {
        //没有选中餐盘，不更新餐盘内容
        if (currentLayer == -1) return;
        List<TextView> textViews = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        List<ImageView> imageViews = tableLayer == 3 ? Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree) : Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree, ivCancelSelectFour);

        //生日模式只更新第一层餐盘
        if (currentModeIndex == Constants.MODE_BIRTHDAY) {
            if (TextUtils.isEmpty(textViews.get(0).getText().toString())) {
                imageViews.get(0).setVisibility(View.VISIBLE);
            }
            textViews.get(0).setText(tableName);
            return;
        }

        if (currentModeIndex != Constants.MODE_DELIVERY_FOOD) return;

        //配送模式既更新餐盘，又向下移动
        for (int i = 0; i < tableLayer; i++) {
            TextView textView = textViews.get(i);
            if (textView != null && i == currentLayer) {
                textView.setText(tableName);
            }
        }

        if (currentLayer + 1 <= tableLayer - 1) {
            currentLayer++;
        }

        for (int i = 0; i < tableLayer; i++) {
            TextView textView = textViews.get(i);
            ImageView imageView = imageViews.get(i);
            if (textView == null) continue;
            if (TextUtils.isEmpty(textView.getText().toString())) {
                imageView.setVisibility(View.INVISIBLE);
            } else {
                imageView.setVisibility(View.VISIBLE);
            }
            if (i == currentLayer) {
                textView.setBackgroundResource(R.drawable.bg_layer_selected);
            } else {
                textView.setBackgroundResource(R.drawable.bg_layer_normal);
            }
        }
    }

    @Override
    public void onTableNumberAdd(List<String> tables, int position) {
        if (currentModeIndex == Constants.MODE_RECYCLE_2) {
            layerAdapter.setList(tables);
            layerAdapter.notifyDataSetChanged();
            mHandler.postDelayed(() -> scrollView.fullScroll(View.FOCUS_DOWN), 300);
        } else {
            notifyMultiDeliveryDataSetUpdate(tables);
        }
    }

    private void notifyMultiDeliveryDataSetUpdate(List<String> tables) {
        if (multiDeliveryData == null) multiDeliveryData = new HashMap<>();
        List<String> currentTables = multiDeliveryData.get(currentLayer);
        if (currentTables == null) {
            currentTables = new ArrayList<>();
        } else {
            currentTables.clear();
        }
        currentTables.addAll(tables);
        multiDeliveryData.put(currentLayer, currentTables);
        String text = currentTables.toString();
        String tableStr = text.replace("[", "").replace("]", "");
        List<TextView> textViews = tableLayer == 3 ? Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree) : Arrays.asList(tvLayerOne, tvLayerTwo, tvLayerThree, tvLayerFour);
        List<ImageView> imageViews = tableLayer == 3 ? Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree) : Arrays.asList(ivCancelSelectOne, ivCancelSelectTwo, ivCancelSelectThree, ivCancelSelectFour);
        textViews.get(currentLayer).setText(tableStr);
        if (!tables.isEmpty()) {
            imageViews.get(currentLayer).setVisibility(View.VISIBLE);
        } else {
            imageViews.get(currentLayer).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onTableNumberRemove(List<String> tables, int position) {
        if (currentModeIndex == Constants.MODE_RECYCLE_2) {
            layerAdapter.setList(tables);
            layerAdapter.notifyDataSetChanged();
            mHandler.postDelayed(() -> scrollView.fullScroll(View.FOCUS_DOWN), 300);
        } else {
            notifyMultiDeliveryDataSetUpdate(tables);
        }
    }

    public int getCurrentModeIndex() {
        return currentModeIndex;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLightKeyTouch(RobotEvent.OnTouchEvent event) {
        if (!ros.isEmergencyStopDown() && Event.getCoreData().charger == 2) {
            ros.moveForward();
        }
    }

    @Override
    public void onWaypointUpdate(Event.OnWaypointUpdateEvent event) {
        if (currentModeIndex != Constants.MODE_CRUISE)
            fetchPoints(true);
    }
}