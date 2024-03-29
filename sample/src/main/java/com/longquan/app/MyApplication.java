package com.longquan.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.longquan.common.wifiap.WifiTracker;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class MyApplication extends Application {

    private String TAG = "MyApplication";
    public static MyApplication sApp;
    public static String versionName;

    public static MyApplication getInstance() {
        return sApp;
    }

    WifiTracker mWifiTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        versionName = getVersionName(sApp);
        mWifiTracker = WifiTracker.getInstance();
        registerReceiver(mWifiTracker.getReceiver(),mWifiTracker.newIntentFilter());

    }



    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageInfo pInfo = null;

        try {
            //通过PackageManager可以得到PackageInfo
            PackageManager pManager = context.getPackageManager();
            pInfo = pManager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ((PackageInfo) pInfo).versionName;
    }
}
