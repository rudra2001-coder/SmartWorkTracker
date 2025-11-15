package com.rudra.smartworktracker.ui.screens.reports

import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.WorkLog

sealed interface ReportListItem {
    val date: Long
}

data class WorkLogReportItem(val workLog: WorkLog) : ReportListItem {
    override val date: Long = workLog.date.time
}

data class IncomeReportItem(val income: Income) : ReportListItem {
    override val date: Long = income.timestamp
}

data class ExpenseReportItem(val expense: Expense) : ReportListItem {
    override val date: Long = expense.timestamp
}

data class ReportUiState(
    val selectedCategory: ReportCategory = ReportCategory.All,
    val selectedDateRange: DateRange = DateRange.ThisMonth,
    val filteredItems: List<ReportListItem> = emptyList()
)
