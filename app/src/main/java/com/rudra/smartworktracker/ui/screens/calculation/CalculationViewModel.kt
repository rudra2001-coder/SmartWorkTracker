package com.rudra.smartworktracker.ui.screens.calculation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.ManualMealEntry
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
    val cost: Double,
    val mealCount: Int = 1,
    val isOvertime: Boolean = false,
    val overrideCost: Double? = null
)

data class WeekBreakdown(
    val weekLabel: String,
    val mealCost: Double,
    val dayCount: Int
)

data class CalculationUiState(
    val normalMealRate: Double = 70.0,
    val specialMealRate: Double = 90.0,
    val mealCountPerDay: Int = 1,
    val monthlyMealBudget: Double = 0.0,
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
    val weekBreakdown: List<WeekBreakdown> = emptyList(),
    val overtimeHours: Double = 0.0,
    val overtimeCost: Double = 0.0,
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
    val budgetRemaining: Double = 0.0,
    val budgetPercent: Float = 0f,
    val pieChartData: Map<String, Float> = emptyMap(),
    val monthlyBreakdown: List<Pair<String, Double>> = emptyList(),
    val previousYearBreakdown: List<Pair<String, Double>> = emptyList(),
    val manualNormalRate: Double = 70.0,
    val manualSpecialRate: Double = 90.0,
    val manualMealCount: Int = 1,
    val manualSelectedDates: Map<Long, ManualDateType> = emptyMap(),
    val manualOverrides: Map<Long, Double> = emptyMap(),
    val manualSelectedMonth: Date = Date(),
    val manualTotal: Int = 0,
    val manualNormal: Int = 0,
    val manualSpecial: Int = 0,
    val manualCost: Double = 0.0,
    val manualCalculated: Boolean = false,
    val manualDayBreakdown: List<DayMealBreakdown> = emptyList()
)

