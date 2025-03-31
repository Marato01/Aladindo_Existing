package com.reeman.delige.request.service;


import com.reeman.delige.request.model.LoginResponse;
import com.reeman.delige.request.model.MapVO;
import com.reeman.delige.request.model.PathPoint;
import com.reeman.delige.request.model.PathPointModel;
import com.reeman.delige.request.model.Point;
import com.reeman.delige.request.model.Response;
import com.reeman.delige.request.model.StateRecord;
import com.reeman.delige.repository.entities.DeliveryRecord;
import com.reeman.delige.request.model.ChargeRecord;
import com.reeman.delige.request.model.FaultRecord;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RobotService {

    @GET
    Observable<List<MapVO>> getMapList(@Url String url);

    @GET
    Observable<Map<String, List<String>>> fetchPointsAsync(@Url String url);

    @GET
    Call<Map<String, List<Point>>> fetchPoints(@Url String url);

    @GET
    Call<PathPointModel> fetchPathPoints(@Url String url);

    @GET
    Call<Map<String, List<List<Double>>>> fetchRoutes(@Url String url);


    @GET
    Observable<List<String>> fetchMusic(@Url String url);

    @Streaming
    @POST
    Observable<ResponseBody> download(@Url String url, @Body Map<String, String> map);

    /**
     * 上报充电结果
     *
     * @param url
     * @param record
     * @return
     */
    @POST
    Observable<Map<String, Object>> reportChargeResult(@Url String url, @Body ChargeRecord record);

    /**
     * 登录获取token
     *
     * @param url
     * @param loginModel
     * @return
     */
    @POST
    Observable<LoginResponse> login(@Url String url, @Body Map<String, String> loginModel);

    /**
     * 同步登录获取token
     *
     * @param url
     * @param loginModel
     * @return
     */
    @POST
    Call<LoginResponse> loginSync(@Url String url, @Body Map<String, String> loginModel);

    /**
     * 上报状态
     *
     * @param url
     * @param record
     * @return
     */
    @POST
    Call<Map<String, Object>> heartbeat(@Url String url, @Body StateRecord record);

    /**
     * 上报任务执行结果
     *
     * @param url
     * @param record
     * @return
     */
    @POST
    Observable<Response> reportTaskResult(@Url String url, @Body DeliveryRecord record);

    @POST
    Call<Map<String, Object>> reportTaskListResult(@Url String url, @Body List<DeliveryRecord> record);

    /**
     * 上报硬件异常
     *
     * @param url
     * @param record
     * @return
     */
    @POST
    Observable<Response> reportHardwareError(@Url String url, @Body FaultRecord record);

    @POST
    Observable<Map<String, Object>> savePath(@Url String url, @Body Map<String, List<List<Float>>> list);

    @POST
    Call<Map<String, Object>> savePathSync(@Url String url, @Body Map<String, List<List<Double>>> list);

    @POST
    Observable<Map<String, Object>> notify(@Url String url, @Body Map<String, String> body);
}
