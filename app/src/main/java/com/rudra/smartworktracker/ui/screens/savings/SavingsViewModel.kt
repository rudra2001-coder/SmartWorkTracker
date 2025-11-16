package com.rudra.smartworktracker.ui.screens.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val financialTransactionDao = db.financialTransactionDao()

    private val _savings = MutableStateFlow(0.0)
    val savings: StateFlow<Double> = _savings.asStateFlow()

    fun addToSavings(amount: Double) {
        viewModelScope.launch {
            _savings.value += amount

            val transaction = FinancialTransaction(
                type = TransactionType.SAVINGS_ADD,
                amount = amount,
                source = AccountType.BALANCE,
                destination = AccountType.SAVINGS,
                note = "Added to savings",
                date = System.currentTimeMillis()
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }

    fun withdrawFromSavings(amount: Double) {
        viewModelScope.launch {
            _savings.value -= amount

            val transaction = FinancialTransaction(
                type = TransactionType.SAVINGS_WITHDRAW,
                amount = amount,
                source = AccountType.SAVINGS,
                destination = AccountType.BALANCE,
                note = "Withdrew from savings",
                date = System.currentTimeMillis()
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }
}
