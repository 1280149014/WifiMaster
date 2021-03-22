package com.longquan.ui.fragment

import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.longquan.R
import com.longquan.adapter.WifiScanAdapter
import com.longquan.adapter.WifiScanAdapter.onClickListener
import com.longquan.bean.WifiInfo
import com.longquan.common.event.EditPwdTextEvent
import com.longquan.utils.LogUtils
import com.longquan.common.wifiap.WifiHelper
import com.longquan.common.wifiap.WifiSupport
import com.longquan.common.wifiap.WifiTracker
import com.thanosfisherman.wifiutils.TypeEnum
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionErrorCode
import com.thanosfisherman.wifiutils.wifiDisconnect.DisconnectionSuccessListener
import com.thanosfisherman.wifiutils.wifiRemove.RemoveErrorCode
import com.thanosfisherman.wifiutils.wifiRemove.RemoveSuccessListener
import kotlinx.android.synthetic.main.fragment_wifi_connect.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class WifiConnectFragment : Fragment() , WifiTracker.WifiTrackerReceiver, onClickListener {

    private var TAG = WifiConnectFragment::class.java.simpleName ;
    private var mWifiTracker: WifiTracker? = null
    private var mContext: Context? = null
    private var mWifiManager: WifiManager? = null

    private var adapter:WifiScanAdapter? = null
    private var mWifiHelper: WifiHelper? = null
    private var mWifiSupport: WifiSupport? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWifiManager = activity?.application?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        mWifiTracker = WifiTracker.getInstance()
        mWifiTracker!!.addWifiListener(this)
        mWifiHelper = WifiHelper(activity, mWifiManager)
        mWifiSupport = activity?.let { mWifiManager?.let { it1 -> WifiSupport(it, it1) } }
        mContext = context
        EventBus.getDefault().register(this);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_wifi_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = WifiScanAdapter(activity)
        wifi_recycleView.adapter = adapter
        wifi_recycleView.layoutManager = LinearLayoutManager(activity)
        adapter?.setOnClickListener(this)
        adapter!!.notifyDataSetChanged()
        mCurSelected = WifiInfo()
    }

    override fun onResume() {
        super.onResume()
        mWifiTracker!!.startScan()
//        mWifiManager?.wifiState?.let { onWifiStateChanged(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mWifiTracker!!.receiver) {
            activity?.unregisterReceiver(mWifiTracker!!.receiver)
            mWifiTracker!!.stopScan()
        }
        mWifiTracker!!.removeWifiListener(this)
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    fun onEventMainThread(event: EditPwdTextEvent) {
        LogUtils.d(TAG, "EditPwdTextEvent text:" + event.mText)
        if (!TextUtils.isEmpty(event.mText)) {
            mCurSelected?.isConnecting ?: true  //用户输入密码,继续尝试
            val pwd: String = event.mText
            mCurSelected?.setPassword(pwd)
//            mContext?.let { connectWithWpa(it, mCurSelected!!.ssid,pwd) }
            mWifiHelper!!.connectWifi(mCurSelected!!.ssid,mCurSelected!!.password,mCurSelected!!.capabilities)
        } else {
            //不再尝试登录
            mCurSelected?.isConnecting ?: false
        }
        adapter?.notifyDataSetChanged()
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

//    override fun onWifiStateChanged(state: Int) {
//        LogUtils.d(TAG, "onWifiStateChanged state:$state")
//        when (state) {
//            WifiManager.WIFI_STATE_DISABLED->{
//
//            }
//            WifiManager.WIFI_STATE_ENABLING -> {
//
//            }
//            WifiManager.WIFI_STATE_ENABLED->{
//                mWifiTracker!!.startScan()
//            }
//            WifiManager.WIFI_STATE_DISABLING->{
//
//            }
//        }
//    }

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

    override fun onItemInfoClickListener(position: Int, selected: WifiInfo?) {
        LogUtils.d(TAG, "onSupplicantCompleted")
    }

    override fun onAddOthersNetwork() {
        LogUtils.d(TAG, "onAddOthersNetwork")
    }
    private val isScroll = false
    private var mCurSelected: WifiInfo? = null
    private var isNeedShowPasswordError = false
    override fun onItemClickListener(position: Int, selected: WifiInfo?) {
        LogUtils.d(TAG, "onAddOthersNetwork")
        if (isScroll) {
            return
        }
        mCurSelected = selected
        if (selected != null) {
            toConnect(selected)
        }
    }

    /**
     * 连接wifi
     *
     * @param info
     */
    private fun toConnect(info: WifiInfo) {
        LogUtils.d(TAG, "toConnect=:$info")
        if (info.security === WifiInfo.Security.NONE || !TextUtils.isEmpty(info.password)) {
            LogUtils.d(TAG, "toConnect WifiInfo Security is NONE Or Password Has Saved:$info")
            mWifiSupport!!.join(info)
        } else {
            LogUtils.d(TAG, "mWifiSupport:$mWifiSupport")
            if(mWifiSupport == null || info == null){
                return
            }
            val config: WifiConfiguration? = mWifiSupport!!.isExsits(info.ssid)
            if (config?.preSharedKey != null) {
                LogUtils.d(TAG, "toConnect WifiInfo Has configured:$info")
                mWifiSupport?.join(config)
            } else {
                LogUtils.d(TAG, "toConnect WifiInfo Has not configured:$info")
                val passwdDialogFrag = WifiConnectPasswdDialogFrag()
                passwdDialogFrag.show(parentFragmentManager, "WifiConnectPasswdDialogFrag")
            }
            isNeedShowPasswordError = true
        }
    }

    private fun connectWithWpa(context: Context,SSID: String,PASSWORD: String) {
        WifiUtils.withContext(context)
                .connectWith(SSID, PASSWORD)
                .setTimeout(15000)
                .onConnectionResult(object : ConnectionSuccessListener {
                    override fun success() {
                        Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                    }

                    override fun failed(errorCode: ConnectionErrorCode) {
                        Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                    }
                })
                .start()
    }

    private fun connectHidden(context: Context,SSID: String,PASSWORD: String) {
        WifiUtils.withContext(context)
                .connectWith(SSID, PASSWORD, TypeEnum.EAP)
                .onConnectionResult(object : ConnectionSuccessListener {
                    override fun success() {
                        Toast.makeText(context, "SUCCESS!", Toast.LENGTH_SHORT).show()
                    }

                    override fun failed(errorCode: ConnectionErrorCode) {
                        Toast.makeText(context, "EPIC FAIL!$errorCode", Toast.LENGTH_SHORT).show()
                    }
                })
                .start()
    }

    private fun disconnect(context: Context) {
        WifiUtils.withContext(context)
                .disconnect(object : DisconnectionSuccessListener {
                    override fun success() {
                        Toast.makeText(context, "Disconnect success!", Toast.LENGTH_SHORT).show()
                    }

                    override fun failed(errorCode: DisconnectionErrorCode) {
                        Toast.makeText(context, "Failed to disconnect: $errorCode", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun remove(context: Context,SSID: String) {
        WifiUtils.withContext(context)
                .remove(SSID, object : RemoveSuccessListener {
                    override fun success() {
                        Toast.makeText(context, "Remove success!", Toast.LENGTH_SHORT).show()
                    }

                    override fun failed(errorCode: RemoveErrorCode) {
                        Toast.makeText(context, "Failed to disconnect and remove: $errorCode", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun check(context: Context,SSID: String) {
        val result = WifiUtils.withContext(context).isWifiConnected(SSID)
        Toast.makeText(context, "Wifi Connect State: $result", Toast.LENGTH_SHORT).show()
    }


}