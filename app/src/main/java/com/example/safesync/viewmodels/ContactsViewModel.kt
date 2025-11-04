package com.example.safesync.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.safesync.data.Contact
import com.example.safesync.data.ContactsRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    private val _contacts = mutableStateOf<List<Contact>>(emptyList())
    val contacts: State<List<Contact>> = _contacts

    private val auth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            loadContacts()
        } else {
            _contacts.value = emptyList()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        loadContacts()
    }

    fun loadContacts() {
        repository.loadContacts { loadedContacts ->
            _contacts.value = loadedContacts
        }
    }

    fun addContact(name: String, phoneNumber: String) {
        val newContact = Contact(id = UUID.randomUUID().toString(), name = name, phoneNumber = phoneNumber)
        repository.addContact(newContact)
    }

    fun deleteContact(contactId: String) {
        repository.deleteContact(contactId)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
