package com.example.safesync.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: Hourly notification alarm triggered.")
        val serviceIntent = Intent(context, AloneModeService::class.java).apply {
            action = AloneModeService.ACTION_SEND_HOURLY_NOTIFICATION
        }
        context.startService(serviceIntent)
    }

    companion object {
        private const val TAG = "NotificationReceiver"
    }
}
