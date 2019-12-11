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
import com.linecorp.linesdk.LoginDelegate
import com.linecorp.linesdk.LoginListener
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginResult
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.models.User
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterCore
//import sun.jvm.hotspot.utilities.IntArray // ㅇㅐ는 왜계속 에러가 날까.. 구글링 해보자 ...

class MainActivity : AppCompatActivity() , View.OnClickListener{
    
    private var facebookManager: CallbackManager? = null
    private val twitterCoreSession = TwitterCore.getInstance().sessionManager.activeSession
    private val twitterAuthClient = TwitterAuthClient()
    private var twitterInfo1 = ""
    private var facebookInfo1 = ""
    private val lineChannelId = "1653637332"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        main_btn_twitter_login_btn.setOnClickListener(this)
        main_text_facebook_login_btn.setOnClickListener(this)
        loginLine()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.main_btn_twitter_login_btn -> {
                loginTwitter()
            }
            R.id.main_text_facebook_login_btn -> {
                loginFacebook()
            }
        }
    }

    private fun loginLine(){

        main_text_line_login_btn.setChannelId(lineChannelId)
        main_text_line_login_btn.enableLineAppAuthentication(true)
        main_text_line_login_btn.setAuthenticationParams(LineAuthenticationParams.Builder().scopes(listOf(Scope.PROFILE)).build())

        Toast.makeText(this@MainActivity, "Failure", Toast.LENGTH_SHORT).show()

        // A delegate for delegating the login result to the internal login handler.
        val loginDelegate = LoginDelegate.Factory.create()
        main_text_line_login_btn.setLoginDelegate(loginDelegate)

        main_text_line_login_btn.addLoginListener(object : LoginListener {
            override fun onLoginSuccess(result: LineLoginResult) {
                Toast.makeText(this@MainActivity, "Login success", Toast.LENGTH_SHORT).show()
            }

            override fun onLoginFailure(result: LineLoginResult?) {
                Toast.makeText(this@MainActivity, "Login failure", Toast.LENGTH_SHORT).show()
            }
        })

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
                    Picasso.get().load(Profile.getCurrentProfile().getProfilePictureUri(200,200)).into(main_image_profile_facebook)

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
        if (twitterCoreSession == null){
            twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
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
            fetchTwitterEmail(twitterCoreSession)
        }
    }

    fun fetchTwitterEmail(twitterSession: TwitterSession?) {
        twitterAuthClient.requestEmail(twitterSession, object : Callback<String>() {
            override fun success(result: Result<String>) {
                Toast.makeText(this@MainActivity, "fetchTwitterEmail", Toast.LENGTH_SHORT).show()
                val twitterInfo2 = "e-mail : ${result.data} \n\n user_id : ${twitterSession?.userId} \n\n screen_name :  ${twitterSession?.userName}" // 유져네임은 시크릿하게만 가져 온다,아래꺼랑 다르게 토큰 가져 올수 있음 온유라는 이름은 못가져옴 // 유저 아이디 (숫자)/ 이메일은 밑에랑 동알하게 가져옴

                fetchTwitterProfileUrl()
               main_text_user_info_twitter.text = "$twitterInfo1  $twitterInfo2"
            }

            override fun failure(exception: TwitterException) {
                Toast.makeText(this@MainActivity, "뭔가 잘못됬어요.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun fetchTwitterProfileUrl(){
        val user = TwitterCore.getInstance().apiClient.accountService.verifyCredentials(true, true, true)
        user.enqueue(object : Callback<User>() {

            override fun success(userResult: Result<User>) {
                val name = userResult.data.name // 유저네임은 정확하게 온유라고 가져옴 ..
                val email = userResult.data.email //여기서도 이메일 잘가져온다......
              //  main_text_user_info_twitter.text = "$name  $email"

                val photoUrlNormalSize = userResult.data.profileImageUrlHttps // 이미지 뜬듯.. 힘들다..
//                val photoUrlBiggerSize = userResult.data.profileImageUrl.replace("_normal", "_bigger")
//                val photoUrlMiniSize = userResult.data.profileImageUrl.replace("_normal", "_mini")
//                val photoUrlOriginalSize = userResult.data.profileImageUrl.replace("_normal", "")

                Picasso.get().load(photoUrlNormalSize).into(main_image_profile_twitter)

            }

            override fun failure(exc: TwitterException) {
                Log.d("TwitterKit", "Verify Credentials Failure", exc)
            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, responseCode: Int, intent: Intent?) {
        twitterAuthClient.onActivityResult(requestCode, responseCode, intent)
        facebookManager?.onActivityResult(requestCode, responseCode, intent)
    }
}