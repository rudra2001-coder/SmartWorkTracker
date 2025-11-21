package com.rudra.smartworktracker.ui.screens.emi

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmiViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
