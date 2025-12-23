package com.rudra.smartworktracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.WorkLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportCategory { All, Work, Income, Expense }
enum class DateRange { Today, Yesterday, ThisWeek, LastWeek, ThisMonth, LastMonth, ThisYear, Custom }
enum class SortOption { DateNewest, DateOldest, AmountHighest, AmountLowest }

// Add this data class for custom date range
data class CustomDateRange(
    val startDate: Long? = null,
    val endDate: Long? = null
)

class ReportsViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ReportCategory.All)
    private val _selectedDateRange = MutableStateFlow(DateRange.ThisMonth)
    private val _customDateRange = MutableStateFlow(CustomDateRange())
    private val _workTypeFilter = MutableStateFlow<String?>(null)
    private val _incomeCategoryFilter = MutableStateFlow<String?>(null)
    private val _expenseCategoryFilter = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.DateNewest)

    // Add new states for custom date picker
    private val _showCustomDatePicker = MutableStateFlow(false)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)

    val showCustomDatePicker: StateFlow<Boolean> = _showCustomDatePicker
    val customStartDate: StateFlow<Long?> = _customStartDate
    val customEndDate: StateFlow<Long?> = _customEndDate

    val uiState: StateFlow<ReportUiState> = combine(
        workLogRepository.getAllWorkLogs(),
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncomes(),
        _selectedCategory,
        _selectedDateRange,
        _customDateRange,
        _workTypeFilter,
        _incomeCategoryFilter,
        _expenseCategoryFilter,
        _sortOption
    ) { values ->
        val workLogs = values[0] as List<WorkLog>
        val expenses = values[1] as List<Expense>
        val incomes = values[2] as List<Income>
        val category = values[3] as ReportCategory
        val dateRange = values[4] as DateRange
        val customDateRange = values[5] as CustomDateRange
        val workTypeFilter = values[6] as String?
        val incomeCategoryFilter = values[7] as String?
        val expenseCategoryFilter = values[8] as String?
        val sortOption = values[9] as SortOption

        val allItems = workLogs.map { WorkLogReportItem(it) } +
                incomes.map { IncomeReportItem(it) } +
                expenses.map { ExpenseReportItem(it) }

        val calendar = Calendar.getInstance()
        val (startTime, endTime) = when (dateRange) {
            DateRange.Today -> {
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.Yesterday -> {
                calendar.add(Calendar.DATE, -1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.ThisWeek -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.LastWeek -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.ThisMonth -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.LastMonth -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.ThisYear -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                calendar.set(Calendar.MONTH, 11) // December
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                val end = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                start to end
            }
            DateRange.Custom -> {
                val start = customDateRange.startDate ?: 0L
                val end = customDateRange.endDate ?: Long.MAX_VALUE
                // Adjust end date to end of day
                val adjustedEnd = if (end != Long.MAX_VALUE) {
                    Calendar.getInstance().apply {
                        timeInMillis = end
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                } else end
                // Adjust start date to beginning of day
                val adjustedStart = if (start != 0L) {
                    Calendar.getInstance().apply {
                        timeInMillis = start
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                } else start
                adjustedStart to adjustedEnd
            }
        }

        val dateFilteredItems = allItems.filter {
            it.date in startTime..endTime
        }

        var categoryFilteredItems = when (category) {
            ReportCategory.All -> dateFilteredItems
            ReportCategory.Work -> dateFilteredItems.filterIsInstance<WorkLogReportItem>()
            ReportCategory.Income -> dateFilteredItems.filterIsInstance<IncomeReportItem>()
            ReportCategory.Expense -> dateFilteredItems.filterIsInstance<ExpenseReportItem>()
        }

        if (workTypeFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter {
                it is WorkLogReportItem && it.workLog.workType.name == workTypeFilter
            }
        }
        if (incomeCategoryFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter {
                it is IncomeReportItem && it.income.category == incomeCategoryFilter
            }
        }
        if (expenseCategoryFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter {
                it is ExpenseReportItem && it.expense.category.name == expenseCategoryFilter
            }
        }

        val sortedItems = when (sortOption) {
            SortOption.DateNewest -> categoryFilteredItems.sortedByDescending { it.date }
            SortOption.DateOldest -> categoryFilteredItems.sortedBy { it.date }
            SortOption.AmountHighest -> categoryFilteredItems.sortedByDescending { it.amount }
            SortOption.AmountLowest -> categoryFilteredItems.sortedBy { it.amount }
        }

        val totalWorkHours = categoryFilteredItems.filterIsInstance<WorkLogReportItem>().sumOf {
            val start = it.workLog.startTime?.let { time ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)?.time
            } ?: 0L
            val end = it.workLog.endTime?.let { time ->
                SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)?.time
            } ?: 0L
            (end - start).toDouble() / (1000 * 60 * 60)
        }.toLong()

        val totalIncome = categoryFilteredItems.filterIsInstance<IncomeReportItem>().sumOf { it.income.amount }
        val totalExpense = categoryFilteredItems.filterIsInstance<ExpenseReportItem>().sumOf { it.expense.amount }
        val netProfit = totalIncome - totalExpense

        ReportUiState(
            selectedCategory = category,
            selectedDateRange = dateRange,
            customDateRange = customDateRange,
            filteredItems = sortedItems,
            totalWorkHours = totalWorkHours,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netProfit = netProfit,
            workTypeFilter = workTypeFilter,
            incomeCategoryFilter = incomeCategoryFilter,
            expenseCategoryFilter = expenseCategoryFilter,
            sortOption = sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    // Existing functions remain the same
    fun onCategoryChange(category: ReportCategory) {
        _selectedCategory.value = category
    }

    fun onDateRangeChange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
    }

    // NEW: Function to open custom date picker
    fun showCustomDatePicker(show: Boolean) {
        _showCustomDatePicker.value = show
    }

    // NEW: Function to set custom dates
    fun setCustomStartDate(date: Long?) {
        _customStartDate.value = date
    }

    fun setCustomEndDate(date: Long?) {
        _customEndDate.value = date
    }

    // NEW: Function to apply custom date filter
    fun applyCustomDateFilter() {
        val start = _customStartDate.value
        val end = _customEndDate.value

        if (start != null && end != null) {
            _customDateRange.value = CustomDateRange(start, end)
            _selectedDateRange.value = DateRange.Custom
        }
    }

    // NEW: Function to clear custom date filter
    fun clearCustomDateFilter() {
        _customDateRange.value = CustomDateRange()
        _customStartDate.value = null
        _customEndDate.value = null
        _selectedDateRange.value = DateRange.ThisMonth
    }

    fun onWorkTypeFilterChange(workType: String?) {
        _workTypeFilter.value = workType
    }

    fun onIncomeCategoryFilterChange(category: String?) {
        _incomeCategoryFilter.value = category
    }

    fun onExpenseCategoryFilterChange(category: String?) {
        _expenseCategoryFilter.value = category
    }

    fun onSortOptionChange(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun generateTextReport(): String {
        val reportBuilder = StringBuilder()

        val uiStateValue = uiState.value

        reportBuilder.append("Report (${uiStateValue.selectedDateRange.name})\n")

        if (uiStateValue.selectedDateRange == DateRange.Custom &&
            uiStateValue.customDateRange != null) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDate = if (uiStateValue.customDateRange.startDate != null)
                sdf.format(Date(uiStateValue.customDateRange.startDate!!))
            else "N/A"
            val endDate = if (uiStateValue.customDateRange.endDate != null)
                sdf.format(Date(uiStateValue.customDateRange.endDate!!))
            else "N/A"
            reportBuilder.append("Date Range: $startDate to $endDate\n")
        }

        reportBuilder.append("Total Work Hours: ${uiStateValue.totalWorkHours} hrs\n")
        reportBuilder.append("Total Income: ${uiStateValue.totalIncome} TK\n")
        reportBuilder.append("Total Expense: ${uiStateValue.totalExpense} TK\n")
        reportBuilder.append("Net Profit: ${uiStateValue.netProfit} TK\n\n")
        reportBuilder.append("Details:\n")

        uiStateValue.filteredItems.forEach { item ->
            when (item) {
                is WorkLogReportItem -> {
                    reportBuilder.append("Work Log: ${item.workLog.workType} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.workLog.date.time))}\n")
                }
                is IncomeReportItem -> {
                    reportBuilder.append("Income: ${item.income.amount} TK - ${item.income.category} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.income.timestamp))}\n")
                }
                is ExpenseReportItem -> {
                    reportBuilder.append("Expense: ${item.expense.amount} TK - ${item.expense.category} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.expense.timestamp))}\n")
                }
            }
        }

        return reportBuilder.toString()
    }
}