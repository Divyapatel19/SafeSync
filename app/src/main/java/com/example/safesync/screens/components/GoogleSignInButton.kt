package com.example.safesync.screens.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.safesync.R
import com.example.safesync.data.AuthResult
import com.example.safesync.data.FirebaseAuthenticator
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    authenticator: FirebaseAuthenticator,
    onSignInResult: (AuthResult) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val oneTapClient = remember { Identity.getSignInClient(context) }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            scope.launch {
                val authResult = authenticator.signInWithGoogleFromIntent(oneTapClient, result.data!!)
                onSignInResult(authResult)
            }
        } else {
            onSignInResult(AuthResult.Error("Google Sign-In was cancelled or failed."))
        }
    }

    Button(
        onClick = {
            scope.launch {
                val intentSender = authenticator.beginGoogleSignIn(oneTapClient)
                if (intentSender != null) {
                    activityResultLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                } else {
                    onSignInResult(AuthResult.Error("Failed to begin Google Sign-In."))
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = "Google sign-in",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text("  Sign in with Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
