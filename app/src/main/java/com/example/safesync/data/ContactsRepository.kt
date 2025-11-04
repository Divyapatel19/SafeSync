package com.example.safesync.data

import com.google.firebase.auth.FirebaseAuth

class ContactsRepository(private val firebaseDB: FirebaseRealtimeDB) {

    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun loadContacts(onContactsLoaded: (List<Contact>) -> Unit) {
        val userId = getUserId()
        if (userId == null) {
            onContactsLoaded(emptyList())
            return
        }
        val path = "users/$userId/contacts"
        firebaseDB.loadDataList(path, Contact::class.java, onContactsLoaded)
    }

    fun addContact(contact: Contact) {
        val userId = getUserId()
        if (userId == null) return
        val path = "users/$userId/contacts/${contact.id}"
        firebaseDB.saveData(path, contact)
    }

    fun deleteContact(contactId: String) {
        val userId = getUserId()
        if (userId == null) return
        val path = "users/$userId/contacts/$contactId"
        firebaseDB.deleteData(path)
    }
}
