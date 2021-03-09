package com.longquan.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import com.longquan.app.MyApplication;
import com.longquan.bean.WifiInfo;
import com.thanosfisherman.wifiutils.sample.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author : charile yuan
 * date   : 21-2-23
 * desc   :
 */
public class WifiTracker {
    private final String TAG = WifiTracker.class.getSimpleName();
    private Context mContext;
    private List<WifiTrackerReceiver> mWifiListeners = new ArrayList<>();
    WiFiStateListener mWifiStateListener;
    private WifiManager mWifiManager;
    private WifiHelper mWifiHelper;
    private final AtomicBoolean mConnected = new AtomicBoolean(false);
    /**
     * 原生Settings，扫描周期为10s
     * Combo scans can take 5-6s to complete - set to 10s.
     */
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private Scanner mScanner;

    /**
     * 是否进行了四次握手
     * ①有密码，密码正确，连接成功
     * ②有密码，密码错误，连接失败
     * 这2种情况下流程上都会走到FOUR_WAY_HANDSHAKE
     */
    private boolean HAS_FOUR_WAY_HANDSHAKE = false;

    private static WifiTracker mWifiTracker;


    public static WifiTracker getInstance(){
        if(mWifiTracker == null){
            synchronized (WifiTracker.class){
                if(mWifiTracker == null){
                    mWifiTracker = new WifiTracker(MyApplication.sApp,
                            (WifiManager) MyApplication.sApp.getSystemService(Context.WIFI_SERVICE));
                }
            }
        }
        return mWifiTracker;
    }

    private WifiTracker(Context context, WifiManager wifiManager) {
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mWifiHelper = new WifiHelper(mContext, mWifiManager);
        this.mScanner = new Scanner();
    }


