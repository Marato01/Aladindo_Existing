package com.reeman.delige.request.model;

public class Response {
    public int code;
    public String msg;

    @Override
    public String toString() {
        return "Response{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
