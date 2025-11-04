package com.example.safesync.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.safesync.screens.components.ActionButtonsSection
import com.example.safesync.screens.components.AppBottomNavigation
import com.example.safesync.screens.components.EmergencyButtonSection
import com.example.safesync.screens.components.SafetyContactsSection
import com.example.safesync.ui.theme.SafeSyncTheme
import com.example.safesync.viewmodels.ContactsViewModel
import com.example.safesync.viewmodels.HomeViewModel
import com.example.safesync.viewmodels.LocationViewModel

@Composable
fun HomeScreen(navController: NavController, contactsViewModel: ContactsViewModel, locationViewModel: LocationViewModel, homeViewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val contacts by contactsViewModel.contacts

    SafeSyncTheme {
        Scaffold(
            bottomBar = { AppBottomNavigation(navController = navController) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (locationViewModel.isSharingLocation) "Sharing Location..." else "Status: Safe",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (locationViewModel.isSharingLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                EmergencyButtonSection(onEmergency = {
                    homeViewModel.handleEmergencyAction(context, contacts, locationViewModel)
                })
                Spacer(modifier = Modifier.weight(1f))
                ActionButtonsSection(onShareLocation = { 
                    locationViewModel.shareLocation(context, contacts)
                })
                Spacer(modifier = Modifier.weight(1f))
                SafetyContactsSection(contacts = contacts)
            }
        }
    }
}
