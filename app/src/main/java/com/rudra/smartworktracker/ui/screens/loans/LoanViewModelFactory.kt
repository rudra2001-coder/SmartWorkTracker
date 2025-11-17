package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.LoanRepository

class LoanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
            val loanDao = AppDatabase.getDatabase(application).loanDao()
            val transactionDao = AppDatabase.getDatabase(application).financialTransactionDao()
            val loanRepository = LoanRepository(loanDao, transactionDao)
            @Suppress("UNCHECKED_CAST")
            return LoanViewModel(loanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
