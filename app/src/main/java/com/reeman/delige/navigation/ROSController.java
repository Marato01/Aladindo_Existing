package com.reeman.delige.navigation;


import static com.reeman.delige.base.BaseApplication.navigationMode;

import android.os.SystemClock;

import com.elvishew.xlog.LogLevel;
import com.reeman.delige.BuildConfig;
import com.reeman.delige.constants.State;
import com.reeman.delige.event.Event;
import com.reeman.delige.navigation.filter.ROSFilter;
import com.reeman.delige.utils.TimeUtils;
import com.reeman.ros.controller.RobotActionController;
import com.reeman.ros.controller.RosCallbackParser;
import com.reeman.ros.util.Log;

import org.greenrobot.eventbus.EventBus;


public class ROSController implements RosCallbackParser.RosCallback {

    private int level = 0;
    private int emergencyStop = -1;
    private int charge = -1;
    private int lastChargeType;
    private int lastPowerLevel;
    private int dockFailCount;
    private int missPositionResult;
    private long lastChargeStartTime;
    private double lastNavY;
    private double lastNavX;
    private double lastNavZ;
    private double[] currentPosition;
    private String currentDest = "";
    private String lastNavPoint = "";
    private String lastSensorState = "";
    private boolean isNavigating = false;
    private boolean missPoseUpload = false;
    private boolean checkNet = false;
    private boolean isWiFiConnecting = false;
    private boolean isRelocating = false;
    private boolean requestModeMyself = false;
    private State state = State.IDLE;
    private String targetSpecialArea;
    private int targetSpecialAreaType;
    private long powerOnTime;
    private double lastDistance;
    private boolean isInSpecialArea = false;

    public boolean isInSpecialArea() {
        return isInSpecialArea;
    }

    public void setInSpecialArea(boolean inSpecialArea) {
        isInSpecialArea = inSpecialArea;
    }

    public long getPowerOnTime() {
        return powerOnTime;
    }

    public void setPowerOnTime(long powerOnTime) {
        this.powerOnTime = powerOnTime;
    }

    public int getTargetSpecialAreaType() {
        return targetSpecialAreaType;
    }

    public void setTargetSpecialAreaType(int targetSpecialAreaType) {
        this.targetSpecialAreaType = targetSpecialAreaType;
    }

    public String getTargetSpecialArea() {
        return targetSpecialArea;
    }

