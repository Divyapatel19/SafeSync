package com.example.safesync.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safesync.screens.components.FormTextField
import com.example.safesync.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController, userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var phoneNumber by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            FormTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name"
            )
            Spacer(modifier = Modifier.height(8.dp))
            FormTextField(
                value = user?.email ?: "",
                onValueChange = {},
                label = "Email",
                readOnly = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            FormTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    userViewModel.updateUserProfile(name, user?.email ?: "", phoneNumber)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}