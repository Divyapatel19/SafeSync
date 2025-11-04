package com.example.safesync

import android.app.Application
import com.example.safesync.data.ContactsRepository
import com.example.safesync.data.FirebaseRealtimeDB
import com.example.safesync.data.MedicalInfoRepository
import com.example.safesync.data.UserRepository
import com.example.safesync.viewmodels.ContactsViewModelFactory
import com.example.safesync.viewmodels.MedicalInfoViewModelFactory
import com.example.safesync.viewmodels.UserViewModelFactory
import com.google.firebase.FirebaseApp

class SafeSyncApp : Application() {

    private val firebaseDB by lazy { FirebaseRealtimeDB() }
    val medicalInfoRepository by lazy { MedicalInfoRepository(firebaseDB) }
    val medicalInfoViewModelFactory by lazy { MedicalInfoViewModelFactory(medicalInfoRepository) }
    val contactsRepository by lazy { ContactsRepository(firebaseDB) }
    val contactsViewModelFactory by lazy { ContactsViewModelFactory(contactsRepository) }
    val userRepository by lazy { UserRepository(firebaseDB) }
    val userViewModelFactory by lazy { UserViewModelFactory(userRepository) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
