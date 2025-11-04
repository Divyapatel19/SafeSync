package com.example.safesync.viewmodels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.safesync.data.Contact

class HomeViewModel : ViewModel() {

    fun handleEmergencyAction(context: Context, contacts: List<Contact>, locationViewModel: LocationViewModel) {
        if (contacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts found.", Toast.LENGTH_LONG).show()
            return
        }

        val requiredPermissions = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val allPermissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            Toast.makeText(context, "Critical permissions are missing. Please check app settings.", Toast.LENGTH_LONG).show()
            return
        }

        // Call first emergency contact
        val firstContactPhone = contacts.first().phoneNumber
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$firstContactPhone")
        }
        try {
            context.startActivity(callIntent)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Could not initiate call. Please check permissions.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // Share location with all emergency contacts
        locationViewModel.shareLocation(context, contacts)
    }
}