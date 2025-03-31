package com.reeman.delige.utils;

import com.aill.androidserialport.ByteUtil;
import com.aill.androidserialport.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortParser {

    private SerialPort serialPortParser;
    private byte[] bytes;
    private final Thread thread;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private volatile boolean stopped = false;
    private OnDataResultListener listener;

    public SerialPortParser(File file, int baudrate, OnDataResultListener listener) throws Exception {
        serialPortParser = new SerialPort(file, baudrate, 0);
        inputStream = serialPortParser.getInputStream();
        outputStream = serialPortParser.getOutputStream();
        this.listener = listener;
        bytes = new byte[1024];
        thread = new Thread(new ReadRunnable(), "serial-port-read-thread");
    }

    public void start() {
        thread.start();
    }


    public void stop() {
        stopped = true;
    }

    public void sendCommand(byte[] bytes) throws IOException {
        if (this.outputStream != null) {
            this.outputStream.write(bytes);
        }
    }

    public void sendStr(String str) throws IOException {
        if (this.outputStream != null) {
            this.outputStream.write(ByteUtil.hexStringToBytes(str));
        }
    }

    public interface OnDataResultListener {
        void onDataResult(byte[] bytes, int len);
    }

    private class ReadRunnable implements Runnable {

        @Override
        public void run() {
            while (!stopped) {
                try {
                    if (inputStream.available() <= 0) {
                        continue;
                    }
                    int len;
                    if ((len = inputStream.read(bytes)) > 0) {
                        if (listener != null) {
                            listener.onDataResult(bytes, len);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            serialPortParser.close();
            serialPortParser = null;
            bytes = null;
        }
    }

}
