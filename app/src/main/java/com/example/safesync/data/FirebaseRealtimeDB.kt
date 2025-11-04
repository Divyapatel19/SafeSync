package com.example.safesync.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class FirebaseRealtimeDB {

    private val database = FirebaseDatabase.getInstance("https://safesync-f10dc-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    fun getNewKey(path: String): String? {
        return database.child(path).push().key
    }

    suspend fun saveLocation(userId: String, locationData: Map<String, Any>) {
        database.child("users").child(userId).child("location").setValue(locationData).await()
    }

    fun <T> saveData(path: String, data: T) {
        database.child(path).setValue(data)
    }

    fun <T> saveData(path: String, data: T, onComplete: () -> Unit) {
        database.child(path).setValue(data).addOnCompleteListener { onComplete() }
    }

    fun <T> loadData(path: String, valueType: Class<T>, onDataLoaded: (T?) -> Unit) {
        database.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onDataLoaded(snapshot.getValue(valueType))
            }

            override fun onCancelled(error: DatabaseError) {
                onDataLoaded(null) // Handle error by returning null
            }
        })
    }

    // Overload for handling generic types like Map
    fun <T> loadData(path: String, typeIndicator: GenericTypeIndicator<T>, onDataLoaded: (T?) -> Unit) {
        database.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    onDataLoaded(snapshot.getValue(typeIndicator))
                } catch (e: Exception) {
                    onDataLoaded(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onDataLoaded(null)
            }
        })
    }

    fun <T> loadDataList(path: String, valueType: Class<T>, onDataLoaded: (List<T>) -> Unit) {
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(valueType) }
                onDataLoaded(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataLoaded(emptyList()) // Handle error by returning empty list
            }
        })
    }

    fun <T> loadDataListOnce(path: String, valueType: Class<T>, onDataLoaded: (List<T>) -> Unit) {
        database.child(path).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(valueType) }
                onDataLoaded(items)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataLoaded(emptyList()) // Handle error by returning empty list
            }
        })
    }

    fun deleteData(path: String) {
        database.child(path).removeValue()
    }
}
