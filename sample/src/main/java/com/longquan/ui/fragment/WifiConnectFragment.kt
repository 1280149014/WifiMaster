package com.longquan.ui.fragment

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.longquan.adapter.BaseAdapter
import com.longquan.adapter.WifiScanAdapter
import com.longquan.bean.SsidBean
import com.longquan.bean.WifiInfo
import com.longquan.ui.HomeActivity
import com.longquan.utils.GPSUtil
import com.longquan.utils.LogUtils
import com.longquan.utils.WifiHelper
import com.longquan.utils.WifiTracker
import com.thanosfisherman.wifiutils.sample.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_wifi_connect.*

class WifiConnectFragment : Fragment() , WifiTracker.WifiTrackerReceiver {

    private var TAG = WifiConnectFragment::class.java.simpleName ;
    private var mWifiTracker: WifiTracker? = null
    private val mContext: Context? = null
    private var mWifiManager: WifiManager? = null

    private var adapter:WifiScanAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWifiManager = activity?.application?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        mWifiTracker = WifiTracker(activity,mWifiManager)
        mWifiTracker!!.setWifiListener(this)
        activity?.registerReceiver(mWifiTracker!!.receiver, mWifiTracker!!.newIntentFilter())
    }

    override fun onResume() {
        super.onResume()
        mWifiManager?.wifiState?.let { onWifiStateChanged(it) }
    }

    private fun generateDummyList(size: Int): ArrayList<SsidBean> {
        val list = ArrayList<SsidBean>()
        for (i in 0 until size) {
            val item = SsidBean("wifi 热点$i")
            list += item
        }
        return list
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = WifiScanAdapter(activity)
        wifi_recycleView.adapter = adapter
        wifi_recycleView.layoutManager = LinearLayoutManager(activity)
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mWifiTracker!!.receiver) {
            activity?.unregisterReceiver(mWifiTracker!!.receiver)
            mWifiTracker!!.stopScan()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                WifiConnectFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }



    override fun onSupplicantScanning() {
        LogUtils.d(TAG, "onSupplicantScanning")
    }

    override fun onWrongPassword(ssid: String?) {
        LogUtils.d(TAG, "onWrongPassword $ssid")
    }

    override fun recWifiDisConnected(wifiInfo: android.net.wifi.WifiInfo?) {
        LogUtils.d(TAG, "recWifiDisConnected $wifiInfo")
    }

    override fun onRssiChanged() {
        LogUtils.d(TAG, "onRssiChanged")
    }

    override fun recWifiConnected(wifiInfo: android.net.wifi.WifiInfo?) {
        LogUtils.d(TAG, "recWifiConnected wifiInfo = $wifiInfo" )
    }

    override fun onSupplicantDisconnected() {
        LogUtils.d(TAG, "onSupplicantDisconnected")
    }

    override fun init() {
        LogUtils.d(TAG, "init")
    }

    override fun onWifiStateChanged(state: Int) {
        LogUtils.d(TAG, "onWifiStateChanged state:$state")
        when (state) {
            WifiManager.WIFI_STATE_DISABLED->{

            }
            WifiManager.WIFI_STATE_ENABLING -> {

            }
            WifiManager.WIFI_STATE_ENABLED->{
                mWifiTracker!!.startScan()
            }
            WifiManager.WIFI_STATE_DISABLING->{

            }
        }
    }

    override fun onConnectFail() {
        LogUtils.d(TAG, "onConnectFail")
    }

    override fun onScanResultsAvailable(avaiableWifiInfos: MutableList<WifiInfo>?) {
        LogUtils.d(TAG, "onScanResultsAvailable  avaiableWifiInfos = $avaiableWifiInfos")
//        mLoadingView.setVisibility(View.GONE)
        if (avaiableWifiInfos != null && adapter != null) {
            val size: Int = avaiableWifiInfos.size
            if (size != 0) {
                wifi_recycleView.setVisibility(View.VISIBLE)
                adapter!!.updateData(avaiableWifiInfos)
//                wifi_recycleView.scrollToPosition(0)
                adapter!!.notifyDataSetChanged()
            } else {
                LogUtils.d(TAG, "onScanResultsAvailable availableWifiInfo size$size")
            }
        }
    }

    override fun onSupplicantCompleted() {
        LogUtils.d(TAG, "onSupplicantCompleted")
    }
}