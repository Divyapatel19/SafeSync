package com.example.safesync.data

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.safesync.BuildConfig
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

sealed class AuthResult {
    data class Success(val user: FirebaseUser, val isNewUser: Boolean = false) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class FirebaseAuthenticator {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(authResult.user!!)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signUp(name: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user!!
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()
            AuthResult.Success(user, isNewUser = true)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    // Methods for Google One Tap Sign-In
    suspend fun beginGoogleSignIn(oneTapClient: SignInClient): IntentSender? = withContext(Dispatchers.IO) {
        try {
            val result = oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(true)
                    .build()
            ).await()
            result.pendingIntent.intentSender
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            null
        }
    }

    suspend fun signInWithGoogleFromIntent(oneTapClient: SignInClient, intent: Intent): AuthResult = withContext(Dispatchers.IO) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val idToken = credential.googleIdToken
            if (idToken == null) {
                return@withContext AuthResult.Error("Google Sign-In failed: ID token was null.")
            }
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            AuthResult.Success(authResult.user!!, isNewUser)
        } catch (e: ApiException) {
            e.printStackTrace()
            AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed.")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
