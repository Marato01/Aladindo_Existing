package com.reeman.delige.request.model;


import com.google.gson.annotations.SerializedName;

public class Point extends BaseItem {
//    @SerializedName("name")
//    public String name;
    @SerializedName("type")
    public String type;
    @SerializedName("pose")
    public PoseDTO pose;

    public Point(){

    }

    public Point(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "Point{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", pose=" + pose +
                '}';
    }

    public static class PoseDTO {
        @SerializedName("x")
        public Double x;
        @SerializedName("y")
        public Double y;
        @SerializedName("theta")
        public Double theta;

        @Override
        public String toString() {
            return "PoseDTO{" +
                    "x=" + x +
                    ", y=" + y +
                    ", theta=" + theta +
                    '}';
        }
    }
}