    public IntentFilter newIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        return filter;
    }

    public boolean isConnected() {
        return mConnected.get();
    }

    public BroadcastReceiver getReceiver() {
        return mReceiver;
    }

    public void setWifiListener(List<WifiTrackerReceiver> listeners) {
        this.mWifiListeners = listeners;
    }

    public void addWifiListener(WifiTrackerReceiver listener) {
        this.mWifiListeners.add(listener);
    }

    public void removeWifiListener(WifiTrackerReceiver listener) {
        this.mWifiListeners.remove(listener);
    }

    public void setWifiStateListener(WiFiStateListener listener){
        this.mWifiStateListener = listener;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                LogUtils.d(TAG, "WIFI_STATE_CHANGED_ACTION ");
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    startScan();
                } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                    stopScan();
                }
                if (mWifiStateListener != null) {
                    mWifiStateListener.onWifiStateChanged(state);
                }
            }
            else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                boolean isUpdatedScanResult = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                LogUtils.d(TAG, "SCAN_RESULTS_AVAILABLE_ACTION Update -->" + isUpdatedScanResult);
                fetchScansAndConfigsAndUpdateAccessPoints();
            }
            else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                LogUtils.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION ");
                final SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                recSupplicantStatus(intent, state);
            }
            else if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
                final boolean isConnected = intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED);
                recSupplicantConnectStatus(isConnected);
            }
            else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                LogUtils.d(TAG, "NETWORK_STATE_CHANGED_ACTION ");
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                android.net.wifi.WifiInfo wifiInfo = intent.getParcelableExtra("wifiInfo");
                recNetworkState(networkInfo, wifiInfo);
            }
            else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                LogUtils.d(TAG, "RSSI_CHANGED_ACTION ");
                for (WifiTrackerReceiver listener : mWifiListeners) {
                    listener.onRssiChanged();
                }
            }
        }
    };

    public void startScan() {
        if (mScanner == null) {
            mScanner = new Scanner();
        }
        if (!mScanner.isScanning()) {
            mScanner.resume();
        }
    }


    public void pauseScan() {
        if (mScanner != null) {
            mScanner.pause();
        }
    }

    public void stopScan() {
        if (mScanner != null) {
            mScanner.pause();
            mScanner = null;
        }
    }

    /**
     * 请求的连接状态
     *
     * @param isConnected true连接 false 断开
     */
    protected void recSupplicantConnectStatus(boolean isConnected) {
        LogUtils.d(TAG, "recSupplicantConnectStatus- [" + isConnected + "]");
    }

    /**
     * wifi连接状态处理
     *
     * @param state
     */

    private void recSupplicantStatus(Intent intent, SupplicantState state) {
        final boolean hasErrorCode = intent.hasExtra(WifiManager.EXTRA_SUPPLICANT_ERROR);

        LogUtils.d(TAG, "SupplicantStatus-hasNewState=" + hasErrorCode);
        if (SupplicantState.SCANNING.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-SCANNING");
            for(WifiTrackerReceiver listener : mWifiListeners){
                listener.onSupplicantScanning();
            }
            HAS_FOUR_WAY_HANDSHAKE = false;
        } else if (SupplicantState.INACTIVE.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-INACTIVE");
        } else if (SupplicantState.ASSOCIATING.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-ASSOCIATING");
        } else if (SupplicantState.ASSOCIATED.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-ASSOCIATED");
        } else if (SupplicantState.FOUR_WAY_HANDSHAKE.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-FOUR_WAY_HANDSHAKE");
            HAS_FOUR_WAY_HANDSHAKE = true;
        } else if (SupplicantState.DISCONNECTED.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-DISCONNECTED --> enableWifiConnectivityManager true");
            WifiSupport.enableWifiConnectivityManager(mWifiManager, true);
            for(WifiTrackerReceiver listener : mWifiListeners){
                listener.onSupplicantDisconnected();
            }
            if (!hasErrorCode) {
                // 连接失败
                for(WifiTrackerReceiver listener : mWifiListeners) {
                    listener.onConnectFail();
                }
            }

        } else if (SupplicantState.GROUP_HANDSHAKE.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-GROUP_HANDSHAKE");
        } else if (SupplicantState.COMPLETED.equals(state)) {
            LogUtils.d(TAG, "recSupplicantStatus-COMPLETED --> enableWifiConnectivityManager true");
            WifiSupport.enableWifiConnectivityManager(mWifiManager, true);
            for(WifiTrackerReceiver listener : mWifiListeners) {
                listener.onSupplicantCompleted();
            }
        }

        if (hasErrorCode) {
            final int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
            LogUtils.d(TAG, "recSupplicantStatus-hasErrorCode=" + errorCode + ";HAS_FOUR_WAY_HANDSHAKE=" + HAS_FOUR_WAY_HANDSHAKE);
            if (errorCode == WifiManager.ERROR_AUTHENTICATING && HAS_FOUR_WAY_HANDSHAKE) {
                // 密码错误
                LogUtils.d(TAG,"SupplicantStatus- Show Pwd Error Dialog WirelessUtils.getCurrentJoinAP -->"+ WirelessUtils.getCurrentJoinAP());
                for(WifiTrackerReceiver listener : mWifiListeners) {
                    listener.onWrongPassword(WirelessUtils.getCurrentJoinAP());
                }

            }

        }

    }


    /**
     * 网络连接状态已经变更。
     *
     * @param networkInfo
     * @param wifiInfo
     */
    private void recNetworkState(NetworkInfo networkInfo, android.net.wifi.WifiInfo wifiInfo) {
        if (networkInfo != null && wifiInfo == null) {
            if (networkInfo.isConnected()) {
                wifiInfo = mWifiManager.getConnectionInfo();
            }
        }
        // 网络连接成功
        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            LogUtils.d(TAG, "recNetworkState-connect" + wifiInfo);
            if (mWifiListeners != null && wifiInfo != null) {
                for(WifiTrackerReceiver listener : mWifiListeners) {
                    listener.recWifiConnected(wifiInfo);
                }
            }
        } else if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
            LogUtils.d(TAG, "recNetworkState-disconnect" + wifiInfo);
            if (mWifiListeners != null) {
                for(WifiTrackerReceiver listener : mWifiListeners) {
                    listener.recWifiDisConnected(wifiInfo);
                }
            }
        }
    }

    private void fetchScansAndConfigsAndUpdateAccessPoints() {
        final List<ScanResult> newScanResults = mWifiManager.getScanResults();
        for (ScanResult res : newScanResults) {
            LogUtils.d(TAG, "getScanResults() :" + res.toString());
        }
        for(WifiTrackerReceiver listener : mWifiListeners) {
            listener.onScanResultsAvailable(mWifiHelper.getAllAvailableAccessPoint(newScanResults));
        }
    }

    public interface WiFiStateListener{
        /**
         * * Called when the state of Wifi has changed, the state will be one of
         * * the following.
         * *
         * * <li>{@link WifiManager#WIFI_STATE_DISABLED}</li>
         * * <li>{@link WifiManager#WIFI_STATE_ENABLED}</li>
         * * <li>{@link WifiManager#WIFI_STATE_DISABLING}</li>
         * * <li>{@link WifiManager#WIFI_STATE_ENABLING}</li>
         * * <li>{@link WifiManager#WIFI_STATE_UNKNOWN}</li>
         * * <p>
         * *
         * * @param state The new state of wifi.
         */
        void onWifiStateChanged(int state);
    }


    public interface WifiTrackerReceiver {

        /**
         * Called when scan results is available
         * see SCAN_RESULTS_AVAILABLE_ACTION
         *
         * @param avaiableWifiInfos
         */
        void onScanResultsAvailable(List<WifiInfo> avaiableWifiInfos);

        /**
         * SupplicantState.SCANNING
         * see SUPPLICANT_STATE_CHANGED_ACTION
         */
        void onSupplicantScanning();

        /**
         * SupplicantState.COMPLETED
         * see SUPPLICANT_STATE_CHANGED_ACTION
         */
        void onSupplicantCompleted();

        /**
         * SupplicantState.DISCONNECTED
         * see SUPPLICANT_STATE_CHANGED_ACTION
         */
        void onSupplicantDisconnected();

        /**
         * Called when connect fail
         */
        void onConnectFail();

        /**
         * Called when password is wrong
         */
        void onWrongPassword(String ssid);

        /**
         * Called when NETWORK_STATE_CHANGED_ACTION ,that wifi is connected
         *
         * @param wifiInfo
         */
        void recWifiConnected(android.net.wifi.WifiInfo wifiInfo);

        /**
         * Called when NETWORK_STATE_CHANGED_ACTION ,that wifi is disconnected
         *
         * @param wifiInfo
         */
        void recWifiDisConnected(android.net.wifi.WifiInfo wifiInfo);

        /**
         * Called when RSSI_CHANGED_ACTION ,
         */
        void onRssiChanged();

        void init();
    }


    class Scanner extends Handler {
        static final int MSG_SCAN = 0;

        private int mRetry = 0;

        void resume() {
            if (!hasMessages(MSG_SCAN)) {
                LogUtils.d(TAG, "resume --> sendEmptyMessage(MSG_SCAN)");
                sendEmptyMessage(MSG_SCAN);
            }
        }

        void pause() {
            mRetry = 0;
            LogUtils.d(TAG, "pause --> removeMessages(MSG_SCAN)");
            removeMessages(MSG_SCAN);
        }


        boolean isScanning() {
            return hasMessages(MSG_SCAN);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what != MSG_SCAN) {
                return;
            }
            if (mWifiManager.startScan()) {
                LogUtils.d(TAG, "handleMessage  WifiManager.startScan() ");
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                return;
            }
            sendEmptyMessageDelayed(MSG_SCAN, WIFI_RESCAN_INTERVAL_MS);
        }
    }
}
