package com.example.cleanapp

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {

    private val preferences: SharedPreferences
    private val NAME = "setting"
    private val ISNOTAUTH = "isNotAuth"
    private val IS_CLEANED = "isCleaned"

    var isCleaned: Boolean
        get() = preferences.getBoolean(IS_CLEANED, false)
        set(value) {
            preferences.edit().putBoolean(IS_CLEANED, value).apply()
        }


    fun clear() {
        //preferences.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var instance: PreferenceHelper? = null
        fun getInstance(context: Context): PreferenceHelper? {
            if (instance == null) PreferenceHelper(context)
            return instance
        }
    }

    init {
        instance = this
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }
}
