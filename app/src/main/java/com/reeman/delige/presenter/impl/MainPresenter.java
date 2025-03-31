package com.reeman.delige.presenter.impl;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.reeman.delige.activities.TaskExecutingActivity;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.MainContract;
import com.reeman.delige.exceptions.NoRequiredPointsException;
import com.reeman.delige.exceptions.NoRouteException;
import com.reeman.delige.light.LightController;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.Path;
import com.reeman.delige.request.model.PathPoint;
import com.reeman.delige.request.model.PathPointModel;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.DestHelper;
import com.reeman.delige.utils.PointUtil;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.VoiceHelper;
import com.reeman.delige.event.Event;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeEmitter;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeOnSubscribe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class MainPresenter implements MainContract.Presenter {

    public static final int REQUEST_CODE_FOR_TASK = 2001;
    private final MainContract.View view;

    public MainPresenter(MainContract.View view) {
        this.view = view;
    }

    @Override
    public void startBirthdayTask(Activity context, String target) {
        Intent intent = new Intent(context, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, target);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_BIRTHDAY);
        context.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startRecycleTask(Activity context, Route currentRoute) {
        Intent intent = new Intent(context, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, currentRoute);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_RECYCLE);
        context.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startRecycleTask(Activity context, String currentRoute) {
        Intent intent = new Intent(context, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, currentRoute);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_RECYCLE);
        context.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startCruiseTask(Activity context, Route currentRoute) {
        Intent intent = new Intent(context, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, currentRoute);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_CRUISE);
        context.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startDeployGuide(Context context) {
        if (!SpManager.getInstance().getBoolean(Constants.KEY_IS_MAP_BUILDING_GUIDE, false)) {
            VoiceHelper.play("voice_start_map_building_procedure");
            view.showGuideDeployDialog();
            return;
        }
        startOperationGuide(context);
    }

    @Override
    public void startOperationGuide(Context context) {
        if (!SpManager.getInstance().getBoolean(Constants.KEY_IS_OPERATION_GUIDED, false)) {
            //引导过建图流程，没引导过操作，引导用户操作
            VoiceHelper.play("voice_not_guide_for_novice");
            view.showOperationGuideView();
        } else {
            view.showAllGuidanceCompleteView();
        }
    }

    @Override
    public void startDeliveryFoodTask(Activity context, HashMap<Integer, String> level2TableMap) {
        Intent intent = new Intent(context, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, level2TableMap);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_DELIVERY_FOOD);
        context.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startRecycle2Task(Activity activity, HashMap<Integer, String> map) {
        Intent intent = new Intent(activity, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, map);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_RECYCLE_2);
        activity.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void startMultiDeliveryTask(Activity activity, HashMap<Integer, List<String>> map) {
        Intent intent = new Intent(activity, TaskExecutingActivity.class);
        intent.putExtra(Constants.TASK_TARGET, map);
        intent.putExtra(Constants.TASK_MODE, Constants.MODE_MULTI_DELIVERY);
        activity.startActivityForResult(intent, REQUEST_CODE_FOR_TASK);
    }

    @Override
    public void fetchRoutes(Context context, boolean isManualRefresh) {
        //不是手动刷新走缓存
        if (!isManualRefresh) {
            String routeStr = SpManager.getInstance().getString(Constants.KEY_ROUTE_INFO, null);
            if (!TextUtils.isEmpty(routeStr)) {
                Maybe.create(new MaybeOnSubscribe<List<Route>>() {
                            @Override
                            public void subscribe(@NonNull MaybeEmitter<List<Route>> emitter) {
                                try {
                                    List<Route> list = new Gson().fromJson(routeStr, new TypeToken<List<Route>>() {

                                    }.getType());
                                    emitter.onSuccess(list);
                                    emitter.onComplete();
                                } catch (Exception e) {
                                    emitter.onError(new IllegalStateException());
                                }
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new MaybeObserver<List<Route>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull List<Route> routes) {
                                DestHelper.getInstance().setRoutes(routes);
                                if (routes.isEmpty()) {
                                    view.onEmptyDataLoaded(false);
                                } else {
                                    view.onDataLoadSuccess(routes, false, true);
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                DestHelper.getInstance().setRoutes(new ArrayList<>());
                                view.onLoadFailed(false);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                return;
            }
        }
        //手动刷新走网络，网络不通再走缓存
        Observable
                .create(new ObservableOnSubscribe<List<Route>>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<Route>> emitter) {
                        Response<Map<String, List<List<Double>>>> response;
                        try {
                            response = ServiceFactory.getRobotService().fetchRoutes(API.fetchRoutesAPI(Event.getIpEvent().ipAddress)).execute();
                            Map<String, List<List<Double>>> body = response.body();
                            if (response.code() == 500) {
                                emitter.onError(new NoRouteException());
                                return;
                            }
                            if (body == null) {
                                throw new IllegalStateException();
                            }
                            List<Route> list = new ArrayList<>();
                            for (Map.Entry<String, List<List<Double>>> entry : body.entrySet()) {
                                list.add(new Route(entry.getKey(), entry.getValue()));
                            }
                            SpManager.getInstance().edit().putString(Constants.KEY_ROUTE_INFO, new Gson().toJson(list)).apply();
                            emitter.onNext(list);
                            emitter.onComplete();
                        } catch (Exception e) {
                            String routeStr = SpManager.getInstance().getString(Constants.KEY_ROUTE_INFO, null);
                            if (TextUtils.isEmpty(routeStr)) {
                                emitter.onError(new ConnectException());
                                return;
                            }
                            List<Route> list = new Gson().fromJson(routeStr, new TypeToken<List<Route>>() {

                            }.getType());
                            if (list == null || list.isEmpty()) {
                                emitter.onError(new ConnectException());
                                return;
                            }
                            emitter.onNext(list);
                            emitter.onComplete();
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Route>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<Route> routes) {
                        DestHelper.getInstance().setRoutes(routes);
                        if (routes.isEmpty()) {
                            view.onEmptyDataLoaded(false);
                        } else {
                            view.onDataLoadSuccess(routes, false, false);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        DestHelper.getInstance().setRoutes(new ArrayList<>());
                        if (e instanceof NoRouteException) {
                            view.onEmptyDataLoaded(false);
                        } else {
                            view.onLoadFailed(false);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void fetchPoints(Context context, boolean isManualRefresh) {
        //不是手动刷新走缓存
        if (!isManualRefresh) {
            String pointInfo = SpManager.getInstance().getString(Constants.KEY_POINT_INFO, null);
            if (!TextUtils.isEmpty(pointInfo)) {
                Observable.create(new ObservableOnSubscribe<List<Point>>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<List<Point>> emitter) {
                                try {
                                    List<Point> waypoints = new Gson().fromJson(pointInfo, new TypeToken<List<Point>>() {
                                    }.getType());
                                    List<Point> list = PointUtil.checkPoints(emitter, waypoints);
                                    if (list == null) {
                                        XLog.w("网络数据加载成功，没有充电桩，出品点");
                                        return;
                                    }
                                    emitter.onNext(list);
                                    emitter.onComplete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Point>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull List<Point> points) {
                                DestHelper.getInstance().setPoints(points);
                                if (points.isEmpty()) {
                                    view.onEmptyDataLoaded(true);
                                } else {
                                    view.onDataLoadSuccess(points, true, true);
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                NoRequiredPointsException e1 = (NoRequiredPointsException) e;
                                DestHelper.getInstance().setPoints(e1.list);
                                view.onLackOfRequiredPoint(e1.list, e1.isChargingPileMarked);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                return;
            }
        }

        Observable
                .create(new ObservableOnSubscribe<List<Point>>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<Point>> emitter) {
                        String hostIp = Event.getIpEvent().ipAddress;
                        Response<Map<String, List<Point>>> response;

                        try {
                            response = ServiceFactory.getRobotService().fetchPoints(API.fetchPointAPI(hostIp)).execute();
                            Map<String, List<Point>> map = response.body();
                            if (map == null || map.size() == 0) {
                                SpManager.getInstance().edit().putString(Constants.KEY_POINT_INFO, null).apply();
                                throw new ConnectException();
                            }
                            List<Point> waypoints = map.get("waypoints");
                            //将餐位缓存下来
                            SpManager.getInstance().edit().putString(Constants.KEY_POINT_INFO, new Gson().toJson(waypoints)).apply();

                            List<Point> list = PointUtil.checkPoints(emitter, waypoints);
                            if (list == null) {
                                XLog.w("网络数据加载成功，没有充电桩，出品点");
                                return;
                            }

                            //数据加载完成
                            XLog.w("使用网络数据");
                            emitter.onNext(list);
                            emitter.onComplete();

                        } catch (Exception e) {
                            // 本地也没有缓存
                            SharedPreferences instance = SpManager.getInstance();
                            String pointStr = instance.getString(Constants.KEY_POINT_INFO, null);
                            if (TextUtils.isEmpty(pointStr)) {
                                XLog.w("网络加载失败，缓存为空");
                                emitter.onError(new ConnectException());
                                return;
                            }

                            List<Point> waypoints = new Gson().fromJson(pointStr, new TypeToken<List<Point>>() {
                            }.getType());
                            List<Point> points = PointUtil.checkPoints(emitter, waypoints);
                            if (points == null) {
                                XLog.w("网络加载失败，缓存没有充电桩出品点");
                                return;
                            }

                            XLog.w("使用缓存数据");
                            //加载缓存完成
                            emitter.onNext(points);
                            emitter.onComplete();
                        }

                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Point>>() {
                    @Override
                    public void accept(List<Point> points) throws Throwable {
                        DestHelper.getInstance().setPoints(points);
                        if (points.isEmpty()) {
                            view.onEmptyDataLoaded(true);
                        } else {
                            view.onDataLoadSuccess(points, true, isManualRefresh);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        if (throwable instanceof NoRequiredPointsException) {
                            NoRequiredPointsException throwable1 = (NoRequiredPointsException) throwable;
                            List<? extends BaseItem> list = throwable1.list;
                            DestHelper.getInstance().setPoints(list);
                            view.onLackOfRequiredPoint(list, throwable1.isChargingPileMarked);
                        } else if (throwable instanceof ConnectException) {
                            DestHelper.getInstance().setPoints(new ArrayList<>());
                            view.onLoadFailed(true);
                        } else {
                            DestHelper.getInstance().setPoints(new ArrayList<>());
                            XLog.w("" + throwable.getMessage());
                            view.onLoadFailed(true);
                        }
                    }
                });
    }

    @Override
    public void fetchFixPoints(Context context, boolean isManualRefresh) {
        //不是手动刷新走缓存
        Gson gson = new Gson();
        if (!isManualRefresh) {
            String pointInfo = SpManager.getInstance().getString(Constants.KEY_POINT_INFO, null);
            String pathInfo = SpManager.getInstance().getString(Constants.KEY_PATH_INFO,null);
            if (!TextUtils.isEmpty(pointInfo)) {
                Observable.create(new ObservableOnSubscribe<List<PathPoint>>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<List<PathPoint>> emitter) {
                                try {
                                    List<PathPoint> waypoints = gson.fromJson(pointInfo, new TypeToken<List<PathPoint>>() {
                                    }.getType());
                                    List<Path> pathList = gson.fromJson(pathInfo,new TypeToken<List<Path>>(){}.getType());
                                    DestHelper.getInstance().setPathList(pathList);
                                    List<PathPoint> list = PointUtil.checkPathPoints(emitter, waypoints);
                                    if (list == null) {
                                        XLog.w("网络数据加载成功，没有充电桩，出品点");
                                        return;
                                    }
                                    emitter.onNext(list);
                                    emitter.onComplete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<PathPoint>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onNext(@NonNull List<PathPoint> points) {
                                DestHelper.getInstance().setPoints(points);
                                if (points.isEmpty()) {
                                    view.onEmptyDataLoaded(true);
                                } else {
                                    view.onDataLoadSuccess(points, true, true);
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                NoRequiredPointsException e1 = (NoRequiredPointsException) e;
                                DestHelper.getInstance().setPoints(e1.list);
                                view.onLackOfRequiredPoint(e1.list, e1.isChargingPileMarked);
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
                return;
            }
        }

        Observable
                .create(new ObservableOnSubscribe<List<PathPoint>>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<PathPoint>> emitter) {
                        String hostIp = Event.getIpEvent().ipAddress;
                        Response<PathPointModel> response;

                        try {
                            response = ServiceFactory.getRobotService().fetchPathPoints(API.fetchPathPointAPI(hostIp)).execute();
                            PathPointModel pathPointModel = response.body();
                            if (pathPointModel == null || pathPointModel.getPoint() == null || pathPointModel.getPoint().isEmpty()) {
                                SpManager.getInstance().edit().putString(Constants.KEY_POINT_INFO, null).apply();
                                throw new ConnectException();
                            }
                            List<PathPoint> waypoints = pathPointModel.getPoint();
                            List<Path> paths = pathPointModel.getPath();
                            DestHelper.getInstance().setPathList(paths);
                            //将餐位缓存下来
                            SpManager.getInstance().edit()
                                    .putString(Constants.KEY_POINT_INFO, gson.toJson(waypoints, new TypeToken<List<PathPoint>>() {
                                    }.getType()))
                                    .putString(Constants.KEY_PATH_INFO, gson.toJson(paths,new TypeToken<List<Path>>(){}.getType()))
                                    .apply();

                            List<PathPoint> list = PointUtil.checkPathPoints(emitter, waypoints);
                            if (list == null) {
                                XLog.w("网络数据加载成功，没有充电桩，出品点");
                                return;
                            }

                            //数据加载完成
                            XLog.w("使用网络数据");
                            emitter.onNext(list);
                            emitter.onComplete();

                        } catch (Exception e) {
                            // 本地也没有缓存
                            SharedPreferences instance = SpManager.getInstance();
                            String pointStr = instance.getString(Constants.KEY_POINT_INFO, null);
                            String pathInfo = SpManager.getInstance().getString(Constants.KEY_PATH_INFO,null);
                            if (TextUtils.isEmpty(pointStr)) {
                                XLog.w("网络加载失败，缓存为空");
                                emitter.onError(new ConnectException());
                                return;
                            }

                            List<PathPoint> waypoints = gson.fromJson(pointStr, new TypeToken<List<PathPoint>>() {
                            }.getType());
                            List<Path> pathList = gson.fromJson(pathInfo,new TypeToken<List<Path>>(){}.getType());
                            DestHelper.getInstance().setPathList(pathList);
                            List<PathPoint> points = PointUtil.checkPathPoints(emitter, waypoints);
                            if (points == null) {
                                XLog.w("网络加载失败，缓存没有充电桩出品点");
                                return;
                            }

                            XLog.w("使用缓存数据");
                            //加载缓存完成
                            emitter.onNext(points);
                            emitter.onComplete();
                        }

                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PathPoint>>() {
                    @Override
                    public void accept(List<PathPoint> points) throws Throwable {
                        DestHelper.getInstance().setPoints(points);
                        if (points.isEmpty()) {
                            view.onEmptyDataLoaded(true);
                        } else {
                            view.onDataLoadSuccess(points, true, isManualRefresh);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        if (throwable instanceof NoRequiredPointsException) {
                            NoRequiredPointsException throwable1 = (NoRequiredPointsException) throwable;
                            List<? extends BaseItem> list = throwable1.list;
                            DestHelper.getInstance().setPoints(list);
                            view.onLackOfRequiredPoint(list, throwable1.isChargingPileMarked);
                        } else if (throwable instanceof ConnectException) {
                            DestHelper.getInstance().setPoints(new ArrayList<>());
                            view.onLoadFailed(true);
                        } else {
                            DestHelper.getInstance().setPoints(new ArrayList<>());
                            XLog.w("" + throwable.getMessage());
                            view.onLoadFailed(true);
                        }
                    }
                });
    }

    /**
     * 根据餐位名称找到所属页
     *
     * @param str
     * @return
     */
    @Override
    public int getTableGroupByTableName(String str) {
        int index = 0;
        List<? extends BaseItem> points = DestHelper.getInstance().getPoints();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).name.equals(str)) {
                index = i;
                break;
            }
        }
        return index / (SpManager.getInstance().getInt(Constants.KEY_POINT_COLUMN, Constants.DEFAULT_POINT_COLUMN) * 4);
    }

    public void openAllLights() {
        LightController.getInstance().openAll();
    }

    public void closeAllLights() {
        LightController.getInstance().closeAll();
    }

}
