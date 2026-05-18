package com.rudra.smartworktracker.ui.screens.calculation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import com.rudra.smartworktracker.data.entity.MealRateSetting
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class CalcMode { AUTO, DATE_RANGE, WEEKLY }

data class MealRateEntry(
    val id: Long = 0,
    val label: String = "",
    val rate: Double = 0.0
)

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _calculation = MutableStateFlow<Calculation?>(null)
    val calculation: StateFlow<Calculation?> = _calculation.asStateFlow()

    private val _travelExpense = MutableStateFlow<TravelAndExpense?>(null)
    val travelExpense: StateFlow<TravelAndExpense?> = _travelExpense.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _calcMode = MutableStateFlow(CalcMode.AUTO)
    val calcMode: StateFlow<CalcMode> = _calcMode.asStateFlow()

    private val _startDate = MutableStateFlow(Date())
    val startDate: StateFlow<Date> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(Date())
    val endDate: StateFlow<Date> = _endDate.asStateFlow()

    private val _selectedWeekDays = MutableStateFlow(setOf<Int>())
    val selectedWeekDays: StateFlow<Set<Int>> = _selectedWeekDays.asStateFlow()

    private val _mealRateEntries = MutableStateFlow<List<MealRateEntry>>(emptyList())
    val mealRateEntries: StateFlow<List<MealRateEntry>> = _mealRateEntries.asStateFlow()

    private val _totalMealRate = MutableStateFlow(0.0)
    val totalMealRate: StateFlow<Double> = _totalMealRate.asStateFlow()

    private val _customMonthlyCost = MutableStateFlow(0.0)
    val customMonthlyCost: StateFlow<Double> = _customMonthlyCost.asStateFlow()

    private val _customSixMonthCost = MutableStateFlow(0.0)
    val customSixMonthCost: StateFlow<Double> = _customSixMonthCost.asStateFlow()

    private val _customYearlyCost = MutableStateFlow(0.0)
    val customYearlyCost: StateFlow<Double> = _customYearlyCost.asStateFlow()

    private val _customDaysCount = MutableStateFlow(0)
    val customDaysCount: StateFlow<Int> = _customDaysCount.asStateFlow()

    // Existing state
    private val _mealCostPerWeek = MutableStateFlow(0.0)
    val mealCostPerWeek: StateFlow<Double> = _mealCostPerWeek.asStateFlow()
    private val _mealCostPerMonth = MutableStateFlow(0.0)
    val mealCostPerMonth: StateFlow<Double> = _mealCostPerMonth.asStateFlow()
    private val _mealCostPerYear = MutableStateFlow(0.0)
    val mealCostPerYear: StateFlow<Double> = _mealCostPerYear.asStateFlow()
    private val _travelCostPerWeek = MutableStateFlow(0.0)
    val travelCostPerWeek: StateFlow<Double> = _travelCostPerWeek.asStateFlow()
    private val _travelCostPerMonth = MutableStateFlow(0.0)
    val travelCostPerMonth: StateFlow<Double> = _travelCostPerMonth.asStateFlow()
    private val _travelCostPerYear = MutableStateFlow(0.0)
    val travelCostPerYear: StateFlow<Double> = _travelCostPerYear.asStateFlow()
    private val _otherExpensePerMonth = MutableStateFlow(0.0)
    val otherExpensePerMonth: StateFlow<Double> = _otherExpensePerMonth.asStateFlow()
    private val _otherExpensePerYear = MutableStateFlow(0.0)
    val otherExpensePerYear: StateFlow<Double> = _otherExpensePerYear.asStateFlow()
    private val _totalExpensePerMonth = MutableStateFlow(0.0)
    val totalExpensePerMonth: StateFlow<Double> = _totalExpensePerMonth.asStateFlow()
    private val _totalExpensePerYear = MutableStateFlow(0.0)
    val totalExpensePerYear: StateFlow<Double> = _totalExpensePerYear.asStateFlow()
    private val _officeDays = MutableStateFlow(0)
    val officeDays: StateFlow<Int> = _officeDays.asStateFlow()
    private val _homeOfficeDays = MutableStateFlow(0)
    val homeOfficeDays: StateFlow<Int> = _homeOfficeDays.asStateFlow()
    private val _pieChartData = MutableStateFlow<Map<String, Float>>(emptyMap())
    val pieChartData: StateFlow<Map<String, Float>> = _pieChartData.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _monthlyBreakdown = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val monthlyBreakdown: StateFlow<List<Pair<String, Double>>> = _monthlyBreakdown.asStateFlow()

    init {
        loadData()
        loadMealRateSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                combine(
                    db.calculationDao().getCalculation(),
                    db.travelExpenseDao().getTravelExpense()
                ) { calc, travelExpense -> Pair(calc, travelExpense) }
                    .collectLatest { (calc, travelExp) ->
                        val currentCalc = calc ?: Calculation(
                            dailyMealRate = 0.0,
                            lastUpdated = System.currentTimeMillis()
                        )
                        val currentTravelExp = travelExp ?: TravelAndExpense(
                            dailyTravelCost = 0.0, otherExpenses = 0.0,
                            otherExpenseDescription = "",
                            lastUpdated = System.currentTimeMillis()
                        )
                        _calculation.value = currentCalc
                        _travelExpense.value = currentTravelExp
                        updateTotalMealRate()
                        refreshCalculations()
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadMealRateSettings() {
        viewModelScope.launch {
            db.mealRateSettingDao().getAllMealRateSettings().collect { settings ->
                _mealRateEntries.value = settings.map { MealRateEntry(it.id, it.label, it.rate) }
                updateTotalMealRate()
                recalculateCustom()
            }
        }
    }

    private fun updateTotalMealRate() {
        val baseRate = _calculation.value?.dailyMealRate ?: 0.0
        val extraRate = _mealRateEntries.value.sumOf { it.rate }
        _totalMealRate.value = baseRate + extraRate
    }

    private suspend fun refreshCalculations() {
        fetchWorkLogData()
        fetchMonthlyBreakdown()
        recalculateCustom()
    }

    fun saveAllSettings(dailyMealRate: Double, dailyTravelCost: Double, otherExpenses: Double, otherExpenseDescription: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Save Meal Rate
                val currentCalc = _calculation.value ?: Calculation(id = "1", lastUpdated = System.currentTimeMillis())
                val updatedCalc = currentCalc.copy(dailyMealRate = dailyMealRate, lastUpdated = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
                db.calculationDao().insert(updatedCalc)
                _calculation.value = updatedCalc

                // Save Travel Expense
                val currentTravel = _travelExpense.value ?: TravelAndExpense()
                val updatedTravel = currentTravel.copy(
                    dailyTravelCost = dailyTravelCost,
                    otherExpenses = otherExpenses,
                    otherExpenseDescription = otherExpenseDescription,
                    lastUpdated = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                db.travelExpenseDao().insert(updatedTravel)
                _travelExpense.value = updatedTravel

                updateTotalMealRate()
                refreshCalculations()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addMealRateEntry(label: String, rate: Double) {
        viewModelScope.launch {
            db.mealRateSettingDao().insert(
                MealRateSetting(label = label, rate = rate)
            )
        }
    }

    fun removeMealRateEntry(id: Long) {
        viewModelScope.launch {
            db.mealRateSettingDao().deleteById(id)
        }
    }

    fun setCalcMode(mode: CalcMode) {
        _calcMode.value = mode
        recalculateCustom()
    }

    fun setStartDate(date: Date) {
        _startDate.value = date
        recalculateCustom()
    }

    fun setEndDate(date: Date) {
        _endDate.value = date
        recalculateCustom()
    }

    fun toggleWeekDay(day: Int) {
        val current = _selectedWeekDays.value.toMutableSet()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _selectedWeekDays.value = current
        recalculateCustom()
    }

    private suspend fun fetchWorkLogData() {
        try {
            val date = _selectedDate.value
            val totalMealRate = _totalMealRate.value
            val travelExpense = _travelExpense.value ?: TravelAndExpense()

            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val selectedMonthYear = monthYearFormat.format(date)
            val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
            val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
            val homeOfficeDaysCount = workLogs.count { it.workType == WorkType.HOME_OFFICE }
            
            _officeDays.value = officeDaysCount
            _homeOfficeDays.value = homeOfficeDaysCount
            _pieChartData.value = mapOf("Office" to officeDaysCount.toFloat(), "Home Office" to homeOfficeDaysCount.toFloat())
            
            calculateAllCosts(totalMealRate, travelExpense, officeDaysCount)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to fetch work logs: ${e.message}"
        }
    }

    private suspend fun fetchMonthlyBreakdown() {
        try {
            val totalMealRate = _totalMealRate.value
            val travelExpense = _travelExpense.value ?: TravelAndExpense()
            
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val monthlyData = mutableListOf<Pair<String, Double>>()
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            
            for (month in 0..11) {
                calendar.set(currentYear, month, 1)
                val monthName = monthFormat.format(calendar.time)
                val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                val selectedMonthYear = yearMonthFormat.format(calendar.time)
                val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
                val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
                val monthlyCost = (totalMealRate + travelExpense.dailyTravelCost) * officeDaysCount + travelExpense.otherExpenses
                monthlyData.add(monthName to monthlyCost)
            }
            _monthlyBreakdown.value = monthlyData
        } catch (e: Exception) {
            _errorMessage.value = "Failed to fetch monthly breakdown: ${e.message}"
        }
    }

    private fun calculateAllCosts(totalMealRate: Double, travelExpense: TravelAndExpense, officeDays: Int) {
        try {
            val calendar = Calendar.getInstance()
            calendar.time = _selectedDate.value
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val weeksInMonth = if (daysInMonth > 0) daysInMonth / 7.0 else 4.33
            val weeklyOfficeDays = if (weeksInMonth > 0) officeDays / weeksInMonth else 0.0
            
            _mealCostPerWeek.value = totalMealRate * weeklyOfficeDays
            _mealCostPerMonth.value = totalMealRate * officeDays
            _mealCostPerYear.value = _mealCostPerMonth.value * 12
            
            _travelCostPerWeek.value = travelExpense.dailyTravelCost * weeklyOfficeDays
            _travelCostPerMonth.value = travelExpense.dailyTravelCost * officeDays
            _travelCostPerYear.value = _travelCostPerMonth.value * 12
            
            _otherExpensePerMonth.value = travelExpense.otherExpenses
            _otherExpensePerYear.value = travelExpense.otherExpenses * 12
            
            _totalExpensePerMonth.value = _mealCostPerMonth.value + _travelCostPerMonth.value + _otherExpensePerMonth.value
            _totalExpensePerYear.value = _mealCostPerYear.value + _travelCostPerYear.value + _otherExpensePerYear.value
        } catch (e: Exception) {
            _errorMessage.value = "Calculation error: ${e.message}"
        }
    }

    private fun recalculateCustom() {
        val totalRate = _totalMealRate.value
        if (totalRate <= 0) {
            _customMonthlyCost.value = 0.0
            _customSixMonthCost.value = 0.0
            _customYearlyCost.value = 0.0
            _customDaysCount.value = 0
            return
        }
        when (_calcMode.value) {
            CalcMode.DATE_RANGE -> {
                val diff = _endDate.value.time - _startDate.value.time
                val days = (diff / (1000L * 60 * 60 * 24)).toInt() + 1
                if (days <= 0) {
                    _customDaysCount.value = 0; _customMonthlyCost.value = 0.0
                    _customSixMonthCost.value = 0.0; _customYearlyCost.value = 0.0
                    return
                }
                _customDaysCount.value = days
                val total = days * totalRate
                _customMonthlyCost.value = total
                _customSixMonthCost.value = total * 6
                _customYearlyCost.value = total * 12
            }
            CalcMode.WEEKLY -> {
                val selected = _selectedWeekDays.value
                if (selected.isEmpty()) {
                    _customDaysCount.value = 0; _customMonthlyCost.value = 0.0
                    _customSixMonthCost.value = 0.0; _customYearlyCost.value = 0.0
                    return
                }
                val daysPerWeek = selected.size
                val daysPerMonth = daysPerWeek * 4.33
                val daysPerSixMonth = daysPerMonth * 6
                val daysPerYear = daysPerMonth * 12
                _customDaysCount.value = daysPerMonth.toInt()
                _customMonthlyCost.value = daysPerMonth * totalRate
                _customSixMonthCost.value = daysPerSixMonth * totalRate
                _customYearlyCost.value = daysPerYear * totalRate
            }
            CalcMode.AUTO -> {
                val days = _officeDays.value + _homeOfficeDays.value
                _customDaysCount.value = days
                _customMonthlyCost.value = days * totalRate
                _customSixMonthCost.value = days * 6 * totalRate
                _customYearlyCost.value = days * 12 * totalRate
            }
        }
    }

    fun saveDailyMealRate(rate: Double) {
        saveAllSettings(rate, _travelExpense.value?.dailyTravelCost ?: 0.0, _travelExpense.value?.otherExpenses ?: 0.0, _travelExpense.value?.otherExpenseDescription ?: "")
    }

    fun saveTravelExpense(dailyTravelCost: Double, otherExpenses: Double, description: String = "") {
        saveAllSettings(_calculation.value?.dailyMealRate ?: 0.0, dailyTravelCost, otherExpenses, description)
    }

    fun goToPreviousMonth() {
        val calendar = Calendar.getInstance(); calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, -1); _selectedDate.value = calendar.time
        viewModelScope.launch { refreshCalculations() }
    }

    fun goToNextMonth() {
        val calendar = Calendar.getInstance(); calendar.time = _selectedDate.value
        calendar.add(Calendar.MONTH, 1); _selectedDate.value = calendar.time
        viewModelScope.launch { refreshCalculations() }
    }

    fun clearErrorMessage() { _errorMessage.value = null }
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun exportToExcel(context: Context) { Toast.makeText(context, "Exporting to Excel...", Toast.LENGTH_SHORT).show() }
}
