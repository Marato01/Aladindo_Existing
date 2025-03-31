package com.reeman.delige.utils;

import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.Path;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DestHelper {
    private static final DestHelper pointsContainer = new DestHelper();
    private List<? extends BaseItem> points;
    private List<? extends BaseItem> pathPoints;
    private List<Route> routes;
    private String chargePoint;
    private double[] chargePointCoordinate;
    private String productPoint;
    private String recyclePoint;
    private List<Path> pathList;

    private DestHelper() {

    }

    public static DestHelper getInstance() {
        return pointsContainer;
    }


    public List<Path> getPathList() {
        return pathList;
    }

    public void setPathList(List<Path> pathList) {
        this.pathList = pathList;
    }

    public String getChargePoint() {
        return chargePoint;
    }

    public void setChargePoint(String chargePoint) {
        this.chargePoint = chargePoint;
    }

    public double[] getChargePointCoordinate() {
        return chargePointCoordinate;
    }

    public void setChargePointCoordinate(double[] chargePointCoordinate) {
        this.chargePointCoordinate = chargePointCoordinate;
    }

    public String getProductPoint() {
        return productPoint;
    }

    public void setProductPoint(String productPoint) {
        this.productPoint = productPoint;
    }

    public String getRecyclePoint() {
        return recyclePoint;
    }

    public void setRecyclePoint(String recyclePoint) {
        this.recyclePoint = recyclePoint;
    }

    public List<? extends BaseItem> getPoints() {
        return points;
    }

    public void setPoints(List<? extends BaseItem> points) {
        this.points = points;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<? extends BaseItem> getPathPoints() {
        return pathPoints;
    }

    public void setPathPoints(List<? extends BaseItem> pathPoints) {
        this.pathPoints = pathPoints;
    }
}
