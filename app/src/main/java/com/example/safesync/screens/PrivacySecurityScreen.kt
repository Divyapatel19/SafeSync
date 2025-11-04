package com.example.safesync.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Our Commitment to Your Privacy",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "At SafeSync, your privacy and security are our top priorities. We are committed to protecting your personal information and ensuring that your data is handled with the utmost care and transparency. This document outlines our practices regarding the collection, use, and protection of your information.\n\nInformation We Collect\nWe collect information that you provide directly to us, such as when you create an account, add emergency contacts, or input your medical details. This may include your name, email address, phone number, and other personal information. We also collect location data to provide our core safety features, but only with your explicit consent.\n\nHow We Use Your Information\nYour information is used to operate, maintain, and improve our services. This includes sharing your location with your trusted contacts during an emergency, providing first responders with critical medical information, and personalizing your experience. We do not sell your personal data to third parties.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
