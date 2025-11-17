package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.TransactionRepository

class FinancialStatementViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialStatementViewModel::class.java)) {
            val transactionDao = AppDatabase.getDatabase(application).financialTransactionDao()
            val transactionRepository = TransactionRepository(transactionDao)
            @Suppress("UNCHECKED_CAST")
            return FinancialStatementViewModel(transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
