package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.LoanRepository

class LoanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val loanDao = database.loanDao()
            val transactionDao = database.financialTransactionDao()
            val accountDao = database.accountDao()
            val loanRepository = LoanRepository(loanDao, transactionDao, accountDao)
            @Suppress("UNCHECKED_CAST")
            return LoanViewModel(loanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
