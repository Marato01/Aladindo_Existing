package com.reeman.delige.dispatch.config;

import android.util.Log;

import com.aill.androidserialport.ByteUtil;
import com.elvishew.xlog.XLog;
import com.reeman.ros.controller.SerialPortParser;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EspConfigurer {
    private SerialPortParser instance;
    private static final byte[] CMD_ENTER_TRANSMISSION = new byte[]{(byte) 0xAA, 0x55, 0x03, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    private static final byte[] CMD_GET_MAC = new byte[]{(byte) 0xAA, 0x55, 0x01, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    private static final String CMD_EXIT_TRANSMISSION = "+++";
    private final StringBuilder sb = new StringBuilder();
    private final Pattern pattern = Pattern.compile("AA54");
    private final Pattern macAddressPattern = Pattern.compile("(\\w{2}:){5}\\w{2}");

    private String currentMacAddress;

    private final StringBuilder macAddress = new StringBuilder();

    public static final int ACTION_GET_MAC_ADDRESS = 0;
    public static final int ACTION_QUICK_CONFIG = 1;
    private volatile int action = -1;

    public void setAction(int action) {
        this.action = action;
    }

    public String getCurrentMacAddress() {
        return currentMacAddress;
    }

    public static String byteArr2HexString(byte[] inBytArr, int len) {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < len; ++i) {
            strBuilder.append(String.format("%02x", inBytArr[i]).toUpperCase());
        }

        return strBuilder.toString();
    }

    public static String XORCheck(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            checkData = start ^ checkData;
        }
        return String.format("%02x", checkData).toUpperCase();
    }

    public static String hexStringToASCIIStr(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, StandardCharsets.UTF_8);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static byte[] string2BH(String res) {
        byte[] bytes = res.getBytes();
        byte[] byte1 = new byte[4 + bytes.length];
        byte1[byte1.length - 1] = (byte) bytes.length;

        for (int i = 0; i < bytes.length; ++i) {
            byte1[byte1.length - 1] ^= bytes[i];
            byte1[i + 3] = bytes[i];
        }
        byte1[0] = -86;
        byte1[1] = 84;
        byte1[2] = (byte) bytes.length;
        return byte1;
    }

    public void start() throws Exception {
        File file = new File("/sys/bus/usb/devices/1-1.3/1-1.3:1.0");
        File targetFile = null;
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File tempFile : files) {
                    if (tempFile.getName().startsWith("ttyUSB")) {
                        targetFile = tempFile;
                        break;
                    }
                }
            }
        }
        if (targetFile == null) throw new FileNotFoundException();
        instance = new SerialPortParser(new File("/dev/" + targetFile.getName()), 115200, new SerialPortParser.OnDataResultListener() {
            @Override
            public void onDataResult(byte[] bytes, int len) {
                if (action == ACTION_GET_MAC_ADDRESS) {
                    macAddress.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
                    if (macAddress.length() < 17) return;
                    Matcher matcher = macAddressPattern.matcher(macAddress);
                    if (matcher.find()) {
                        action = -1;
                        event.type = 1;
                        event.msg = macAddress.substring(matcher.start(), matcher.end());
                        currentMacAddress = event.msg;
                        macAddress.delete(0, macAddress.length());
                        EventBus.getDefault().post(event);
                    }
                } else {
                    sb.append(byteArr2HexString(bytes, len));
                    while (sb.length() != 0) {
                        if (sb.length() < 4) break;
                        Matcher matcher = pattern.matcher(sb);
                        if (matcher.find()) {
                            try {
                                int start = matcher.start();
                                int startIndex = start + 4;

                                if (startIndex + 2 >= sb.length())
                                    break;

                                String dataSize = sb.substring(startIndex, startIndex + 2);
                                int intSize = ByteUtil.hexStringToInt(dataSize);

                                int dataLastIndex = startIndex + intSize * 2 + 2;

                                if (dataLastIndex + 2 > sb.length())
                                    break;

                                String dataHexSum = sb.substring(startIndex, dataLastIndex);
                                String checkSum = sb.substring(dataLastIndex, dataLastIndex + 2);
                                event.type = 2;
                                if (checkSum.equals(XORCheck(dataHexSum))) {
                                    event.setMsg(hexStringToASCIIStr(sb.substring(startIndex + 2, dataLastIndex)));
                                    sb.delete(0, dataLastIndex + 2);
                                } else if (matcher.find()) {
                                    XLog.w("异常数据: " + sb);
                                    sb.delete(0, matcher.start());
                                } else {
                                    XLog.w("异常数据:" + sb);
                                    sb.delete(0, sb.length());
                                }
                                EventBus.getDefault().post(event);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sb.delete(0, sb.length());
                            }
                        } else {
                            sb.delete(0, sb.length());
                        }
                    }
                }
            }
        });
        instance.start();
    }

    public void stop() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    public void sendCommand(String command) {
        try {
            instance.sendCommand(command.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data) {
        try {
            instance.sendCommand(string2BH(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enterTransmission() {
        try {
            instance.sendCommand(CMD_ENTER_TRANSMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMacAddress() {
        try {
            setAction(ACTION_GET_MAC_ADDRESS);
            macAddress.delete(0, macAddress.length());
            instance.sendCommand(CMD_GET_MAC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitTransmission() {
        try {
            byte[] bytes = CMD_EXIT_TRANSMISSION.getBytes();
            instance.sendCommand(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setMacAddress(HashSet<String> set) {
        if (currentMacAddress != null)
            set.remove(currentMacAddress);
        int size = set.size();
        byte len = (byte) (size * 6);
        byte[] bytes = new byte[4 + len];
        bytes[0] = (byte) 0xaa;
        bytes[1] = (byte) 0x55;
        bytes[2] = (byte) 0x02;
        bytes[3] = len;
        int count = 4;
        try {
            for (String s : set) {
                String[] split = s.split(":");
                for (String s1 : split) {
                    int b = Integer.parseInt(s1, 16);
                    bytes[count++] = (byte) b;
                }
            }
            Log.w("xuedong", byteArr2HexString(bytes, bytes.length));
            instance.sendCommand(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final MsgEvent event = new MsgEvent();

    public static class MsgEvent {
        public String msg;
        public int type;

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
