package com.rudra.smartworktracker.ui.screens.calculation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.System
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Long

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _calculation = MutableStateFlow<Calculation?>(null)
    val calculation: StateFlow<Calculation?> = _calculation.asStateFlow()

    private val _travelExpense = MutableStateFlow<TravelAndExpense?>(null)
    val travelExpense: StateFlow<TravelAndExpense?> = _travelExpense.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Meal costs
    private val _mealCostPerWeek = MutableStateFlow(0.0)
    val mealCostPerWeek: StateFlow<Double> = _mealCostPerWeek.asStateFlow()

    private val _mealCostPerMonth = MutableStateFlow(0.0)
    val mealCostPerMonth: StateFlow<Double> = _mealCostPerMonth.asStateFlow()

    private val _mealCostPerYear = MutableStateFlow(0.0)
    val mealCostPerYear: StateFlow<Double> = _mealCostPerYear.asStateFlow()

    // Travel costs
    private val _travelCostPerWeek = MutableStateFlow(0.0)
    val travelCostPerWeek: StateFlow<Double> = _travelCostPerWeek.asStateFlow()

    private val _travelCostPerMonth = MutableStateFlow(0.0)
    val travelCostPerMonth: StateFlow<Double> = _travelCostPerMonth.asStateFlow()

    private val _travelCostPerYear = MutableStateFlow(0.0)
    val travelCostPerYear: StateFlow<Double> = _travelCostPerYear.asStateFlow()

    // Other expenses
    private val _otherExpensePerMonth = MutableStateFlow(0.0)
    val otherExpensePerMonth: StateFlow<Double> = _otherExpensePerMonth.asStateFlow()

    private val _otherExpensePerYear = MutableStateFlow(0.0)
    val otherExpensePerYear: StateFlow<Double> = _otherExpensePerYear.asStateFlow()

    // Totals
    private val _totalExpensePerMonth = MutableStateFlow(0.0)
    val totalExpensePerMonth: StateFlow<Double> = _totalExpensePerMonth.asStateFlow()

    private val _totalExpensePerYear = MutableStateFlow(0.0)
    val totalExpensePerYear: StateFlow<Double> = _totalExpensePerYear.asStateFlow()

    // Office days
    private val _officeDays = MutableStateFlow(0)
    val officeDays: StateFlow<Int> = _officeDays.asStateFlow()

    private val _homeOfficeDays = MutableStateFlow(0)
    val homeOfficeDays: StateFlow<Int> = _homeOfficeDays.asStateFlow()

    // Pie chart data
    private val _pieChartData = MutableStateFlow<Map<String, Float>>(emptyMap())
    val pieChartData: StateFlow<Map<String, Float>> = _pieChartData.asStateFlow()

    // Error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Monthly breakdown
    private val _monthlyBreakdown = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val monthlyBreakdown: StateFlow<List<Pair<String, Double>>> = _monthlyBreakdown.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Collect both calculation and travel expense
                combine(
                    db.calculationDao().getCalculation(),
                    db.travelExpenseDao().getTravelExpense()
                ) { calc, travelExpense ->
                    Pair(calc, travelExpense)
                }.collectLatest { (calc, travelExp) ->
                    val currentCalc = calc ?: Calculation(
                        dailyMealRate = 58.0, // Default value
                        lastUpdated = System.currentTimeMillis()
                    )
                    val currentTravelExp = travelExp ?: TravelAndExpense(
                        dailyTravelCost = 150.0, // Default value
                        otherExpenses = 0.0,
                        otherExpenseDescription = "",
                        lastUpdated = System.currentTimeMillis()
                    )

                    _calculation.value = currentCalc
                    _travelExpense.value = currentTravelExp

                    fetchWorkLogData(currentCalc.dailyMealRate, currentTravelExp, _selectedDate.value)
                    fetchMonthlyBreakdown(currentCalc.dailyMealRate, currentTravelExp)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchWorkLogData(
        dailyMealRate: Double,
        travelExpense: TravelAndExpense,
        date: Date
    ) {
        try {
            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val selectedMonthYear = monthYearFormat.format(date)

            val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
            val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
            val homeOfficeDaysCount = workLogs.count { it.workType == WorkType.HOME_OFFICE }

            _officeDays.value = officeDaysCount
            _homeOfficeDays.value = homeOfficeDaysCount

            // Update pie chart data
            _pieChartData.value = mapOf(
                "Office" to officeDaysCount.toFloat(),
                "Home Office" to homeOfficeDaysCount.toFloat()
            )

            calculateAllCosts(dailyMealRate, travelExpense, officeDaysCount)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to fetch work logs: ${e.message}"
        }
    }

    private suspend fun fetchMonthlyBreakdown(
        dailyMealRate: Double,
        travelExpense: TravelAndExpense
    ) {
        try {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val monthlyData = mutableListOf<Pair<String, Double>>()
            
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            
            for (month in 0..11) {
                calendar.set(currentYear, month, 1)
                val monthName = monthFormat.format(calendar.time)
                
                // Fetch work logs for this month
                val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val selectedMonthYear = yearMonthFormat.format(calendar.time)
                val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
                val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
                
                val monthlyCost = (dailyMealRate + travelExpense.dailyTravelCost) * officeDaysCount + travelExpense.otherExpenses
                monthlyData.add(monthName to monthlyCost)
            }
            
            _monthlyBreakdown.value = monthlyData
        } catch (e: Exception) {
            _errorMessage.value = "Failed to fetch monthly breakdown: ${e.message}"
        }
    }

    private fun calculateAllCosts(
        dailyMealRate: Double,
        travelExpense: TravelAndExpense,
        officeDays: Int
    ) {
        try {
            val calendar = Calendar.getInstance()
            calendar.time = _selectedDate.value
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val weeksInMonth = if (daysInMonth > 0) daysInMonth / 7.0 else 4.33
            
            val weeklyOfficeDays = if (weeksInMonth > 0) officeDays / weeksInMonth else 0.0

            // Meal Costs
            _mealCostPerWeek.value = dailyMealRate * weeklyOfficeDays
            _mealCostPerMonth.value = dailyMealRate * officeDays
            _mealCostPerYear.value = _mealCostPerMonth.value * 12

            // Travel Costs
            _travelCostPerWeek.value = travelExpense.dailyTravelCost * weeklyOfficeDays
            _travelCostPerMonth.value = travelExpense.dailyTravelCost * officeDays
            _travelCostPerYear.value = _travelCostPerMonth.value * 12

            // Other Expenses (fixed monthly)
            _otherExpensePerMonth.value = travelExpense.otherExpenses
            _otherExpensePerYear.value = travelExpense.otherExpenses * 12

            // Total Expenses
            _totalExpensePerMonth.value = _mealCostPerMonth.value + _travelCostPerMonth.value + _otherExpensePerMonth.value
            _totalExpensePerYear.value = _mealCostPerYear.value + _travelCostPerYear.value + _otherExpensePerYear.value
        } catch (e: Exception) {
            _errorMessage.value = "Calculation error: ${e.message}"
        }
    }

    fun saveDailyMealRate(rate: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentCalculation = _calculation.value ?: Calculation(
                    dailyMealRate = 58.0, // Default value
                    lastUpdated = System.currentTimeMillis()
                )
                val updatedCalculation = currentCalculation.copy(
                    dailyMealRate = rate,
                    lastUpdated = System.currentTimeMillis()
                )
                db.calculationDao().insert(updatedCalculation)
                _calculation.value = updatedCalculation
                
                // Refetch data after saving
                fetchWorkLogData(rate, _travelExpense.value ?: TravelAndExpense(), _selectedDate.value)
                fetchMonthlyBreakdown(rate, _travelExpense.value ?: TravelAndExpense())
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save meal rate: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveTravelExpense(dailyTravelCost: Double, otherExpenses: Double, description: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentExpense = _travelExpense.value ?: TravelAndExpense()
                val updatedExpense = currentExpense.copy(
                    dailyTravelCost = dailyTravelCost,
                    otherExpenses = otherExpenses,
                    otherExpenseDescription = description,
                    lastUpdated = System.currentTimeMillis()
                )
                db.travelExpenseDao().insert(updatedExpense)
                _travelExpense.value = updatedExpense
                
                // Refetch data after saving
                fetchWorkLogData(_calculation.value?.dailyMealRate ?: 100.0, updatedExpense, _selectedDate.value)
                fetchMonthlyBreakdown(_calculation.value?.dailyMealRate ?: 100.0, updatedExpense)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save travel expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun goToPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, -1)
        _selectedDate.value = calendar.time
        refreshData()
    }

    fun goToNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, 1)
        _selectedDate.value = calendar.time
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchWorkLogData(
                    _calculation.value?.dailyMealRate ?: 100.0,
                    _travelExpense.value ?: TravelAndExpense(),
                    _selectedDate.value
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun exportToExcel(context: Context) {
        // Placeholder for export functionality
        Toast.makeText(context, "Exporting to Excel...", Toast.LENGTH_SHORT).show()
    }
}
