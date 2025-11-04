package com.example.safesync.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.safesync.data.MedicalInfo
import com.example.safesync.data.MedicalInfoRepository

class MedicalInfoViewModel(private val repository: MedicalInfoRepository) : ViewModel() {

    var medicalInfo = mutableStateOf(MedicalInfo())
        private set

    init {
        loadMedicalInfo()
    }

    fun onFieldChange(field: String, value: String) {
        medicalInfo.value = when (field) {
            "Date of Birth" -> medicalInfo.value.copy(dateOfBirth = value)
            "Blood Type" -> medicalInfo.value.copy(bloodType = value)
            "Height" -> medicalInfo.value.copy(height = value)
            "Weight" -> medicalInfo.value.copy(weight = value)
            "Allergies" -> medicalInfo.value.copy(allergies = value)
            "Pregnancy Status" -> medicalInfo.value.copy(pregnancyStatus = value)
            "Medications" -> medicalInfo.value.copy(medications = value)
            "Address" -> medicalInfo.value.copy(address = value)
            "Medical Notes" -> medicalInfo.value.copy(medicalNotes = value)
            "Organ Donor" -> medicalInfo.value.copy(organDonor = value)
            else -> medicalInfo.value
        }
    }

    fun saveMedicalInfo() {
        repository.saveMedicalInfo(medicalInfo.value)
    }

    private fun loadMedicalInfo() {
        repository.loadMedicalInfo { loadedInfo ->
            if (loadedInfo != null) {
                medicalInfo.value = loadedInfo
            }
        }
    }
}
