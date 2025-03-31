package com.reeman.delige.contract;

import android.app.Activity;
import android.content.Context;

import com.reeman.delige.presenter.IPresenter;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.view.IView;

import java.util.HashMap;
import java.util.List;

public interface MainContract {
    
    interface Presenter extends IPresenter{

        void fetchRoutes(Context context, boolean isManualRefresh);

        void fetchPoints(Context context, boolean isManualRefresh);

        void fetchFixPoints(Context context,boolean isManualRefresh);

        int getTableGroupByTableName(String str);

        void startDeliveryFoodTask(Activity context, HashMap<Integer, String> level2TableMap);

        void startBirthdayTask(Activity context, String target);

        void startRecycleTask(Activity context, Route route);

        void startRecycleTask(Activity context, String currentRoute);

        void startCruiseTask(Activity context, Route currentRoute);

        void startDeployGuide(Context context);

        void startOperationGuide(Context context);

        void startRecycle2Task(Activity activity, HashMap<Integer, String> map);

        void startMultiDeliveryTask(Activity activity, HashMap<Integer, List<String>> map);
    }
    
    interface View extends IView{

        void onLackOfRequiredPoint(List<? extends BaseItem> list, boolean isChargingPileMarked);

        void onDataLoadSuccess(List<? extends BaseItem> points, boolean isPoint, boolean isManualRefresh);

        void onLoadFailed(boolean isPoint);

        void onEmptyDataLoaded(boolean isPoint);

        void showGuideDeployDialog();

        void showAllGuidanceCompleteView();

        void showOperationGuideView();
    }
}
