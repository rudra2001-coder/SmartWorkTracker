package com.rudra.smartworktracker.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseByCategory
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.IncomeByCategory
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyReportUiState(
    val selectedMonth: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val useCustomRange: Boolean = false,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val compareWithPrevious: Boolean = false,
    val isLoading: Boolean = true,
    val workLogs: List<WorkLog> = emptyList(),
    val officeCount: Int = 0,
    val homeCount: Int = 0,
    val offCount: Int = 0,
    val extraCount: Int = 0,
    val overtimeCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netAmount: Double = 0.0,
    val mealExpense: Double = 0.0,
    val totalSavingsDeposited: Double = 0.0,
    val totalSavingsWithdrawn: Double = 0.0,
    val netSavings: Double = 0.0,
    val expenseByCategory: List<ExpenseByCategory> = emptyList(),
    val incomeByCategory: List<IncomeByCategory> = emptyList(),
    val previousPeriod: PeriodComparison? = null
)

data class PeriodComparison(
    val label: String = "",
    val officeCount: Int = 0,
    val homeCount: Int = 0,
    val offCount: Int = 0,
    val extraCount: Int = 0,
    val overtimeCount: Int = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netAmount: Double = 0.0
)

class MonthlyReportViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val _selectedMonthIndex = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _useCustomRange = MutableStateFlow(false)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _compareWithPrevious = MutableStateFlow(false)

    val months: List<String> = (0..11).map {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, it)
        cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!
    }

    val uiState: StateFlow<MonthlyReportUiState> = combine(
        _selectedMonthIndex,
        _selectedYear,
        _useCustomRange,
        _customStartDate,
        _customEndDate,
        _compareWithPrevious,
        workLogRepository.getAllWorkLogs(),
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncomes(),
        db.savingsDao().getAllSavings()
    ) { array ->
        val monthIndex = array[0] as Int
        val year = array[1] as Int
        val useCustom = array[2] as Boolean
        val customStart = array[3] as Long?
        val customEnd = array[4] as Long?
        val compare = array[5] as Boolean
        val allWorkLogs = array[6] as List<WorkLog>
        val allExpenses = array[7] as List<Expense>
        val allIncomes = array[8] as List<Income>
        val allSavings = array[9] as List<Savings>

        val (startTime, endTime) = computeDateRange(monthIndex, year, useCustom, customStart, customEnd)

        val filteredLogs = allWorkLogs.filter { it.date.time in startTime..endTime }
        val filteredExpenses = allExpenses.filter { it.timestamp in startTime..endTime }
        val filteredIncomes = allIncomes.filter { it.timestamp in startTime..endTime }
        val filteredSavings = allSavings.filter { it.timestamp in startTime..endTime }

        val officeCount = filteredLogs.count { it.workType == WorkType.OFFICE }
        val homeCount = filteredLogs.count { it.workType == WorkType.HOME_OFFICE }
        val offCount = filteredLogs.count { it.workType == WorkType.OFF_DAY }
        val extraCount = filteredLogs.count { it.workType == WorkType.EXTRA_WORK }
        val overtimeCount = filteredLogs.count { it.workType == WorkType.OVERTIME }

        val totalIncome = filteredIncomes.sumOf { it.amount }
        val totalExpense = filteredExpenses.sumOf { it.amount }
        val netAmount = totalIncome - totalExpense

        val mealExpense = filteredExpenses.filter { it.category == ExpenseCategory.MEAL }.sumOf { it.amount }

        val deposits = filteredSavings.filter { it.amount > 0 }.sumOf { it.amount }
        val withdrawals = filteredSavings.filter { it.amount < 0 }.sumOf { -it.amount }

        val expenseByCategory = filteredExpenses.groupBy { it.category }.map { (cat, items) ->
            ExpenseByCategory(cat, items.sumOf { it.amount })
        }.sortedByDescending { it.total }

        val incomeByCategory = filteredIncomes.groupBy { it.category }.map { (cat, items) ->
            IncomeByCategory(cat, items.sumOf { it.amount })
        }.sortedByDescending { it.total }

        val previousPeriod = if (compare) {
            val duration = endTime - startTime
            val prevEnd = startTime - 1
            val prevStart = prevEnd - duration

            val prevLogs = allWorkLogs.filter { it.date.time in prevStart..prevEnd }
            val prevExpenses = allExpenses.filter { it.timestamp in prevStart..prevEnd }
            val prevIncomes = allIncomes.filter { it.timestamp in prevStart..prevEnd }

            val prevOffice = prevLogs.count { it.workType == WorkType.OFFICE }
            val prevHome = prevLogs.count { it.workType == WorkType.HOME_OFFICE }
            val prevOff = prevLogs.count { it.workType == WorkType.OFF_DAY }
            val prevExtra = prevLogs.count { it.workType == WorkType.EXTRA_WORK }
            val prevOvertime = prevLogs.count { it.workType == WorkType.OVERTIME }
            val prevIncome = prevIncomes.sumOf { it.amount }
            val prevExpense = prevExpenses.sumOf { it.amount }
            val prevNet = prevIncome - prevExpense

            PeriodComparison(
                label = formatDateLabel(prevStart, prevEnd),
                officeCount = prevOffice,
                homeCount = prevHome,
                offCount = prevOff,
                extraCount = prevExtra,
                overtimeCount = prevOvertime,
                totalIncome = prevIncome,
                totalExpense = prevExpense,
                netAmount = prevNet
            )
        } else null

        MonthlyReportUiState(
            selectedMonth = months[monthIndex],
            year = year,
            useCustomRange = useCustom,
            customStartDate = customStart,
            customEndDate = customEnd,
            compareWithPrevious = compare,
            isLoading = false,
            workLogs = filteredLogs,
            officeCount = officeCount,
            homeCount = homeCount,
            offCount = offCount,
            extraCount = extraCount,
            overtimeCount = overtimeCount,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netAmount = netAmount,
            mealExpense = mealExpense,
            totalSavingsDeposited = deposits,
            totalSavingsWithdrawn = withdrawals,
            netSavings = deposits - withdrawals,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory,
            previousPeriod = previousPeriod
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyReportUiState()
    )

    fun onMonthSelected(month: String) {
        val index = months.indexOf(month)
        if (index >= 0) {
            _selectedMonthIndex.value = index
        }
    }

    fun onYearChanged(year: Int) {
        _selectedYear.value = year
    }

    fun onYearIncrement() {
        _selectedYear.value = _selectedYear.value + 1
    }

    fun onYearDecrement() {
        _selectedYear.value = (_selectedYear.value - 1).coerceAtLeast(2000)
    }

    fun onUseCustomRangeChanged(use: Boolean) {
        _useCustomRange.value = use
        if (!use) {
            _customStartDate.value = null
            _customEndDate.value = null
        }
    }

    fun onCustomStartDateChanged(date: Long?) {
        _customStartDate.value = date
    }

    fun onCustomEndDateChanged(date: Long?) {
        _customEndDate.value = date
    }

    fun onCompareWithPreviousChanged(compare: Boolean) {
        _compareWithPrevious.value = compare
    }

    private fun computeDateRange(
        monthIndex: Int,
        year: Int,
        useCustom: Boolean,
        customStart: Long?,
        customEnd: Long?
    ): Pair<Long, Long> {
        if (useCustom && customStart != null && customEnd != null) {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = customStart
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = customEnd
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return startCal.timeInMillis to endCal.timeInMillis
        }

        val startCal = Calendar.getInstance().apply {
            set(year, monthIndex, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(year, monthIndex, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return startCal.timeInMillis to endCal.timeInMillis
    }

    private fun formatDateLabel(start: Long, end: Long): String {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val startStr = fmt.format(Date(start))
        val endFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val endStr = endFmt.format(Date(end))
        return "$startStr - $endStr"
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            return if (amount >= 0) {
                "\u09F3${"%,.0f".format(amount)}"
            } else {
                "-\u09F3${"%,.0f".format(-amount)}"
            }
        }
    }
}
