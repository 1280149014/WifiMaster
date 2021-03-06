package com.longquan.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.MyApplication;
import com.longquan.bean.WifiInfo;

import java.lang.reflect.Method;
import java.util.List;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WifiSupport {

    private static final String TAG = WifiSupport.class.getSimpleName();

    private WifiManager mWifiManager;
    private WifiTracker mWifiTracker;


    private Context context;

    public WifiSupport(Context context, WifiManager wifiManager) {
        this.context = context;
        this.mWifiManager = wifiManager;
        this.mWifiTracker = new WifiTracker(context, mWifiManager);
    }


    /**
     * Get Method  @SystemApi android.net.wifi.WifiManager
     * public void connect(WifiConfiguration config, ActionListener listener)
     *
     * @return Method
     */

    private Method getConnectMethod() {
        Method connect_wifiConfig = null;
        try {
            LogUtils.d(TAG, "try getConnectMethod ");
            Class ActionListener = Class.forName("android.net.wifi.WifiManager$ActionListener", false, null);
            connect_wifiConfig = WifiManager.class.getDeclaredMethod("connect", WifiConfiguration.class, ActionListener);
            return connect_wifiConfig;
        } catch (Exception e) {
            LogUtils.d(TAG, "getConnectMethod Exception:" + e.toString());
            e.printStackTrace();
        }
        return connect_wifiConfig;
    }


    /**
     * Enable/disable WifiConnectivityManager
     */
    public static void enableWifiConnectivityManager(WifiManager wifiManager, boolean isEnable) {
        try {
            LogUtils.d(TAG, "try enableWifiConnectivityManager isEnable:" + isEnable);
            Method method = wifiManager.getClass().getMethod("enableWifiConnectivityManager", boolean.class);
            method.invoke(wifiManager, isEnable);
        } catch (Exception e) {
            LogUtils.d(TAG, "catch enableWifiConnectivityManager exception:" + e.toString());
            e.printStackTrace();
        }
    }


    /**
     * 连接隐藏SSID的网络，支持 NONE 和 WPA_PSK
     *
     * @param ssid
     * @param passswd
     * @param security
     */
    public boolean joinHide(String ssid, String passswd, int security) {
        LogUtils.d(TAG, "joinHide SSID:" + ssid + " , passswd:" + passswd + ", security:" + security);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.hiddenSSID = true;
        if (security == WifiInfo.Security.NONE) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (security == WifiInfo.Security.WPA) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            if (!TextUtils.isEmpty(passswd)) {
                String password = passswd;
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
        }
        int netId = mWifiManager.addNetwork(config);
        mWifiManager.disconnect();
        boolean isEnable = mWifiManager.enableNetwork(netId, true);
        LogUtils.d(TAG, "joinHide enableWifiConnectivityManager(false) after enableNetwork");
        enableWifiConnectivityManager(mWifiManager, false);
        boolean isReconnected = mWifiManager.reconnect();
        LogUtils.d(TAG, "joinHide netId:" + netId + " , isEnable:" + isEnable + ", isReconnected:" + isReconnected);
        return isEnable;
    }

    /**
     *  加入网络的接口
     * @param toJoin
     * @return
     */
    public boolean join(WifiInfo toJoin) {
        LogUtils.d(TAG, "join:" + toJoin);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager);
        }
        WifiConfiguration config = generateWifiConfig(toJoin.getSsid(), toJoin.getPassword(), toJoin.getCapabilities());
        LogUtils.d(TAG, "join updateCurrentJoinAP:" + toJoin.getSsid());
        WirelessUtils.updateCurrentJoinAP(toJoin.getSsid());
        LogUtils.d(TAG, "config:" + config);
        int netId = mWifiManager.addNetwork(config);
        mWifiManager.disconnect();
        if (!toJoin.getSsid().startsWith("NBY_")) {
            mWifiManager.saveConfiguration();
        }
        boolean isEnable = mWifiManager.enableNetwork(netId, true);
        LogUtils.d(TAG, "join enableWifiConnectivityManager(false) after enableNetwork");
        enableWifiConnectivityManager(mWifiManager, false);
        boolean isReconnected = mWifiManager.reconnect();
        LogUtils.d(TAG, "join netId:" + netId + " , isEnable:" + isEnable + ", isReconnected:" + isReconnected);
        return isEnable;
    }

    public boolean join(WifiConfiguration config) {
        LogUtils.d(TAG, "join config:" + config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager);
        }
        int netId = mWifiManager.updateNetwork(config);
        LogUtils.d(TAG, "join config updateCurrentJoinAP:" + config.SSID);
        WirelessUtils.updateCurrentJoinAP(config.SSID);
        mWifiManager.disconnect();
        if (!config.SSID.startsWith("NBY_")) {
            mWifiManager.saveConfiguration();
        }
        boolean isEnable = mWifiManager.enableNetwork(netId, true);
        LogUtils.d(TAG, "join enableWifiConnectivityManager(false) after enableNetwork");
        enableWifiConnectivityManager(mWifiManager, false);
        boolean isReconnected = mWifiManager.reconnect();
        LogUtils.d(TAG, "join netId:" + netId + " , isEnable:" + isEnable + ", isReconnected:" + isReconnected);
        return isEnable;
    }


    /**
     * android.net.wifi.ScanResult 的 capabilities 描述了认证、密钥管理、接入点所支持的加密方案
     *
     * @return config
     */
    private WifiConfiguration generateWifiConfig(String SSID,
                                                 String password,
                                                 String scanResultCapabilities) {
        LogUtils.d(TAG,
                "generateWifiConfig -SSID" + SSID
                + ";password=" + password
                + ";capabilities=" + scanResultCapabilities);
        WifiConfiguration tempConfig = isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear(); //Ciphers 密码
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        config.status = WifiConfiguration.Status.ENABLED;

        String firstCapabilities = scanResultCapabilities.substring(1, scanResultCapabilities.indexOf("]"));
        String[] capabilities = firstCapabilities.split("-");
        String auth = capabilities[0];
        String keyMgmt = "";
        String pairwiseCipher = "";

        if (capabilities.length > 1) {
            keyMgmt = capabilities[1];
        }
        if (capabilities.length > 2) {
            pairwiseCipher = capabilities[2];
        }
        LogUtils.d(TAG, "generateWifiConfig --> auth:" + auth +
                " ; keyMgmt:" + keyMgmt +
                " ; pairwiseCipher:" + pairwiseCipher);
        /**
         * Recognized IEEE 802.11 authentication algorithms.
         *
         * @see
         * android.net.wifi.WifiConfiguration.AuthAlgorithm
         *
         * Open System authentication (required for WPA/WPA2)
         *   -- public static final int OPEN = 0;
         * Shared Key authentication (requires static WEP keys)
         *   -- public static final int SHARED = 1;
         * LEAP/Network EAP (only used with LEAP)
         *   -- public static final int LEAP = 2;
         */

        if (auth.contains("EAP")) {
            // EAP
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.LEAP);
        } else if (auth.contains("WPA")) {
            // WPA/WPA2
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        } else if (auth.contains("WEP")) {
            // WEP
            config.wepKeys[0] = "\"" + password + "\"";
            config.wepTxKeyIndex = 0;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else {
            // NONE
            LogUtils.d(TAG, "generateWifiConfig --> auth None:" + auth);
        }

        /**
         * Recognized security protocols.
         *
         * @see
         * android.net.wifi.WifiConfiguration.Protocol
         *
         * WPA/IEEE 802.11i/D3.0
         *  -- public static final int WPA = 0;
         * WPA2/IEEE 802.11i
         *  -- public static final int RSN = 1;
         */
        if (auth.contains("WPA2")) {
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        } else if (auth.contains("WPA")) {
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        }


        /**
         * Recognized key management schemes.
         *
         * @see
         * android.net.wifi.WifiConfiguration.KeyMgmt
         *
         * WPA is not used; plaintext or static WEP could be used.
         *   -- public static final int NONE = 0;
         * WPA pre-shared key (requires {@code preSharedKey} to be specified).
         *   -- public static final int WPA_PSK = 1;
         * WPA using EAP authentication. Generally used with an external authentication server.
         *   -- public static final int WPA_EAP = 2;
         * IEEE 802.1X using EAP authentication and (optionally) dynamically generated WEP keys.
         *   -- public static final int IEEE8021X = 3;
         */

        if (!keyMgmt.equals("")) {
            if (keyMgmt.contains("IEEE802.1X")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
            } else if (auth.contains("WPA") && keyMgmt.contains("EAP")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            } else if (auth.contains("WPA") && keyMgmt.contains("PSK")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            }
        } else {
            LogUtils.d(TAG, "generateWifiConfig KeyMgmt.NONE:" + keyMgmt);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        /**
         * Recognized pairwise ciphers & group ciphers for WPA
         *
         * @see
         * android.net.wifi.WifiConfiguration.PairwiseCipher
         *
         * @see
         * android.net.wifi.WifiConfiguration.GroupCipher
         */
        if (!pairwiseCipher.equals("")) {
            if (pairwiseCipher.contains("CCMP")) {
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            }
            if (pairwiseCipher.contains("TKIP")) {
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            }
        }

        return config;
    }


    /**
     * @param ssid     ssid
     * @param pswd     密码
     * @param security 方式
     */
    public boolean connect(final String ssid, final String pswd,
                           final int security) {
        LogUtils.d(TAG, "connect-ssid" + ssid + ";pswd=" + pswd + ";security=" + security);
        final WifiConfiguration config = generateConfig(ssid, pswd, security);
        LogUtils.d(TAG, "connect-:" + config);
        final int netWorkId = mWifiManager.addNetwork(config);
        return connect(netWorkId, ssid);
    }

    /**
     * 有记录信息连接方式
     *
     * @param config
     */

    public boolean connect(WifiConfiguration config) {
        LogUtils.d(TAG, "connect-2-config" + config);
        mWifiManager.updateNetwork(config);
        return connect(config.networkId, config.SSID);
    }


    /**
     * 连接网络
     */
    private boolean connect(int networkId, String ssid) {
        enableNetworks();
        //dvr无线会自己在dvr应用内连接，不走设置过
        LogUtils.d(TAG, "---connect---is DVR WiFi : " + ssid.startsWith("NBY_"));
        if (!ssid.startsWith("NBY_")) {
            mWifiManager.saveConfiguration();
        }
        final boolean isEnable = mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
        LogUtils.d(TAG, "connect-networkId=" + networkId + ";isEnable" + isEnable);
        return isEnable;
    }

    private void enableNetworks() {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (config != null
                        && config.status != WifiConfiguration.Status.ENABLED) {
                    mWifiManager.enableNetwork(config.networkId, false);
                }
            }
        }
    }


    public WifiConfiguration generateConfig(String ssid, String pswd, int security) {
        WifiConfiguration tempConfig = isExsits(ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.hiddenSSID = true;
        switch (security) {
            case WifiInfo.Security.NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case WifiInfo.Security.WEP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                if (!TextUtils.isEmpty(pswd)) {
                    int length = pswd.length();
                    String password = pswd;
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;
            case WifiInfo.Security.PSK:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                if (!TextUtils.isEmpty(pswd)) {
                    String password = pswd;
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                break;
            case WifiInfo.Security.EAP:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                break;
            default:
                return null;
        }
        config.priority = 2;
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return config;
    }

    /**
     * 断开指定的wifi
     *
     * @param wifiInfo
     */
    public void disconnect(WifiInfo wifiInfo) {
        final int networkID = this.getNetworkId(wifiInfo.getSsid());
        LogUtils.d(TAG, "disconnect[" + networkID + "]" + wifiInfo);
        if (networkID != -1) {
            mWifiManager.disableNetwork(networkID);
            mWifiManager.disconnect();
        }

    }


    /**
     * forget 网络
     *
     * @param wifiInfo
     */
    public void ignore(WifiInfo wifiInfo) {
        final int networkID = this.getNetworkId(wifiInfo.getSsid());
        LogUtils.d(TAG, "ignore[" + networkID + "]" + wifiInfo);
        if (networkID != -1) {
            mWifiManager.removeNetwork(networkID);
            mWifiManager.saveConfiguration();
        }

        mWifiTracker.startScan();
    }


    /**
     * 是否存在ap的config信息
     *
     * @param ssid
     * @return
     */
    public WifiConfiguration isExsits(String ssid) {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                LogUtils.d(TAG, "existingConfig: " + existingConfig.SSID);
                if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private int getNetworkId(String ssid) {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return 0;
        }
        List<WifiConfiguration> mConfigs = mWifiManager.getConfiguredNetworks();
        if (mConfigs != null) {
            for (WifiConfiguration config : mConfigs) {
                LogUtils.d(TAG, "getNetworkId[" + ssid + "][" + config.SSID + "]["
                        + config.networkId + "]");
                if (config != null
                        && ssid.equals(removeDoubleQuotes(config.SSID))) {
                    return config.networkId;
                }
            }
        }

        return -1;
    }

    private String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

}
