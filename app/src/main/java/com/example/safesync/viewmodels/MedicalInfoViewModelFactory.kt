package com.example.safesync.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.safesync.data.MedicalInfoRepository

class MedicalInfoViewModelFactory(private val repository: MedicalInfoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicalInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicalInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
