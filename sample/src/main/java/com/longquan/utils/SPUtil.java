package com.longquan.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class SPUtil {
    public static SharedPreferences instanceSP = null;

    //默认使用这个
    public static SharedPreferences initDefaultSp(Application appContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            instanceSP = PreferenceManager.
                    getDefaultSharedPreferences(appContext.createDeviceProtectedStorageContext());
        }else {
            instanceSP = PreferenceManager.getDefaultSharedPreferences(appContext);
        }
        return instanceSP;
    }

    public static void addListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (instanceSP != null) {
            instanceSP.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }
    }

    public static void removeListener(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (instanceSP != null) {
            instanceSP.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        }
    }

    public static void putString(String key, String value) {
        if (instanceSP == null) {
            return;
        } else {
            instanceSP.edit().putString(key, value).apply();
        }
    }

    public static String getString(String key, String defaultValue) {
        if (instanceSP == null) {
            return defaultValue;
        } else {
            return instanceSP.getString(key, defaultValue);
        }
    }

    public static String getString(String key) {
        if (instanceSP == null) {
            return "";
        } else {
            return instanceSP.getString(key, "");
        }
    }

    public static void putBoolean(String key, boolean value) {
        if (instanceSP == null) {
            return;
        } else {
            instanceSP.edit().putBoolean(key, value).apply();
        }
    }

    public static boolean getBoolean(String key) {
        if (instanceSP == null) {
            return false;
        } else {
            return instanceSP.getBoolean(key, false);
        }
    }

    public static void putInt(String key, int value) {
        if (instanceSP == null) {
            return;
        } else {
            instanceSP.edit().putInt(key, value).apply();
        }
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        if (instanceSP == null) {
            return defaultValue;
        } else {
            return instanceSP.getInt(key, defaultValue);
        }
    }


    public static void putLong(String key, long value) {
        if (instanceSP == null) {
            return;
        } else {
            instanceSP.edit().putLong(key, value).apply();
        }
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    public static long getLong(String key, long defaultValue) {
        if (instanceSP == null) {
            return defaultValue;
        } else {
            return instanceSP.getLong(key, defaultValue);
        }
    }


    public Object getObject(String key) {
        if (null != instanceSP) {
            String str = instanceSP.getString(key, null);
            if (null != str) {
                return Base64.decode(str, Base64.DEFAULT);
            }
        }
        return null;
    }

    public void saveObject(String key, Object object) {
        if (null != instanceSP) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                String productBase64 = Base64.encodeToString(baos
                        .toByteArray(), Base64.DEFAULT);
                instanceSP.edit().putString(key, productBase64).apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
