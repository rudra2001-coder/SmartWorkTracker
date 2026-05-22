package com.rudra.smartworktracker.ui.screens.calculation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Calculation
import com.rudra.smartworktracker.data.entity.DailyMealRate
import com.rudra.smartworktracker.data.entity.MealType
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import com.rudra.smartworktracker.data.entity.WeeklyMealRate
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PerMealCost(val mealTypeName: String, val monthly: Double, val weekly: Double, val yearly: Double)

data class CalculationUiState(
    val mealTypes: List<MealType> = emptyList(),
    val weeklyRates: List<WeeklyMealRate> = emptyList(),
    val dailyRates: List<DailyMealRate> = emptyList(),
    val selectedDate: Date = Date(),
    val selectedWeek: Int = 1,
    val selectedDates: List<Long> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val officeDays: Int = 0,
    val homeOfficeDays: Int = 0,
    val perMealCosts: List<PerMealCost> = emptyList(),
    val totalMealMonthly: Double = 0.0,
    val totalMealQuarterly: Double = 0.0,
    val totalMealYearly: Double = 0.0,
    val travelCostPerWeek: Double = 0.0,
    val travelCostPerMonth: Double = 0.0,
    val travelCostPerYear: Double = 0.0,
    val otherExpensePerMonth: Double = 0.0,
    val otherExpensePerYear: Double = 0.0,
    val totalExpensePerMonth: Double = 0.0,
    val totalExpensePerQuarter: Double = 0.0,
    val totalExpensePerYear: Double = 0.0,
    val monthlyBreakdown: List<Pair<String, Double>> = emptyList(),
    val pieChartData: Map<String, Float> = emptyMap(),
    val dailyMealRate: Double = 58.0,
    val dailyTravelCost: Double = 150.0,
    val otherExpenses: Double = 0.0,
    val otherExpenseDescription: String = ""
)

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(CalculationUiState())
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    init {
        loadData()
        seedDefaultMealTypes()
    }

    private fun seedDefaultMealTypes() {
        viewModelScope.launch {
            val existing = db.mealTypeDao().getAllMealTypesList()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    MealType(name = "Breakfast", defaultRate = 30.0, sortOrder = 1),
                    MealType(name = "Lunch", defaultRate = 80.0, sortOrder = 2),
                    MealType(name = "Dinner", defaultRate = 80.0, sortOrder = 3),
                    MealType(name = "Snacks", defaultRate = 20.0, sortOrder = 4)
                )
                defaults.forEach { db.mealTypeDao().insert(it) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                combine(
                    db.calculationDao().getCalculation(),
                    db.travelExpenseDao().getTravelExpense(),
                    db.mealTypeDao().getAllMealTypes()
                ) { calc, travelExp, mealTypes ->
                    Triple(calc, travelExp, mealTypes)
                }.collectLatest { (calc, travelExp, mealTypes) ->
                    val currentCalc = calc ?: Calculation(
                        dailyMealRate = 58.0,
                        lastUpdated = System.currentTimeMillis()
                    )
                    val currentTravelExp = travelExp ?: TravelAndExpense(
                        dailyTravelCost = 150.0,
                        otherExpenses = 0.0,
                        otherExpenseDescription = "",
                        lastUpdated = System.currentTimeMillis()
                    )

                    val s = _state.value
                    _state.value = s.copy(
                        mealTypes = mealTypes,
                        dailyMealRate = currentCalc.dailyMealRate,
                        dailyTravelCost = currentTravelExp.dailyTravelCost,
                        otherExpenses = currentTravelExp.otherExpenses,
                        otherExpenseDescription = currentTravelExp.otherExpenseDescription,
                        isLoading = false
                    )

                    refreshCalculations()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Failed to load: ${e.message}", isLoading = false)
            }
        }
    }

    fun refreshCalculations() {
        viewModelScope.launch {
            val s = _state.value
            try {
                fetchWorkLogData(s.selectedDate)
                fetchAllMealRates()
                calculateAllCosts()
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = "Calculation error: ${e.message}")
            }
        }
    }

    private suspend fun fetchWorkLogData(date: Date) {
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val selectedMonthYear = monthYearFormat.format(date)
        val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
        val officeDaysCount = workLogs.count { it.workType == WorkType.OFFICE }
        val homeOfficeDaysCount = workLogs.count { it.workType == WorkType.HOME_OFFICE }

        _state.value = _state.value.copy(
            officeDays = officeDaysCount,
            homeOfficeDays = homeOfficeDaysCount,
            pieChartData = mapOf(
                "Office" to officeDaysCount.toFloat(),
                "Home Office" to homeOfficeDaysCount.toFloat()
            )
        )
    }

    private suspend fun fetchAllMealRates() {
        val s = _state.value
        val cal = Calendar.getInstance()
        cal.time = s.selectedDate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        val weeklyRates = db.weeklyMealRateDao().getRatesForWeekList(s.selectedWeek, year)

        val startOfMonth = Calendar.getInstance().apply { set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val endOfMonth = Calendar.getInstance().apply { set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59) }.timeInMillis
        val dayMs = 86400000L
        val daysInMonth = mutableListOf<Long>()
        var cursor = startOfMonth
        while (cursor <= endOfMonth) {
            daysInMonth.add(cursor)
            cursor += dayMs
        }

        val dailyRates = db.dailyMealRateDao().getRatesForDates(daysInMonth)

        val monthStartWeek = Calendar.getInstance().apply { time = s.selectedDate; set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.WEEK_OF_YEAR)
        val monthEndWeek = Calendar.getInstance().apply { time = s.selectedDate; set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)) }.get(Calendar.WEEK_OF_YEAR)

        val allWeeklyRates = mutableListOf<WeeklyMealRate>()
        for (w in monthStartWeek..monthEndWeek) {
            allWeeklyRates.addAll(db.weeklyMealRateDao().getRatesForWeekList(w, year))
        }

        _state.value = _state.value.copy(weeklyRates = allWeeklyRates, dailyRates = dailyRates)
    }

    private suspend fun calculateAllCosts() {
        val s = _state.value
        val mealTypes = s.mealTypes
        if (mealTypes.isEmpty()) return

        val cal = Calendar.getInstance()
        cal.time = s.selectedDate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val weeksInMonth = if (daysInMonth > 0) daysInMonth / 7.0 else 4.33

        val allDailyRates = s.dailyRates
        val allWeeklyRates = s.weeklyRates
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val selectedMonthYear = monthYearFormat.format(s.selectedDate)
        val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
        val workingDays = workLogs.filter { it.workType == WorkType.OFFICE || it.workType == WorkType.HOME_OFFICE }
        val workingDayDates = workingDays.map { wl ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            wl.date.time
        }.filter { it > 0 }

        val dayMs = 86400000L
        val calForWeek = Calendar.getInstance()

        val perMealCosts = mealTypes.map { mt ->
            var monthlyMealCost = 0.0
            for (dayTs in workingDayDates) {
                calForWeek.timeInMillis = dayTs
                val weekNum = calForWeek.get(Calendar.WEEK_OF_YEAR)

                val dailyOverride = allDailyRates.find { it.mealTypeId == mt.id && it.date == dayTs }
                val weeklyOverride = if (dailyOverride == null) {
                    allWeeklyRates.find { it.mealTypeId == mt.id && it.weekNumber == weekNum && it.year == year }
                } else null

                val rate = dailyOverride?.rate ?: weeklyOverride?.rate ?: mt.defaultRate
                monthlyMealCost += rate
            }

            val weekly = if (weeksInMonth > 0) monthlyMealCost / weeksInMonth else 0.0
            val yearly = monthlyMealCost * 12
            PerMealCost(mt.name, monthlyMealCost, weekly, yearly)
        }

        val totalMealMonthly = perMealCosts.sumOf { it.monthly }
        val totalMealYearly = perMealCosts.sumOf { it.yearly }
        val totalMealQuarterly = totalMealMonthly * 3

        val weeklyOfficeDays = if (weeksInMonth > 0) s.officeDays / weeksInMonth else 0.0
        val travelWeekly = s.dailyTravelCost * weeklyOfficeDays
        val travelMonthly = s.dailyTravelCost * s.officeDays
        val travelYearly = travelMonthly * 12

        val otherMonthly = s.otherExpenses
        val otherYearly = otherMonthly * 12

        val totalMonthly = totalMealMonthly + travelMonthly + otherMonthly
        val totalQuarterly = totalMonthly * 3
        val totalYearly = totalMealYearly + travelYearly + otherYearly

        _state.value = _state.value.copy(
            perMealCosts = perMealCosts,
            totalMealMonthly = totalMealMonthly,
            totalMealQuarterly = totalMealQuarterly,
            totalMealYearly = totalMealYearly,
            travelCostPerWeek = travelWeekly,
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
        val mealTypes = s.mealTypes
        if (mealTypes.isEmpty()) {
            _state.value = _state.value.copy(monthlyBreakdown = emptyList())
            return
        }
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyData = mutableListOf<Pair<String, Double>>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for (month in 0..11) {
            calendar.set(currentYear, month, 1)
            val monthName = monthFormat.format(calendar.time)
            val selectedMonthYear = yearMonthFormat.format(calendar.time)
            val workLogs = db.workLogDao().getWorkLogsByMonth(selectedMonthYear)
            val workingDays = workLogs.filter { it.workType == WorkType.OFFICE || it.workType == WorkType.HOME_OFFICE }
            val workingDayDates = workingDays.map { wl ->
                wl.date.time
            }.filter { it > 0 }

            var monthlyCost = 0.0
            for (dayTs in workingDayDates) {
                for (mt in mealTypes) {
                    val dailyRate = db.dailyMealRateDao().getRate(mt.id, dayTs)
                    val rate = dailyRate?.rate ?: mt.defaultRate
                    monthlyCost += rate
                }
            }
            monthlyCost += s.dailyTravelCost * workingDays.size + s.otherExpenses
            monthlyData.add(monthName to monthlyCost)
        }

        _state.value = _state.value.copy(monthlyBreakdown = monthlyData)
    }

    fun saveDailyMealRate(rate: Double) {
        viewModelScope.launch {
            val currentCalc = Calculation(dailyMealRate = rate, lastUpdated = System.currentTimeMillis())
            db.calculationDao().insert(currentCalc)
            _state.value = _state.value.copy(dailyMealRate = rate)
            refreshCalculations()
        }
    }

    fun saveTravelExpense(dailyTravelCost: Double, otherExpenses: Double, description: String = "") {
        viewModelScope.launch {
            val currentExpense = TravelAndExpense(
                dailyTravelCost = dailyTravelCost,
                otherExpenses = otherExpenses,
                otherExpenseDescription = description,
                lastUpdated = System.currentTimeMillis()
            )
            db.travelExpenseDao().insert(currentExpense)
            _state.value = _state.value.copy(
                dailyTravelCost = dailyTravelCost,
                otherExpenses = otherExpenses,
                otherExpenseDescription = description
            )
            refreshCalculations()
        }
    }

    // --- Meal Type Management ---

    fun addMealType(name: String, defaultRate: Double) {
        viewModelScope.launch {
            val maxSort = (_state.value.mealTypes.maxOfOrNull { it.sortOrder } ?: 0) + 1
            db.mealTypeDao().insert(MealType(name = name, defaultRate = defaultRate, sortOrder = maxSort))
        }
    }

    fun updateMealTypeRate(mealTypeId: Int, rate: Double) {
        viewModelScope.launch {
            val mt = db.mealTypeDao().getMealTypeById(mealTypeId) ?: return@launch
            db.mealTypeDao().update(mt.copy(defaultRate = rate, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteMealType(mealTypeId: Int) {
        viewModelScope.launch {
            db.mealTypeDao().softDelete(mealTypeId)
        }
    }

    // --- Weekly Rate Management ---

    fun setSelectedWeek(week: Int) {
        _state.value = _state.value.copy(selectedWeek = week)
        viewModelScope.launch {
            val year = Calendar.getInstance().apply { time = _state.value.selectedDate }.get(Calendar.YEAR)
            val rates = db.weeklyMealRateDao().getRatesForWeekList(week, year)
            _state.value = _state.value.copy(weeklyRates = rates)
            refreshCalculations()
        }
    }

    fun saveWeeklyMealRate(mealTypeId: Int, rate: Double, weekNumber: Int) {
        viewModelScope.launch {
            val year = Calendar.getInstance().apply { time = _state.value.selectedDate }.get(Calendar.YEAR)
            db.weeklyMealRateDao().insert(
                WeeklyMealRate(
                    mealTypeId = mealTypeId,
                    rate = rate,
                    weekNumber = weekNumber,
                    year = year
                )
            )
            refreshCalculations()
        }
    }

    fun deleteWeeklyMealRate(mealTypeId: Int, weekNumber: Int) {
        viewModelScope.launch {
            val year = Calendar.getInstance().apply { time = _state.value.selectedDate }.get(Calendar.YEAR)
            db.weeklyMealRateDao().deleteRate(mealTypeId, weekNumber, year)
            refreshCalculations()
        }
    }

    // --- Daily Rate Management ---

    fun setSelectedDates(dates: List<Long>) {
        _state.value = _state.value.copy(selectedDates = dates)
        if (dates.isNotEmpty()) {
            viewModelScope.launch {
                val rates = db.dailyMealRateDao().getRatesForDates(dates)
                _state.value = _state.value.copy(dailyRates = rates)
                refreshCalculations()
            }
        }
    }

    fun saveDailyMealRateForDate(mealTypeId: Int, rate: Double, date: Long) {
        viewModelScope.launch {
            val normalizedDate = normalizeDate(date)
            db.dailyMealRateDao().insert(
                DailyMealRate(
                    mealTypeId = mealTypeId,
                    rate = rate,
                    date = normalizedDate
                )
            )
            refreshCalculations()
        }
    }

    fun deleteDailyMealRate(mealTypeId: Int, date: Long) {
        viewModelScope.launch {
            db.dailyMealRateDao().deleteRate(mealTypeId, normalizeDate(date))
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
        private fun normalizeDate(date: Long): Long {
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