class CalculationViewModel(private val db: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(CalculationUiState())
    val state: StateFlow<CalculationUiState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthShortFormat = SimpleDateFormat("MMM", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    init {
        listenToDataChanges()
    }

    private fun getMonthStartEnd(date: Date): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { time = date }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }

    private fun getMonthStartEndMillis(year: Int, month: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return start to end
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
                mealCountPerDay = settings?.mealCountPerDay ?: 1,
                monthlyMealBudget = settings?.monthlyMealBudget ?: 0.0,
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
                val (monthStart, monthEnd) = getMonthStartEnd(s.selectedDate)
                val workLogs = db.workLogDao().getWorkLogsInRange(monthStart, monthEnd)

                val officeCount = workLogs.count { it.workType == WorkType.OFFICE }
                val officeDateSet = workLogs.filter { it.workType == WorkType.OFFICE }
                    .map { normalizeDate(it.date.time) }.toSet()
                val allWorkDates = workLogs.map { normalizeDate(it.date.time) }

                val overtimeLogs = db.workLogDao().getOvertimeLogsInRange(monthStart, monthEnd).first()
                var totalOvertimeHours = 0.0
                for (olog in overtimeLogs) {
                    val st = olog.startTime
                    val et = olog.endTime
                    if (st != null && et != null) {
                        try {
                            val sh = st.substringBefore(":").toInt()
                            val sm = st.substringAfter(":").toInt()
                            val eh = et.substringBefore(":").toInt()
                            val em = et.substringAfter(":").toInt()
                            totalOvertimeHours += (eh - sh) + (em - sm) / 60.0
                        } catch (_: Exception) {}
                    }
                }
                val overtimeCost = totalOvertimeHours * (overtimeLogs.firstOrNull()?.overtimeRate ?: 1.5)

                _state.value = _state.value.copy(
                    officeDays = officeCount,
                    workLogDates = allWorkDates,
                    officeDates = officeDateSet.toList(),
                    overtimeHours = totalOvertimeHours,
                    overtimeCost = overtimeCost,
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
        val mealCountPerDay = s.mealCountPerDay.coerceAtLeast(1)

        var totalCost = 0.0
        var normalCount = 0
        var specialCount = 0
        val breakdown = mutableListOf<DayMealBreakdown>()
        val weekMap = mutableMapOf<Int, MutableList<Double>>()

        for (log in workLogs) {
            val normalizedDate = normalizeDate(log.date.time)
            val cal = Calendar.getInstance().apply { time = log.date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayName = dayNameFormat.format(log.date)

            val isOffice = log.workType == WorkType.OFFICE
            val inMealDays = dayOfWeek in s.mealDays
            val isSpecial = normalizedDate in specialDateSet

            val rate = when {
                !isOffice || !inMealDays -> 0.0
                isSpecial -> s.specialMealRate
                else -> s.normalMealRate
            }
            val cost = rate * mealCountPerDay

            if (cost > 0) {
                totalCost += cost
                if (isSpecial) specialCount++ else normalCount++
                val isoWeek = cal.get(Calendar.WEEK_OF_YEAR)
                weekMap.getOrPut(isoWeek) { mutableListOf() }.add(cost)
            }

            breakdown.add(
                DayMealBreakdown(
                    dateLabel = dateFormat.format(log.date),
                    dayName = dayName,
                    workType = log.workType.name,
                    inMealDays = inMealDays,
                    isSpecial = isSpecial,
                    rate = rate,
                    cost = cost,
                    mealCount = if (cost > 0) mealCountPerDay else 0,
                    isOvertime = log.isOvertime
                )
            )
        }

        val weekBreakdown = weekMap.entries.sortedBy { it.key }.map { (week, costs) ->
            WeekBreakdown(
                weekLabel = "Week $week",
                mealCost = costs.sum(),
                dayCount = costs.size
            )
        }

        _state.value = _state.value.copy(
            totalMeals = normalCount + specialCount,
            normalMeals = normalCount,
            specialMeals = specialCount,
            totalMealMonthlyCost = totalCost,
            totalMealQuarterlyCost = totalCost * 3,
            totalMealYearlyCost = totalCost * 12,
            dayBreakdown = breakdown,
            weekBreakdown = weekBreakdown
        )
    }

    private suspend fun calculateTotals() {
        val s = _state.value
        val travelMonthly = s.dailyTravelCost * s.officeDays
        val travelYearly = travelMonthly * 12
        val otherMonthly = s.otherExpenses
        val otherYearly = otherMonthly * 12
        val totalMonthly = s.totalMealMonthlyCost + travelMonthly + otherMonthly + s.overtimeCost
        val totalQuarterly = totalMonthly * 3
        val totalYearly = s.totalMealYearlyCost + travelYearly + otherYearly + s.overtimeCost * 12

        val budgetRemaining = if (s.monthlyMealBudget > 0) (s.monthlyMealBudget - s.totalMealMonthlyCost).coerceAtLeast(0.0) else 0.0
        val budgetPercent = if (s.monthlyMealBudget > 0) (s.totalMealMonthlyCost / s.monthlyMealBudget).toFloat().coerceIn(0f, 1f) else 0f

        _state.value = _state.value.copy(
            travelCostPerMonth = travelMonthly,
            travelCostPerYear = travelYearly,
            otherExpensePerMonth = otherMonthly,
            otherExpensePerYear = otherYearly,
            totalExpensePerMonth = totalMonthly,
            totalExpensePerQuarter = totalQuarterly,
            totalExpensePerYear = totalYearly,
            budgetRemaining = budgetRemaining,
            budgetPercent = budgetPercent
        )
        fetchMonthlyBreakdown()
    }

    private suspend fun fetchMonthlyBreakdown() {
        val s = _state.value
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyData = mutableListOf<Pair<String, Double>>()
        val prevYearData = mutableListOf<Pair<String, Double>>()
        val specialDateSet = s.specialDates.toSet()
        val mealCountPerDay = s.mealCountPerDay.coerceAtLeast(1)

        for (month in 0..11) {
            calendar.set(currentYear, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthName = monthShortFormat.format(calendar.time)
            val (monthStart, monthEnd) = getMonthStartEndMillis(currentYear, month)
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
            cost = cost * mealCountPerDay + s.dailyTravelCost * officeCount + s.otherExpenses
            monthlyData.add(monthName to cost)

            calendar.set(currentYear - 1, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val (prevStart, prevEnd) = getMonthStartEndMillis(currentYear - 1, month)
            val prevLogs = db.workLogDao().getWorkLogsInRange(prevStart, prevEnd)
            var prevCost = 0.0
            var prevOfficeCount = 0
            for (log in prevLogs) {
                if (log.workType != WorkType.OFFICE) continue
                prevOfficeCount++
                val cal = Calendar.getInstance().apply { time = log.date }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek !in s.mealDays) continue
                cost += if (normalizeDate(log.date.time) in specialDateSet) s.specialMealRate else s.normalMealRate
            }
            prevCost = prevCost * mealCountPerDay + s.dailyTravelCost * prevOfficeCount + s.otherExpenses
            prevYearData.add(monthName to prevCost)
        }

        _state.value = _state.value.copy(
            monthlyBreakdown = monthlyData,
            previousYearBreakdown = prevYearData
        )
    }

    // ── User Actions ─────────────────────────────────────────────

    fun saveMealSettings(normalRate: Double, specialRate: Double, mealDays: Set<Int>, mealCount: Int = 1, budget: Double = 0.0) {
        viewModelScope.launch {
            db.mealSettingsDao().insert(
                MealSettings(
                    normalMealRate = normalRate,
                    specialMealRate = specialRate,
                    mealDays = mealDays,
                    mealCountPerDay = mealCount.coerceAtLeast(1),
                    monthlyMealBudget = budget.coerceAtLeast(0.0),
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

    // ── Manual Calendar Actions (persisted to DB) ────────────────

    fun toggleManualDate(dateMillis: Long, type: ManualDateType) {
        val normalized = normalizeDate(dateMillis)
        viewModelScope.launch {
            val existing = db.manualMealEntryDao().getEntryByDate(normalized)
            if (existing != null && existing.type == type.name) {
                db.manualMealEntryDao().deleteByDate(normalized)
                updateManualDatesFromDb()
            } else {
                db.manualMealEntryDao().insert(
                    ManualMealEntry(
                        date = normalized,
                        type = type.name,
                        mealCount = _state.value.manualMealCount
                    )
                )
                updateManualDatesFromDb()
            }
        }
    }

    fun clearAllManualDates() {
        val s = _state.value
        viewModelScope.launch {
            val (start, end) = getMonthStartEnd(s.manualSelectedMonth)
            db.manualMealEntryDao().deleteInRange(start, end)
            updateManualDatesFromDb()
        }
    }

    fun overrideManualDayCost(dateMillis: Long, cost: Double) {
        val normalized = normalizeDate(dateMillis)
        viewModelScope.launch {
            val existing = db.manualMealEntryDao().getEntryByDate(normalized)
            if (existing != null) {
                db.manualMealEntryDao().insert(existing.copy(overrideCost = cost))
            } else {
                db.manualMealEntryDao().insert(
                    ManualMealEntry(date = normalized, type = ManualDateType.NORMAL.name, overrideCost = cost)
                )
            }
            updateManualDatesFromDb()
        }
    }

    fun clearManualDayOverride(dateMillis: Long) {
        val normalized = normalizeDate(dateMillis)
        viewModelScope.launch {
            val existing = db.manualMealEntryDao().getEntryByDate(normalized)
            if (existing != null) {
                db.manualMealEntryDao().insert(existing.copy(overrideCost = null))
            }
            updateManualDatesFromDb()
        }
    }

    fun saveManualRates(normalRate: Double, specialRate: Double, mealCount: Int = 1) {
        _state.value = _state.value.copy(
            manualNormalRate = normalRate,
            manualSpecialRate = specialRate,
            manualMealCount = mealCount.coerceAtLeast(1),
            manualCalculated = false
        )
    }

    private suspend fun updateManualDatesFromDb() {
        val s = _state.value
        val (start, end) = getMonthStartEnd(s.manualSelectedMonth)
        val entries = db.manualMealEntryDao().getEntriesInRangeOnce(start, end)
        val dateMap = mutableMapOf<Long, ManualDateType>()
        val overrides = mutableMapOf<Long, Double>()
        for (e in entries) {
            dateMap[e.date] = if (e.type == ManualDateType.SPECIAL.name) ManualDateType.SPECIAL else ManualDateType.NORMAL
            if (e.overrideCost != null) overrides[e.date] = e.overrideCost
        }
        _state.value = _state.value.copy(
            manualSelectedDates = dateMap,
            manualOverrides = overrides,
            manualCalculated = false
        )
    }

    fun calculateManual() {
        val s = _state.value
        var total = 0
        var normal = 0
        var special = 0
        var cost = 0.0
        val breakdown = mutableListOf<DayMealBreakdown>()
        val df = dateFormat

        for ((date, type) in s.manualSelectedDates) {
            total++
            val overrideCost = s.manualOverrides[date]
            val dayCost = overrideCost ?: when (type) {
                ManualDateType.NORMAL -> s.manualNormalRate * s.manualMealCount
                ManualDateType.SPECIAL -> s.manualSpecialRate * s.manualMealCount
            }
            cost += dayCost
            when (type) {
                ManualDateType.NORMAL -> normal++
                ManualDateType.SPECIAL -> special++
            }
            breakdown.add(
                DayMealBreakdown(
                    dateLabel = df.format(Date(date)),
                    dayName = dayNameFormat.format(Date(date)),
                    workType = "MANUAL",
                    inMealDays = true,
                    isSpecial = type == ManualDateType.SPECIAL,
                    rate = (dayCost / s.manualMealCount.coerceAtLeast(1)),
                    cost = dayCost,
                    mealCount = s.manualMealCount,
                    overrideCost = overrideCost
                )
            )
        }

        _state.value = _state.value.copy(
            manualTotal = total,
            manualNormal = normal,
            manualSpecial = special,
            manualCost = cost,
            manualCalculated = true,
            manualDayBreakdown = breakdown.sortedBy { it.dateLabel }
        )
    }

    fun manualGoToPreviousMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.manualSelectedMonth
        cal.add(Calendar.MONTH, -1)
        _state.value = _state.value.copy(
            manualSelectedMonth = cal.time,
            manualCalculated = false
        )
        viewModelScope.launch { updateManualDatesFromDb() }
    }

    fun manualGoToNextMonth() {
        val cal = Calendar.getInstance()
        cal.time = _state.value.manualSelectedMonth
        cal.add(Calendar.MONTH, 1)
        _state.value = _state.value.copy(
            manualSelectedMonth = cal.time,
            manualCalculated = false
        )
        viewModelScope.launch { updateManualDatesFromDb() }
    }

    fun loadManualMonthEntries() {
        viewModelScope.launch { updateManualDatesFromDb() }
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun exportToClipboard(context: Context) {
        val s = _state.value
        val sb = StringBuilder()
        sb.appendLine("=== Meal Calculation Report ===")
        sb.appendLine("Month: ${monthLabelFormat.format(s.selectedDate)}")
        sb.appendLine()
        sb.appendLine("Settings:")
        sb.appendLine("  Normal Rate: ৳${"%,.0f".format(s.normalMealRate)}")
        sb.appendLine("  Special Rate: ৳${"%,.0f".format(s.specialMealRate)}")
        sb.appendLine("  Meals/Day: ${s.mealCountPerDay}")
        sb.appendLine("  Office Days: ${s.officeDays}")
        sb.appendLine()
        sb.appendLine("Meal Summary:")
        sb.appendLine("  Total Meals: ${s.totalMeals}")
        sb.appendLine("  Normal: ${s.normalMeals}")
        sb.appendLine("  Special: ${s.specialMeals}")
        sb.appendLine("  Monthly Cost: ৳${"%,.0f".format(s.totalMealMonthlyCost)}")
        sb.appendLine("  Quarterly: ৳${"%,.0f".format(s.totalMealQuarterlyCost)}")
        sb.appendLine("  Yearly: ৳${"%,.0f".format(s.totalMealYearlyCost)}")
        if (s.monthlyMealBudget > 0) {
            sb.appendLine("  Budget: ৳${"%,.0f".format(s.monthlyMealBudget)}")
            sb.appendLine("  Used: ${(s.budgetPercent * 100).toInt()}%")
        }
        sb.appendLine()
        sb.appendLine("Other Costs:")
        sb.appendLine("  Travel: ৳${"%,.0f".format(s.travelCostPerMonth)}/mo")
        sb.appendLine("  Other: ৳${"%,.0f".format(s.otherExpensePerMonth)}/mo")
        sb.appendLine("  Overtime: ৳${"%,.0f".format(s.overtimeCost)}")
        sb.appendLine()
        sb.appendLine("Total: ৳${"%,.0f".format(s.totalExpensePerMonth)}/mo")
        sb.appendLine("       ৳${"%,.0f".format(s.totalExpensePerQuarter)}/quarter")
        sb.appendLine("       ৳${"%,.0f".format(s.totalExpensePerYear)}/year")

        val clip = ClipData.newPlainText("Meal Calculation", sb.toString())
        context.getSystemService(Context.CLIPBOARD_SERVICE)?.let { mgr ->
            (mgr as ClipboardManager).setPrimaryClip(clip)
            Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
        }
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
