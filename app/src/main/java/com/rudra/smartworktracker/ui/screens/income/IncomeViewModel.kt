package com.rudra.smartworktracker.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Income
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class IncomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _income = MutableStateFlow<Double>(0.0)
    val income: StateFlow<Double> = _income.asStateFlow()

    init {
        viewModelScope.launch {
            db.incomeDao().getLatestIncome().map { it?.amount ?: 0.0 }.collect {
                _income.value = it
            }
        }
    }

    fun saveIncome(amount: Double) {
        viewModelScope.launch {
            db.incomeDao().insert(Income(amount = amount))
        }
    }
}