package com.example.snslogin

import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.*
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.FacebookCallback
import com.linecorp.linesdk.api.LineApiClient

class MainActivity : AppCompatActivity() , View.OnClickListener{
    
    private var facebookManager: CallbackManager? = null
    private val twitterAuthClient = TwitterAuthClient()
    private var twitterInfo1 = ""
    private var facebookInfo1 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        main_btn_twitter_login_btn.setOnClickListener(this)
        main_text_facebook_login_btn.setOnClickListener(this)
        main_text_line_login_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.main_btn_twitter_login_btn -> {
                loginTwitter()
            }
            R.id.main_text_facebook_login_btn -> {
                loginFacebook()
            }
            R.id.main_text_line_login_btn -> {
                twitterLogin()
            }
        }
    }

    
    private fun twitterLogin(){
        

    }
    
    private fun loginFacebook(){
        facebookManager = CallbackManager.Factory.create()

        LoginManager.getInstance().logInWithReadPermissions(this@MainActivity, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(facebookManager, object:FacebookCallback<LoginResult> {

            override fun onSuccess(result:LoginResult) {

                val request:GraphRequest
                request = GraphRequest.newMeRequest(result.accessToken) { user, response ->
                    Log.i("TAG", "user: $user")
                    Log.i("TAG", "AccessToken: " + result.accessToken.token)
                    setResult(Activity.RESULT_OK)

                    facebookInfo1 = "${user.getString("id")} + ${user.getString("name")} "
                    main_text_user_info_facebook.text = facebookInfo1
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,birthday")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onError(error:FacebookException) {
                Log.w("onYou", "Error: ${error.message}")
            }

            override fun onCancel() {
            }
        })

    }

    private fun loginTwitter() {
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
        facebookManager?.onActivityResult(requestCode, responseCode, intent)
    }
}