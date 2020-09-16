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

    override fun onCreate() {
        super.onCreate()

        Twitter.initialize(TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(TwitterAuthConfig(Constants.twitterConsumerKey, Constants.twitterConsumerSecret))
            .debug(true)
            .build())

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

    }
}