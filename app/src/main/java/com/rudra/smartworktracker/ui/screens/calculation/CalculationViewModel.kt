package com.rudra.smartworktracker.ui.screens.calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _calculation = MutableStateFlow<Calculation?>(null)
    val calculation: StateFlow<Calculation?> = _calculation.asStateFlow()

    init {
        viewModelScope.launch {
            db.calculationDao().getCalculation().collect {
                _calculation.value = it
            }
        }
    }

    fun saveCalculation(mealRate: Double, overtimeRate: Double) {
        viewModelScope.launch {
            db.calculationDao().insert(Calculation(mealRate = mealRate, overtimeRate = overtimeRate))
        }
    }
}
