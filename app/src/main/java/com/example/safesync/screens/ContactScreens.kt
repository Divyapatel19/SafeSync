package com.example.safesync.screens

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.safesync.screens.components.AppBottomNavigation
import com.example.safesync.ui.theme.SafeSyncTheme

@Composable
fun ContactsScreen(navController: NavController) {
    Scaffold(
        bottomBar = { AppBottomNavigation(navController = navController) }
    ) {
        Text(text = "Contacts Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    SafeSyncTheme {
        ContactsScreen(navController = rememberNavController())
    }
}
