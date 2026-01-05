package com.wizarpos.util;

import java.util.Locale;

/**
 * @author john
 * Convert byte[] to hex string
 */
//"Hello World" in annotation stands for "72 101 108 108 111 32 87 111 114 108 100", ignore the spaces
public class ByteConvertStringUtil {
    /**
     * 将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * @param src byte[] data
     */

    //Hello World   -->     48656C6C6F20576F726C64
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toUpperCase(Locale.ROOT);
    }

    //Hello World   -->     48 65 6C 6C 6F 20 57 6F 72 6C 64
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(" ");
        }
        return stringBuilder.toString().toUpperCase(Locale.ROOT);
    }

    //Hello World   -->     48 65 6C 6C 6F 20 57 6F 72 6C 64
    public static String buf2StringCompact(byte[] buf) {
        int i, index;
        StringBuilder sBuf = new StringBuilder();
//        sBuf.append("[");
        for (i = 0; i < buf.length; i++) {
            index = buf[i] < 0 ? buf[i] + 256 : buf[i];
            if (index < 16) {
                sBuf.append("0").append(Integer.toHexString(index));
            } else {
                sBuf.append(Integer.toHexString(index));
            }
            sBuf.append(" ");
        }
        String substring = sBuf.substring(0, sBuf.length() - 1);
//        return (substring + "]").toUpperCase();
        return (substring).toUpperCase();
    }


    public static String byteToHexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = src & 0xFF;
        String hv = Integer.toHexString(v);
        return hv;
    }

    //Returns true if the input string follows big decimal format
    public static boolean isValidBigDecimalFormat(String input) {
        if (input == null || input.isEmpty()) {
            return false; // Invalid if null or empty
        }
        // Regex explanation:
        // ^[+-]? - Optional '+' or '-' at the start
        // \\d*   - Zero or more digits
        // (\\.\\d+)? - Optional decimal point followed by one or more digits
        // $       - End of the string
        return input.matches("^[+-]?\\d*(\\.\\d+)?$");
    }

}
