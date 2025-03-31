package com.reeman.delige.utils;

public class ByteUtils {

    public static String checksum(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            checkData = start ^ checkData;
        }
        return String.format("%02X", checkData).toUpperCase();
    }

    public static String byteArr2HexString(byte[] inBytArr, int len) {
        StringBuilder strBuilder = new StringBuilder();

        for (int i = 0; i < len; ++i) {
            strBuilder.append(String.format("%02X", inBytArr[i]).toUpperCase());
        }
        return strBuilder.toString();
    }
}
