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

class ReportsViewModel(
    private val workLogRepository: WorkLogRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ReportCategory.All)
    private val _selectedDateRange = MutableStateFlow(DateRange.ThisMonth)
    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    private val _workTypeFilter = MutableStateFlow<String?>(null)
    private val _incomeCategoryFilter = MutableStateFlow<String?>(null)
    private val _expenseCategoryFilter = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(SortOption.DateNewest)

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
        val customDateRange = values[5] as Pair<Long, Long>?
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
                val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                start to end
            }
            DateRange.Yesterday -> {
                calendar.add(Calendar.DATE, -1)
                val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                start to end
            }
            DateRange.ThisWeek -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                start to end
            }
            DateRange.LastWeek -> {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
                start to end
            }
            DateRange.ThisMonth -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.timeInMillis
                start to end
            }
            DateRange.LastMonth -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.DATE, -1)
                val end = calendar.timeInMillis
                start to end
            }
            DateRange.ThisYear -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = calendar.timeInMillis
                calendar.set(Calendar.MONTH, 11) // December
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                val end = calendar.timeInMillis
                start to end
            }
            DateRange.Custom -> {
                customDateRange ?: (0L to Long.MAX_VALUE)
            }
        }

        val dateFilteredItems = allItems.filter { it.date in startTime..endTime }

        var categoryFilteredItems = when (category) {
            ReportCategory.All -> dateFilteredItems
            ReportCategory.Work -> dateFilteredItems.filterIsInstance<WorkLogReportItem>()
            ReportCategory.Income -> dateFilteredItems.filterIsInstance<IncomeReportItem>()
            ReportCategory.Expense -> dateFilteredItems.filterIsInstance<ExpenseReportItem>()
        }

        if (workTypeFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter { it is WorkLogReportItem && it.workLog.workType.name == workTypeFilter }
        }
        if (incomeCategoryFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter { it is IncomeReportItem && it.income.category == incomeCategoryFilter }
        }
        if (expenseCategoryFilter != null) {
            categoryFilteredItems = categoryFilteredItems.filter { it is ExpenseReportItem && it.expense.category.name == expenseCategoryFilter }
        }

        val sortedItems = when (sortOption) {
            SortOption.DateNewest -> categoryFilteredItems.sortedByDescending { it.date }
            SortOption.DateOldest -> categoryFilteredItems.sortedBy { it.date }
            SortOption.AmountHighest -> categoryFilteredItems.sortedByDescending { it.amount }
            SortOption.AmountLowest -> categoryFilteredItems.sortedBy { it.amount }
        }

        val totalWorkHours = categoryFilteredItems.filterIsInstance<WorkLogReportItem>().sumOf { 
            val start = it.workLog.startTime?.let { time -> SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)?.time } ?: 0L
            val end = it.workLog.endTime?.let { time -> SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)?.time } ?: 0L
            (end - start).toDouble() / (1000 * 60 * 60)
        }.toLong()
        val totalIncome = categoryFilteredItems.filterIsInstance<IncomeReportItem>().sumOf { it.income.amount }
        val totalExpense = categoryFilteredItems.filterIsInstance<ExpenseReportItem>().sumOf { it.expense.amount }
        val netProfit = totalIncome - totalExpense

        ReportUiState(
            selectedCategory = category,
            selectedDateRange = dateRange,
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

    fun onCategoryChange(category: ReportCategory) {
        _selectedCategory.value = category
    }

    fun onDateRangeChange(dateRange: DateRange) {
        _selectedDateRange.value = dateRange
    }
    fun onCustomDateRangeChange(startDate: Long, endDate: Long) {
        _customDateRange.value = startDate to endDate
        _selectedDateRange.value = DateRange.Custom
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
        viewModelScope.launch {
            val uiState = uiState.first()
            reportBuilder.append("Report (${uiState.selectedDateRange.name})\n")
            reportBuilder.append("Total Work Hours: ${uiState.totalWorkHours} hrs\n")
            reportBuilder.append("Total Income: ${uiState.totalIncome} TK\n")
            reportBuilder.append("Total Expense: ${uiState.totalExpense} TK\n")
            reportBuilder.append("Net Profit: ${uiState.netProfit} TK\n\n")

            uiState.filteredItems.forEach { item ->
                when (item) {
                    is WorkLogReportItem -> {
                        reportBuilder.append("Work Log: ${item.workLog.workType} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.workLog.date.time))}\n")
                    }
                    is IncomeReportItem -> {
                        reportBuilder.append("Income: ${item.income.amount} - ${item.income.category} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.income.timestamp))}\n")
                    }
                    is ExpenseReportItem -> {
                        reportBuilder.append("Expense: ${item.expense.amount} - ${item.expense.category} - ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.expense.timestamp))}\n")
                    }
                }
            }
        }
        return reportBuilder.toString()
    }
}
