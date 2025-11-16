package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FinancialStatementViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialStatementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinancialStatementViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
