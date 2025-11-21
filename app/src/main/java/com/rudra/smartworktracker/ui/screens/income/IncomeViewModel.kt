package com.rudra.smartworktracker.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _income = MutableStateFlow<Double>(0.0)
    val income: StateFlow<Double> = _income.asStateFlow()

    init {
        loadLatestIncome()
    }

    private fun loadLatestIncome() {
        viewModelScope.launch {
            val latestIncome = db.incomeDao().getLatestIncome()
            //           _income.value = latestIncome?.amount ?: 0.0
        }
    }

    // Simplified version for basic income saving
    fun saveIncome(amount: Double) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val newIncome = Income(
                amount = amount,
                description = "Monthly Income",
                category = "Salary",
                timestamp = timestamp,
                source = "Primary Job"
            )
            db.incomeDao().insertIncome(newIncome)
            _income.value = amount // Update the state immediately

            // Create and save the corresponding financial transaction
            val transaction = FinancialTransaction(
                type = TransactionType.INCOME,
                amount = amount,
                source = AccountType.BALANCE, // Or determine dynamically
                destination = null,
                note = "Monthly Income - Salary",
                date = timestamp
            )
            db.financialTransactionDao().insertTransaction(transaction)
        }
    }

    // Optional: Full version with all parameters
    fun saveIncome(amount: Double, description: String, category: String, source: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val newIncome = Income(
                amount = amount,
                description = description,
                category = category,
                timestamp = timestamp,
                source = source
            )
            db.incomeDao().insertIncome(newIncome)
            _income.value = amount

            // Create and save the corresponding financial transaction
            val transaction = FinancialTransaction(
                type = TransactionType.INCOME,
                amount = amount,
                source = AccountType.BALANCE, // Or determine dynamically
                destination = null,
                note = "$description - $category",
                date = timestamp
            )
            db.financialTransactionDao().insertTransaction(transaction)
        }
    }
}
