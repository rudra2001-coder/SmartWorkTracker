package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.AccountRepository
import com.rudra.smartworktracker.data.repository.LoanRepository

class LoanViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoanViewModel::class.java)) {
            val db = AppDatabase.getDatabase(application)
            val loanDao = db.loanDao()
            val transactionDao = db.financialTransactionDao()
            val accountDao = db.accountDao()
            val accountRepository = AccountRepository(accountDao)
            val loanRepository = LoanRepository(loanDao, transactionDao, accountRepository)
            @Suppress("UNCHECKED_CAST")
            return LoanViewModel(loanRepository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
