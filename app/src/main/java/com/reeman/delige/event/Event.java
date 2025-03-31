package com.reeman.delige.event;

import android.text.TextUtils;

import com.reeman.delige.event.model.Room;
import com.reeman.ros.util.BatteryCurveLogUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Event {
    private static final OnVersionEvent onVersionEvent = new OnVersionEvent();
    private static final OnHostnameEvent onHostnameEvent = new OnHostnameEvent();
    private static final OnIpEvent onIpEvent = new OnIpEvent();
    private static final OnEncounterObstacleEvent onEncounterObstacleEvent = new OnEncounterObstacleEvent();
    private static final OnMapEvent onMapEvent = new OnMapEvent();
    private static final OnMoveDoneEvent onMoveDoneEvent = new OnMoveDoneEvent();
    private static final OnNavModeEvent onNavModeEvent = new OnNavModeEvent();
    private static final OnInitPoseEvent onInitPoseEvent = new OnInitPoseEvent();
    private static final OnPositionEvent onPositionEvent = new OnPositionEvent();
    private static final OnSpeedEvent onSpeedEvent = new OnSpeedEvent();
    private static final OnWiFiEvent onWiFiEvent = new OnWiFiEvent();
    private static final OnApplyMapEvent onApplyMapEvent = new OnApplyMapEvent();
    private static final OnPathEvent onPathEvent = new OnPathEvent();
    private static final OnMissPoseEvent onMissPoseEvent = new OnMissPoseEvent();
    private static final OnLaserEvent onLaserEvent = new OnLaserEvent();
    private static final OnCoreDataEvent onCoreDataEvent = new OnCoreDataEvent();
    private static final OnModelEvent onModelEvent = new OnModelEvent();
    private static final OnHflsVersionEvent onHflsVersionEvent = new OnHflsVersionEvent();
    private static final OnNavResultEvent onNavResultEvent = new OnNavResultEvent();
    private static final OnGetFlagPointEvent onGetFlagPointEvent = new OnGetFlagPointEvent();
    private static final OnGetPointNotFoundEvent onGetPointNotFoundEvent = new OnGetPointNotFoundEvent();
    private static final OnSetFlagPointEvent onSetFlagPointEvent = new OnSetFlagPointEvent();
    private static final OnDelFlagPointEvent onDelFlagPointEvent = new OnDelFlagPointEvent();
    private static final OnPowerOffEvent onPowerOffEvent = new OnPowerOffEvent();
    private static final OnBaseUpgradeEvent onBaseUpgradeEvent = new OnBaseUpgradeEvent();
    private static final OnGetStopTimeEvent onGetStopTimeEvent = new OnGetStopTimeEvent();
    private static final OnGlobalPEvent onGlobalPEvent = new OnGlobalPEvent();
    private static final OnBatteryFixedEvent onBatteryFixedEvent = new OnBatteryFixedEvent();
    private static final OnBatteryDynamicEvent onBatteryDynamicEvent = new OnBatteryDynamicEvent();
    private static final OnCheckSensorsEvent onCheckSensorsEvent = new OnCheckSensorsEvent();
    private static final OnMoveStatusEvent onMoveStatusEvent = new OnMoveStatusEvent();
    private static final OnWheelStatusEvent onWheelStatusEvent = new OnWheelStatusEvent();
    private static final OnUncommonlyUsedEvent onUncommonlyUsedEvent = new OnUncommonlyUsedEvent();
    private static final OnGlobalPathEvent onPathObtainEvent = new OnGlobalPathEvent();
    private static final OnWaypointUpdateEvent onWayPointUpdateEvent = new OnWaypointUpdateEvent();
    private static final OnBaseValEvent onBaseValEvent = new OnBaseValEvent();
    private static final OnStartTaskEvent onStartTaskEvent = new OnStartTaskEvent();
    private static final OnStopTaskEvent onStopTaskEvent = new OnStopTaskEvent();
    private static final OnRangeSensorEvent onRangeSensorEvent = new OnRangeSensorEvent();
    private static final OnSpecialPlanEvent onSpecialPlanEvent = new OnSpecialPlanEvent();
    private static final OnAvoidEvent onAvoidEvent = new OnAvoidEvent();
    private static final OnResumeTaskEvent onResumeTaskEvent = new OnResumeTaskEvent();
    private static final OnPauseTaskEvent onPauseTaskEvent = new OnPauseTaskEvent();
    private static final OnHumanDetectionEvent onHumanDetectionEvent = new OnHumanDetectionEvent();
    private static final OnAgvNavResultEvent onAgvNavResultEvent = new OnAgvNavResultEvent();
    private static final OnAltitudeEvent onAltitudeEvent = new OnAltitudeEvent();
    private static final OnMoveEndEvent onMoveEndEvent = new OnMoveEndEvent();
    private static final OnDispatchPauseEvent onDispatchPauseEvent = new OnDispatchPauseEvent();
    private static final OnDispatchResumeEvent onDispatchResumeEvent = new OnDispatchResumeEvent();
    private static final OnRoutePointEvent onRoutePointEvent = new OnRoutePointEvent();
    private static final OnSpecialAreaEvent onSpecialAreaEvent = new OnSpecialAreaEvent();
    private static final OnGetPlanDijEvent onGetPlanDijEvent = new OnGetPlanDijEvent();

    public static OnGetPlanDijEvent getOnGetPlanDijEvent(String data) {
        onGetPlanDijEvent.points.clear();
        if (!data.equals("getplan_dij:nofind")) {
            String[] split = data.replace("getplan_dij:", "").trim().split(" ");
            onGetPlanDijEvent.points.addAll(Arrays.asList(split));
        }
        return onGetPlanDijEvent;
    }

    public static OnSpecialAreaEvent getOnSpecialAreaEvent(String data) {
        if (data.startsWith("special_area[")) {
            String[] split = data.replace("special_area[", "").replace("]", "").split(",");
            onSpecialAreaEvent.name = split[0];
            onSpecialAreaEvent.type = Integer.parseInt(split[1]);
        } else if (data.startsWith("in_polygon:")) {
            if (data.equals("in_polygon:")) {
                onSpecialAreaEvent.name = "";
                onSpecialAreaEvent.type = -1;
            } else {
                String[] split = data.replace("in_polygon:", "").split(",");
                onSpecialAreaEvent.name = split[0];
                onSpecialAreaEvent.type = Integer.parseInt(split[1]);
            }
        } else if (data.startsWith("special_area:out")) {
            onSpecialAreaEvent.name = "";
            onSpecialAreaEvent.type = -1;
        }
        return onSpecialAreaEvent;
    }


    public static OnRoutePointEvent getOnRoutePointEvent(String data) {
        String[] points = data.replace("short_dij:", "").trim().split(" ");
        onRoutePointEvent.points.clear();
        onRoutePointEvent.points.addAll(Arrays.asList(points));
        return onRoutePointEvent;
    }

    public static OnDispatchPauseEvent getOnDispatchPauseEvent() {
        return onDispatchPauseEvent;
    }

    public static OnDispatchResumeEvent getOnDispatchResumeEvent() {
        return onDispatchResumeEvent;
    }

    public static OnMoveEndEvent getOnMoveEndEvent(int type) {
        onMoveEndEvent.type = type;
        return onMoveEndEvent;
    }

    public static OnBaseValEvent getOnBaseValEvent() {
        return onBaseValEvent;
    }


    public static OnPositionEvent getOnPositionEvent() {
        return onPositionEvent;
    }

    public static OnPowerOffEvent getOnPowerOffEvent(String data) {
        onPowerOffEvent.result = Integer.parseInt(data.split(":")[1]);
        return onPowerOffEvent;
    }

    public static OnWheelStatusEvent getOnWheelStatusEvent() {
        return onWheelStatusEvent;
    }

    public static OnHflsVersionEvent getOnHflsVersionEvent() {
        return onHflsVersionEvent;
    }

    public static OnVersionEvent getVersionEvent() {
        return onVersionEvent;
    }

    public static OnHostnameEvent getOnHostnameEvent() {
        return onHostnameEvent;
    }

    public static OnIpEvent getIpEvent() {
        return onIpEvent;
    }

    public static OnMapEvent getMapEvent() {
        return onMapEvent;
    }

    public static OnEncounterObstacleEvent getEncounterObstacleEvent() {
        return onEncounterObstacleEvent;
    }

    public static OnMissPoseEvent getOnMissPoseEvent() {
        return onMissPoseEvent;
    }

    public static boolean isRangeSensorDiff(String data) {
        return !data.equals(onRangeSensorEvent.rawData);
    }

    public static boolean isWheelStatusDiff(String data) {
        return !data.equals(onWheelStatusEvent.rawData);
    }

    public static boolean isHflsVersionDiff(String data) {
        return !data.equals(onHflsVersionEvent.rawData);
    }

    public static boolean isCoreDataDiff(String data) {
        return !data.equals(onCoreDataEvent.rawData);
    }

    public static boolean isLaserDiff(String data) {
        return !data.equals(onLaserEvent.rawData);
    }

    public static boolean isMissPoseDiff(String data) {
        return !data.equals(onMissPoseEvent.rawData);
    }

    public static OnPauseTaskEvent getOnPauseTaskEvent() {
        return onPauseTaskEvent;
    }

    public static OnResumeTaskEvent getOnResumeTaskEvent() {
        return onResumeTaskEvent;
    }

    public static OnAltitudeEvent getOnAltitudeEvent(int result) {
        onAltitudeEvent.result = result;
        return onAltitudeEvent;
    }

    public static OnAgvNavResultEvent getOnAgvNavResultEvent(String result) {
        if (result.startsWith("agv_success{")) {
            onAgvNavResultEvent.tag = result.replace("agv_success{", "").replace("}", "");
            onAgvNavResultEvent.success = true;
        } else {
            onAgvNavResultEvent.tag = null;
            onAgvNavResultEvent.success = false;
        }
        return onAgvNavResultEvent;
    }

    public static OnHumanDetectionEvent getOnHumanDetectionEvent(int result) {
        onHumanDetectionEvent.result = result;
        return onHumanDetectionEvent;
    }

    public static OnAvoidEvent getOnAvoidEvent(String result) {
        if (result.equals("answer_nearest{nofind}")) {
            onAvoidEvent.coordinate = null;
        } else if (result.startsWith("answer_nearest{")) {
            onAvoidEvent.coordinate = new double[3];
            String coordinate = result.replace("answer_nearest{", "").replace("}", "");
            String[] split = coordinate.split(" ");
            for (int i = 0; i < split.length; i++) {
                onAvoidEvent.coordinate[i] = Double.parseDouble(split[i]);
            }
        } else if (result.equals("avoid:stop")) {
            onAvoidEvent.coordinate = null;
        } else {
            onAvoidEvent.coordinate = new double[3];
            String coordinate = result.replace("avoid:", "");
            String[] split = coordinate.split(",");
            for (int i = 0; i < split.length; i++) {
                onAvoidEvent.coordinate[i] = Double.parseDouble(split[i]);
            }
        }
        return onAvoidEvent;
    }


    public static OnSpecialPlanEvent getOnSpecialPlanEvent() {
        return onSpecialPlanEvent;
    }

    public static OnSpecialPlanEvent getOnSpecialPlanEvent(String str) {
        List<Room> list = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(str);
            JSONArray sp = jsonObject.optJSONArray("sp");
            if (sp != null) {
                for (int i = 0; i < sp.length(); i++) {
                    JSONObject temp = sp.getJSONObject(i);
                    Room room = new Room();
                    room.name = temp.optString("n");
                    if (TextUtils.isEmpty(room.name)) continue;
                    room.type = temp.optInt("type");
                    JSONArray c = temp.optJSONArray("c");
                    if (c == null || c.length() == 0) continue;
                    room.coordination = new ArrayList<>();
                    for (int i1 = 0; i1 < c.length(); i1++) {
                        room.coordination.add(c.optDouble(i1));
                    }
                    list.add(room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        onSpecialPlanEvent.rooms = list;
        return onSpecialPlanEvent;
    }

    public static OnRangeSensorEvent getOnRangeSensorEvent(String result) {
        onRangeSensorEvent.rawData = result;
        String[] split = result.replace("range_sensor{", "").replace("}", "").split(" ");
        if (split.length == 3) {
            onRangeSensorEvent.range1 = Integer.parseInt(split[0]);
            onRangeSensorEvent.range2 = Integer.parseInt(split[1]);
            onRangeSensorEvent.range3 = Integer.parseInt(split[2]);
        }
        return onRangeSensorEvent;
    }

    public static OnStartTaskEvent getOnStartTaskEvent(String result) {
        onStartTaskEvent.taskStr = result.replace("start_task:", "");
        return onStartTaskEvent;
    }

    public static OnStopTaskEvent getOnStopTaskEvent() {
        return onStopTaskEvent;
    }

    public static OnBaseValEvent getOnBaseValEvent(String result) {
        String[] split = result.replace("base_vel[", "").replace("]", "").split(" ");
        onBaseValEvent.lineSpeed = Double.parseDouble(split[0]);
        onBaseValEvent.palstance = Double.parseDouble(split[1]);
        return onBaseValEvent;
    }

    public static OnWaypointUpdateEvent getOnWayPointUpdateEvent(String result) {
        onWayPointUpdateEvent.state = result.split(":")[1];
        return onWayPointUpdateEvent;
    }

    public static OnInitPoseEvent getOnInitPoseEvent(String result) {
        if (result.endsWith("initpose:0")) {
            onInitPoseEvent.currentPosition = "";
        } else {
            onInitPoseEvent.currentPosition = result.replace("initpose:0,", "");
        }
        return onInitPoseEvent;
    }

    public static OnGlobalPathEvent getOnPathObtainEvent() {
        return onPathObtainEvent;
    }

    public static void clearGlobalPath() {
        onPathObtainEvent.arrays = new CopyOnWriteArrayList<>();
        onPathObtainEvent.isComplete = true;
    }


    public static OnGlobalPathEvent getOnPathObtainEvent(String path) {
        boolean newPath = path.startsWith("global_path:");
        path = path.replace("global_path:", "");
        path = path.replace("global_path1:", "");
        if (!onNavResultEvent.isNavigating) return onPathObtainEvent;
        if (onPathObtainEvent.arrays == null) {
            onPathObtainEvent.arrays = new CopyOnWriteArrayList<>();
        }

        if (newPath) {
            onPathObtainEvent.arrays.clear();
        }

        if (path.endsWith("+")) {
            onPathObtainEvent.isComplete = false;
            path = path.replace(" +", "");
        } else {
            onPathObtainEvent.isComplete = true;
        }
        String[] s = path.split(" ");
        for (String temp : s) {
            onPathObtainEvent.arrays.add(Double.parseDouble(temp));
        }
        return onPathObtainEvent;
    }

    public static OnUncommonlyUsedEvent getOnUncommonlyUsedEvent(String result) {
        onUncommonlyUsedEvent.rawData = result;
        return onUncommonlyUsedEvent;
    }

    public static OnWheelStatusEvent getOnWheelStatusEvent(String data) {
        onWheelStatusEvent.rawData = data;
        String[] split = data.replace("wheel_status{", "").replace("}", "").split(" ");
        if (split.length >= 11) {
            onWheelStatusEvent.state = Integer.parseInt(split[0]);
            onWheelStatusEvent.currentLeft = Integer.parseInt(split[1]);
            onWheelStatusEvent.currentRight = Integer.parseInt(split[2]);
            onWheelStatusEvent.tempLeft = Float.parseFloat(split[3]);
            onWheelStatusEvent.tempRight = Float.parseFloat(split[4]);
            onWheelStatusEvent.driverTempLeft = Float.parseFloat(split[5]);
            onWheelStatusEvent.driverTempRight = Float.parseFloat(split[6]);
            onWheelStatusEvent.codeLeft = Integer.parseInt(split[7]);
            onWheelStatusEvent.codeRight = Integer.parseInt(split[8]);
            onWheelStatusEvent.model = split[9];
            onWheelStatusEvent.version = split[10];
        }
        return onWheelStatusEvent;
    }

    public static OnCheckSensorsEvent getOnCheckSensorsEvent(String data) {
        onCheckSensorsEvent.rawData = data;
        String[] split = data.replace("check_sensors{", "").replace("}", "").split(" ");
        if (split.length >= 4) {
            boolean imuWarn = split[0].equals("0");
            if (imuWarn && ++onCheckSensorsEvent.imuWarnCount < 3) {
                onCheckSensorsEvent.imuWarn = false;
            } else {
                if (!imuWarn) {
                    onCheckSensorsEvent.imuWarnCount = 0;
                }
                onCheckSensorsEvent.imuWarn = imuWarn;
            }
            boolean lidarWarn = split[1].equals("0");
            if (lidarWarn && ++onCheckSensorsEvent.lidarWarnCount < 3) {
                onCheckSensorsEvent.lidarWarn = false;
            } else {
                if (!lidarWarn) {
                    onCheckSensorsEvent.lidarWarnCount = 0;
                }
                onCheckSensorsEvent.lidarWarn = lidarWarn;
            }
            boolean odomWarn = split[2].equals("0");
            if (odomWarn && ++onCheckSensorsEvent.odomWarnCount < 3) {
                onCheckSensorsEvent.odomWarn = false;
            } else {
                if (!odomWarn) {
                    onCheckSensorsEvent.odomWarnCount = 0;
                }
                onCheckSensorsEvent.odomWarn = odomWarn;
            }
            boolean cam3DWarn = split[3].equals("0");
            if (cam3DWarn && ++onCheckSensorsEvent.cam3DWarnCount < 3) {
                onCheckSensorsEvent.cam3DWarn = false;
            } else {
                if (!cam3DWarn) {
                    onCheckSensorsEvent.cam3DWarnCount = 0;
                }
                onCheckSensorsEvent.cam3DWarn = cam3DWarn;
            }
        }
        return onCheckSensorsEvent;
    }

    public static OnMoveStatusEvent getOnMoveStatusEvent(String data) {
        onMoveStatusEvent.moveStatus = Integer.parseInt(data.replace("move_status:", ""));
        return onMoveStatusEvent;
    }

    public static OnBatteryFixedEvent getOnBatteryFixedEvent(String data) {
        String[] split = data.replace("battery_info{", "").replace("}", "").split(" ");
        if (split.length >= 8) {
            onBatteryFixedEvent.manufacturer = split[0];
            onBatteryFixedEvent.nominalVoltage = Integer.parseInt(split[1]);
            onBatteryFixedEvent.temperature = Float.parseFloat(split[2]);
            onBatteryFixedEvent.cycleTimes = Integer.parseInt(split[3]);
            onBatteryFixedEvent.ratedCapacity = Integer.parseInt(split[4]);
            onBatteryFixedEvent.fullCapacity = Integer.parseInt(split[5]);
            onBatteryFixedEvent.capacity = Integer.parseInt(split[6]);
            onBatteryFixedEvent.health = Integer.parseInt(split[7]);
        }
        return onBatteryFixedEvent;
    }

    public static OnBatteryDynamicEvent getOnBatteryDynamicEvent(String data) {
        String[] split = data.replace("current_info{", "").replace("}", "").split(" ");
        if (split.length >= 5) {
            onBatteryDynamicEvent.voltage = Integer.parseInt(split[0]);
            onBatteryDynamicEvent.current = Integer.parseInt(split[1]);
            onBatteryDynamicEvent.adapterCurrent = Integer.parseInt(split[2]);
            onBatteryDynamicEvent.warning = split[3];
            onBatteryDynamicEvent.offline = split[4].equals("0");
            BatteryCurveLogUtil.getInstance().save("电量 : " + onCoreDataEvent.battery + ", 充电状态 : " + onCoreDataEvent.charger + ", " + onBatteryDynamicEvent.toString());
        }
        return onBatteryDynamicEvent;
    }

    public static OnGetStopTimeEvent getOnGetStopTimeEvent(String data) {
        onGetStopTimeEvent.stopTime = Integer.parseInt(data.replace("get_stop_time:", "").split("\\.")[0]);
        return onGetStopTimeEvent;
    }

    public static OnGlobalPEvent getOnGlobalPEvent(String data) {
        onGlobalPEvent.handleObstacles = "set_globalcost_p[0]".equals(data);
        return onGlobalPEvent;
    }

    public static OnBaseUpgradeEvent getOnBaseUpgradeEvent(String data) {
        onBaseUpgradeEvent.state = data.split(":")[1];
        return onBaseUpgradeEvent;
    }


    public static OnGetFlagPointEvent getOnGetFlagPointEvent(String data) {
        String[] split = data.replace("get_flag_point[", "").replace("]", "").split(",");
        if (split.length == 5) {
            onGetFlagPointEvent.x = Double.parseDouble(split[0]);
            onGetFlagPointEvent.y = Double.parseDouble(split[1]);
            onGetFlagPointEvent.radian = Double.parseDouble(split[2]);
            onGetFlagPointEvent.type = split[3];
            onGetFlagPointEvent.name = split[4];
        }
        return onGetFlagPointEvent;
    }

    public static OnGetPointNotFoundEvent getOnGetPointNotFoundEvent() {
        return onGetPointNotFoundEvent;
    }

    public static OnSetFlagPointEvent getOnSetFlagPointEvent(String data) {
        onSetFlagPointEvent.result = Integer.parseInt(data.replace("set_flag_point:", ""));
        return onSetFlagPointEvent;
    }

    public static OnDelFlagPointEvent getOnDelFlagPointEvent(String data) {
        onDelFlagPointEvent.result = Integer.parseInt(data.replace("del_flag_point:", ""));
        return onDelFlagPointEvent;
    }

    public static OnNavResultEvent getOnNavResultEvent(String data) {
        onNavResultEvent.rawData = data;
        String replace = data.replace("nav_result{", "").replace("}", "");
        String[] split = replace.split(" ");
        if (split.length == 5) {
            onNavResultEvent.state = Integer.parseInt(split[0]);
            onNavResultEvent.code = Integer.parseInt(split[1]);
            onNavResultEvent.name = split[2];
            onNavResultEvent.distToGoal = Float.parseFloat(split[3]);
            onNavResultEvent.mileage = Float.parseFloat(split[4]);
            onNavResultEvent.isNavigating = onNavResultEvent.state == 1;
            if (onNavResultEvent.state == 0) {
                onBaseValEvent.lineSpeed = 0;
            }
        }
        return onNavResultEvent;
    }

    public static OnHflsVersionEvent getOnHflsVersionEvent(String data) {
        onHflsVersionEvent.rawData = data;
        String[] split = data.replace("hfls_version:", "").split(" ");
        if (split.length == 4) {
            onHflsVersionEvent.hardwareVersion = split[0];
            onHflsVersionEvent.firmwareVersion = split[1];
            onHflsVersionEvent.loaderVersion = split[2];
            onHflsVersionEvent.softVersion = split[3];
        }
        return onHflsVersionEvent;
    }

    public static OnModelEvent getOnModelEvent(int model) {
        onModelEvent.model = model;
        return onModelEvent;
    }

    public static OnCoreDataEvent getCoreData() {
        return onCoreDataEvent;
    }

    public static OnCoreDataEvent getOnCoreDataEvent(String data) {
        onCoreDataEvent.rawData = data;
        String[] split = data.replace("core_data{", "").replace("}", "").split(" ");
        if (split.length >= 5) {
            onCoreDataEvent.bumper = Integer.parseInt(split[0]);
            onCoreDataEvent.cliff = Integer.parseInt(split[1]);
            onCoreDataEvent.button = Integer.parseInt(split[2]);
            onCoreDataEvent.battery = Integer.parseInt(split[3]);
            onCoreDataEvent.charger = Integer.parseInt(split[4]);
        }
        return onCoreDataEvent;
    }


    public static OnPathEvent getOnPathEvent(int res) {
        onPathEvent.res = res;
        return onPathEvent;
    }

    public static OnLaserEvent getLaser() {
        return onLaserEvent;
    }

    public static OnLaserEvent getOnLaserEvent(String result) {
        onLaserEvent.rawData = result;
        onLaserEvent.laser = Float.parseFloat(result.replace("laser[", "").replace("]", ""));
        return onLaserEvent;
    }


    public static OnApplyMapEvent getOnApplyMapEvent(String result) {
        if (result.startsWith("apply_map:")) {
            onApplyMapEvent.success = false;
        } else {
            String replace = result.replace("apply_map[", "").replace("]", "");
            if (replace.contains(" ")) {
                String[] split = replace.split(" ");
                onApplyMapEvent.map = split[0];
                onApplyMapEvent.alias = split[1];
                onMapEvent.map = split[0];
                onMapEvent.alias = split[1];
            } else {
                onApplyMapEvent.map = replace;
                onApplyMapEvent.alias = null;
                onMapEvent.map = replace;
                onMapEvent.alias = null;
            }
            onApplyMapEvent.success = true;
        }
        return onApplyMapEvent;
    }

    public static OnWiFiEvent getOnWiFiEvent(boolean result) {
        onWiFiEvent.isConnect = result;
        return onWiFiEvent;
    }

    public static OnSpeedEvent getOnSpeedEvent(String result) {
        onSpeedEvent.speed = Float.parseFloat(result);
        return onSpeedEvent;
    }

    public static OnVersionEvent getVersionEvent(String result) {
        onVersionEvent.rawData = result;
        onVersionEvent.version = result.replace("ver:", "");
        return onVersionEvent;
    }

    public static OnHostnameEvent getHostnameEvent(String result) {
        onHostnameEvent.rawData = result;
        onHostnameEvent.hostname = result.replace("sys:boot:", "");
        return onHostnameEvent;
    }

    public static OnIpEvent getIpEvent(String result) {
        onIpEvent.rawData = result;
        if (TextUtils.isEmpty(result)) {
            onIpEvent.wifiName = "";
            onIpEvent.ipAddress = "127.0.0.1";
            return onIpEvent;
        }

        String[] split = result.split(":");
        if (split.length != 3) {
            onIpEvent.wifiName = "";
            onIpEvent.ipAddress = "127.0.0.1";
        } else if (result.contains("connecting")) {
            onIpEvent.wifiName = "";
            onIpEvent.ipAddress = "127.0.0.1";
        } else {
            onIpEvent.wifiName = split[1];
            onIpEvent.ipAddress = split[2];
        }
        return onIpEvent;
    }

    public static OnMapEvent getMapEvent(String result) {
        String map = result.replace("current_map[", "").replace("]", "");
        if (map.contains(" ")) {
            String[] split = map.split(" ");
            onMapEvent.map = split[0];
            onMapEvent.alias = split[1];
        } else {
            onMapEvent.map = map;
            onMapEvent.alias = null;
        }
        return onMapEvent;
    }

    public static OnMoveDoneEvent getOnMoveDoneEvent(String result) {
        onMoveDoneEvent.moveResult = Integer.parseInt(result.replace("move:done:", ""));
        return onMoveDoneEvent;
    }

    public static OnNavModeEvent getOnNavModeEvent(int mode) {
        onNavModeEvent.mode = mode;
        return onNavModeEvent;
    }

    public static OnPositionEvent getOnPositionEvent(String res) {
        String[] split = res.split(",");
        if (split.length == 3) {
            onPositionEvent.position[0] = Double.parseDouble(split[0]);
            onPositionEvent.position[1] = Double.parseDouble(split[1]);
            onPositionEvent.position[2] = Double.parseDouble(split[2]);
        }
        return onPositionEvent;
    }

    private static synchronized void refreshPath(double[] position) {
        if (onPathObtainEvent.arrays != null && onPathObtainEvent.arrays.size() >= 4 && onPathObtainEvent.isComplete) {
            int index = -1;
            double min = Double.MAX_VALUE;
            for (int i = 0; i < onPathObtainEvent.arrays.size(); i = i + 2) {
                double dis = calcDistance(position[0], position[1], onPathObtainEvent.arrays.get(i), onPathObtainEvent.arrays.get(i + 1));
                if (dis < min) {
                    min = dis;
                    index = i;
                }
            }
            if (min < 0.3 && index <= onPathObtainEvent.arrays.size() - 1) {
                try {
                    for (int i = 0; i <= index + 1; i++) {
                        onPathObtainEvent.arrays.remove(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double calcDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static OnMissPoseEvent getOnMissPoseEvent(String result) {
        onMissPoseEvent.rawData = result;
        onMissPoseEvent.result = Integer.parseInt(result.replace("miss_pose:", ""));
        return onMissPoseEvent;
    }

    public static class BaseEvent {
        public String rawData;
    }

    public static class OnVersionEvent extends BaseEvent {
        public String version;
    }

    public static class OnHostnameEvent extends BaseEvent {
        public String hostname;
    }


    public static class OnIpEvent extends BaseEvent {
        public String wifiName;
        public String ipAddress;
    }

    public static class OnPositionEvent extends BaseEvent {
        public double[] position = new double[3];
    }

    public static class OnNavModeEvent {
        public int mode;
    }


    public static class OnMapEvent extends BaseEvent {
        public String map;
        public String alias;
    }

    public static class OnMoveDoneEvent {
        public int moveResult;
    }


    public static class OnEncounterObstacleEvent {

    }

    public static class OnInitPoseEvent {
        public String currentPosition;

    }


    public static class OnSpeedEvent {
        public float speed;
    }

    public static class OnWiFiEvent {
        public boolean isConnect;
    }

    public static class OnApplyMapEvent {
        public boolean success;
        public String map;
        public String alias;
    }

    public static class OnPathEvent {
        public int res;
    }

    public static class OnMissPoseEvent extends BaseEvent {
        public int result;
    }

    public static class OnLaserEvent extends BaseEvent {
        public float laser;
    }

    public static class OnHflsVersionEvent extends BaseEvent {
        public String hardwareVersion;
        public String firmwareVersion;
        public String loaderVersion;
        public String softVersion;
    }

    public static class OnModelEvent {
        public int model;
    }

    public static class OnCoreDataEvent extends BaseEvent {
        public int bumper;
        public int cliff;
        public int button;
        public int battery;
        public int charger;
    }

    public static class OnNavResultEvent extends BaseEvent {
        public int state;
        public int code;
        public String name;
        public float distToGoal;
        public float mileage;
        public boolean isNavigating;
    }

    public static class OnGetFlagPointEvent {
        public double x;
        public double y;
        public double radian;
        public String type;
        public String name;
    }

    public static class OnGetPointNotFoundEvent {

    }

    public static class OnSetFlagPointEvent {
        public int result;
    }

    public static class OnDelFlagPointEvent {
        public int result;
    }

    public static class OnPowerOffEvent {
        public int result;
    }

    public static class OnBaseUpgradeEvent {
        public String state;
    }

    public static class OnGetStopTimeEvent {
        public int stopTime;
    }

    public static class OnGlobalPEvent {
        public boolean handleObstacles;
    }

    public static class OnBatteryFixedEvent {
        public String manufacturer;//制造商
        public int nominalVoltage;//标称电压
        public float temperature;//温度
        public int cycleTimes;//循环次数
        public int ratedCapacity;//额定容量
        public int fullCapacity;//满电容量
        public int capacity;//当前容量
        public int health;//电池健康

        @Override
        public String toString() {
            return "制造商 : " + manufacturer + "\n" +
                    "标称电压 : " + nominalVoltage + "mV\n" +
                    "温度 : " + temperature / 10.0f + "℃\n" +
                    "循环次数 : " + cycleTimes + "\n" +
                    "额定容量 : " + ratedCapacity + "mAh\n" +
                    "满电容量 : " + fullCapacity + "mAh\n" +
                    "当前容量 : " + capacity + "mAh\n" +
                    "健康程度 : " + getHealth(health);
        }

        private String getHealth(int health) {
            if (health == 0) {
                return "未知";
            } else if (health == 1) {
                return "健康";
            } else if (health == 2) {
                return "过热";
            } else if (health == 3) {
                return "容量老化";
            } else if (health == 4) {
                return "过冷";
            }
            return "";
        }
    }

    public static class OnBatteryDynamicEvent {
        public int voltage;//电压
        public int current;//电流
        public int adapterCurrent;//适配器电流
        public String warning;//警告
        public boolean offline;//离线

        @Override
        public String toString() {
            return "电压 : " + voltage + "mV" +
                    ", 电流 : " + current + "mA" +
                    ", 适配器电流 : " + adapterCurrent + "mA";
        }
    }

    public static class OnCheckSensorsEvent extends BaseEvent {
        public boolean imuWarn;
        public boolean lidarWarn;
        public boolean odomWarn;
        public boolean cam3DWarn;
        public int imuWarnCount;
        public int lidarWarnCount;
        public int odomWarnCount;
        public int cam3DWarnCount;

        @Override
        public String toString() {
            return rawData.replace("check_sensors{", "").replace("}", "").replace(" ", "");
        }
    }

    public static class OnMoveStatusEvent {
        public int moveStatus;
    }

    public static class OnWheelStatusEvent extends BaseEvent {
        public int state;
        public int currentLeft;
        public int currentRight;
        public float tempLeft;
        public float tempRight;
        public float driverTempLeft;
        public float driverTempRight;
        public int codeLeft;
        public int codeRight;
        public String model;
        public String version;

    }

    public static class OnUncommonlyUsedEvent extends BaseEvent {
    }

    public static class OnGlobalPathEvent {
        public volatile CopyOnWriteArrayList<Double> arrays;
        public boolean isComplete = false;
    }

    public static class OnWaypointUpdateEvent {
        public String state;
    }

    public static class OnBaseValEvent {
        public double lineSpeed;
        public double palstance;
    }

    public static class OnStartTaskEvent {
        public String taskStr;
    }

    public static class OnStopTaskEvent {

    }

    public static class OnRangeSensorEvent extends BaseEvent {
        public int range1;
        public int range2;
        public int range3;
    }

    public static class OnSpecialPlanEvent implements Serializable {
        public List<Room> rooms;
    }

    public static class OnAvoidEvent {
        public double[] coordinate = null;
    }

    public static class OnResumeTaskEvent {

    }

    public static class OnPauseTaskEvent {

    }

    public static class OnHumanDetectionEvent {
        public int result;
    }

    public static class OnAgvNavResultEvent {
        public String tag;
        public boolean success;
    }

    public static class OnAltitudeEvent {
        public int result;
    }

    public static class OnMoveEndEvent {
        public int type;
    }

    public static class OnDispatchResumeEvent {

    }

    public static class OnDispatchPauseEvent {

    }

    public static class OnRoutePointEvent {
        public List<String> points = new ArrayList<>();
    }

    public static class OnGetPlanDijEvent {
        public List<String> points = new ArrayList<>();
    }

    public static class OnSpecialAreaEvent {
        public int type = 0;
        public String name;
    }


}
