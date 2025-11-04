package com.example.safesync.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AloneModeActionsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AloneModeService.ACTION_IM_SAFE) {
            // Send a command to the running service to reset the timer
            val serviceIntent = Intent(context, AloneModeService::class.java).apply {
                action = AloneModeService.ACTION_IM_SAFE
            }
            context.startService(serviceIntent)
        }
    }
}
