package com.longquan.common.wifiap;

import java.util.Random;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WifiApUtil {
    private static String TAG = WifiApUtil.class.getSimpleName();

    public static final int DEFAULT_AP_WIFI_PWD_LENGTH = 12;

    public static final String HOTSPOT_SSID = "Lynk & Co";

    public static final int SECURITY = 1;

    /**
     * 随机字符串生成工具
     *
     * @param length 随机生成字符串的个数，范围：a-z,A-Z,0-9
     */
    public static String getRandomStrForApWifi(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result = 0;
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append(String.valueOf((char) result));
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append(String.valueOf((char) result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }

    public static String addDoubleQuates(String src) {
        return "\"" + src + "\"";
    }

    /**
     * 去除引号
     * 4.0之后获取的名称包含有双引号
     */
    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static boolean checkSSID(String connectSSID, String nowSSID) {
        // 4.0以后 getSSID返回值额外加入“”
        // return connectSSID.equals(nowSSID);

        return connectSSID.equals(String.format("\"%s\"", nowSSID));
    }

    public static Integer[] toIntegerArray(int[] arr) {
        int length = arr.length;
        Integer[] iarr = new Integer[length];
        for (int i = 0; i < length; i++) {
            iarr[i] = new Integer(arr[i]);
        }
        return iarr;
    }

}
