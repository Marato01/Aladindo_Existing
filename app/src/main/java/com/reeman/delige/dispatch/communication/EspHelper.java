package com.reeman.delige.dispatch.communication;

import android.util.Log;

import com.aill.androidserialport.ByteUtil;
import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.utils.ByteUtils;
import com.reeman.ros.controller.SerialPortParser;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EspHelper {
    private SerialPortParser instance;
    private static final byte[] CMD_ENTER_BOARD_CAST = new byte[]{(byte) 0xAA, 0x55, 0x04, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    private static final byte[] CMD_ENTER_TRANSMISSION = new byte[]{(byte) 0xAA, 0x55, 0x03, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    private static final byte[] CMD_GET_MAC = new byte[]{(byte) 0xAA, 0x55, 0x01, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
    private static final byte[] CMD_SET_COMMAND1 = new byte[]{(byte) 0xAA, 0x55, 0x02, 0x12, (byte) 0x94, (byte) 0xb5, 0x55, (byte) 0xf8, (byte) 0x7f, 0x2c, (byte) 0xb8, (byte) 0xd6, 0x1a, (byte) 0xa7, (byte) 0xdd, (byte) 0xb8, (byte) 0xE0, (byte) 0xE2, (byte) 0xE6, 0x0B, 0x70, 0x2C};
    private static final String CMD_EXIT_TRANSMISSION = "+++";

    private final StringBuilder sb = new StringBuilder();
    private final Pattern pattern = Pattern.compile("AA54");
    private boolean stopped = false;
    private MsgSender msgSender;
    private boolean ready;
    private static long lastPublishTimeMills = 0;
    private final Gson gson = new Gson();
    public EspHelper() {
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
        instance = new SerialPortParser(new File("/dev/" + targetFile.getName()), 115200, (bytes, len) -> {
            sb.append(ByteUtils.byteArr2HexString(bytes, len));
            while (sb.length() != 0) {
                if (sb.length() < 4) break;
                Matcher matcher = pattern.matcher(sb);
                if (matcher.find()) {
                    try {
                        int start = matcher.start();
                        int startIndex = start + 4;

                        if (startIndex + 2 >= sb.length()) break;

                        String dataSize = sb.substring(startIndex, startIndex + 2);
                        int intSize = ByteUtil.hexStringToInt(dataSize);

                        int dataLastIndex = startIndex + intSize * 2 + 2;

                        if (dataLastIndex + 2 > sb.length())
                            break;

                        String dataHexSum = sb.substring(startIndex, dataLastIndex);
                        String checkSum = sb.substring(dataLastIndex, dataLastIndex + 2);
                        if (checkSum.equals(ByteUtils.checksum(dataHexSum))) {
                            String s = hexStringToASCIIStr(sb.substring(startIndex + 2, dataLastIndex));
//                            RobotInfo robotInfo = gson.fromJson(s, RobotInfo.class);
//                            Log.w("robotInfo",robotInfo.toString());
//                            EventBus.getDefault().post(robotInfo);
                            EventBus.getDefault().post(gson.fromJson(s, RobotInfo.class));
                            sb.delete(0, dataLastIndex + 2);
                        } else if (matcher.find()) {
                            Log.w("xuedong", "ESP数据包校验不通过:" + hexStringToASCIIStr(sb.substring(startIndex + 2, dataLastIndex)));
                            sb.delete(0, matcher.start());
                        } else {
                            Log.w("xuedong", "ESP数据包校验不通过:" + sb);
                            sb.delete(0, sb.length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        XLog.w("ESP数据包 ::" + e.getMessage());
                        sb.delete(0, sb.length());
                    }
                } else {
                    sb.delete(0, sb.length());
                }
            }
        });
        instance.start();
        msgSender = new MsgSender();
        new Thread(msgSender).start();
    }

    public void stop() {
        if (instance != null) {
            instance.stop();
        }
        if (msgSender != null) {
            stopped = true;
        }
    }

    public void enterTransmission() {
        try {
            instance.sendCommand(CMD_ENTER_TRANSMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enterBoardCast(){
        try {
            instance.sendCommand(CMD_ENTER_BOARD_CAST);
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

    public void getMacAddress() {
        try {
            instance.sendCommand(CMD_GET_MAC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMacAddress() {
        try {
            instance.sendCommand(CMD_SET_COMMAND1);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void send(RobotInfo msg) {
        msgSender.send(msg);
    }

    public boolean getReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    private class MsgSender implements Runnable {
        private final ConcurrentLinkedQueue<RobotInfo> queue;
        private final Gson gson;

        public void send(RobotInfo msg) {
            queue.add(msg);
        }

        public MsgSender() {
            queue = new ConcurrentLinkedQueue<>();
            gson = new Gson();
        }

        @Override
        public void run() {
            while (true) {
                if (stopped) break;
                long l = System.currentTimeMillis();
                if (l - lastPublishTimeMills > 50) {
                    lastPublishTimeMills = l;
                    try {
                        if (!queue.isEmpty()) {
                            RobotInfo msg = queue.poll();
                            instance.sendCommand(string2BH(gson.toJson(msg)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
