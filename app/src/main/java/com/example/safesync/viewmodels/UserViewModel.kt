package com.example.safesync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.safesync.data.User
import com.example.safesync.data.UserRepository
import com.google.firebase.auth.FirebaseAuth

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    val userName: LiveData<String?> = MutableLiveData()

    private val auth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            loadUser(firebaseUser.uid)
        } else {
            _user.postValue(null)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun loadUser(userId: String) {
        repository.loadUser(userId) { user ->
            _user.postValue(user)
        }
    }

    fun updateUserProfile(name: String, email: String, phoneNumber: String) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            val userId = firebaseUser.uid
            val updatedUser = User(uid = userId, name = name, email = email, phoneNumber = phoneNumber)
            repository.saveUser(updatedUser) {
                _user.postValue(updatedUser)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
