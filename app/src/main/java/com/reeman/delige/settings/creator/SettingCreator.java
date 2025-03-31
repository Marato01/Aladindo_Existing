package com.reeman.delige.settings.creator;

import com.google.gson.InstanceCreator;
import com.reeman.delige.base.BaseSetting;
import com.reeman.delige.settings.BirthdayModeSetting;
import com.reeman.delige.settings.CruiseModeSetting;
import com.reeman.delige.settings.DeliveryMealSetting;
import com.reeman.delige.settings.MultiDeliverySetting;
import com.reeman.delige.settings.ObstacleSetting;
import com.reeman.delige.settings.RecycleModeSetting;

import java.lang.reflect.Type;

public class SettingCreator<T extends BaseSetting> implements InstanceCreator<T> {

    @Override
    public T createInstance(Type type) {
        if (type == BirthdayModeSetting.class) {
            return (T) BirthdayModeSetting.getDefault();
        } else if (type == CruiseModeSetting.class) {
            return (T) CruiseModeSetting.getDefault();
        } else if (type == DeliveryMealSetting.class) {
            return (T) DeliveryMealSetting.getDefault();
        } else if (type == MultiDeliverySetting.class) {
            return (T) MultiDeliverySetting.getDefault();
        } else if (type == RecycleModeSetting.class) {
            return (T) RecycleModeSetting.getDefault();
        } else if (type == ObstacleSetting.class) {
            return (T) ObstacleSetting.getDefault();
        }
        return null;
    }
}