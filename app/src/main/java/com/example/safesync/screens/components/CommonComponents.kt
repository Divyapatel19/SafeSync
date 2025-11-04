package com.example.safesync.screens.components

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.safesync.data.Contact
import com.example.safesync.services.AloneModeService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.abs

@Composable
fun AppBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(BottomNavItem.Home, BottomNavItem.Features, BottomNavItem.Profile)

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { popUpTo(it) { saveState = true } }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(var title: String, var icon: ImageVector, var route: String) {
    object Home : BottomNavItem("Home", Icons.Default.Home, "home")
    object Features : BottomNavItem("Features", Icons.Default.Favorite, "features")
    object Profile : BottomNavItem("Profile", Icons.Default.AccountCircle, "profile")
}

@Composable
fun EmergencyButtonSection(onEmergency: () -> Unit) {
    var showEmergencyDialog by remember { mutableStateOf(false) }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Activate Emergency?") },
            text = { Text("This will immediately call your first emergency contact and send your location to all of them. Do you want to proceed?") },
            confirmButton = { Button(onClick = { showEmergencyDialog = false; onEmergency() }) { Text("ACTIVATE") } },
            dismissButton = { Button(onClick = { showEmergencyDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier.size(190.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().clickable { showEmergencyDialog = true }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Emergency, "Emergency Siren", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("EMERGENCY", color = MaterialTheme.colorScheme.onError, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActionButtonsSection(onShareLocation: () -> Unit) {
    var isAloneModeActive by remember { mutableStateOf(AloneModeService.isServiceRunning) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else { null }

    if (showConfirmationDialog) {
        AloneModeConfirmationDialog(
            onConfirm = {
                showConfirmationDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(context, "Permission for emergency alerts is required.", Toast.LENGTH_LONG).show()
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also { context.startActivity(it) }
                        return@AloneModeConfirmationDialog
                    }
                }
                if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                    notificationPermissionState.launchPermissionRequest()
                    return@AloneModeConfirmationDialog
                }
                isAloneModeActive = true
                context.startService(Intent(context, AloneModeService::class.java))
            },
            onDismiss = { showConfirmationDialog = false }
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton("Share Location", Icons.AutoMirrored.Filled.Send, onShareLocation, Modifier.weight(1f))
        ActionButton(
            text = if (isAloneModeActive) "Exit Alone Mode" else "Alone Mode",
            icon = Icons.Default.Check,
            onClick = {
                if (isAloneModeActive) {
                    isAloneModeActive = false
                    context.stopService(Intent(context, AloneModeService::class.java))
                } else {
                    showConfirmationDialog = true
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AloneModeConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activate Alone Mode?") },
        text = { Text("\"\"\"When you activate Alone Mode:\n\n• Your location will be updated in Firebase every 5 seconds.\n• Every hour, you'll get a notification to confirm you're safe.\n• If you don't respond for 3 hours, your live location will be shared with your emergency contacts.\"\"\"") },
        confirmButton = { Button(onClick = onConfirm) { Text("Activate") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, text, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SafetyContactsSection(contacts: List<Contact>) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text("Safety Contacts", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp, start = 8.dp), fontWeight = FontWeight.Bold)
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Text("Add safety contacts in your profile.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                items(contacts) { contact ->
                    ContactItem(contact)
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    val initials = contact.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
    val backgroundColor = colors[abs(contact.name.hashCode()) % colors.size]

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(contact.name, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}