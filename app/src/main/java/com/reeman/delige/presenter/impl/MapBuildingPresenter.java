package com.reeman.delige.presenter.impl;

import static com.reeman.delige.base.BaseApplication.ros;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;

import com.google.gson.Gson;
import com.reeman.delige.R;

import com.reeman.delige.constants.Constants;
import com.reeman.delige.contract.MapBuildingContract;
import com.reeman.delige.navigation.ROS;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.Route;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.SpManager;
import com.reeman.delige.utils.ToastUtils;
import com.reeman.delige.widgets.EasyDialog;
import com.reeman.delige.event.Event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Response;

public class MapBuildingPresenter implements MapBuildingContract.Presenter {

    private final MapBuildingContract.View view;

    private int currentMode = 2;

    private boolean isBackFromMapScanning = false;

    private int pointType;

    private List<List<Double>> pointList;

    private String currentMarkPointName;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ros.getPosition();
            handler.postDelayed(runnable, 1000);
        }
    };


    public List<List<Double>> getPointList() {
        return pointList;
    }

    public boolean isBackFromMapScanning() {
        return isBackFromMapScanning;
    }

    public void setBackFromMapScanning(boolean backFromMapScanning) {
        isBackFromMapScanning = backFromMapScanning;
    }

    @Override
    public void changeToConstructMap() {
        this.currentMode = 2;
        ros.modelMapping();
    }

    @Override
    public void changeToNavMode() {
        this.currentMode = 1;
        ros.modelNavi();
    }

    @Override
    public void saveMap() {
        this.currentMode = 1;
        ros.saveMap();
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }

    public int getPointType() {
        return pointType;
    }


    @Override
    public void getCurrentPosition() {
        ros.getPosition();
    }

    @Override
    public void exitWithoutSaving() {
        this.currentMode = 1;
        ros.modelNavi();
    }

    @Override
    public void markPoint(Context context, double[] position, String currentMarkPointName) {
        if (pointType == 1) {
            ros.markPoint(position, ROS.PT.CHARGE, context.getString(R.string.point_charging_pile));
            this.currentMarkPointName = context.getString(R.string.point_charging_pile);
        } else if (pointType == 2) {
            ros.markPoint(position,ROS.PT.DELIVERY, currentMarkPointName);
            this.currentMarkPointName = currentMarkPointName;
        } else if (pointType == 3) {
            ros.markPoint(position,ROS.PT.PRODUCT, context.getString(R.string.point_product_point));
            this.currentMarkPointName = context.getString(R.string.point_product_point);
        } else if (pointType == 4) {
            ros.markPoint(position,ROS.PT.RECYCLE, context.getString(R.string.point_recycle_point));
            this.currentMarkPointName = context.getString(R.string.point_recycle_point);
        }
    }

    @Override
    public void reloadPoints(Context context) {
        String ipAddress = Event.getIpEvent().ipAddress;
        if (TextUtils.isEmpty(ipAddress)) return;

        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                Response<Map<String, List<Point>>> pointRes = ServiceFactory.getRobotService().fetchPoints(API.fetchPointAPI(ipAddress)).execute();
                Map<String, List<Point>> map = pointRes.body();

                if (pointRes.code() == 200 && map != null) {
                    List<Point> waypoints = map.get("waypoints");
                    SpManager.getInstance().edit().putString(Constants.KEY_POINT_INFO, new Gson().toJson(waypoints)).apply();
                } else {
                    SpManager.getInstance().edit().remove(Constants.KEY_POINT_INFO).apply();
                }
                Response<Map<String, List<List<Double>>>> routeRes = ServiceFactory.getRobotService().fetchRoutes(API.fetchRoutesAPI(ipAddress)).execute();
                Map<String, List<List<Double>>> routeMap = routeRes.body();
                if (routeRes.code() == 200 && routeMap != null) {
                    List<Route> list = new ArrayList<>();
                    for (Map.Entry<String, List<List<Double>>> entry : routeMap.entrySet()) {
                        list.add(new Route(entry.getKey(), entry.getValue()));
                    }
                    SpManager.getInstance().edit().putString(Constants.KEY_ROUTE_INFO, new Gson().toJson(list)).apply();
                } else {
                    SpManager.getInstance().edit().remove(Constants.KEY_ROUTE_INFO).apply();
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Object o) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.onReloadPointFailed();
                    }

                    @Override
                    public void onComplete() {
                        view.onReloadPointSuccess();
                    }
                });
    }

    @Override
    public void startDrawPath(Context context) {
        if (pointList == null) {
            pointList = new ArrayList<>();
        }
        handler.post(runnable);
    }

    @Override
    public void onPositionLoaded(double[] position) {
        if (pointList.isEmpty()) {
            List<Double> list = new ArrayList<>();
            list.add(position[0]);
            list.add(position[1]);
            pointList.add(list);
        } else {
            List<Double> lastPoint = pointList.get(pointList.size() - 1);
            if (Math.sqrt(Math.pow(lastPoint.get(0) - position[0], 2) + Math.pow(lastPoint.get(1) - position[1], 2)) > 1) {
                List<Double> list = new ArrayList<>();
                list.add(position[0]);
                list.add(position[1]);
                pointList.add(list);
            }
        }
    }

    @Override
    public void abandonPath() {
        handler.removeCallbacks(runnable);
        if (pointList != null) {
            pointList.clear();
        }
    }

    @Override
    public void savePath(Context context) {
        handler.removeCallbacks(runnable);
        if (pointList == null || pointList.size() < 3) {
            ToastUtils.showShortToast(context.getString(R.string.text_path_too_short));
            return;
        }

        EasyDialog.newCustomInstance(context, R.layout.layout_input_route_name).showInputRouteNameDialog(new EasyDialog.OnViewClickListener() {
            @Override
            public void onViewClick(Dialog dialog, int id) {
                if (id == R.id.btn_confirm) {
                    EditText editText = (EditText) EasyDialog.getInstance().getView(R.id.et_content);
                    String str = editText.getText().toString();
                    if (TextUtils.isEmpty(str)) {
                        ToastUtils.showShortToast(context.getString(R.string.text_route_name_can_not_be_empty));
                        return;
                    }
                    dialog.dismiss();
                    handler.postDelayed(() -> {
                        EasyDialog.getLoadingInstance(context).loading(context.getString(R.string.text_saving_path));
                        Map<String, List<List<Double>>> params = new HashMap<>();

                        final String ipAddress = Event.getIpEvent().ipAddress;
                        Observable.create(new ObservableOnSubscribe<Object>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<Object> emitter) throws Throwable {
                                Response<Map<String, List<List<Double>>>> response = ServiceFactory.getRobotService().fetchRoutes(API.fetchRoutesAPI(ipAddress)).execute();
                                Map<String, List<List<Double>>> body = response.body();
                                if (body != null) {
                                    for (Map.Entry<String, List<List<Double>>> entry : body.entrySet()) {
                                        if (entry.getKey().equals(str)) continue;
                                        params.put(entry.getKey(), entry.getValue());
                                    }
                                }
                                params.put(str, pointList);
                                ServiceFactory.getRobotService().savePathSync(API.savePathAPI(ipAddress), params).execute();
                                emitter.onNext(1);
                                emitter.onComplete();
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Object>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onNext(@NonNull Object o) {

                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                        pointList.clear();
                                        view.onPathSaveFailed(e.getMessage());
                                    }

                                    @Override
                                    public void onComplete() {
                                        pointList.clear();
                                        view.onPathSaveSuccess();
                                    }
                                });
                    }, 500);
                } else {
                    dialog.dismiss();
                    abandonPath();
                }
            }
        });
    }

    @Override
    public void deletePoint() {
        ros.deletePoint(currentMarkPointName);
    }


    public MapBuildingPresenter(MapBuildingContract.View view) {
        this.view = view;
    }

    public static double m2(double f) {
        BigDecimal bg = new BigDecimal(f);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;
    }
}
