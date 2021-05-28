package com.example.cleanapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // We use this to make sure that we execute code, only when this exact
        // Alarm triggered our Broadcast receiver
        if (intent?.action == "MyBroadcastReceiverAction") {
            PreferenceHelper.getInstance(context!!)?.isCleaned = false
            Log.d("ALARM", "RECEIVED ${PreferenceHelper.getInstance(context)?.isCleaned}")
        }
    }
}