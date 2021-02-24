package com.longquan.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WirelessUtils {
    private static final String TAG = WirelessUtils.class.getSimpleName();

    private static final String KEY_CURRENT_SELECTED_AP = "key_current_selected_access_point";
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isApStateOn(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WIFI_AP_STATE_ENABLED || getWifiApState(wifiManager) == WIFI_AP_STATE_ENABLING;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isApStateChanging(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WIFI_AP_STATE_DISABLING || getWifiApState(wifiManager) == WIFI_AP_STATE_ENABLING;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isWifiApDisabled(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WIFI_AP_STATE_DISABLED;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean stopSoftAp(WifiManager wifiManager) {
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Method method = wifiManager.getClass().getDeclaredMethod("stopSoftAp");
            method.setAccessible(true);
            return (boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LogUtils.d(TAG, e.toString());
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static int getWifiApState(WifiManager wifiManager) {
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            return (int) method.invoke(wifiManager);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LogUtils.d(TAG, e.toString());
        }
        return -1;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isWifiApEnabled(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WIFI_AP_STATE_ENABLED;
       /* try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            return (boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LogUtils.d(TAG, e.toString());
        }*/
        //  return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }


    public static void updateCurrentJoinAP(String ssid) {
        SPUtil.putString(KEY_CURRENT_SELECTED_AP, ssid);
    }

    public static void clearCurrentJoinAP() {
        updateCurrentJoinAP("");
    }

    public static String getCurrentJoinAP() {
        return SPUtil.getString(KEY_CURRENT_SELECTED_AP, "");
    }

    public static boolean isWifiEnabled(WifiManager wifiManager) {
        return wifiManager.isWifiEnabled();
    }
}
