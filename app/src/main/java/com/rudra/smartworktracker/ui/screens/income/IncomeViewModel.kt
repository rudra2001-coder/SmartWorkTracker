package com.rudra.smartworktracker.ui.screens.income

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class IncomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _income = MutableStateFlow(0.0)
    val income: StateFlow<Double> = _income.asStateFlow()

    init {
        loadTotalIncome()
    }

    private fun loadTotalIncome() {
        viewModelScope.launch {
            db.incomeDao().getTotalIncome().collect { totalIncome ->
                _income.value = totalIncome ?: 0.0
            }
        }
    }

    fun saveIncome(amount: Double, description: String, category: String, source: String, timestamp: Long) {
        viewModelScope.launch {
            val newIncome = Income(
                amount = amount,
                description = description,
                category = category,
                timestamp = timestamp,
                source = source
            )
            db.incomeDao().insertIncome(newIncome)

            val transaction = FinancialTransaction(
                type = TransactionType.INCOME,
                amount = amount,
                source = AccountType.BALANCE,
                destination = null,
                note = "$description - $category",
                date = timestamp
            )
            db.financialTransactionDao().insertTransaction(transaction)
        }
    }
}



