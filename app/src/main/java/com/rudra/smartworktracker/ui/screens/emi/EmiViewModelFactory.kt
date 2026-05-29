package com.rudra.smartworktracker.ui.screens.emi

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.EmiRepository

class EmiViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmiViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val emiRepository = EmiRepository(
                database.emiDao(),
                database.loanDao(),
                database.financialTransactionDao(),
                database.accountDao()
            )
            @Suppress("UNCHECKED_CAST")
            return EmiViewModel(emiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
