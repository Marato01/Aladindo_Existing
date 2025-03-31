package com.reeman.delige.event.model;


import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {

    public String name;
    public List<Double> coordination;
    public int type;

    @Override
    public String toString() {
        return "Room{" +
                "name='" + name + '\'' +
                ", coordination=" + coordination +
                ", type=" + type +
                '}';
    }
}
