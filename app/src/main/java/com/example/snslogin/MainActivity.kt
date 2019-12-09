package com.example.snslogin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.widget.Toast
import com.twitter.sdk.android.core.*

class MainActivity : AppCompatActivity() , View.OnClickListener{

    private val twitterAuthClient = TwitterAuthClient()
    private var twitterInfo1 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        main_btn_twitter_login_btn.setOnClickListener(this)
        main_text_facebook_login_btn.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.main_btn_twitter_login_btn -> {
                action()
            }
            R.id.main_text_facebook_login_btn -> {

            }
        }
    }


    private fun action() {
        if (getTwitterSession() == null){
            TwitterAuthClient().authorize(this, object : Callback<TwitterSession>() {
                override fun success(result: Result<TwitterSession>) {                              // 세션이 아직 없다면
                    Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                    val twitterSession = result.data
                    fetchTwitterEmail(twitterSession)

                    val authToken = twitterSession.authToken
                    val token = authToken.token
                    val secret = authToken.secret

                    twitterInfo1 = "token : \n $token \n\n secret : $secret \n\n"
                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(this@MainActivity, "Failure", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            fetchTwitterEmail(getTwitterSession())
        }
    }

    private fun getTwitterSession(): TwitterSession? {
        return TwitterCore.getInstance().sessionManager.activeSession
    }

    fun fetchTwitterEmail(twitterSession: TwitterSession?) {
        TwitterAuthClient().requestEmail(twitterSession, object : Callback<String>() {
            override fun success(result: Result<String>) {
                Toast.makeText(this@MainActivity, "fetchTwitterEmail", Toast.LENGTH_SHORT).show()
                val twitterInfo2 = "e-mail : ${result.data} \n\n user_id : ${twitterSession?.userId} \n\n screen_name :  ${twitterSession?.userName}"
                main_text_user_info_twitter.text = "$twitterInfo1  $twitterInfo2"
            }

            override fun failure(exception: TwitterException) {
                Toast.makeText(this@MainActivity, "뭔가 잘못됬어요.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, responseCode: Int, intent: Intent?) {
        twitterAuthClient.onActivityResult(requestCode, responseCode, intent)
    }
}