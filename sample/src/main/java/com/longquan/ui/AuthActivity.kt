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
//        object : Thread(){
//            override fun run() {
//                val  isWifiSetPortal = isWifiSetPortal()
//                Log.d("test","$isWifiSetPortal = as");
//            }
//        }.start()
    }

    private fun getSetting(context: Context, symbol: String, defaultValue: String): String? {
        val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getString(context.contentResolver, symbol)
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
        }
        return value ?: defaultValue
    }

}