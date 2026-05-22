package com.rudra.smartworktracker.ui.screens.calculation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.MealSettings
import com.rudra.smartworktracker.data.entity.SpecialMealDate
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CalculationUiState(
    val normalMealRate: Double = 70.0,
    val specialMealRate: Double = 90.0,
    val mealDays: Set<Int> = setOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY),
    val specialDates: List<Long> = emptyList(),
    val selectedDate: Date = Date(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val officeDays: Int = 0,
    val totalMeals: Int = 0,
    val normalMeals: Int = 0,
    val specialMeals: Int = 0,
    val totalMealMonthlyCost: Double = 0.0,
    val totalMealQuarterlyCost: Double = 0.0,
    val totalMealYearlyCost: Double = 0.0,
    val dailyTravelCost: Double = 150.0,
    val otherExpenses: Double = 0.0,
    val otherExpenseDescription: String = "",
    val travelCostPerMonth: Double = 0.0,
    val travelCostPerYear: Double = 0.0,
    val otherExpensePerMonth: Double = 0.0,
    val otherExpensePerYear: Double = 0.0,
    val totalExpensePerMonth: Double = 0.0,
    val totalExpensePerQuarter: Double = 0.0,
    val totalExpensePerYear: Double = 0.0,
    val pieChartData: Map<String, Float> = emptyMap(),
    val monthlyBreakdown: List<Pair<String, Double>> = emptyList()
)

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(CalculationUiState())
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.value = _state.value.copy(isLoading = true)
        combine(
            db.mealSettingsDao().getMealSettings(),
            db.specialMealDateDao().getAllSpecialDates(),
            db.travelExpenseDao().getTravelExpense()
        ) { settings, dates, travelExp ->
            val mealDays = settings?.mealDays ?: setOf(
                Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY
            )
            _state.value = _state.value.copy(
                normalMealRate = settings?.normalMealRate ?: 70.0,
                specialMealRate = settings?.specialMealRate ?: 90.0,
                mealDays = mealDays,
                specialDates = dates.map { it.date },
                dailyTravelCost = travelExp?.dailyTravelCost ?: 150.0,
                otherExpenses = travelExp?.otherExpenses ?: 0.0,
                otherExpenseDescription = travelExp?.otherExpenseDescription ?: "",
                isLoading = false
            )
            refreshCalculations()
        }.launchIn(viewModelScope)
    }

    fun refreshCalculations() {
        viewModelScope.launch {
            try {
                fetchWorkLogData()
                calculateMealCost()
                calculateTotals()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Calculation error: ${e.message}")
            }
        }
    }

    private suspend fun fetchWorkLogData() {
        val s = _state.value
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val selectedMonthYear = monthYearFormat.format(s.selectedDate)
        val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
        val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }

        _state.value = _state.value.copy(
            officeDays = officeDaysCount,
            pieChartData = mapOf("Office" to officeDaysCount.toFloat())
        )
    }

    private suspend fun calculateMealCost() {
        val s = _state.value
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val selectedMonthYear = monthYearFormat.format(s.selectedDate)
        val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
        val specialDateSet = s.specialDates.toSet()

        var totalCost = 0.0
        var normalCount = 0
        var specialCount = 0

        for (log in workLogs) {
            if (log.workType != WorkType.OFFICE) continue
            val cal = Calendar.getInstance().apply { time = log.date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek !in s.mealDays) continue

            val normalizedDate = normalizeDate(log.date.time)
            if (normalizedDate in specialDateSet) {
                totalCost += s.specialMealRate
                specialCount++
            } else {
                totalCost += s.normalMealRate
                normalCount++
            }
        }

        _state.value = _state.value.copy(
            totalMeals = normalCount + specialCount,
            normalMeals = normalCount,
            specialMeals = specialCount,
            totalMealMonthlyCost = totalCost,
            totalMealQuarterlyCost = totalCost * 3,
            totalMealYearlyCost = totalCost * 12
        )
    }

    private suspend fun calculateTotals() {
        val s = _state.value
        val travelMonthly = s.dailyTravelCost * s.officeDays
        val travelYearly = travelMonthly * 12
        val otherMonthly = s.otherExpenses
        val otherYearly = otherMonthly * 12
        val totalMonthly = s.totalMealMonthlyCost + travelMonthly + otherMonthly
        val totalQuarterly = totalMonthly * 3
        val totalYearly = s.totalMealYearlyCost + travelYearly + otherYearly

        _state.value = _state.value.copy(
            travelCostPerMonth = travelMonthly,
            travelCostPerYear = travelYearly,
            otherExpensePerMonth = otherMonthly,
            otherExpensePerYear = otherYearly,
            totalExpensePerMonth = totalMonthly,
            totalExpensePerQuarter = totalQuarterly,
            totalExpensePerYear = totalYearly
        )
        fetchMonthlyBreakdown()
    }

    private suspend fun fetchMonthlyBreakdown() {
        val s = _state.value
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyData = mutableListOf<Pair<String, Double>>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val specialDateSet = s.specialDates.toSet()

        for (month in 0..11) {
            calendar.set(currentYear, month, 1)
            val monthName = monthFormat.format(calendar.time)
            val selectedMonthYear = yearMonthFormat.format(calendar.time)
            val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)

            var monthlyCost = 0.0
            for (log in workLogs) {
                if (log.workType != WorkType.OFFICE) continue
                val cal = Calendar.getInstance().apply { time = log.date }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek !in s.mealDays) continue
                val normalizedDate = normalizeDate(log.date.time)
                monthlyCost += if (normalizedDate in specialDateSet) s.specialMealRate else s.normalMealRate
            }
            monthlyCost += s.dailyTravelCost * workLogs.count { it.workType == WorkType.OFFICE } + s.otherExpenses
            monthlyData.add(monthName to monthlyCost)
        }

        _state.value = _state.value.copy(monthlyBreakdown = monthlyData)
    }

    // --- Meal Settings ---

    fun saveMealSettings(normalRate: Double, specialRate: Double, mealDays: Set<Int>) {
        viewModelScope.launch {
            db.mealSettingsDao().insert(
                MealSettings(
                    normalMealRate = normalRate,
                    specialMealRate = specialRate,
                    mealDays = mealDays,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            _state.value = _state.value.copy(normalMealRate = normalRate, specialMealRate = specialRate, mealDays = mealDays)
            refreshCalculations()
        }
    }

    // --- Special Meal Dates ---

    fun toggleSpecialDate(dateMillis: Long) {
        viewModelScope.launch {
            val normalized = normalizeDate(dateMillis)
            val existing = db.specialMealDateDao().getSpecialDate(normalized)
            if (existing != null) {
                db.specialMealDateDao().deleteByDate(normalized)
            } else {
                db.specialMealDateDao().insert(SpecialMealDate(date = normalized))
            }
        }
    }

    // --- Travel & Expense ---

    fun saveTravelExpense(dailyTravelCost: Double, otherExpenses: Double, description: String = "") {
        viewModelScope.launch {
            db.travelExpenseDao().insert(
                TravelAndExpense(
                    dailyTravelCost = dailyTravelCost,
                    otherExpenses = otherExpenses,
                    otherExpenseDescription = description,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            _state.value = _state.value.copy(
                dailyTravelCost = dailyTravelCost,
                otherExpenses = otherExpenses,
                otherExpenseDescription = description
            )
            refreshCalculations()
        }
    }

    // --- Date Navigation ---

    fun goToPreviousMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.selectedDate
        cal.add(Calendar.MONTH, -1)
        _state.value = _state.value.copy(selectedDate = cal.time)
        refreshCalculations()
    }

    fun goToNextMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.selectedDate
        cal.add(Calendar.MONTH, 1)
        _state.value = _state.value.copy(selectedDate = cal.time)
        refreshCalculations()
    }

    fun clearErrorMessage() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun exportToExcel(context: Context) {
        Toast.makeText(context, "Exporting to Excel...", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun normalizeDate(date: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
