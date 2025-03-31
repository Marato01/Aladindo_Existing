package com.reeman.delige.exceptions;

import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.Point;

import java.util.List;

public class NoRequiredPointsException extends IllegalStateException {
    public final List<? extends BaseItem> list;
    public final boolean isProductPointMarked;
    public final boolean isChargingPileMarked;

    public NoRequiredPointsException(List<? extends BaseItem> list, boolean isChargingPileMarked, boolean isProductPointMarked) {
        this.list = list;
        this.isChargingPileMarked = isChargingPileMarked;
        this.isProductPointMarked = isProductPointMarked;
    }
}


