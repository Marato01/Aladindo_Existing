package com.reeman.delige.request.model;

import com.google.gson.annotations.SerializedName;

public class PathPoint extends BaseItem{
    @SerializedName("type")
    public String type;
    @SerializedName(value = "vehicleOrientationAngle",alternate = "a")
    private String vehicleOrientationAngle;
    @SerializedName(value = "xPosition",alternate = "x")
    private String xPosition;
    @SerializedName(value = "yPosition",alternate = "y")
    private String yPosition;


    public PathPoint(String name) {
        this.name = name;
    }

    public PathPoint(
            String name,
            String type,
            String vehicleOrientationAngle,
            String xPosition,
            String yPosition
    ) {
        this.name = name;
        this.type = type;
        this.vehicleOrientationAngle = vehicleOrientationAngle;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getAngle() {
        return Math.toRadians(Double.parseDouble(vehicleOrientationAngle)/1000);
    }

    public double getXPosition() {
        return Double.parseDouble(xPosition)/1000;
    }

    public double getYPosition() {
        return Double.parseDouble(yPosition)/1000;
    }


    @Override
    public String toString() {
        return "PathPoint{" +
                "t='" + type + '\'' +
                ", a='" + vehicleOrientationAngle + '\'' +
                ", x='" + xPosition + '\'' +
                ", y='" + yPosition + '\'' +
                "} " + super.toString();
    }
}
