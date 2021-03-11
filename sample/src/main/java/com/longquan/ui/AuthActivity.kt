package com.longquan.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.longquan.R
import kotlinx.android.synthetic.main.activity_authz2.*


class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authz2)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://m.baidu.com")
    }


}