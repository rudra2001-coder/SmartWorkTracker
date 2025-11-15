package com.rudra.smartworktracker.ui.screens.calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _calculation = MutableStateFlow<Calculation?>(null)
    val calculation: StateFlow<Calculation?> = _calculation.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _mealCostPerWeek = MutableStateFlow(0.0)
    val mealCostPerWeek: StateFlow<Double> = _mealCostPerWeek.asStateFlow()

    private val _mealCostPerMonth = MutableStateFlow(0.0)
    val mealCostPerMonth: StateFlow<Double> = _mealCostPerMonth.asStateFlow()

    private val _mealCostPerYear = MutableStateFlow(0.0)
    val mealCostPerYear: StateFlow<Double> = _mealCostPerYear.asStateFlow()

    private val _officeDays = MutableStateFlow(0)
    val officeDays: StateFlow<Int> = _officeDays.asStateFlow()

    private val _homeOfficeDays = MutableStateFlow(0)
    val homeOfficeDays: StateFlow<Int> = _homeOfficeDays.asStateFlow()

    init {
        viewModelScope.launch {
            db.calculationDao().getCalculation().collectLatest { calc ->
                val currentCalc = calc ?: Calculation()
                _calculation.value = currentCalc
                fetchWorkLogData(currentCalc.dailyMealRate, _selectedDate.value)
            }
        }
    }

    private suspend fun fetchWorkLogData(dailyMealRate: Double, date: Date) {
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val selectedMonthYear = monthYearFormat.format(date)

        val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
        val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
        val homeOfficeDaysCount = workLogs.count { it.workType == WorkType.HOME_OFFICE }

        _officeDays.value = officeDaysCount
        _homeOfficeDays.value = homeOfficeDaysCount

        calculateMealCosts(dailyMealRate, officeDaysCount)
    }

    private fun calculateMealCosts(dailyMealRate: Double, officeDays: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val weeksInMonth = daysInMonth / 7.0

        val weeklyOfficeDays = officeDays / weeksInMonth
        _mealCostPerWeek.value = dailyMealRate * weeklyOfficeDays
        _mealCostPerMonth.value = dailyMealRate * officeDays
        _mealCostPerYear.value = _mealCostPerMonth.value * 12
    }

    fun saveDailyMealRate(rate: Double) {
        viewModelScope.launch {
            val currentCalculation = _calculation.value ?: Calculation()
            val updatedCalculation = currentCalculation.copy(dailyMealRate = rate)
            db.calculationDao().insert(updatedCalculation)
            // Refetch data after saving new rate
            fetchWorkLogData(rate, _selectedDate.value)
        }
    }

    fun goToPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, -1)
        _selectedDate.value = calendar.time
        viewModelScope.launch {
            fetchWorkLogData(_calculation.value?.dailyMealRate ?: 60.0, _selectedDate.value)
        }
    }

    fun goToNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, 1)
        _selectedDate.value = calendar.time
        viewModelScope.launch {
            fetchWorkLogData(_calculation.value?.dailyMealRate ?: 60.0, _selectedDate.value)
        }
    }
}
