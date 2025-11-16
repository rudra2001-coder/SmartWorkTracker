package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LoanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoanViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
