package com.rudra.smartworktracker.ui.screens.income

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Income
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class IncomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _income = MutableStateFlow(0.0)
    val income: StateFlow<Double> = _income.asStateFlow()
    private val _recentIncomes = MutableStateFlow<List<Income>>(emptyList())
    val recentIncomes: StateFlow<List<Income>> = _recentIncomes.asStateFlow()

    init {
        loadTotalIncome()
        loadRecentIncomes()
    }

    private fun loadTotalIncome() {
        viewModelScope.launch {
            db.incomeDao().getTotalIncome().collect { totalIncome ->
                _income.value = totalIncome ?: 0.0
            }
        }
    }
    private fun loadRecentIncomes() {
        viewModelScope.launch {
            db.incomeDao().getLatest5Incomes().collect { incomes ->
                _recentIncomes.value = incomes
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            db.incomeDao().deleteIncome(income)
        }
    }

    fun saveIncome(amount: Double, description: String, category: String, source: String, accountType: AccountType, timestamp: Long) {
        viewModelScope.launch {
            val newIncome = Income(
                amount = amount,
                description = description,
                category = category,
                timestamp = timestamp,
                source = source
            )
            db.incomeDao().insertIncome(newIncome)
        }
    }
}



