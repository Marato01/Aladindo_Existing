package com.reeman.delige.request.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PathPointModel implements Serializable {
    @SerializedName("point")
    private List<PathPoint> point;

    @SerializedName("path")
    private List<Path> path;

    public PathPointModel(List<PathPoint> point) {
        this.point = point;
    }

    public List<PathPoint> getPoint() {
        return point;
    }

    public void setPoint(List<PathPoint> point) {
        this.point = point;
    }

    public List<Path> getPath() {
        return path;
    }

    public void setPath(List<Path> path) {
        this.path = path;
    }
}
