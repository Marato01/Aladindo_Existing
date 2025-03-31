package com.reeman.delige.request.model;

import java.util.ArrayList;
import java.util.List;

public class Route extends BaseItem {

    public List<List<Double>> pointList;


    public Route() {

    }

    public Route(String name) {
        this.name = name;
    }

    public Route(String name, List<List<Double>> pointList) {
        super(name);
        this.pointList = pointList;
    }


    @Override
    public String toString() {
        return "Route{" +
                "pointList=" + pointList +
                ", name='" + name + '\'' +
                '}';
    }

    public static Route clone(Route route, boolean reverse) {
        Route temp = new Route();
        temp.name = new String(route.name);
        temp.pointList = new ArrayList<>();
        if (reverse) {
            for (int i = route.pointList.size()-1; i >= 0; i--) {
                List<Double> list = new ArrayList<>();
                List<Double> floats = route.pointList.get(i);
                list.add(floats.get(0));
                list.add(floats.get(1));
                temp.pointList.add(list);
            }
        } else {
            for (List<Double> floats : route.pointList) {
                List<Double> list = new ArrayList<>();
                list.add(floats.get(0));
                list.add(floats.get(1));
                temp.pointList.add(list);
            }
        }
        return temp;
    }

}
