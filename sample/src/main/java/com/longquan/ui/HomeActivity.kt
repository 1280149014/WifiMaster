package com.longquan.ui

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.longquan.R
import com.longquan.bean.WifiInfo
import com.longquan.common.event.EditPwdTextEvent
import com.longquan.common.statusbar.StatusBarUtil
import com.longquan.ui.fragment.OpenGpsFragment
import com.longquan.ui.fragment.OpenWifiFragment
import com.longquan.ui.fragment.WifiConnectFragment
import com.longquan.utils.GPSUtil
import com.longquan.utils.LogUtils
import com.longquan.utils.WifiHelper
import com.longquan.utils.WifiTracker
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

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
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().unregister(this);
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
    }

    override fun onConnectFail() {
        LogUtils.d(TAG, "onConnectFail")
    }

    override fun onScanResultsAvailable(avaiableWifiInfos: MutableList<WifiInfo>?) {
        // Do nothing
    }

    override fun onSupplicantCompleted() {
        // Do nothing
    }
}