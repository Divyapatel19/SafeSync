package com.example.safesync.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.safesync.data.Contact
import com.example.safesync.screens.components.ContactListItem
import com.example.safesync.viewmodels.ContactsViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageContactsScreen(navController: NavController, contactsViewModel: ContactsViewModel) {
    val context = LocalContext.current
    val contacts by contactsViewModel.contacts
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var showAddContactDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf<Contact?>(null) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
    ) { contactUri ->
        contactUri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val hasPhoneNumberIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                    val contactId = c.getString(idIndex)
                    val name = c.getString(nameIndex)

                    if (c.getInt(hasPhoneNumberIndex) > 0) {
                        val phoneCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        phoneCursor?.use { pCursor ->
                            if (pCursor.moveToFirst()) {
                                val phoneIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                val phoneNumber = pCursor.getString(phoneIndex)
                                if (userId != null) {
                                    contactsViewModel.addContact(name, phoneNumber)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permission needed to read contacts.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Safety Contacts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddContactDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No safety contacts added yet.")
                    }
                }
            } else {
                items(contacts) { contact ->
                    ContactListItem(contact = contact, onDelete = { showDeleteConfirmation = contact })
                    Divider()
                }
            }
        }

        if (showAddContactDialog) {
            AddContactDialog(
                onDismiss = { showAddContactDialog = false },
                onAddManually = { showAddContactDialog = false /* TODO: Implement manual add screen */ },
                onAddFromContacts = {
                    showAddContactDialog = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        contactPickerLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
            )
        }

        showDeleteConfirmation?.let { contact ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Delete Contact") },
                text = { Text("Are you sure you want to delete ${contact.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            if (userId != null) {
                                contactsViewModel.deleteContact(contact.id)
                            }
                            showDeleteConfirmation = null
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAddManually: () -> Unit,
    onAddFromContacts: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Contact") },
        text = { Text("Add a new contact manually or from your phone's contact list.") },
        confirmButton = {
            Button(onClick = onAddFromContacts) {
                Text("From Contacts")
            }
        },
        dismissButton = {
            TextButton(onClick = onAddManually) {
                Text("Manually")
            }
        }
    )
}
