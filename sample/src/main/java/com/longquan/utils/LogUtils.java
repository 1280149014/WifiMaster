package com.longquan.utils;

import android.util.Log;

import com.MyApplication;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class LogUtils {
    private static final String TAG = MyApplication.versionName+ "wifi_log";

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + " " + msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String tag, String msg){
        Log.d(TAG, tag + " " + msg);
    }

    public static void e(String msg, Throwable b) {
        Log.e(TAG, msg, b);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " " + msg);
    }
}
