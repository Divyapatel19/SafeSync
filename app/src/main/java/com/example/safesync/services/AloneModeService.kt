package com.example.safesync.services

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.safesync.MainActivity
import com.example.safesync.R
import com.example.safesync.data.Contact
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.data.FirebaseRealtimeDB
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.GenericTypeIndicator
import java.util.concurrent.TimeUnit

class AloneModeService : Service() {

    private lateinit var firebaseAuthenticator: FirebaseAuthenticator
    private lateinit var firebaseRealtimeDB: FirebaseRealtimeDB
    private var emergencySmsSent = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                updateLocationInFirebase(location.latitude, location.longitude)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Service is being created.")
        firebaseAuthenticator = FirebaseAuthenticator()
        firebaseRealtimeDB = FirebaseRealtimeDB()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Received action: ${intent?.action}")
        when (intent?.action) {
            ACTION_IM_SAFE -> handleImSafeAction()
            ACTION_SEND_EMERGENCY_SMS -> {
                shareLocationWithEmergencyContacts()
                emergencySmsSent = true
                stopForeground(true)
                cancelHourlyNotificationAlarm()
                cancelEmergencyAlarm()
            }
            ACTION_SEND_HOURLY_NOTIFICATION -> {
                if (!emergencySmsSent) {
                    sendSafetyNotification()
                    scheduleHourlyNotificationAlarm()
                }
            }
            else -> {
                if (!isServiceRunning) {
                    Log.d(TAG, "onStartCommand: Starting new Alone Mode session.")
                    isServiceRunning = true
                    emergencySmsSent = false
                    startForegroundService()
                    startLocationUpdates()
                    scheduleHourlyNotificationAlarm()
                    scheduleEmergencyAlarm()
                }
            }
        }

        return START_STICKY
    }

    private fun handleImSafeAction() {
        Log.d(TAG, "handleImSafeAction: User is safe. Resetting emergency alarm.")
        cancelEmergencyAlarm()
        scheduleEmergencyAlarm()

        getSystemService(NotificationManager::class.java).cancel(2)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Service is being destroyed.")
        stopLocationUpdates()
        cancelHourlyNotificationAlarm()
        cancelEmergencyAlarm()
        isServiceRunning = false
    }

    private fun startForegroundService() {
        val channelId = "alone_mode_channel"
        val channelName = "Alone Mode"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alone Mode Active")
            .setContentText("Your location is being shared privately.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(5)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocationInFirebase(latitude: Double, longitude: Double) {
        val userId = firebaseAuthenticator.getCurrentUserId()
        if (userId != null) {
            val locationData = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "timestamp" to System.currentTimeMillis()
            )
            firebaseRealtimeDB.saveData("users/$userId/location", locationData)
        }
    }

    private fun sendSafetyNotification() {
        Log.d(TAG, "sendSafetyNotification: Sending safety check notification.")
        val channelId = "safety_check_channel"
        val channelName = "Safety Check"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val safeIntent = Intent(this, AloneModeActionsReceiver::class.java).apply { action = ACTION_IM_SAFE }
        val safePendingIntent = PendingIntent.getBroadcast(this, 0, safeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Are you safe?")
            .setContentText("Please confirm that you are safe.")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .addAction(NotificationCompat.Action(0, "I'm Safe", safePendingIntent))
            .build()

        getSystemService(NotificationManager::class.java).notify(2, notification)
    }

    private fun scheduleHourlyNotificationAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val triggerAtMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1) // For testing: 1 minute
        Log.d(TAG, "scheduleHourlyNotificationAlarm: Scheduling for $triggerAtMillis")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
             Log.e(TAG, "scheduleHourlyNotificationAlarm: Cannot schedule exact alarms. Permission missing or denied.")
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun cancelHourlyNotificationAlarm() {
        Log.d(TAG, "cancelHourlyNotificationAlarm: Cancelling hourly notification alarm.")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleEmergencyAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, EmergencyReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val triggerAtMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2) // 2 minutes for testing
        Log.d(TAG, "scheduleEmergencyAlarm: Scheduling emergency alarm for ${triggerAtMillis}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.e(TAG, "scheduleEmergencyAlarm: Cannot schedule exact alarms. Permission missing or denied.")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun cancelEmergencyAlarm() {
        Log.d(TAG, "cancelEmergencyAlarm: Cancelling emergency alarm.")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, EmergencyReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun shareLocationWithEmergencyContacts() {
        Log.d(TAG, "shareLocationWithEmergencyContacts: Attempting to share location with contacts.")
        val userId = firebaseAuthenticator.getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "shareLocationWithEmergencyContacts: FAILED - User ID is null.")
            return
        }
        Log.d(TAG, "shareLocationWithEmergencyContacts: Got user ID: $userId")

        val typeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
        firebaseRealtimeDB.loadData("users/$userId/location", typeIndicator) { locationData ->
            if (locationData == null) {
                Log.e(TAG, "shareLocationWithEmergencyContacts: FAILED - Location data is null.")
                return@loadData
            }
            val latitude = locationData["latitude"] as? Double
            val longitude = locationData["longitude"] as? Double

            if (latitude == null || longitude == null) {
                Log.e(TAG, "shareLocationWithEmergencyContacts: FAILED - Latitude or longitude is null.")
                return@loadData
            }
            Log.d(TAG, "shareLocationWithEmergencyContacts: Got location: $latitude, $longitude")

            firebaseRealtimeDB.loadDataListOnce("users/$userId/contacts", Contact::class.java) { contacts ->
                if (contacts.isEmpty()) {
                    Log.e(TAG, "shareLocationWithEmergencyContacts: FAILED - No emergency contacts found.")
                    return@loadDataListOnce
                }
                Log.d(TAG, "shareLocationWithEmergencyContacts: Found ${contacts.size} contacts. Preparing to dispatch SMS.")

                val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                val message = "I am in trouble. This is my current location: $googleMapsUrl. You can reply with \"SAFESYNC-LOCATION\" to get my updated location."

                val intent = Intent(applicationContext, SmsDispatcherActivity::class.java).apply {
                    putStringArrayListExtra(SmsDispatcherActivity.EXTRA_PHONE_NUMBERS, ArrayList(contacts.map { it.phoneNumber }))
                    putExtra(SmsDispatcherActivity.EXTRA_MESSAGE, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                applicationContext.startActivity(intent)
            }
        }
    }

    companion object {
        private const val TAG = "AloneModeService"

        @Volatile
        var isServiceRunning = false
            private set

        const val ACTION_IM_SAFE = "com.example.safesync.ACTION_IM_SAFE"
        const val ACTION_SEND_EMERGENCY_SMS = "com.example.safesync.ACTION_SEND_EMERGENCY_SMS"
        const val ACTION_SEND_HOURLY_NOTIFICATION = "com.example.safesync.ACTION_SEND_HOURLY_NOTIFICATION"
    }
}
