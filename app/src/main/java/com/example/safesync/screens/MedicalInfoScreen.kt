package com.example.safesync.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.safesync.screens.components.HeightInputField
import com.example.safesync.screens.components.MedicalInfoTextField
import com.example.safesync.viewmodels.MedicalInfoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoScreen(navController: NavController, medicalInfoViewModel: MedicalInfoViewModel) {
    val medicalInfo by medicalInfoViewModel.medicalInfo

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var bloodTypeExpanded by remember { mutableStateOf(false) }
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        medicalInfoViewModel.saveMedicalInfo()
                        navController.popBackStack()
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Date of Birth
            OutlinedTextField(
                value = medicalInfo.dateOfBirth,
                onValueChange = { },
                label = { Text("Date of Birth") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Date")
                    }
                }
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                medicalInfoViewModel.onFieldChange("Date of Birth", sdf.format(Date(it)))
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Blood Type
            Box {
                OutlinedTextField(
                    value = medicalInfo.bloodType,
                    onValueChange = {},
                    label = { Text("Blood Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { bloodTypeExpanded = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, "Select Blood Type", Modifier.clickable { bloodTypeExpanded = true })
                    }
                )
                DropdownMenu(
                    expanded = bloodTypeExpanded,
                    onDismissRequest = { bloodTypeExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bloodTypes.forEach { bloodType ->
                        DropdownMenuItem(
                            text = { Text(bloodType) },
                            onClick = {
                                medicalInfoViewModel.onFieldChange("Blood Type", bloodType)
                                bloodTypeExpanded = false
                            }
                        )
                    }
                }
            }

            HeightInputField(
                value = medicalInfo.height,
                onValueChange = { medicalInfoViewModel.onFieldChange("Height", it) }
            )

            MedicalInfoTextField(
                value = medicalInfo.weight,
                onValueChange = { medicalInfoViewModel.onFieldChange("Weight", it) },
                label = "Weight (kg)",
                keyboardType = KeyboardType.Number
            )

            MedicalInfoTextField(
                value = medicalInfo.allergies,
                onValueChange = { medicalInfoViewModel.onFieldChange("Allergies", it) },
                label = "Allergies"
            )

            MedicalInfoTextField(
                value = medicalInfo.pregnancyStatus,
                onValueChange = { medicalInfoViewModel.onFieldChange("Pregnancy Status", it) },
                label = "Pregnancy Status"
            )

            MedicalInfoTextField(
                value = medicalInfo.medications,
                onValueChange = { medicalInfoViewModel.onFieldChange("Medications", it) },
                label = "Medications"
            )

            MedicalInfoTextField(
                value = medicalInfo.address,
                onValueChange = { medicalInfoViewModel.onFieldChange("Address", it) },
                label = "Address"
            )

            MedicalInfoTextField(
                value = medicalInfo.medicalNotes,
                onValueChange = { medicalInfoViewModel.onFieldChange("Medical Notes", it) },
                label = "Medical Notes"
            )

            // Organ Donor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Organ Donor")
                Switch(
                    checked = medicalInfo.organDonor.equals("Yes", ignoreCase = true),
                    onCheckedChange = { isChecked ->
                        medicalInfoViewModel.onFieldChange("Organ Donor", if (isChecked) "Yes" else "No")
                    }
                )
            }
        }
    }
}
