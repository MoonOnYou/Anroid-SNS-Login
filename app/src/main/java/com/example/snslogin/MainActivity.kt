package com.example.snslogin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User
import kotlinx.android.synthetic.main.activity_main.*

/*
* 사용자 로그인 정보 받아올때 각각의 프로필 사진과 각각의 이메일은
* 반드시 null 처리 해주세요
* */

class MainActivity : AppCompatActivity() , View.OnClickListener{
    
    private var facebookManager: CallbackManager? = null
    private val twitterCoreSession = TwitterCore.getInstance().sessionManager.activeSession
    private val twitterAuthClient = TwitterAuthClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        main_btn_twitter_login_btn.setOnClickListener(this)
        main_text_facebook_login_btn.setOnClickListener(this)
        main_text_line_login_btn.setOnClickListener(this)
        textViewInstaTest.setOnClickListener(this)
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
                loginLine()
            }
            R.id.textViewInstaTest -> {
                startInstarWebView()
            }
        }
    }

    private fun startInstarWebView() {
        // webView visible , gone 하기 귀찮아서 그냥 액티비티 하나 띄움
        startActivity(Intent(this, InastaWebViewActivity::class.java))
    }

    private fun loginLine(){

        val loginIntent = LineLoginApi.getLoginIntent(this, Constants.lineChannelId, LineAuthenticationParams.Builder().scopes(listOf(Scope.PROFILE,Scope.OPENID_CONNECT, Scope.OC_EMAIL)).build())
        startActivityForResult(loginIntent, Constants.lineRequestCode)

    }
    

    
    private fun loginFacebook(){
        facebookManager = CallbackManager.Factory.create()

        LoginManager.getInstance().logInWithReadPermissions(this@MainActivity, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(facebookManager, object:FacebookCallback<LoginResult> {

            override fun onSuccess(result:LoginResult) {


                //Profile.getCurrentProfile() 여기 자체에서도 기본적인거는 많이 가져올수 잇는데 ...

                val request:GraphRequest = GraphRequest.newMeRequest(result.accessToken) { user, response ->


                    if (user.has("email")) {
                        val email= user.getString("email")
                        Log.v("OnYou","facebook email : $email")
                    }

                    val userId = user.getString("id")                                 // 숫자로 된 아이 입니다!
                    val userName = user.getString("name")                             // 한글이름 입니다!
                    val accessToken = result.accessToken.token                              // 엄청 길어요!
                    val imageUrl = Profile.getCurrentProfile().getProfilePictureUri(200,200)

                    var facebookInfo1 = "user_id:$userId\nuser_name:$userName\nAccessToken:$accessToken"
                    main_text_user_info.text = facebookInfo1


                    Picasso.get().load(imageUrl).into(main_image_profile)

                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,birthday")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onError(error:FacebookException) {
                Log.v("onYou", "Error: ${error.message}")
            }

            override fun onCancel() {
            }
        })

    }

    private fun loginTwitter() {
        if (twitterCoreSession == null){                                                            //이 아이 덕분에 세션이 있으면 그냥 바로 불러올수 있습니다
            twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
                override fun success(result: Result<TwitterSession>) {
                    val twitterSession = result.data
                    fetchTwitterEmail(twitterSession)
                }

                override fun failure(exception: TwitterException) {
                    Log.v("OnYou", "twitterAuthClient.authorize Failure ${exception.message}")
                }
            })
        } else {
            fetchTwitterEmail(twitterCoreSession)
        }
    }

    fun fetchTwitterEmail(twitterSession: TwitterSession?) {
        twitterAuthClient.requestEmail(twitterSession, object : Callback<String>() {
            @SuppressLint("SetTextI18n")
            override fun success(result: Result<String>) {

                val authToken = twitterSession?.authToken                          // authToken 도 fetchTwitterEmail 에서만 가져올 수 있습니다!
                val token = authToken?.token
                val secret = authToken?.secret
                val email = result.data
                val uerId = twitterSession?.userId                                           // 숫자만 나오는 아이디 입니다!
                val screenName = twitterSession?.userName                                   // 영어와 숫자가 섞여 있는애 입니다! fetchTwitterEmail 에서만 가져올수 있습니다 !
                val twitterInfo = "e-mail:$email\n\n user_id:$uerId\n\nscreen_name:$screenName"
                main_text_user_info.text = twitterInfo
                fetchTwitterProfileUrl()
            }

            override fun failure(exception: TwitterException) {
                Log.v("OnYou", "requestEmail Failure ${exception.message}")
            }
        })
    }

    fun fetchTwitterProfileUrl(){
        val user = TwitterCore.getInstance().apiClient.accountService.verifyCredentials(true, true, true)
        user.enqueue(object : Callback<User>() {

            override fun success(userResult: Result<User>) {

                val name = userResult.data.name                                             // 한글로 정확하게 이름(ex.온유)은 여기서 가져올수 있습니다 fetchTwitterEmail() 에서는 안되더라구요 .. 못찾는 건가
                val photoUrlNormalSize = userResult.data.profileImageUrlHttps
                //val photoUrlBiggerSize = userResult.data.profileImageUrl.replace("_normal", "_bigger")
                //val photoUrlMiniSize = userResult.data.profileImageUrl.replace("_normal", "_mini")
                //val photoUrlOriginalSize = userResult.data.profileImageUrl.replace("_normal", "") // 나중에 사진 사이즈를 다르게 가져오고 싶을때 사용하세요!

                Picasso.get().load(photoUrlNormalSize).into(main_image_profile)

            }

            override fun failure(exc: TwitterException) {
                Log.v("OnYou", "Verify Credentials Failure ${exc.message}")
            }
        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, responseCode: Int, intent: Intent?) {
        twitterAuthClient.onActivityResult(requestCode, responseCode, intent)
        facebookManager?.onActivityResult(requestCode, responseCode, intent)
        if (requestCode == Constants.lineRequestCode) lineRequest(intent)

    }

    @SuppressLint("SetTextI18n")
    private fun lineRequest( intent: Intent?){

        val result = LineLoginApi.getLoginResultFromIntent(intent)

        when (result.responseCode) {

            LineApiResponseCode.SUCCESS -> {                                                        // Login successful

                val email = result.lineIdToken?.email

                val accessToken = result.lineCredential!!.accessToken.tokenString
                val displayName = result.lineProfile!!.displayName                           // 한글이름(ex.온유) 입니다!
                val statusMessage = result.lineProfile!!.statusMessage
                val userId = result.lineProfile!!.userId                                     // 숫자와 영어만 나오는 아이디 입니다!!
                val profileUrl = result.lineProfile!!.pictureUrl.toString()                  // 사용자가 사진을 설정 안했으면 null 로 들어옵니다(그냥 아무 그림도 안뜸), 트위터는 기본이미지가 들어오더라구요!
                main_text_user_info.text = "email: $email\n displayName : $displayName \n statusMessage: $statusMessage \n userId : $userId \n accessToken: $accessToken"
                Picasso.get().load(profileUrl).into(main_image_profile)
            }

            LineApiResponseCode.CANCEL ->                                                           // Login canceled by user

                Log.v("OnYou", "LINE Login Canceled by user.")

            else -> {                                                                               // Login canceled due to other error

                Log.v("OnYou", "Login FAILED!")
                Log.v("OnYou", result.errorData.toString())
            }
        }

    }
}