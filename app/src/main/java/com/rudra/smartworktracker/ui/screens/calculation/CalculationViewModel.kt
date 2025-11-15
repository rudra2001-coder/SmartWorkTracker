package com.rudra.smartworktracker.ui.screens.calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _calculation = MutableStateFlow<Calculation?>(null)
    val calculation: StateFlow<Calculation?> = _calculation.asStateFlow()

    private val _mealRatePerDay = MutableStateFlow(0.0)
    val mealRatePerDay: StateFlow<Double> = _mealRatePerDay.asStateFlow()

    private val _mealCostPerWeek = MutableStateFlow(0.0)
    val mealCostPerWeek: StateFlow<Double> = _mealCostPerWeek.asStateFlow()

    private val _mealCostPerMonth = MutableStateFlow(0.0)
    val mealCostPerMonth: StateFlow<Double> = _mealCostPerMonth.asStateFlow()

    private val _mealCostPerYear = MutableStateFlow(0.0)
    val mealCostPerYear: StateFlow<Double> = _mealCostPerYear.asStateFlow()

    private val _totalOfficeDays = MutableStateFlow(0)
    val totalOfficeDays: StateFlow<Int> = _totalOfficeDays.asStateFlow()

    init {
        viewModelScope.launch {
            db.calculationDao().getCalculation().collectLatest { calc ->
                _calculation.value = calc
                calculateValues(calc)
            }
        }
    }

    private fun calculateValues(calculation: Calculation?) {
        calculation?.let {
            val workingDaysInOffice = it.totalWorkingDays - it.homeOfficeDays
            _totalOfficeDays.value = workingDaysInOffice

            if (it.totalWorkingDays > 0) {
                val dailyMealRate = it.mealCost / it.totalWorkingDays
                _mealRatePerDay.value = dailyMealRate
                _mealCostPerWeek.value = dailyMealRate * (workingDaysInOffice / 4.0) // Assuming 4 weeks in a month
                _mealCostPerMonth.value = dailyMealRate * workingDaysInOffice
                _mealCostPerYear.value = (dailyMealRate * workingDaysInOffice) * 12
            } else {
                _mealRatePerDay.value = 0.0
                _mealCostPerWeek.value = 0.0
                _mealCostPerMonth.value = 0.0
                _mealCostPerYear.value = 0.0
            }
        }
    }

    fun saveCalculation(
        mealRate: Double,
        overtimeRate: Double,
        mealCost: Double,
        totalWorkingDays: Int,
        homeOfficeDays: Int
    ) {
        viewModelScope.launch {
            val newCalculation = Calculation(
                mealRate = mealRate,
                overtimeRate = overtimeRate,
                mealCost = mealCost,
                totalWorkingDays = totalWorkingDays,
                homeOfficeDays = homeOfficeDays
            )
            db.calculationDao().insert(newCalculation)
        }
    }
}
