package com.example.snslogin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_inasta_web_view.*
import java.net.URL

class InastaWebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inasta_web_view)

        settingInstarWebView()
    }

    private fun settingInstarWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val test = URL(url)
                if (test.host.contains("socialsizzle.herokuapp.com")) {
                    Toast.makeText(this@InastaWebViewActivity, url, Toast.LENGTH_LONG).show()
                    Log.i("iiiiii", url)
                    return true
                } else {
                    Log.i("iiiiii", "not contains")
                    return false
                }
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Log.i("iiiiii", "error")
            }
        }

        // 인스타그램 로그인 팝업 띄어주고,
        // 로그인하면 리로드 url 주는데 마지막 code 뒤에 있는걸 저장해놧다가 ( 마지막의 샾은 포함하지 않음 ... 아마도 ? )
        // 토큰 가져오는 쿼리를 날리면 된다
        val instargramTestBasicUrl = "https://api.instagram.com/oauth/authorize?client_id=990602627938098&redirect_uri=https://socialsizzle.herokuapp.com/auth/&scope=user_profile,user_media&response_type=code"
        webView.loadUrl(instargramTestBasicUrl)

    }

    private fun isContainsDomain(url: String, array: ArrayList<String>) :Boolean {
        for(item in array) {
            if (url.contains(item)) return true
        }
        return false
    }
}