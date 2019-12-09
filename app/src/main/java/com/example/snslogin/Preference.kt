package com.example.snslogin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context


class Preferences private constructor() {

    private var context: Context? = null

    // context 설정
    fun setInit(context: Context) {
        if (this.context == null) this.context = context
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var preferences: Preferences? = null

        // 세션 초기화
        val instance: Preferences?
            get() {
                synchronized(Preferences::class.java) {
                    if (preferences == null) preferences = Preferences()
                }
                return preferences
            }

    }

    fun put(key: String, value: String, mode: String) {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value) //key 라는 key 값으로 value 데이터를 저장한다.
        editor.apply()
    }

    fun put(key: String, value: Int, mode: String) {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }


    fun put(key: String, value: Boolean, mode: String) {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    // getValue 는 값을 가져오는 것이고 value 는 각각 타입에 따른 초기값 (0,"",false)를 넣어주면 된다
    fun getValue(key: String, value: String, mode: String): String? {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        return try {
            sharedPreferences.getString(key, value)
        } catch (e: Exception) {
            value
        }

    }

    fun getValue(key: String, value: Int, mode: String): Int {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        return try {
            sharedPreferences.getInt(key, value)
        } catch (e: Exception) {
            value
        }

    }

    fun getValue(key: String, value: Boolean, mode: String): Boolean {
        val sharedPreferences = context!!.getSharedPreferences(mode, Activity.MODE_PRIVATE)
        return try {
            sharedPreferences.getBoolean(key, value)
        } catch (e: Exception) {
            value
        }

    }
}
