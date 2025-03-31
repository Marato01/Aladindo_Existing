package com.reeman.delige.calling;

import android.os.Environment;
import android.text.TextUtils;

import com.aill.androidserialport.ByteUtil;
import com.reeman.delige.event.RobotEvent;
import com.reeman.delige.utils.ByteUtils;
import com.reeman.delige.utils.FileMapUtils;
import com.reeman.delige.utils.PackageUtils;
import com.reeman.delige.utils.SerialPortParser;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallingHelper {
    private static CallingHelper sInstance;
    private SerialPortParser parser;
    private boolean start = false;
    private final Pattern mPattern = Pattern.compile("AA55");

    private CallingHelper() {

    }

    public static CallingHelper getInstance() {
        if (sInstance == null) {
            sInstance = new CallingHelper();
        }
        return sInstance;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public void start() throws Exception {
        File file = new File("/sys/bus/usb/devices/1-1.2/1-1.2:1.0");
        File[] files = file.listFiles();
        if (!file.exists() || files == null || files.length == 0) {
            throw new FileNotFoundException();
        }
        File target = null;
        for (File temp : files) {
            if (temp.getName().startsWith("ttyUSB")) {
                target = temp;
                break;
            }
        }
        if (target == null) throw new FileNotFoundException();
        parser = new SerialPortParser(new File("/dev/" + target.getName()), 115200, new SerialPortParser.OnDataResultListener() {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public void onDataResult(byte[] bytes, int len) {
                sb.append(ByteUtils.byteArr2HexString(bytes, len));
                while (sb.length() != 0) {
                    if (sb.length() < 4) break;
                    Matcher matcher = mPattern.matcher(sb);
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
                            if (checkSum.equals(ByteUtils.checksum(dataHexSum))) {
                                String key = sb.substring(dataLastIndex - 2, dataLastIndex);
                                String s = FileMapUtils.get(Environment.getExternalStorageDirectory() + File.separator + PackageUtils.getAppName() + File.separator + "key-map.txt", key);
                                if (!TextUtils.isEmpty(s)) {
                                    EventBus.getDefault().post(RobotEvent.getOnCallingEvent(s));
                                }
                                sb.delete(0, dataLastIndex + 2);
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
        start = true;
    }

    public void stop() {
        if (parser != null) {
            parser.stop();
        }
        start = false;
        sInstance = null;
    }


}