    public void setTargetSpecialArea(String targetSpecialArea) {
        this.targetSpecialArea = targetSpecialArea;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isEmergencyStopDown() {
        return emergencyStop == ROS.ES.SWITCH_DOWN;
    }

    public int getEmergencyStop() {
        return emergencyStop;
    }

    public void setEmergencyStop(int emergencyStop) {
        this.emergencyStop = emergencyStop;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getLastChargeType() {
        return lastChargeType;
    }

    public void setLastChargeType(int lastChargeType) {
        this.lastChargeType = lastChargeType;
    }

    public int getLastPowerLevel() {
        return lastPowerLevel;
    }

    public void setLastPowerLevel(int lastPowerLevel) {
        this.lastPowerLevel = lastPowerLevel;
    }

    public int getDockFailCount() {
        return dockFailCount;
    }

    public void setDockFailCount(int dockFailCount) {
        this.dockFailCount = dockFailCount;
    }

    public int getMissPositionResult() {
        return missPositionResult;
    }

    public void setMissPositionResult(int missPositionResult) {
        this.missPositionResult = missPositionResult;
    }

    public long getLastChargeStartTime() {
        return lastChargeStartTime;
    }

    public void setLastChargeStartTime(long lastChargeStartTime) {
        this.lastChargeStartTime = lastChargeStartTime;
    }

    public double getLastNavY() {
        return lastNavY;
    }

    public void setLastNavY(double lastNavY) {
        this.lastNavY = lastNavY;
    }

    public double getLastNavX() {
        return lastNavX;
    }

    public void setLastNavX(double lastNavX) {
        this.lastNavX = lastNavX;
    }

    public double[] getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(double[] currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getCurrentDest() {
        return currentDest;
    }

    public void setCurrentDest(String currentDest) {
        this.currentDest = currentDest;
    }

    public String getLastNavPoint() {
        return lastNavPoint;
    }

    public void setLastNavPoint(String lastNavPoint) {
        this.lastNavPoint = lastNavPoint;
    }

    public String getLastSensorState() {
        return lastSensorState;
    }

    public void setLastSensorState(String lastSensorState) {
        this.lastSensorState = lastSensorState;
    }

    public boolean isNavigating() {
        return isNavigating;
    }

    public void setNavigating(boolean navigating) {
        isNavigating = navigating;
    }

    public boolean isMissPoseUpload() {
        return missPoseUpload;
    }

    public void setMissPoseUpload(boolean missPoseUpload) {
        this.missPoseUpload = missPoseUpload;
    }

    public boolean isCheckNet() {
        return checkNet;
    }

    public void setCheckNet(boolean checkNet) {
        this.checkNet = checkNet;
    }

    public boolean isWiFiConnecting() {
        return isWiFiConnecting;
    }

    public void setWiFiConnecting(boolean wiFiConnecting) {
        isWiFiConnecting = wiFiConnecting;
    }

    public boolean isRelocating() {
        return isRelocating;
    }

    public void setRelocating(boolean relocating) {
        isRelocating = relocating;
    }

    public boolean isRequestModeMyself() {
        return requestModeMyself;
    }

    public void setRequestModeMyself(boolean requestModeMyself) {
        this.requestModeMyself = requestModeMyself;
    }

    public RobotActionController getController() {
        return controller;
    }

    private static ROSController INSTANCE;

    private RobotActionController controller;

    public static ROSController getInstance() {
        if (INSTANCE == null) {
            synchronized (ROSController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ROSController();
                }
            }
        }
        return INSTANCE;
    }

    public boolean isCharging() {
        return charge == ROS.CS.CHARGING;
    }

    public boolean isNotCharging() {
        return charge == ROS.CS.NOT_CHARGE;
    }

    public boolean isDocking() {
        return charge == ROS.CS.DOCKING;
    }

    public boolean isChargeFailed() {
        return charge == ROS.CS.CHARGE_FAILURE;
    }


    public void setCharge(int level, int plug) {
        lastPowerLevel = level;
        lastChargeType = plug;
        lastChargeStartTime = System.currentTimeMillis();
    }

    public ROSController() {
        controller = RobotActionController.getInstance();
    }

    public void init() throws Exception {
        try {
            controller.init(LogLevel.ALL, BuildConfig.APP_LOG_DIR, this);
        } catch (Exception e) {
            e.printStackTrace();
            controller = null;
        }
    }

    public void unInit() {
        controller.stopListen();
        controller = null;
    }

    public void navigationByPoint(String point) {
        isNavigating = true;
        lastNavPoint = point;
        currentDest = null;
        if (navigationMode == Mode.AUTO_ROUTE) {
            controller.navigationByPoint(point);
        } else if (navigationMode == Mode.FIX_ROUTE) {
            controller.sendCommand("points_path[" + point + "]");
        }
    }

    public void navigationByCoordinates(double x, double y, double z) {
        isNavigating = true;
        lastNavX = x;
        lastNavY = y;
        lastNavZ = z;
        lastNavPoint = x + "," + y + "," + "z";
        currentDest = null;
        controller.navigationByCoordinates(x, y, Math.toRadians(z));
    }

    public void cancelCharge() {
        charge = ROS.CS.NOT_CHARGE;
        controller.cancelCharge();
    }

    public void requestPowerOnTime() {
        powerOnTime = SystemClock.elapsedRealtime();
        controller.sendCommand("get_poweron_time");
    }

    public void getHostIP() {
        controller.getHostIp();
    }

    public void getHostName() {
        controller.getHostName();
        ;
    }

    public void getPosition(){
        controller.getCurrentPosition();
    }

    public void positionAutoUploadControl(boolean upload) {
        controller.positionAutoUploadControl(upload);
    }

    public void relocateByCoordinate(double[] coordinate) {
        controller.relocateByCoordinate(coordinate);
    }

    public void relocateByPointName(String point) {
        controller.relocByName(point);
    }

    public void moveForward() {
        controller.moveForward();
    }

    public void modelRequest() {
        controller.modelRequest();
    }

    public void getCurrentMap() {
        controller.getCurrentMap();
    }

    public void modelMapping() {
        controller.modelMapping();
    }

    public void modelNavi() {
        controller.modelNavi();
    }

    public void saveMap() {
        controller.saveMap();
    }

    public void markPoint(double[] arr, String type, String point) {
        controller.markPoint(arr, type, point);
    }

    public void deletePoint(String point) {
        controller.deletePoint(point);
    }

    public void stopMove() {
        controller.stopMove();
    }

    public void cancelNavigation() {
        controller.cancelNavigation();
    }

    public void setNavSpeed(String speed) {
        controller.setNavSpeed(speed);
    }

    public void turn(double angle) {
        controller.turn(angle);
    }

    public void connectROSWifi(String ssid, String pwd) {
        controller.connectROSWifi(ssid, pwd);
    }

    public void sysReboot() {
        controller.sysReboot();
    }

    public void cpuPerformance() {
        controller.cpuPerformance();
    }

    public void currentInfoControl(boolean report) {
        controller.currentInfoControl(report);
    }

    public void applyMap(String map) {
        controller.applyMap(map);
    }

    public void getHostVersion() {
        controller.getHostVersion();
    }

    public void expand(double x, double y, double z, String hostname, double speed) {
        controller.expand(x, y, z, hostname, speed);
    }

    public void pauseNavigation() {
        controller.pauseNavigation();
    }

    public void resumeNavigation() {
        controller.resumeNavigation();
    }

    public void autoUploadLogs(String ipAddress) {
        controller.setIpAddress(ipAddress);
    }

    public void getDefinedPlan(String name) {
        controller.sendCommand("get_defined_plan[" + name + "]");
    }


    public void maxPlanDist(double distance) {
        if (distance != lastDistance) {
            lastDistance = distance;
            controller.sendCommand("max_plan_dist[" + distance + "]");
        }
    }

    public void expand(double[] position, String hostname, double lineSpeed) {
        controller.expand(position[0], position[1], position[2], hostname, lineSpeed);
    }

    public void heartBeat() {
        controller.heartBeat();
    }


    @Override
    public void onResult(String result) {
        if (result.startsWith("visual_mark"))
            return;
        if (result.startsWith("ver")) {
            EventBus.getDefault().post(Event.getVersionEvent(result));
        } else if (result.startsWith("current_map")) {
            EventBus.getDefault().post(Event.getMapEvent(result));
        } else if (result.startsWith("sys:boot")) {
            EventBus.getDefault().post(Event.getHostnameEvent(result));
        } else if (result.startsWith("ip")) {
            EventBus.getDefault().post(Event.getIpEvent(result));
        } else if (result.startsWith("wlan")) {
            EventBus.getDefault().post(Event.getIpEvent(""));
        } else if (result.startsWith("move_status:3")) {
            EventBus.getDefault().post(Event.getEncounterObstacleEvent());
        } else if (result.startsWith("move_status:4")) {
            EventBus.getDefault().post(Event.getEncounterObstacleEvent());
        } else if (result.startsWith("move_status:5")) {
            EventBus.getDefault().post(Event.getEncounterObstacleEvent());
        } else if (result.startsWith("move:done:")) {
            EventBus.getDefault().post(Event.getOnMoveDoneEvent(result));
        } else if (result.startsWith("model")) {
            EventBus.getDefault().post(Event.getOnNavModeEvent(Integer.parseInt(result.replace("model:", ""))));
        } else if (result.startsWith("initpose:0")) {
            EventBus.getDefault().post(Event.getOnInitPoseEvent(result));
        } else if (result.startsWith("nav:pose[")) {
            EventBus.getDefault().post(Event.getOnPositionEvent(result.replace("nav:pose[", "").replace("]", "")));
        } else if (result.startsWith("get_max_vel")) {
            EventBus.getDefault().post(Event.getOnSpeedEvent(result.replace("get_max_vel:", "").trim()));
        } else if (result.startsWith("wifi:connect fail")) {
            EventBus.getDefault().post(Event.getOnWiFiEvent(false));
        } else if (result.startsWith("wifi:connect success")) {
            EventBus.getDefault().post(Event.getOnWiFiEvent(true));
        } else if (result.startsWith("apply_map")) {
            EventBus.getDefault().post(Event.getOnApplyMapEvent(result));
        } else if (result.startsWith("hfls_version")) {
            EventBus.getDefault().post(Event.getOnHflsVersionEvent(result));
        } else if (result.startsWith("model:")) {
            EventBus.getDefault().post(Event.getOnModelEvent(result.charAt(6)));
        } else if (result.startsWith("core_data")) {
            EventBus.getDefault().post(Event.getOnCoreDataEvent(result));
        } else if (result.startsWith("nav_result")) {
            if (result.startsWith("nav_result{3")
                    || result.startsWith("nav_result{2")
                    || result.startsWith("nav_result{5")
                    || result.startsWith("nav_result{6")
            ) {
                Log.w("导航回调 :" + result);
                EventBus.getDefault().post(Event.getOnNavResultEvent(result));
            }
        } else if (result.startsWith("battery_info")) {
            EventBus.getDefault().post(Event.getOnBatteryFixedEvent(result));
        } else if (result.startsWith("current_info")) {
            EventBus.getDefault().post(Event.getOnBatteryDynamicEvent(result));
        } else if (result.startsWith("get_flag_point[")) {
            EventBus.getDefault().post(Event.getOnGetFlagPointEvent(result));
        } else if (result.startsWith("get_flag_point:-1")) {
            EventBus.getDefault().post(Event.getOnGetPointNotFoundEvent());
        } else if (result.startsWith("set_flag_point")) {
            EventBus.getDefault().post(Event.getOnSetFlagPointEvent(result));
        } else if (result.startsWith("del_flag_point")) {
            EventBus.getDefault().post(Event.getOnDelFlagPointEvent(result));
        } else if (result.startsWith("power_off")) {
            EventBus.getDefault().post(Event.getOnPowerOffEvent(result));
        } else if (result.startsWith("base_upgrade:")) {
            EventBus.getDefault().post(Event.getOnBaseUpgradeEvent(result));
        } else if (result.startsWith("get_stop_time")) {
            EventBus.getDefault().post(Event.getOnGetStopTimeEvent(result));
        } else if (result.startsWith("get_global_p")) {
            EventBus.getDefault().post(Event.getOnGlobalPEvent(result));
        } else if (result.startsWith("check_sensors")) {
            EventBus.getDefault().post(Event.getOnCheckSensorsEvent(result));
        } else if (result.startsWith("move_status")) {
            EventBus.getDefault().post(Event.getOnMoveStatusEvent(result));
        } else if (result.startsWith("wheel_status")) {
            if (ROSFilter.isWheelDataDiff(result))
                EventBus.getDefault().post(Event.getOnWheelStatusEvent(result));
        } else if (result.startsWith("misspose")) {
            if (ROSFilter.isMissPoseDiff(result))
                EventBus.getDefault().post(Event.getOnMissPoseEvent(result));
        } else if (result.startsWith("waypoint:")) {
            EventBus.getDefault().post(Event.getOnWayPointUpdateEvent(result));
        } else if (result.startsWith("base_vel")) {
            EventBus.getDefault().post(Event.getOnBaseValEvent(result));
        } else if (result.startsWith("upper")) {
            String command = result.replace("upper:", "");
            if (command.startsWith("start_task")) {
                EventBus.getDefault().post(Event.getOnStartTaskEvent(command));
            } else if (command.startsWith("stop_task")) {
                EventBus.getDefault().post(Event.getOnStopTaskEvent());
            }
        } else if (result.startsWith("global_path:") || result.startsWith("global_path1:")) {
            Log.v("receive", result);
            EventBus.getDefault().post(Event.getOnPathObtainEvent(result));
        } else if (result.startsWith("special_plan")) {
            Log.v("receive", result);
            EventBus.getDefault().post(Event.getOnSpecialPlanEvent(result.replace("special_plan:", "")));
        } else if (result.startsWith("agv_")) {
            EventBus.getDefault().post(Event.getOnAgvNavResultEvent(result));
        } else if (result.startsWith("base_data")) {
            Log.d("base_data", "透传:" + result);
            String replace = result.replace("base_data:{", "").replace("}", "").replace(" ", "");
            if (replace.startsWith("05", 4)) {
                EventBus.getDefault().post(Event.getOnHumanDetectionEvent(Integer.parseInt(replace.substring(6, 8), 16)));
            } else if (replace.startsWith("06", 4)) {
                EventBus.getDefault().post(Event.getOnAltitudeEvent(Integer.parseInt(replace.substring(6, 8), 16)));
            }
        } else if (result.startsWith("power_on_t:")) {
            powerOnTime = TimeUtils.convertToTimestamp(result.replace("power_on_t:", ""));
        } else if (result.startsWith("short_dij")) {
            Log.w("生成固定路线 :" + result);
            EventBus.getDefault().post(Event.getOnRoutePointEvent(result));
        } else if (result.startsWith("special_area:out")) {
            isInSpecialArea = false;
            EventBus.getDefault().post(Event.getOnSpecialAreaEvent(result));
        } else if (result.startsWith("special_area[")) {
            Log.v("receive", "进入特殊区: " + result);
            isInSpecialArea = true;
            EventBus.getDefault().post(Event.getOnSpecialAreaEvent(result));
        } else if (result.startsWith("in_polygon:")) {
            isInSpecialArea = true;
            Log.v("receive", "在特殊区 :" + result);
            EventBus.getDefault().post(Event.getOnSpecialAreaEvent(result));
        } else if (result.startsWith("getplan_dij:")) {
            Log.w("查询固定路线 :" + result);
            EventBus.getDefault().post(Event.getOnGetPlanDijEvent(result));
        }else if (result.startsWith("miss_pose:")){
            if (result.contains("1")) {
                Log.w("定位异常 :");
            }
            EventBus.getDefault().post(Event.getOnMissPoseEvent(result));
        }
    }
}
