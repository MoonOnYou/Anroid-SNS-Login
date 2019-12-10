package com.example.snslogin

import kotlinx.android.synthetic.main.activity_main.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.*
import com.facebook.login.LoginResult
import com.squareup.picasso.Picasso




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


                //Profile.getCurrentProfile() 여기 자체에서도 기본적인거는 많이 가져올수 잇는데 ...

                val request:GraphRequest

                request = GraphRequest.newMeRequest(result.accessToken) { user, response ->

                    if (user.has("email")) {
                        Log.e("facebook email", user.getString("email")) // 이메일은 예외처리 해줘야겟다 ,,
                    }

                    facebookInfo1 = "user_id : ${user.getString("id")} \n user_name : ${user.getString("name")} \n AccessToken : ${result.accessToken.token}"
                    main_text_user_info_facebook.text = facebookInfo1
                    Picasso.with(this@MainActivity).load(Profile.getCurrentProfile().getProfilePictureUri(200,200)).into(main_image_profile_facebook)

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


               // result.getProfileImageURL()
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