package com.reeman.delige.utils;

import com.google.gson.Gson;

import com.reeman.delige.exceptions.NoRequiredPointsException;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.PathPoint;
import com.reeman.delige.request.model.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Emitter;

public class PointUtil {


    public static List<PathPoint> checkPathPoints(Emitter<List<PathPoint>> emitter, List<PathPoint> waypoints){
        boolean isChargingPileMarked = false;
        boolean isProductPointMarked = false;
        boolean isRecyclePointMarked = false;

        DestHelper.getInstance().setPathPoints(waypoints);
        List<PathPoint> points = new ArrayList<>();
        for (PathPoint waypoint : waypoints) {
            if (!isChargingPileMarked && waypoint.type.equals(ROS.PT.CHARGE)) {
                isChargingPileMarked = true;
                DestHelper.getInstance().setChargePoint(waypoint.name);
                DestHelper.getInstance().setChargePointCoordinate(new double[]{waypoint.getXPosition(), waypoint.getYPosition(), waypoint.getAngle()});
                continue;
            }
            if (!isProductPointMarked && waypoint.type.equals(ROS.PT.PRODUCT)) {
                isProductPointMarked = true;
                DestHelper.getInstance().setProductPoint(waypoint.name);
                continue;
            }
            if (!isRecyclePointMarked && waypoint.type.equals(ROS.PT.RECYCLE)) {
                isRecyclePointMarked = true;
                DestHelper.getInstance().setRecyclePoint(waypoint.name);
                continue;
            }
            if (waypoint.type.equals(ROS.PT.DELIVERY))
                points.add(waypoint);
        }

        //对餐位进行简单排序
        sortPoints(points);

        if (!isChargingPileMarked || !isProductPointMarked) {
            //没标注充电桩或出品点
            emitter.onError(new NoRequiredPointsException(points, isChargingPileMarked, isProductPointMarked));
            return null;
        }
        return points;
    }


    public static List<Point> checkPoints(Emitter<List<Point>> emitter, List<Point> waypoints) {
        boolean isChargingPileMarked = false;
        boolean isProductPointMarked = false;
        boolean isRecyclePointMarked = false;


        List<Point> points = new ArrayList<>();
        Map<String, Point> pointMap = new HashMap<>();
        Map<String, Point> avoidPointMap = new HashMap<>();
        for (Point waypoint : waypoints) {
            pointMap.put(waypoint.name, waypoint);
            if (waypoint.type.equals(ROS.PT.AVOID)) {
                avoidPointMap.put(waypoint.name, waypoint);
                continue;
            }
            if (!isChargingPileMarked && waypoint.type.equals(ROS.PT.CHARGE)) {
                isChargingPileMarked = true;
                Point.PoseDTO pose = waypoint.pose;
                DestHelper.getInstance().setChargePoint(waypoint.name);
                DestHelper.getInstance().setChargePointCoordinate(new double[]{pose.x, pose.y, pose.theta});
                continue;
            }
            if (!isProductPointMarked && waypoint.type.equals(ROS.PT.PRODUCT)) {
                isProductPointMarked = true;
                DestHelper.getInstance().setProductPoint(waypoint.name);
                continue;
            }
            if (!isRecyclePointMarked && waypoint.type.equals(ROS.PT.RECYCLE)) {
                isRecyclePointMarked = true;
                DestHelper.getInstance().setRecyclePoint(waypoint.name);
                continue;
            }
            if (waypoint.type.equals(ROS.PT.DELIVERY))
                points.add(waypoint);
        }

        //对餐位进行简单排序
        sortPoints(points);

        if (!isChargingPileMarked || !isProductPointMarked) {
            //没标注充电桩或出品点
            emitter.onError(new NoRequiredPointsException(points, isChargingPileMarked, isProductPointMarked));
            return null;
        }
        return points;
    }

    private static <T extends BaseItem> void sortPoints(List<T> points) {
        Collections.sort(points, (o1, o2) -> {
            try {
                return Integer.parseInt(o1.name) - Integer.parseInt(o2.name);
            } catch (Exception ignored) {
            }
            return o1.name.compareTo(o2.name);
        });
    }

}
