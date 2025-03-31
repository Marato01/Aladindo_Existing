package com.reeman.delige.plugins;

import android.util.Log;

import com.elvishew.xlog.XLog;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.request.ServiceFactory;
import com.reeman.delige.request.model.LoginResponse;
import com.reeman.delige.request.service.RobotService;
import com.reeman.delige.request.url.API;
import com.reeman.delige.utils.LocaleUtil;
import com.reeman.delige.utils.SpManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String WAN_PATH = "http://navi.rmbot.cn/OpenAPISpring/ros/locations/find";

    private static final Retrofit client;

    private static OkHttpClient okHttpClient;

    public static final String TAG = RetrofitClient.class.getSimpleName();

    static {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    int dataSync = SpManager.getInstance().getInt(Constants.KEY_DATA_SYNC_TYPE, 1);
                    String s = request.url().toString();
                    int localeType = LocaleUtil.getLocaleType();
                    if (localeType==1){
                        s = s.replace("slam.rmbot.cn", "navi.rmbot.cn");
                    }else {
                        s = s.replace("navi.rmbot.cn", "slam.rmbot.cn");
                    }
//                    if (dataSync == 1) {
//                        s = s.replace("navi.rmbot.cn", "slam.rmbot.cn");
//                    } else {
//                        s = s.replace("slam.rmbot.cn", "navi.rmbot.cn");
//                    }
                    request = request.newBuilder().url(s).build();
                    String accessToken = SpManager.getInstance().getString(Constants.KEY_ACCESS_TOKEN, null);
                    if (accessToken != null) {
                        Headers headers = request.headers().newBuilder().add("Authorization", accessToken).build();
                        request = request.newBuilder().headers(headers).build();
                    }
                    Response response = chain.proceed(request);
                    Log.w(TAG, request.toString() + "=====" + response.toString());

                    if (response.code() == 401 && !request.url().toString().contains("tokens")) {
                        XLog.w("token过期，重新登录");
                        Map<String, String> map = new HashMap<>();
                        map.put("account", "");
                        map.put("password", "");
                        RobotService robotService = ServiceFactory.getRobotService();
                        Call<LoginResponse> loginResponseCall = robotService.loginSync(API.tokenAPI(), map);
                        LoginResponse loginResponse = loginResponseCall.execute().body();
                        if (loginResponse == null || loginResponse.data == null || loginResponse.data.result == null)
                            return response;
                        XLog.w("重新登录成功,重新发起请求");
                        response.close();
                        SpManager.getInstance().edit().putString(Constants.KEY_ACCESS_TOKEN, loginResponse.data.result.accessToken).apply();
                        Headers headers = request.headers().newBuilder().add("Authorization", loginResponse.data.result.accessToken).build();
                        Request newRequest = request.newBuilder().headers(headers).build();
                        Log.w("重新请求", newRequest.toString());
                        return chain.proceed(newRequest);
                    }
                    return response;
                })
                .build();


        client = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .baseUrl(WAN_PATH + "/")
                .client(getOkHttpClient())
                .build();
    }


    private RetrofitClient() {

    }

    public static Retrofit getClient() {
        return client;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    private static String getBodyString(RequestBody requestBody) {
        String bodyString = null;
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            try {
                requestBody.writeTo(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaType contentType = requestBody.contentType();
            Charset charset = StandardCharsets.UTF_8;
            if (contentType != null) {
                charset = contentType.charset(charset);
            }
            bodyString = buffer.readString(charset);
        }
        return bodyString;
    }
}
