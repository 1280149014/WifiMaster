package com.longquan.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.longquan.MyApplication;
import com.longquan.bean.WifiInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.longquan.Contants.OPEN_WIFI_INTENT;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WifiHelper {
    private static final String TAG = WifiHelper.class.getSimpleName();
    private final String UNKNOWN_SSID = "<unknown ssid>";
    private WifiManager mWifiManager;

    private Context mContext;
    private static final String[] mWifiDialogTags = new String[]{"WifiConnectPasswdDialogFrag",
            "DoWifiForgetDialogFrag",
            "DoWifiJoinDialogFrag",
            "DoCurConnectedClick",
            "WifiAddOtherNetWorkEditFrag"};


    public WifiHelper(Context context, WifiManager wifiManager) {
        this.mContext = context;
        this.mWifiManager = wifiManager;

    }

    public boolean checkConnect(String ssid) {
        final android.net.wifi.WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
            if (WifiApUtil.checkSSID(wifiInfo.getSSID(), ssid)) {
                final SupplicantState mSupplicantState = wifiInfo.getSupplicantState();
                if (mSupplicantState == SupplicantState.COMPLETED) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * calculate the different signal level between two same SSID
     * and keep the larger signal level
     *
     * @param infos
     * @param wifiInfo
     * @return
     */
    public boolean handleApLevel(List<WifiInfo> infos, WifiInfo wifiInfo) {
        boolean isReplace = false;
        if (infos != null && infos.size() > 0) {
            Iterator<WifiInfo> it = infos.iterator();
            while (it.hasNext()) {
                WifiInfo info = it.next();
                if (info.getSsid().equals(wifiInfo.getSsid())) {
                    isReplace = true;
                    if (WifiManager.compareSignalLevel(info.getLevel(), wifiInfo.getLevel()) >= 0) {
                        //do nothing
                    } else {
                        it.remove();
                        infos.add(wifiInfo);
                    }
                    break;
                }
            }
        }

        return isReplace;
    }


    /**
     * 从Wifi info list中获取当前已连接的wifi
     *
     * @param infos
     * @return
     */
    public static WifiInfo getConnectedWifi(List<WifiInfo> infos) {
        Iterator<WifiInfo> iterator = infos.iterator();
        WifiInfo info = null;
        while (iterator.hasNext()) {
            WifiInfo tmp = iterator.next();
            if (tmp.isConnect()) {
                iterator.remove();
                info = tmp;
                break;
            }
        }
        LogUtils.d(TAG, "getConnectedWifi:" + info);
        return info;
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


    public static void OpenWifi(Context context) {
        Intent intent = new Intent(OPEN_WIFI_INTENT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, MyApplication.sApp.getPackageName());
        context.startActivity(intent);
    }

    /**
     * 从列表中移除指定的wifi
     */
    public static List<WifiInfo> removeWifi(List<WifiInfo> info, WifiInfo toRemove) {
        LogUtils.d(TAG, "removeWifi:");
        Iterator<WifiInfo> iterator = info.iterator();
        while (iterator.hasNext()) {
            WifiInfo tmp = iterator.next();
            if (tmp.getSsid().equals(toRemove.getSsid())) {
                LogUtils.d(TAG, "removeWifi:" + tmp);
                toRemove.setConnect(true);
                iterator.remove();
                break;
            }
        }
        return info;
    }



    /**
     * 获取当前连接的wifi
     *
     * @return
     */
    public WifiInfo getConnectedWifi() {
        WifiInfo info = new WifiInfo(WifiInfo.Type.CONFIG);
        android.net.wifi.WifiInfo connectedWifi = this.mWifiManager.getConnectionInfo();
        info.setConnect(true);
        info.setBssid(connectedWifi.getBSSID());
        String ssid = connectedWifi.getSSID().replaceAll("\"", "");
        info.setSsid(ssid);
        info.setLevel(connectedWifi.getRssi());
        info.setSecurity(getSecurity(ssid));
        return info;
    }

    public WifiInfo getAvailableConnectedWifi(){
        WifiInfo info = getConnectedWifi();
        if(UNKNOWN_SSID.equals(info.getSsid())){
            return null;
        }
        return info;
    }


    /**
     * 检查wifi是否处于连接状态
     *
     * @return
     */
    public boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }


    /**
     * 通过ssid 从config中获取加密方式
     *
     * @param ssid
     * @return
     */
    public String getSecurity(String ssid) {
        String security = "NONE";
        WifiConfiguration config = isExists(ssid);
        if (config != null) {
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                security = "WPA_PSK";
            } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                    || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                security = "WPA_EAP";
            } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
                security = "NONE";
            }
        }
        return security;
    }

    /**
     * 是否存在ssid信息
     *
     * @param ssid
     * @return
     */
    public WifiConfiguration isExists(String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    /**
     * 判断是否为当前已连接的wifi
     *
     * @param wifiInfo
     * @return
     */
    public boolean isCurConnected(WifiInfo wifiInfo) {
        WifiInfo curConnectedWifi = getConnectedWifi();
        if (null != wifiInfo && null != curConnectedWifi && curConnectedWifi.getSsid().equals(wifiInfo.getSsid())) {
            LogUtils.d(TAG, " isCurConnected true -->" + wifiInfo.getSsid());
            return true;
        }
        LogUtils.d(TAG, " isCurConnected false -->" + wifiInfo.getSsid());
        return false;
    }

    /**
     * 判断该wifi之前是否已配置过
     *
     * @param wifiInfo
     * @return
     */
    public boolean hasConfiged(WifiInfo wifiInfo) {
        if (null == wifiInfo) {
            return false;
        }
        WifiConfiguration config = isExists(wifiInfo.getSsid());
        if (null != config) {
            LogUtils.d(TAG, " config is exist true -->" + config.SSID);
            return true;
        }
        LogUtils.d(TAG, " config is exist false -->" + wifiInfo.getSsid());
        return false;
    }


    public String getSecurityLevel(WifiInfo wifiInfo) {
        String security = "NONE";
        switch (wifiInfo.getSecurity()) {
            case WifiInfo.Security.NONE:
                security = "NONE";
                break;
            case WifiInfo.Security.WEP:
                security = "WEP";
                break;
            case WifiInfo.Security.PSK:
                security = "PSK";
                break;
            case WifiInfo.Security.EAP:
                security = "EAP";
                break;
            case WifiInfo.Security.WPA:
                security = "WPA";
                break;
            default:
                break;
        }
        return security;
    }


    public List<WifiInfo> getAllScanWifiInfos() {
        List<ScanResult> newScanResults = mWifiManager.getScanResults();
        return getAllAvailableAccessPoint(newScanResults);
    }

    public List<WifiInfo> getAllAvailableAccessPoint(List<ScanResult> newScanResults) {
        List<WifiInfo> allAvaiableWifiInfos = new ArrayList<WifiInfo>();
        allAvaiableWifiInfos.clear();
        WifiInfo wifiInfo;
        if (newScanResults != null) {
            for (ScanResult result : newScanResults) {
                if (TextUtils.isEmpty(result.SSID)
                        || result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                wifiInfo = new WifiInfo(WifiInfo.Type.SCAN);
                wifiInfo.setSsid(result.SSID);
                wifiInfo.setBssid(result.BSSID);
                wifiInfo.setLevel(result.level);
                wifiInfo.setCapabilities(result.capabilities);
                boolean isConnect = checkConnect(result.SSID);
                wifiInfo.setConnect(isConnect);

                if (handleApLevel(allAvaiableWifiInfos, wifiInfo)) {
                    wifiInfo = null;

                    continue;
                } else {
                    allAvaiableWifiInfos.add(wifiInfo);
                }
            }
        }

        for (WifiInfo info : allAvaiableWifiInfos) {
            if (info.isConnect()) {
                int connectIndex = allAvaiableWifiInfos.indexOf(info);
                if (connectIndex != 0) {
                    WifiInfo temp = allAvaiableWifiInfos.get(0);
                    allAvaiableWifiInfos.set(0, info);
                    allAvaiableWifiInfos.set(connectIndex, temp);
                    break;
                }
            }
        }

        LogUtils.d(TAG, "getAllAvailableAccessPoint():" + allAvaiableWifiInfos);
        return allAvaiableWifiInfos;
    }

}
