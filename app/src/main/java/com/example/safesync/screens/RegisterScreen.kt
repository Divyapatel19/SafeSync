package com.example.safesync.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.safesync.data.AuthResult
import com.example.safesync.data.FirebaseAuthenticator
import com.example.safesync.screens.components.FormTextField
import com.example.safesync.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController, userViewModel: UserViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authenticator = remember { FirebaseAuthenticator() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create an Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        FormTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
        )
        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Mobile Number",
            keyboardType = KeyboardType.Phone
        )
        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPasswordField = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let {
            Text(it, color = colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
                    errorMessage = "All fields are required."
                } else {
                    isLoading = true
                    scope.launch {
                        val result = authenticator.signUp(name, email, password)
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> {
                                userViewModel.updateUserProfile(name, email, phone)
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                            is AuthResult.Error -> {
                                errorMessage = result.message
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary)
            } else {
                Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
