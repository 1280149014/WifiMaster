package com.longquan.ui

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.longquan.R
import com.longquan.common.statusbar.StatusBarUtil
import com.longquan.ui.fragment.OpenGpsFragment
import com.longquan.ui.fragment.OpenWifiFragment
import com.longquan.ui.fragment.WifiConnectFragment
import com.longquan.utils.GPSUtil
import com.longquan.common.wifiap.WifiHelper
import com.longquan.common.wifiap.WifiTracker

/**
 * author : charile yuan
 * date   : 21-2-18
 * desc   :
 */
class HomeActivity : AppCompatActivity() , WifiTracker.WiFiStateListener {

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
        mWifiTracker = WifiTracker.getInstance()
        mWifiTracker!!.setWifiStateListener(this)
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
    }

    override fun onWifiStateChanged(state: Int) {
        updateUi()
    }

}