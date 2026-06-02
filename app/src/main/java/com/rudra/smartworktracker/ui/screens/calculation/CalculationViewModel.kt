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

enum class ManualDateType { NORMAL, SPECIAL }

data class DayMealBreakdown(
    val dateLabel: String,
    val dayName: String,
    val workType: String,
    val inMealDays: Boolean,
    val isSpecial: Boolean,
    val rate: Double,
    val cost: Double
)

data class CalculationUiState(
    val normalMealRate: Double = 70.0,
    val specialMealRate: Double = 90.0,
    val mealDays: Set<Int> = setOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY),
    val specialDates: List<Long> = emptyList(),
    val workLogDates: List<Long> = emptyList(),
    val officeDates: List<Long> = emptyList(),
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
    val dayBreakdown: List<DayMealBreakdown> = emptyList(),
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
    val monthlyBreakdown: List<Pair<String, Double>> = emptyList(),
    val manualNormalRate: Double = 70.0,
    val manualSpecialRate: Double = 90.0,
    val manualSelectedDates: Map<Long, ManualDateType> = emptyMap(),
    val manualSelectedMonth: Date = Date(),
    val manualTotal: Int = 0,
    val manualNormal: Int = 0,
    val manualSpecial: Int = 0,
    val manualCost: Double = 0.0,
    val manualCalculated: Boolean = false
)

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(CalculationUiState())
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthShortFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    init {
        listenToDataChanges()
    }

    private fun listenToDataChanges() {
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
            runFullCalculation()
        }.launchIn(viewModelScope)
    }

    private fun runFullCalculation() {
        viewModelScope.launch {
            try {
                val s = _state.value
                val cal = Calendar.getInstance().apply { time = s.selectedDate }
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val monthStart = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val monthEnd = cal.timeInMillis
                val workLogs = db.workLogDao().getWorkLogsInRange(monthStart, monthEnd)

                val officeCount = workLogs.count { it.workType == WorkType.OFFICE }
                val officeDateSet = workLogs.filter { it.workType == WorkType.OFFICE }
                    .map { normalizeDate(it.date.time) }.toSet()
                val allWorkDates = workLogs.map { normalizeDate(it.date.time) }

                _state.value = _state.value.copy(
                    officeDays = officeCount,
                    workLogDates = allWorkDates,
                    officeDates = officeDateSet.toList(),
                    pieChartData = mapOf("Office" to officeCount.toFloat())
                )

                calculateMealCost(workLogs, officeDateSet)
                calculateTotals()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Calculation error: ${e.message}")
            }
        }
    }

    private suspend fun calculateMealCost(
        workLogs: List<com.rudra.smartworktracker.model.WorkLog>,
        officeDateSet: Set<Long>
    ) {
        val s = _state.value
        val specialDateSet = s.specialDates.toSet()

        var totalCost = 0.0
        var normalCount = 0
        var specialCount = 0
        val breakdown = mutableListOf<DayMealBreakdown>()

        for (log in workLogs) {
            val normalizedDate = normalizeDate(log.date.time)
            val cal = Calendar.getInstance().apply { time = log.date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(log.date)

            val isOffice = log.workType == WorkType.OFFICE
            val inMealDays = dayOfWeek in s.mealDays
            val isSpecial = normalizedDate in specialDateSet

            val rate = when {
                !isOffice || !inMealDays -> 0.0
                isSpecial -> s.specialMealRate
                else -> s.normalMealRate
            }
            val cost = rate

            if (cost > 0) {
                totalCost += cost
                if (isSpecial) specialCount++ else normalCount++
            }

            breakdown.add(
                DayMealBreakdown(
                    dateLabel = dateFormat.format(log.date),
                    dayName = dayName,
                    workType = log.workType.name,
                    inMealDays = inMealDays,
                    isSpecial = isSpecial,
                    rate = rate,
                    cost = cost
                )
            )
        }

        _state.value = _state.value.copy(
            totalMeals = normalCount + specialCount,
            normalMeals = normalCount,
            specialMeals = specialCount,
            totalMealMonthlyCost = totalCost,
            totalMealQuarterlyCost = totalCost * 3,
            totalMealYearlyCost = totalCost * 12,
            dayBreakdown = breakdown
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
        val specialDateSet = s.specialDates.toSet()

        for (month in 0..11) {
            calendar.set(currentYear, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthName = monthShortFormat.format(calendar.time)
            val monthStart = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis
            val logs = db.workLogDao().getWorkLogsInRange(monthStart, monthEnd)

            var cost = 0.0
            var officeCount = 0
            for (log in logs) {
                if (log.workType != WorkType.OFFICE) continue
                officeCount++
                val cal = Calendar.getInstance().apply { time = log.date }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek !in s.mealDays) continue
                val nd = normalizeDate(log.date.time)
                cost += if (nd in specialDateSet) s.specialMealRate else s.normalMealRate
            }
            cost += s.dailyTravelCost * officeCount + s.otherExpenses
            monthlyData.add(monthName to cost)
        }

        _state.value = _state.value.copy(monthlyBreakdown = monthlyData)
    }

    // ── User Actions ─────────────────────────────────────────────

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
        }
    }

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
        }
    }

    fun goToPreviousMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.selectedDate
        cal.add(Calendar.MONTH, -1)
        _state.value = _state.value.copy(selectedDate = cal.time)
        runFullCalculation()
    }

    fun goToNextMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.selectedDate
        cal.add(Calendar.MONTH, 1)
        _state.value = _state.value.copy(selectedDate = cal.time)
        runFullCalculation()
    }

    fun refresh() {
        runFullCalculation()
    }

    fun clearErrorMessage() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // ── Manual Calendar Actions ──────────────────────────────────

    fun toggleManualDate(dateMillis: Long, type: ManualDateType) {
        val current = _state.value.manualSelectedDates.toMutableMap()
        val normalized = normalizeDate(dateMillis)
        val existing = current[normalized]
        if (existing == type) {
            current.remove(normalized)
        } else {
            current[normalized] = type
        }
        _state.value = _state.value.copy(
            manualSelectedDates = current,
            manualCalculated = false
        )
    }

    fun clearAllManualDates() {
        _state.value = _state.value.copy(
            manualSelectedDates = emptyMap(),
            manualCalculated = false
        )
    }

    fun saveManualRates(normalRate: Double, specialRate: Double) {
        _state.value = _state.value.copy(
            manualNormalRate = normalRate,
            manualSpecialRate = specialRate
        )
    }

    fun calculateManual() {
        val s = _state.value
        var total = 0
        var normal = 0
        var special = 0
        var cost = 0.0
        for ((_, type) in s.manualSelectedDates) {
            total++
            when (type) {
                ManualDateType.NORMAL -> { normal++; cost += s.manualNormalRate }
                ManualDateType.SPECIAL -> { special++; cost += s.manualSpecialRate }
            }
        }
        _state.value = _state.value.copy(
            manualTotal = total,
            manualNormal = normal,
            manualSpecial = special,
            manualCost = cost,
            manualCalculated = true
        )
    }

    fun manualGoToPreviousMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.manualSelectedMonth
        cal.add(Calendar.MONTH, -1)
        _state.value = _state.value.copy(
            manualSelectedMonth = cal.time,
            manualSelectedDates = emptyMap(),
            manualCalculated = false
        )
    }

    fun manualGoToNextMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.manualSelectedMonth
        cal.add(Calendar.MONTH, 1)
        _state.value = _state.value.copy(
            manualSelectedMonth = cal.time,
            manualSelectedDates = emptyMap(),
            manualCalculated = false
        )
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun exportToExcel(context: Context) {
        Toast.makeText(context, "Exporting to Excel...", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val _normalizeCal = ThreadLocal<Calendar>()

        fun normalizeDate(date: Long): Long {
            val cal = _normalizeCal.get() ?: Calendar.getInstance().also { _normalizeCal.set(it) }
            cal.timeInMillis = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
