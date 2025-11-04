package com.example.safesync.screens.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.safesync.data.Contact
import com.example.safesync.viewmodels.MedicalInfoViewModel

@Composable
fun PhotoOptionsDialog(
    onDismissRequest: () -> Unit,
    onChangePhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profile Photo", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Change Photo", modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangePhoto() }
                    .padding(vertical = 12.dp))
                Divider()
                Text("Remove Photo", modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRemovePhoto() }
                    .padding(vertical = 12.dp), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ProfileHeader(userName: String, imageUri: Uri?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "View and edit your profile",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SafetySetupChecklist(
    contacts: List<Contact>,
    medicalInfoViewModel: MedicalInfoViewModel,
    hasLocationPermission: Boolean,
    onLocationPermissionClick: () -> Unit,
    onAddContactClick: () -> Unit,
    onGoToMedicalInfo: () -> Unit
) {
    val medicalInfo by medicalInfoViewModel.medicalInfo
    val isMedicalInfoComplete = medicalInfo.dateOfBirth.isNotBlank() ||
            medicalInfo.bloodType.isNotBlank() ||
            medicalInfo.height.isNotBlank() ||
            medicalInfo.weight.isNotBlank() ||
            medicalInfo.allergies.isNotBlank() ||
            medicalInfo.pregnancyStatus.isNotBlank() ||
            medicalInfo.medications.isNotBlank() ||
            medicalInfo.address.isNotBlank() ||
            medicalInfo.medicalNotes.isNotBlank() ||
            medicalInfo.organDonor.isNotBlank()

    val completedTasks = listOf(contacts.isNotEmpty(), hasLocationPermission, isMedicalInfoComplete).count { it }
    val totalTasks = 3
    val progress = completedTasks.toFloat() / totalTasks.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.size(60.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Safety Setup Checklist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ChecklistItem("Emergency Contacts Added (${contacts.size}/10)", contacts.isNotEmpty(), onAddContactClick)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistItem("Location Permissions Granted", hasLocationPermission, onLocationPermissionClick)
            Spacer(modifier = Modifier.height(8.dp))
            ChecklistItem("Medical Info Completed", isMedicalInfoComplete, onGoToMedicalInfo)
        }
    }
}

@Composable
fun ChecklistItem(text: String, isCompleted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 16.sp, color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = if (isCompleted) "Completed" else "Incomplete",
            tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun GeneralSettings(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "General Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(
                icon = Icons.Default.Edit,
                text = "Edit Profile",
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                onClick = { navController.navigate("edit_profile") })
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.ManageAccounts,
                text = "Manage Safety Contacts",
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                onClick = { navController.navigate("manage_contacts") })
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.Default.Lock,
                text = "Privacy & Security",
                iconBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                onClick = { navController.navigate("privacy_security") })
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                text = "Help and Support",
                iconBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                onClick = { navController.navigate("help") })
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    iconBackgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
