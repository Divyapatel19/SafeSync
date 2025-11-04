package com.example.safesync.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }

    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasExactAlarmPermission = alarmManager.canScheduleExactAlarms()
        }
    }

    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.CALL_PHONE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)

    var showRationaleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(permissionsState) {
        if (!permissionsState.allPermissionsGranted && !permissionsState.shouldShowRationale) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (permissionsState.allPermissionsGranted && hasExactAlarmPermission) {
        LaunchedEffect(Unit) {
            onPermissionsGranted()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Permissions Required",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!permissionsState.allPermissionsGranted) {
                val textToShow = if (permissionsState.shouldShowRationale) {
                    "SafeSync needs several permissions to protect you during an emergency. Please grant them on the next screen."
                } else {
                    "Some permissions have been permanently denied. Please enable them in your phone's settings for SafeSync to function."
                }
                Text(
                    text = textToShow,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
                    if (permissionsState.shouldShowRationale) {
                        showRationaleDialog = true
                    } else {
                        context.openAppSettings()
                    }
                }) {
                    Text(if (permissionsState.shouldShowRationale) "Grant Permissions" else "Open Settings")
                }
            } else if (!hasExactAlarmPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SafeSync also needs permission to schedule precise alarms for safety check-ins. Please enable this special access.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
                            exactAlarmPermissionLauncher.launch(it)
                        }
                    }
                }) {
                    Text("Enable Exact Alarms")
                }
            }
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permissions Required") },
            text = {
                Text("For your safety, SafeSync requires access to your Location, Contacts, and Phone/SMS capabilities.\n\n- Location: To share your whereabouts during an emergency.\n- Contacts: To call and message your trusted contacts.\n- Phone & SMS: To automatically call and send alerts.\n- Notifications: To send you safety check-ins in Alone Mode.\n- Alarms & Reminders: To schedule periodic safety checks and emergency alerts.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
    startActivity(intent)
}