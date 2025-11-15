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
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class ReportCategory { All, Work, Income, Expense }
enum class DateRange { ThisMonth, LastMonth, AllTime }

class ReportsViewModel(
    workLogRepository: WorkLogRepository,
    expenseRepository: ExpenseRepository,
    incomeRepository: IncomeRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ReportCategory.All)
    private val _selectedDateRange = MutableStateFlow(DateRange.ThisMonth)

    val uiState: StateFlow<ReportUiState> = combine(
        workLogRepository.getAllWorkLogs(),
        expenseRepository.getAllExpenses(),
        incomeRepository.getAllIncomes(),
        _selectedCategory,
        _selectedDateRange
    ) { workLogs: List<WorkLog>, expenses: List<Expense>, incomes: List<Income>, category: ReportCategory, dateRange: DateRange ->
        val allItems = workLogs.map { WorkLogReportItem(it) } +
                incomes.map { IncomeReportItem(it) } +
                expenses.map { ExpenseReportItem(it) }

        val calendar = Calendar.getInstance()
        val (startTime, endTime) = when (dateRange) {
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
            DateRange.AllTime -> 0L to Long.MAX_VALUE
        }

        val dateFilteredItems = allItems.filter { it.date in startTime..endTime }

        val categoryFilteredItems = when (category) {
            ReportCategory.All -> dateFilteredItems
            ReportCategory.Work -> dateFilteredItems.filterIsInstance<WorkLogReportItem>()
            ReportCategory.Income -> dateFilteredItems.filterIsInstance<IncomeReportItem>()
            ReportCategory.Expense -> dateFilteredItems.filterIsInstance<ExpenseReportItem>()
        }

        ReportUiState(
            selectedCategory = category,
            selectedDateRange = dateRange,
            filteredItems = categoryFilteredItems.sortedByDescending { it.date }
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
}
