package com.example.safesync.screens

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.screens.components.AppBottomNavigation
import com.example.safesync.screens.components.GeneralSettings
import com.example.safesync.screens.components.ProfileHeader
import com.example.safesync.screens.components.SafetySetupChecklist
import com.example.safesync.ui.theme.SafeSyncTheme
import com.example.safesync.viewmodels.ContactsViewModel
import com.example.safesync.viewmodels.MedicalInfoViewModel
import com.example.safesync.viewmodels.UserViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    contactsViewModel: ContactsViewModel,
    medicalInfoViewModel: MedicalInfoViewModel,
    authenticator: FirebaseAuthenticator
) {
    val user by userViewModel.user.observeAsState()
    val contacts by contactsViewModel.contacts
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    SafeSyncTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Your Info", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = { AppBottomNavigation(navController = navController) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ProfileHeader(userName = user?.name ?: "User", imageUri = null) {
                        navController.navigate("edit_profile")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    SafetySetupChecklist(
                        contacts = contacts,
                        medicalInfoViewModel = medicalInfoViewModel,
                        hasLocationPermission = locationPermissionState.status.isGranted,
                        onLocationPermissionClick = { locationPermissionState.launchPermissionRequest() },
                        onAddContactClick = { navController.navigate("manage_contacts") },
                        onGoToMedicalInfo = { navController.navigate("medical_info") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    GeneralSettings(navController = navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Button(
                        onClick = { authenticator.signOut() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = "Log Out", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
