package com.longquan.bean;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;


import com.longquan.R;

import java.io.Serializable;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WifiInfo implements Serializable ,Comparable<WifiInfo>{
    /**
     * wifi类型
     */
    public interface Type {
        /**
         * 自定义类型wifi
         */
        int CONFIG = 1;

        /**
         * 扫描获取的wifi类型
         */
        int SCAN = 2;
    }

    public interface Security {
        int NONE = 0;

        int WEP = 1;

        int PSK = 2;

        int EAP = 3;

        int WPA = 4;
    }

    private String ssid;

    private String bssid;

    private String capabilities;

    private int level;

    private int security;

    private int networkId = -1;

    /**
     * 1 -- wifiConfig 2 -- scanResult
     */
    private int type;

    private boolean isConnect = false;

    public boolean isConnecting = false;

    private String password = "";

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    public WifiInfo() {
    }

    public WifiInfo(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return WifiInfo.Security.PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return WifiInfo.Security.EAP;
        }
        return WifiInfo.Security.NONE;
    }

    private String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
        this.setSecurity(capabilities);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSecurity() {
        return security;
    }

    public int getAuthentication() {
        return security;
    }

    public void setSecurity(String value) {
        this.security = WifiInfo.Security.NONE;
        if (value.contains("EAP")) {
            this.security = WifiInfo.Security.EAP;
        } else if (value.contains("PSK")) {
            this.security = WifiInfo.Security.PSK;
        } else if (value.contains("WEP")) {
            this.security = WifiInfo.Security.WEP;
        }
    }





    public void setSecurity(int security) {
        this.security = security;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getWifiLevelImage() {
        int img = R.drawable.ic_wifi_signal_0;

        if (getSecurity() != WifiInfo.Security.NONE) {
            if (Math.abs(this.level) > 100) {
                img = R.drawable.ic_wifi_signal_cryp_0;
            } else if (Math.abs(this.level) > 80) {
                img = R.drawable.ic_wifi_signal_cryp_1;
            } else if (Math.abs(this.level) > 70) {
                img = R.drawable.ic_wifi_signal_cryp_2;
            } else if (Math.abs(this.level) > 60) {
                img = R.drawable.ic_wifi_signal_cryp_3;
            } else {
                img = R.drawable.ic_wifi_signal_cryp_4;
            }
        } else {
            if (Math.abs(this.level) > 100) {
                img = R.drawable.ic_wifi_signal_0;
            } else if (Math.abs(this.level) > 80) {
                img = R.drawable.ic_wifi_signal_1;
            } else if (Math.abs(this.level) > 70) {
                img = R.drawable.ic_wifi_signal_2;
            } else if (Math.abs(this.level) > 60) {
                img = R.drawable.ic_wifi_signal_3;
            } else {
                img = R.drawable.ic_wifi_signal_4;
            }
        }
        return img;
    }

    private int[] mSignalImgs = new int[]{
            R.drawable.ic_wifi_signal_level_0,
            R.drawable.ic_wifi_signal_level_1,
            R.drawable.ic_wifi_signal_level_2,
            R.drawable.ic_wifi_signal_level_3,
            R.drawable.ic_wifi_signal_level_4,
    };

    // 新UI wifi 信号强度和是否加密 分开
    public int getWifiLevelImageWithoutCryp() {
        int range = WifiManager.calculateSignalLevel(level, 5);
        return mSignalImgs[range];
    }

    @Override
    public String toString() {
        return "ssid=" + this.ssid + ";password=" + password + ";bssid="
                + this.bssid + ";level=" + level + ";security=" + security
                + ";isConnected=" + this.isConnect;
    }

    @Override
    public int compareTo(@NonNull WifiInfo o) {
        if (this.level > o.getLevel()) {
            return -1;
        }

        if (this.level < o.getLevel()) {
            return 1;
        }

        return this.getSsid().compareTo(o.getSsid());
    }
}
