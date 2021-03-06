package com.longquan.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import com.longquan.MyApplication
import com.longquan.bean.WifiInfo
import java.lang.reflect.Method

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
class WifiSupport(private val context: Context, private val mWifiManager: WifiManager) {
    private val mWifiTracker: WifiTracker

    /**
     * Get Method  @SystemApi android.net.wifi.WifiManager
     * public void connect(WifiConfiguration config, ActionListener listener)
     *
     * @return Method
     */
    private val connectMethod: Method?
        private get() {
            var connect_wifiConfig: Method? = null
            try {
                LogUtils.d(TAG, "try getConnectMethod ")
                val ActionListener = Class.forName("android.net.wifi.WifiManager\$ActionListener", false, null)
                connect_wifiConfig = WifiManager::class.java.getDeclaredMethod("connect", WifiConfiguration::class.java, ActionListener)
                return connect_wifiConfig
            } catch (e: Exception) {
                LogUtils.d(TAG, "getConnectMethod Exception:$e")
                e.printStackTrace()
            }
            return connect_wifiConfig
        }

    /**
     * 连接隐藏SSID的网络，支持 NONE 和 WPA_PSK
     *
     * @param ssid
     * @param passswd
     * @param security
     */
    fun joinHide(ssid: String, passswd: String, security: Int): Boolean {
        LogUtils.d(TAG, "joinHide SSID:$ssid , passswd:$passswd, security:$security")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager)
        }
        val config = WifiConfiguration()
        config.SSID = "\"" + ssid + "\""
        config.hiddenSSID = true
        if (security == WifiInfo.Security.NONE) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        } else if (security == WifiInfo.Security.WPA) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            if (!TextUtils.isEmpty(passswd)) {
                if (passswd.matches(Regex("[0-9A-Fa-f]{64}"))) {
                    config.preSharedKey = passswd
                } else {
                    config.preSharedKey = '"'.toString() + passswd + '"'
                }
            }
        }
        val netId = mWifiManager.addNetwork(config)
        mWifiManager.disconnect()
        val isEnable = mWifiManager.enableNetwork(netId, true)
        LogUtils.d(TAG, "joinHide enableWifiConnectivityManager(false) after enableNetwork")
        enableWifiConnectivityManager(mWifiManager, false)
        val isReconnected = mWifiManager.reconnect()
        LogUtils.d(TAG, "joinHide netId:$netId , isEnable:$isEnable, isReconnected:$isReconnected")
        return isEnable
    }

    /**
     * 加入网络的接口
     * @param toJoin
     * @return
     */
    fun join(toJoin: WifiInfo): Boolean {
        LogUtils.d(TAG, "join:$toJoin")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager)
        }
        val config = generateWifiConfig(toJoin.ssid, toJoin.password, toJoin.capabilities)
        LogUtils.d(TAG, "join updateCurrentJoinAP:" + toJoin.ssid)
        WirelessUtils.updateCurrentJoinAP(toJoin.ssid)
        LogUtils.d(TAG, "config:$config")
        val netId = mWifiManager.addNetwork(config)
        mWifiManager.disconnect()
        if (!toJoin.ssid.startsWith("NBY_")) {
            mWifiManager.saveConfiguration()
        }
        val isEnable = mWifiManager.enableNetwork(netId, true)
        LogUtils.d(TAG, "join enableWifiConnectivityManager(false) after enableNetwork")
        enableWifiConnectivityManager(mWifiManager, false)
        val isReconnected = mWifiManager.reconnect()
        LogUtils.d(TAG, "join netId:$netId , isEnable:$isEnable, isReconnected:$isReconnected")
        return isEnable
    }

    fun join(config: WifiConfiguration): Boolean {
        LogUtils.d(TAG, "join config:$config")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WirelessUtils.stopSoftAp(mWifiManager)
        }
        val netId = mWifiManager.updateNetwork(config)
        LogUtils.d(TAG, "join config updateCurrentJoinAP:" + config.SSID)
        WirelessUtils.updateCurrentJoinAP(config.SSID)
        mWifiManager.disconnect()
        if (!config.SSID.startsWith("NBY_")) {
            mWifiManager.saveConfiguration()
        }
        val isEnable = mWifiManager.enableNetwork(netId, true)
        LogUtils.d(TAG, "join enableWifiConnectivityManager(false) after enableNetwork")
        enableWifiConnectivityManager(mWifiManager, false)
        val isReconnected = mWifiManager.reconnect()
        LogUtils.d(TAG, "join netId:$netId , isEnable:$isEnable, isReconnected:$isReconnected")
        return isEnable
    }

    /**
     * android.net.wifi.ScanResult 的 capabilities 描述了认证、密钥管理、接入点所支持的加密方案
     *
     * @return config
     */
    private fun generateWifiConfig(SSID: String,
                                   password: String,
                                   scanResultCapabilities: String): WifiConfiguration {
        LogUtils.d(TAG,
                "generateWifiConfig -SSID" + SSID
                        + ";password=" + password
                        + ";capabilities=" + scanResultCapabilities)
        val tempConfig = isExsits(SSID)
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId)
        }
        val config = WifiConfiguration()
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear() //Ciphers 密码
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + SSID + "\""
        config.status = WifiConfiguration.Status.ENABLED
        val firstCapabilities = scanResultCapabilities.substring(1, scanResultCapabilities.indexOf("]"))
        val capabilities = firstCapabilities.split("-").toTypedArray()
        val auth = capabilities[0]
        var keyMgmt = ""
        var pairwiseCipher = ""
        if (capabilities.size > 1) {
            keyMgmt = capabilities[1]
        }
        if (capabilities.size > 2) {
            pairwiseCipher = capabilities[2]
        }
        LogUtils.d(TAG, "generateWifiConfig --> auth:" + auth +
                " ; keyMgmt:" + keyMgmt +
                " ; pairwiseCipher:" + pairwiseCipher)
        /**
         * Recognized IEEE 802.11 authentication algorithms.
         *
         * @see  android.net.wifi.WifiConfiguration.AuthAlgorithm
         *
         * Open System authentication
         */
        if (auth.contains("EAP")) {
            // EAP
            config.preSharedKey = "\"" + password + "\""
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.LEAP)
        } else if (auth.contains("WPA")) {
            // WPA/WPA2
            config.preSharedKey = "\"" + password + "\""
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        } else if (auth.contains("WEP")) {
            // WEP
            config.wepKeys[0] = "\"" + password + "\""
            config.wepTxKeyIndex = 0
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        } else {
            // NONE
            LogUtils.d(TAG, "generateWifiConfig --> auth None:$auth")
        }
        /**
         * Recognized security protocols.
         *
         * @see  android.net.wifi.WifiConfiguration.Protocol
         *
         * WPA/IEEE 802.11i/D3.0
         * -- public static final int WPA = 0;
         * WPA2/IEEE 802.11i
         * -- public static final int RSN = 1;
         */
        if (auth.contains("WPA2")) {
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        } else if (auth.contains("WPA")) {
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        }
        /**
         * Recognized key management schemes.
         *
         * @see  android.net.wifi.WifiConfiguration.KeyMgmt
         *
         * WPA is not used; plaintext or static WEP could be used.
         * -- public static final int NONE = 0;
         * WPA pre-shared key
         */
        if (keyMgmt != "") {
            if (keyMgmt.contains("IEEE802.1X")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            } else if (auth.contains("WPA") && keyMgmt.contains("EAP")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
            } else if (auth.contains("WPA") && keyMgmt.contains("PSK")) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }
        } else {
            LogUtils.d(TAG, "generateWifiConfig KeyMgmt.NONE:$keyMgmt")
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        /**
         * Recognized pairwise ciphers & group ciphers for WPA
         *
         * @see  android.net.wifi.WifiConfiguration.PairwiseCipher
         *
         *
         * @see  android.net.wifi.WifiConfiguration.GroupCipher
         */
        if (pairwiseCipher != "") {
            if (pairwiseCipher.contains("CCMP")) {
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            }
            if (pairwiseCipher.contains("TKIP")) {
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            }
        }
        return config
    }

    /**
     * @param ssid     ssid
     * @param pswd     密码
     * @param security 方式
     */
    fun connect(ssid: String, pswd: String,
                security: Int): Boolean {
        LogUtils.d(TAG, "connect-ssid$ssid;pswd=$pswd;security=$security")
        val config = generateConfig(ssid, pswd, security)
        LogUtils.d(TAG, "connect-:$config")
        val netWorkId = mWifiManager.addNetwork(config)
        return connect(netWorkId, ssid)
    }

    /**
     * 有记录信息连接方式
     *
     * @param config
     */
    fun connect(config: WifiConfiguration): Boolean {
        LogUtils.d(TAG, "connect-2-config$config")
        mWifiManager.updateNetwork(config)
        return connect(config.networkId, config.SSID)
    }

    /**
     * 连接网络
     */
    private fun connect(networkId: Int, ssid: String): Boolean {
        enableNetworks()
        //dvr无线会自己在dvr应用内连接，不走设置过
        LogUtils.d(TAG, "---connect---is DVR WiFi : " + ssid.startsWith("NBY_"))
        if (!ssid.startsWith("NBY_")) {
            mWifiManager.saveConfiguration()
        }
        val isEnable = mWifiManager.enableNetwork(networkId, true)
        mWifiManager.reconnect()
        LogUtils.d(TAG, "connect-networkId=$networkId;isEnable$isEnable")
        return isEnable
    }

    private fun enableNetworks() {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val configs = mWifiManager.configuredNetworks
        if (configs != null) {
            for (config in configs) {
                if (config != null
                        && config.status != WifiConfiguration.Status.ENABLED) {
                    mWifiManager.enableNetwork(config.networkId, false)
                }
            }
        }
    }

    fun generateConfig(ssid: String, pswd: String, security: Int): WifiConfiguration? {
        val tempConfig = isExsits(ssid)
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId)
        }
        val config = WifiConfiguration()
        config.SSID = "\"" + ssid + "\""
        config.hiddenSSID = true
        when (security) {
            WifiInfo.Security.NONE -> config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            WifiInfo.Security.WEP -> {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                if (!TextUtils.isEmpty(pswd)) {
                    val length = pswd.length
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && pswd.matches(Regex("[0-9A-Fa-f]*"))) {
                        config.wepKeys[0] = pswd
                    } else {
                        config.wepKeys[0] = '"'.toString() + pswd + '"'
                    }
                }
            }
            WifiInfo.Security.PSK -> {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                if (!TextUtils.isEmpty(pswd)) {
                    if (pswd.matches(Regex("[0-9A-Fa-f]{64}"))) {
                        config.preSharedKey = pswd
                    } else {
                        config.preSharedKey = '"'.toString() + pswd + '"'
                    }
                }
            }
            WifiInfo.Security.EAP -> {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            }
            else -> return null
        }
        config.priority = 2
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        return config
    }

    /**
     * 断开指定的wifi
     *
     * @param wifiInfo
     */
    fun disconnect(wifiInfo: WifiInfo) {
        val networkID = getNetworkId(wifiInfo.ssid)
        LogUtils.d(TAG, "disconnect[$networkID]$wifiInfo")
        if (networkID != -1) {
            mWifiManager.disableNetwork(networkID)
            mWifiManager.disconnect()
        }
    }

    /**
     * forget 网络
     *
     * @param wifiInfo
     */
    fun ignore(wifiInfo: WifiInfo) {
        val networkID = getNetworkId(wifiInfo.ssid)
        LogUtils.d(TAG, "ignore[$networkID]$wifiInfo")
        if (networkID != -1) {
            mWifiManager.removeNetwork(networkID)
            mWifiManager.saveConfiguration()
        }
        mWifiTracker.startScan()
    }

    /**
     * 是否存在ap的config信息
     *
     * @param ssid
     * @return
     */
    fun isExsits(ssid: String): WifiConfiguration? {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        val existingConfigs = mWifiManager.configuredNetworks
        if (existingConfigs != null) {
            for (existingConfig in existingConfigs) {
                LogUtils.d(TAG, "existingConfig: " + existingConfig.SSID)
                if (existingConfig.SSID == "\"" + ssid + "\"") {
                    return existingConfig
                }
            }
        }
        return null
    }

    private fun getNetworkId(ssid: String): Int {
        if (ActivityCompat.checkSelfPermission(MyApplication.sApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return 0
        }
        val mConfigs = mWifiManager.configuredNetworks
        if (mConfigs != null) {
            for (config in mConfigs) {
                LogUtils.d(TAG, "getNetworkId[" + ssid + "][" + config!!.SSID + "]["
                        + config.networkId + "]")
                if (config != null
                        && ssid == removeDoubleQuotes(config.SSID)) {
                    return config.networkId
                }
            }
        }
        return -1
    }

    private fun removeDoubleQuotes(string: String): String {
        val length = string.length
        return if (length > 1 && string[0] == '"'
                && string[length - 1] == '"') {
            string.substring(1, length - 1)
        } else string
    }

    companion object {
        private val TAG = WifiSupport::class.java.simpleName

        /**
         * Enable/disable WifiConnectivityManager
         */
        @JvmStatic
        fun enableWifiConnectivityManager(wifiManager: WifiManager, isEnable: Boolean) {
            try {
                LogUtils.d(TAG, "try enableWifiConnectivityManager isEnable:$isEnable")
                val method = wifiManager.javaClass.getMethod("enableWifiConnectivityManager", Boolean::class.javaPrimitiveType)
                method.invoke(wifiManager, isEnable)
            } catch (e: Exception) {
                LogUtils.d(TAG, "catch enableWifiConnectivityManager exception:$e")
                e.printStackTrace()
            }
        }
    }

    init {
        mWifiTracker = WifiTracker(context, mWifiManager)
    }
}