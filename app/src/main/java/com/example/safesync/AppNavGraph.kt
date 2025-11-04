package com.example.safesync

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.screens.CrisisAlertsScreen
import com.example.safesync.screens.EditProfileScreen
import com.example.safesync.screens.EmergencySosScreen
import com.example.safesync.screens.FeaturesScreen
import com.example.safesync.screens.HelpScreen
import com.example.safesync.screens.HomeScreen
import com.example.safesync.screens.LoginScreen
import com.example.safesync.screens.ManageContactsScreen
import com.example.safesync.screens.MedicalInfoScreen
import com.example.safesync.screens.PermissionScreen
import com.example.safesync.screens.PrivacySecurityScreen
import com.example.safesync.screens.ProfileScreen
import com.example.safesync.screens.RegisterScreen
import com.example.safesync.viewmodels.ContactsViewModel
import com.example.safesync.viewmodels.LocationViewModel
import com.example.safesync.viewmodels.MedicalInfoViewModel
import com.example.safesync.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val application = LocalContext.current.applicationContext as SafeSyncApp
    val userViewModel: UserViewModel = viewModel(factory = application.userViewModelFactory)
    val contactsViewModel: ContactsViewModel = viewModel(factory = application.contactsViewModelFactory)
    val medicalInfoViewModel: MedicalInfoViewModel = viewModel(factory = application.medicalInfoViewModelFactory)
    val locationViewModel: LocationViewModel = viewModel()

    val authenticator = remember { FirebaseAuthenticator() }
    val auth = remember { FirebaseAuth.getInstance() }

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val context = LocalContext.current

    val hasPermissions = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val startDestination = remember(currentUser, hasPermissions) {
        if (!hasPermissions) {
            "permissions"
        } else {
            if (currentUser != null) "home" else "login"
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("permissions") {
            PermissionScreen(onPermissionsGranted = {
                navController.navigate(if (currentUser != null) "home" else "login") {
                    popUpTo("permissions") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                userViewModel = userViewModel,
                userRepository = application.userRepository
            )
        }
        composable("create_account") {
            RegisterScreen(navController = navController, userViewModel = userViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, contactsViewModel = contactsViewModel, locationViewModel = locationViewModel)
        }
        composable("features") {
            FeaturesScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                userViewModel = userViewModel,
                contactsViewModel = contactsViewModel,
                medicalInfoViewModel = medicalInfoViewModel,
                authenticator = authenticator
            )
        }
        composable("manage_contacts") {
            ManageContactsScreen(navController = navController, contactsViewModel = contactsViewModel)
        }
        composable("medical_info") {
            MedicalInfoScreen(navController = navController, medicalInfoViewModel = medicalInfoViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, userViewModel = userViewModel)
        }
        composable("privacy_security") {
            PrivacySecurityScreen(navController = navController)
        }
        composable("help") {
            HelpScreen(navController = navController)
        }
        composable("emergency_sos") {
            EmergencySosScreen(navController = navController)
        }
        composable("crisis_alerts") {
            CrisisAlertsScreen(navController = navController)
        }
    }

    DisposableEffect(currentUser, navController) {
        if (currentUser == null && navController.currentBackStackEntry?.destination?.route != "login" && navController.currentBackStackEntry?.destination?.route != "permissions") {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
        onDispose { }
    }
}
