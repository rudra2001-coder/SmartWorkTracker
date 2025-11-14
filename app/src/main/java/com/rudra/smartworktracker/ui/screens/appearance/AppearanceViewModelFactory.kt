package com.rudra.smartworktracker.ui.screens.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppearanceViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppearanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppearanceViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}