package com.longquan.ui

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.longquan.bean.WifiInfo
import com.longquan.common.statusbar.StatusBarUtil
import com.longquan.ui.fragment.OpenGpsFragment
import com.longquan.ui.fragment.OpenWifiFragment
import com.longquan.ui.fragment.WifiConnectFragment
import com.longquan.utils.GPSUtil
import com.longquan.utils.LogUtils
import com.longquan.utils.WifiHelper
import com.longquan.utils.WifiTracker
import com.thanosfisherman.wifiutils.sample.R

/**
 * author : charile yuan
 * date   : 21-2-18
 * desc   :
 */
class HomeActivity : AppCompatActivity() , WifiTracker.WifiTrackerReceiver{

    private var TAG = HomeActivity::class.java.simpleName ;
    private var openWifiFragment:OpenWifiFragment? = null
    private var openGpsFragment : OpenGpsFragment? = null
    private var wifConnectFragment :WifiConnectFragment? = null
    private val fragmentManager: FragmentManager by lazy { supportFragmentManager }

    private var mWifiTracker: WifiTracker? = null
    private val mContext: Context? = null
    private var mWifiManager: WifiManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setImmersiveStatusBar(this@HomeActivity, false)
        setContentView(R.layout.activity_home)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 555)
        updateUi()
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        mWifiTracker = WifiTracker(this,mWifiManager)
        mWifiTracker!!.setWifiListener(this)
        registerWifiChangeReceiver()
    }

    private fun updateUi() {
        if (!WifiHelper.isWifiEnabled(this)){
            setTabSelection(0)
        }else if (!GPSUtil.isGPSOpen(this)){
            setTabSelection(1)
        }else{
            setTabSelection(2)
        }
    }

    private fun setTabSelection(index: Int) {
        fragmentManager.beginTransaction().apply {
            hideFragments(this)
            when (index) {
                0->{
                    if(openWifiFragment == null){
                        openWifiFragment = OpenWifiFragment.newInstance()
                        add(R.id.homeActivityFragContainer, openWifiFragment!!)
                    } else {
                        show(openWifiFragment!!)
                    }
                }
                1->{
                    if(openGpsFragment == null){
                        openGpsFragment = OpenGpsFragment.newInstance()
                        add(R.id.homeActivityFragContainer, openGpsFragment!!)
                    } else {
                        show(openGpsFragment!!)
                    }
                }
                2->{
                    if(wifConnectFragment == null){
                        wifConnectFragment = WifiConnectFragment.newInstance()
                        add(R.id.homeActivityFragContainer, wifConnectFragment!!)
                    } else {
                        show(wifConnectFragment!!)
                    }
                }
            }
        }.commitAllowingStateLoss()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        transaction.run {
            if (openWifiFragment != null) hide(openWifiFragment!!)
            if (openGpsFragment != null) hide(openGpsFragment!!)
            if (wifConnectFragment != null) hide(wifConnectFragment!!)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mWifiTracker!!.receiver) {
            unregisterReceiver(mWifiTracker!!.receiver)
            mWifiTracker!!.stopScan()
        }
    }

    private fun registerWifiChangeReceiver() {
        registerReceiver(mWifiTracker!!.receiver, mWifiTracker!!.newIntentFilter())
    }

    override fun onSupplicantScanning() {
       LogUtils.d("","onSupplicantScanning")
    }

    override fun onWrongPassword(ssid: String?) {
        LogUtils.d("", "onWrongPassword ssid = $ssid")
    }

    override fun recWifiDisConnected(wifiInfo: android.net.wifi.WifiInfo?) {
        LogUtils.d("", "recWifiDisConnected wifiInfo = $wifiInfo")
    }

    override fun onRssiChanged() {
        LogUtils.d("", "onRssiChanged")
    }

    override fun recWifiConnected(wifiInfo: android.net.wifi.WifiInfo?) {
        LogUtils.d("", "recWifiConnected wifiInfo = $wifiInfo")
    }

    override fun onSupplicantDisconnected() {
        LogUtils.d("", "onSupplicantDisconnected")
    }

    override fun init() {
        LogUtils.d("", "init")
    }

    override fun onWifiStateChanged(state: Int) {
        LogUtils.d("", "onWifiStateChanged state:$state")
//        when (state) {
//            WifiManager.WIFI_STATE_ENABLING -> {
//                showWifiList()
//                mSwitchButton.setEnabled(false)
//                mLoadingView.setVisibility(View.VISIBLE)
//                mRecyclerView.setVisibility(View.GONE)
//            }
//            WifiManager.WIFI_STATE_ENABLED -> {
//                mSwitchButton.setEnabled(true)
//                mSwitchButton.setChecked(true)
//                if (isNeedToScan) {
//                    mWifiTracker!!.startScan()
//                    showWifiList()
//                }
//            }
//            WifiManager.WIFI_STATE_DISABLING -> {
//                hideWifiList()
//                mSwitchButton.setEnabled(false)
//                mLoadingView.setVisibility(View.GONE)
//                mRecyclerView.setVisibility(View.GONE)
//                LogUtils.d(TAG, "onWifiStateChanged WIFI_STATE_DISABLING --> clearCurrentJoinAP")
//                WirelessUtils.clearCurrentJoinAP()
//                mLocalHandler.removeMessages(com.ecarx.settings.wifiap.wifip.WifiSettingsFragment.LocalHandler.JOIN_HIDE_TIME_OUT)
//            }
//            WifiManager.WIFI_STATE_DISABLED -> {
//                mSwitchButton.setEnabled(true)
//                mSwitchButton.setChecked(false)
//                mWifiTracker!!.pauseScan()
//                hideWifiList()
//            }
//            WifiManager.WIFI_STATE_UNKNOWN -> mSwitchButton.setEnabled(true)
//            else -> {
//            }
//        }
    }

    override fun onConnectFail() {
        LogUtils.d(TAG, "onConnectFail")
//        mScanAdapter.refreshConnectFail()
    }

    override fun onScanResultsAvailable(avaiableWifiInfos: MutableList<WifiInfo>?) {
//        mLoadingView.setVisibility(View.GONE)
//        if (availableWifiInfo != null && mScanAdapter != null) {
//            val size: Int = availableWifiInfo.size
//            if (size != 0) {
//                mRecyclerView.setVisibility(View.VISIBLE)
//                mScanList = availableWifiInfo
//                mScanAdapter.updateData(mScanList)
//                mRecyclerView.scrollToPosition(0)
//            } else {
//                LogUtils.d(TAG, "onScanResultsAvailable availableWifiInfo size$size")
//            }
//        }
    }

    override fun onSupplicantCompleted() {
        // Do nothing
    }
}