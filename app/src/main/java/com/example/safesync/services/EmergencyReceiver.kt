package com.example.safesync.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class EmergencyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: Emergency alarm triggered.")
        // This receiver's only job is to trigger the emergency SMS function
        // in the service.
        val serviceIntent = Intent(context, AloneModeService::class.java).apply {
            action = AloneModeService.ACTION_SEND_EMERGENCY_SMS
        }
        context.startService(serviceIntent)
    }

    companion object {
        private const val TAG = "EmergencyReceiver"
    }
}
