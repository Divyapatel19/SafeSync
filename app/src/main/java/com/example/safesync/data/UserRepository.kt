package com.example.safesync.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository(private val firebaseDB: FirebaseRealtimeDB) {

    fun loadUser(userId: String, onDataLoaded: (User?) -> Unit) {
        val path = "users/$userId/profile"
        firebaseDB.loadData(path, User::class.java, onDataLoaded)
    }

    fun saveUser(user: User, onComplete: () -> Unit) {
        val path = "users/${user.uid}/profile"
        firebaseDB.saveData(path, user, onComplete)
    }

    fun findUserByPhone(phone: String, onComplete: (String?) -> Unit) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val query = usersRef.orderByChild("profile/phoneNumber").equalTo(phone)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userSnapshot = snapshot.children.first()
                    val user = userSnapshot.child("profile").getValue(User::class.java)
                    onComplete(user?.email)
                } else {
                    onComplete(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }
}
