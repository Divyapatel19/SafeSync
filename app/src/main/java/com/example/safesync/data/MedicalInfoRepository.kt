package com.example.safesync.data

import com.google.firebase.auth.FirebaseAuth

class MedicalInfoRepository(private val firebaseDB: FirebaseRealtimeDB) {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun saveMedicalInfo(medicalInfo: MedicalInfo) {
        if (userId == null) return
        val path = "users/$userId/medical"
        firebaseDB.saveData(path, medicalInfo)
    }

    fun loadMedicalInfo(onDataLoaded: (MedicalInfo?) -> Unit) {
        if (userId == null) {
            onDataLoaded(null)
            return
        }
        val path = "users/$userId/medical"
        firebaseDB.loadData(path, MedicalInfo::class.java, onDataLoaded)
    }
}
