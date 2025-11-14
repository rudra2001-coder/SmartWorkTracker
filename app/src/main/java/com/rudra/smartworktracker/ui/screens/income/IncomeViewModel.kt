package com.rudra.smartworktracker.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Income
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
//            _income.value = latestIncome?.amount ?: 0.0
        }
    }

    // Simplified version for basic income saving
    fun saveIncome(amount: Double) {
        viewModelScope.launch {
            val newIncome = Income(
                amount = amount,
                description = "Monthly Income",
                category = "Salary",
                timestamp = System.currentTimeMillis(),
                source = "Primary Job"
            )
            db.incomeDao().insertIncome(newIncome)
            _income.value = amount // Update the state immediately
        }
    }

    // Optional: Full version with all parameters
    fun saveIncome(amount: Double, description: String, category: String, source: String) {
        viewModelScope.launch {
            val newIncome = Income(
                amount = amount,
                description = description,
                category = category,
                timestamp = System.currentTimeMillis(),
                source = source
            )
            db.incomeDao().insertIncome(newIncome)
            _income.value = amount
        }
    }
}