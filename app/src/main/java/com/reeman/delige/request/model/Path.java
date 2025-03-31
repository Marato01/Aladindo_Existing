package com.reeman.delige.request.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class Path extends BaseItem {

    @SerializedName(value = "sourcePoint",alternate = "sp")
    public String sourcePoint;

    @SerializedName(value = "destinationPoint",alternate = "dp")
    public String destinationPoint;

    @SerializedName(value = "pathWidth",alternate = "w")
    private String pathWidth;

    public Double getPathWidth() {
        return TextUtils.isEmpty(pathWidth) ? 1.0 : Double.parseDouble(pathWidth) / 100.0;
    }

    public void setPathWidth(String pathWidth) {
        this.pathWidth = pathWidth;
    }

    @Override
    public String toString() {
        return "Path{" +
                "sourcePoint='" + sourcePoint + '\'' +
                ", destinationPoint='" + destinationPoint + '\'' +
                ", pathWidth='" + pathWidth + '\'' +
                "} " + super.toString();
    }
}
