package com.reeman.delige.navigation;

public interface ROS {

    public interface ES {
        int SWITCH_DOWN = 0;
        int SWITCH_UP = 1;
    }

    public interface CS {
        int NOT_CHARGE = 0;
        int CHARGING = 1;
        int DOCKING = 2;
        int CHARGE_FAILURE = 3;
    }

    public interface PT {
        String DELIVERY = "delivery";
        String CHARGE = "charge";
        String PRODUCT = "production";
        String RECYCLE = "recycle";
        String AVOID = "avoid";
        String NORMAL = "normal";
        String AGVTAG = "agvtag";
    }

    public interface NS {
        int FAILURE = -1;//导航失败
        int INITIAL = 0;//初始状态
        int START = 1;//开始导航
        int PAUSE = 2;//暂停
        int COMPLETE = 3;//导航完成
        int CANCEL = 4;//取消导航
        int RESUME = 5;//恢复
        int RECEIVE = 6;//导航端收到导航指令,但还没开始导航
    }
}
