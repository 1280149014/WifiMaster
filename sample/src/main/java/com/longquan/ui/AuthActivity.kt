package com.longquan.ui

import android.content.Context
import android.net.ProxyInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.longquan.R
import kotlinx.android.synthetic.main.activity_authz2.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class AuthActivity : AppCompatActivity() {

    private val url = "http://g.cn/generate_204"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authz2)

        setWeb()
        webView.loadUrl(url)

    }

    private fun setWeb() {
        val webSettings: WebSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.builtInZoomControls = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.visibility = View.VISIBLE
        webSettings.setSupportZoom(true)
        webSettings.domStorageEnabled = true
        webView.requestFocus()
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(true)
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.d("setweb", url)
                return false
            }
        }

    }


    override fun onResume() {
        super.onResume()
        object : Thread(){
            override fun run() {
                val  isWifiSetPortal = isWifiSetPortal()
                Log.d("test","$isWifiSetPortal = as");
            }
        }.start()
    }

    /**
     *  判断是否需要认证
     */
    private fun isWifiSetPortal(): Boolean {
        val mWalledGardenUrl = url
        val WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000
        var urlConnection: HttpURLConnection? = null
        return try {
            val url = URL(mWalledGardenUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.instanceFollowRedirects = false
            urlConnection.connectTimeout = WALLED_GARDEN_SOCKET_TIMEOUT_MS
            urlConnection.readTimeout = WALLED_GARDEN_SOCKET_TIMEOUT_MS
            urlConnection.useCaches = false
            urlConnection.inputStream
            urlConnection.responseCode !== 204
        } catch (e: IOException) {
            //e.printStackTrace();
            false
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect()
            }
        }
    }


    private fun getSetting(context: Context, symbol: String, defaultValue: String): String? {
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getString(context.contentResolver, symbol)
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
        }
        return value ?: defaultValue
    }


    class CaptivePortalProbeResult // captive portal has been appeased.
    @JvmOverloads constructor(
            // HTTP response code returned from Internet probe.
            private val mHttpResponseCode: Int, // Redirect destination returned from Internet probe.
            val redirectUrl: String? = null, // URL where a 204 response code indicates
            val detectUrl: String? = null) {

        val isSuccessful: Boolean
            get() = mHttpResponseCode == SUCCESS_CODE

        val isPortal: Boolean
            get() = !isSuccessful && mHttpResponseCode >= 200 && mHttpResponseCode <= 399

        val isFailed: Boolean
            get() = !isSuccessful && !isPortal

        companion object {
            const val SUCCESS_CODE = 204
            const val FAILED_CODE = 599
            val FAILED = CaptivePortalProbeResult(FAILED_CODE)
            val SUCCESS = CaptivePortalProbeResult(SUCCESS_CODE)
        }

    }



}