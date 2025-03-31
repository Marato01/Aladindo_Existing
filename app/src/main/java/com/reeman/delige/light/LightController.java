package com.reeman.delige.light;

import com.elvishew.xlog.XLog;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.utils.SerialPortParser;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 灯光控制
 */
public class LightController {
    private static LightController lightController;
    private boolean isTouch = false;

    public static final String CMD_OPEN_LIGHT_ONE_LEVEL = "a3b5800db00830050000000000";
    public static final String CMD_OPEN_LIGHT_TWO_LEVEL = "a3b5810db00831050000000000";
    public static final String CMD_OPEN_LIGHT_THREE_LEVEL = "a3b5820db00832050000000000";
    public static final String CMD_BLINK_ONE_LEVEL = "a3b5e40db00830010060000000";
    public static final String CMD_BLINK_TWO_LEVEL = "a3b5e50db00831010060000000";
    public static final String CMD_BLINK_THREE_LEVEL = "a3b5e60db00832010060000000";
    public static final String CMD_OPEN_ALL = "a3b58f0db0083f050000000000";
    public static final String CMD_CLOSE_ALL = "a3b58c0db0083f060000000000";

    //按键按下
    public static final String CALLBACK_KEY_DOWN = "a5b3190af005100100f7";
    public static final String CALLBACK_KEY_UP = "a5b3100af005100000ff";
    private SerialPortParser parser;
    private final Pattern mPattern = Pattern.compile("a5b3", Pattern.CASE_INSENSITIVE);

    private LightController() {

    }

    public static LightController getInstance() {
        if (lightController == null) {
            lightController = new LightController();
        }
        return lightController;
    }

    public void start() throws Exception {
        parser = new SerialPortParser(new File("/dev/ttyS2"), 115200, new SerialPortParser.OnDataResultListener() {
            final StringBuilder sb = new StringBuilder();

            @Override
            public void onDataResult(byte[] bytes, int len) {
                sb.append(bytesToHexString(bytes, len));

                while (sb.length() != 0) {
                    if (sb.length() <= 4) break;
                    Matcher matcher = mPattern.matcher(sb);
                    if (matcher.find()) {
                        try {
                            int frameStartIndex = matcher.start(); //0
                            int checkSumStartIndex = frameStartIndex + 4; //4
                            int checkSumEndIndex = checkSumStartIndex + 2; //6
                            if (checkSumEndIndex >= sb.length()) break;
                            String checkSum = sb.substring(checkSumStartIndex, checkSumEndIndex);
                            int lengthEndIndex = checkSumEndIndex + 2; //8
                            if (lengthEndIndex >= sb.length()) break;
                            int length = Integer.parseInt(sb.substring(checkSumEndIndex, lengthEndIndex), 16);
                            int dataEndIndex = lengthEndIndex + (length - 4) * 2;
                            if (dataEndIndex > sb.length()) break; //8 + 12
                            if (checkSum.equals(checkSum(sb.substring(checkSumEndIndex, dataEndIndex)))) {
                                String dataPackage = sb.substring(lengthEndIndex, dataEndIndex);
                                String command = dataPackage.substring(dataPackage.length() - 6, dataPackage.length() - 4);
                                if (command.equals("01")) {
                                    if (!isTouch) {
                                        isTouch = true;
                                        XLog.w("触控键按下");
                                        EventBus.getDefault().post(RobotEvent.getOnTouchEvent());
                                    }
                                } else if (command.equals("00")) {
                                    isTouch = false;
                                }
                                sb.delete(0, dataEndIndex);
                            } else if (matcher.find()) {
                                sb.delete(0, matcher.start());
                            } else {
                                sb.delete(0, sb.length());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sb.delete(0, sb.length());
                        }
                    } else {
                        sb.delete(0, sb.length());
                    }
                }
            }
        });
        parser.start();
    }

    public void release() {
        if (parser != null) {
            parser.stop();
        }
        parser = null;
    }

    /**
     * 打开所有层的两侧白光灯
     */
    public void openAll() {
        if (parser == null) return;
        try {
            parser.sendStr(CMD_OPEN_ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭两侧所有白光灯
     */
    public void closeAll() {
        if (parser == null) return;
        try {
            parser.sendStr(CMD_CLOSE_ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定层的两侧白光灯开始闪烁
     *
     * @param level
     */
    public void blink(int level) {
        if (parser == null) return;
        try {
            if (level == 0) {
                parser.sendStr(CMD_BLINK_ONE_LEVEL);
            } else if (level == 1) {
                parser.sendStr(CMD_BLINK_TWO_LEVEL);
            } else if (level == 2) {
                parser.sendStr(CMD_BLINK_THREE_LEVEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 某一层两侧白光灯常亮
     *
     * @param level
     */
    public void light(int level) {
        if (parser == null) return;
        try {
            if (level == 0) {
                parser.sendStr(CMD_OPEN_LIGHT_ONE_LEVEL);
            } else if (level == 1) {
                parser.sendStr(CMD_OPEN_LIGHT_TWO_LEVEL);
            } else if (level == 2) {
                parser.sendStr(CMD_OPEN_LIGHT_THREE_LEVEL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < len; i++) {
            stringBuilder.append(String.format("%02x", src[i] & 0xFF));
        }
        return stringBuilder.toString();
    }

    public static String checkSum(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            checkData = start ^ checkData;
        }
        return String.format("%02x", checkData);
    }
}
