package com.reeman.delige.request.model;

public class PointInfo extends BaseItem{

    public double width;

    public PointInfo(String name, double width) {
        super(name);
        this.width = width;
    }

    @Override
    public String toString() {
        return "PointInfo{" +
                "name='" + name + '\'' +
                ", width=" + width +
                "} " + super.toString();
    }
}
