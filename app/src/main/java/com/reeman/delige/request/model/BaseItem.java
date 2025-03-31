package com.reeman.delige.request.model;

import java.io.Serializable;

public class BaseItem implements Serializable {
    public String name;

    public BaseItem(String name) {
        this.name = name;
    }

    public BaseItem() {
    }


    @Override
    public String toString() {
        return "BaseItem{" +
                "name='" + name + '\'' +
                '}';
    }
}