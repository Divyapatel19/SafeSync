package com.example.safesync.viewmodels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safesync.data.Contact
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.data.FirebaseRealtimeDB
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class LocationViewModel(
    private val authenticator: FirebaseAuthenticator = FirebaseAuthenticator(),
    private val database: FirebaseRealtimeDB = FirebaseRealtimeDB()
) : ViewModel() {

    var isSharingLocation by mutableStateOf(false)
        private set

    fun shareLocation(
        context: Context,
        contacts: List<Contact>
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val userId = authenticator.getCurrentUserId()
        if (userId == null) {
            Toast.makeText(context, "You need to be logged in to share location.", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        isSharingLocation = true

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val locationData = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "timestamp" to System.currentTimeMillis()
                    )

                    viewModelScope.launch {
                        try {
                            database.saveLocation(userId, locationData)
                            Toast.makeText(context, "Location shared successfully.", Toast.LENGTH_LONG).show()

                            if (contacts.isNotEmpty()) {
                                val message = "I am in an emergency. Track my location here: https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"
                                sendSmsToContacts(context, contacts, message)
                            } else {
                                Toast.makeText(context, "No emergency contacts to share location with.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to share location.", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(context, "Couldn't get location. Make sure location is enabled on your device.", Toast.LENGTH_SHORT).show()
                }
                isSharingLocation = false // Reset after trying to share
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                isSharingLocation = false // Reset after trying to share
            }
    }

    private fun sendSmsToContacts(context: Context, contacts: List<Contact>, message: String) {
        val smsManager = context.getSystemService(SmsManager::class.java)
        contacts.forEach { contact ->
            try {
                smsManager?.sendTextMessage(contact.phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to send SMS to ${contact.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
