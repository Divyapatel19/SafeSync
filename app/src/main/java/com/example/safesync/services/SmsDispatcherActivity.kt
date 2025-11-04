package com.example.safesync.services

import android.app.Activity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log

class SmsDispatcherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumbers = intent.getStringArrayListExtra(EXTRA_PHONE_NUMBERS)
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        if (phoneNumbers.isNullOrEmpty() || message.isNullOrEmpty()) {
            Log.e(TAG, "SmsDispatcherActivity launched with missing data.")
            finish()
            return
        }

        Log.d(TAG, "Dispatching ${phoneNumbers.size} SMS messages.")
        val smsManager = getSystemService(SmsManager::class.java)
        val parts = smsManager.divideMessage(message)

        for (number in phoneNumbers) {
            try {
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                Log.d(TAG, "Successfully dispatched SMS to $number")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dispatch SMS to $number", e)
            }
        }

        // Immediately finish the activity so the user never sees it.
        finish()
    }

    companion object {
        const val EXTRA_PHONE_NUMBERS = "com.example.safesync.EXTRA_PHONE_NUMBERS"
        const val EXTRA_MESSAGE = "com.example.safesync.EXTRA_MESSAGE"
        private const val TAG = "SmsDispatcherActivity"
    }
}