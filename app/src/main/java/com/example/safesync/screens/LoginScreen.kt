package com.example.safesync.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safesync.R
import com.example.safesync.data.AuthResult
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.data.UserRepository
import com.example.safesync.screens.components.FormTextField
import com.example.safesync.screens.components.GoogleSignInButton
import com.example.safesync.screens.components.PhoneNumberDialog
import com.example.safesync.ui.theme.SafeSyncTheme
import com.example.safesync.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel, userRepository: UserRepository) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val authenticator = remember { FirebaseAuthenticator() }
    var showPhoneDialog by remember { mutableStateOf<AuthResult.Success?>(null) }

    fun handleAuthResult(result: AuthResult) {
        isLoading = false
        when (result) {
            is AuthResult.Success -> {
                val firebaseUser = result.user
                userRepository.loadUser(firebaseUser.uid) { userFromDb ->
                    if (userFromDb?.phoneNumber.isNullOrBlank()) {
                        showPhoneDialog = result
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            }
            is AuthResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    if (showPhoneDialog != null) {
        PhoneNumberDialog(
            onDismiss = { /* This dialog is not dismissible */ },
            onConfirm = { phone ->
                val user = showPhoneDialog!!.user
                userViewModel.updateUserProfile(user.displayName ?: "", user.email ?: "", phone)
                showPhoneDialog = null
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )
    }

    SafeSyncTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img1),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SafeSync",
                        style = typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Safety, Our Priority",
                        style = typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    FormTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = "Email / Phone Number",
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FormTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isPasswordField = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (emailOrPhone.isBlank() || password.isBlank()) {
                                handleAuthResult(AuthResult.Error("Please enter credentials."))
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val isPhone = emailOrPhone.all { it.isDigit() }
                                if (isPhone) {
                                    userRepository.findUserByPhone(emailOrPhone.trim()) { email ->
                                        if (email != null) {
                                            scope.launch {
                                                val result = authenticator.signIn(email, password.trim())
                                                handleAuthResult(result)
                                            }
                                        } else {
                                            handleAuthResult(AuthResult.Error("User not found."))
                                        }
                                    }
                                } else {
                                    val result = authenticator.signIn(emailOrPhone.trim(), password.trim())
                                    handleAuthResult(result)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        Text(text = "Log In", fontSize = 18.sp, color = colorScheme.onPrimary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { /* TODO: Handle forgot password */ }) {
                            Text(text = "Forgot Password?", color = colorScheme.primary)
                        }

                        TextButton(onClick = { navController.navigate("create_account") }) {
                            Text(text = "Create Account", color = colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Divider(modifier = Modifier.weight(1f))
                        Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = colorScheme.onSurfaceVariant)
                        Divider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GoogleSignInButton(authenticator = authenticator, onSignInResult = ::handleAuthResult)

                    Spacer(modifier = Modifier.weight(1f, fill = false))

                    Text(
                        text = "Privacy and Security",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 16.dp)
                            .clickable { navController.navigate("privacy_security") }
                    )
                }
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorScheme.scrim.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
