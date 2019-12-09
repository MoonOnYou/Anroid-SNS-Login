package com.example.snslogin

import android.app.Application
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.TwitterConfig



class BaseApplication : Application() {

    val CONSUMER_KEY = "HDPNjx9O7pCmDBJAoUvli6UUy"
    val CONSUMER_SECRET = "ZMieRQuZFIAeHz4T3kIOoan70IYL6nXqlRMs3NC9sHBzJxPqLo"

    override fun onCreate() {
        super.onCreate()

        Twitter.initialize(TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET))
            .debug(true)
            .build())

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}