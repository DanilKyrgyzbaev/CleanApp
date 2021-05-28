package com.example.cleanapp

import android.app.IntentService
import android.content.Context
import android.content.Intent

class MyIntentService() : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        intent?.apply {
            when (intent.action) {
                ACTION_SEND_TEST_MESSAGE -> {
                    val message = getStringExtra(EXTRA_MESSAGE)
                    PreferenceHelper.getInstance(applicationContext)?.isCleaned = false
                    println("$message, ${PreferenceHelper.getInstance(applicationContext)?.isCleaned}")
                }
            }
        }
    }

    companion object {
        const val ACTION_SEND_TEST_MESSAGE = "ACTION_SEND_TEST_MESSAGE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }

}