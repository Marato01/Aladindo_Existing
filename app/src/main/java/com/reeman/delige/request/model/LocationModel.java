package com.reeman.delige.request.model;

public class LocationModel {

    public String msg;
    public Integer code;
    public DataDTO data;

    public static class DataDTO{
        public ResultDTO result;

        public static class ResultDTO{
            public Integer id;
            public String device;
            public String location;
            public Integer deviceId;
            public String updateTime;

            @Override
            public String toString() {
                return "ResultDTO{" +
                        "id=" + id +
                        ", device='" + device + '\'' +
                        ", location='" + location + '\'' +
                        ", deviceId=" + deviceId +
                        ", updateTime='" + updateTime + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "DataDTO{" +
                    "result=" + result +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LocationModel{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}
