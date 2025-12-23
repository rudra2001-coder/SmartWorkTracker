package com.rudra.smartworktracker.ui.screens.reports

import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.WorkLog

sealed interface ReportListItem {
    val date: Long
    val amount: Double
}

data class WorkLogReportItem(val workLog: WorkLog) : ReportListItem {
    override val date: Long = workLog.date.time
    override val amount: Double = 0.0
}

data class IncomeReportItem(val income: Income) : ReportListItem {
    override val date: Long = income.timestamp
    override val amount: Double = income.amount
}

data class ExpenseReportItem(val expense: Expense) : ReportListItem {
    override val date: Long = expense.timestamp
    override val amount: Double = expense.amount
}

data class ReportUiState(
    val selectedCategory: ReportCategory = ReportCategory.All,
    val selectedDateRange: DateRange = DateRange.ThisMonth,
    val customDateRange: CustomDateRange? = null,
    val filteredItems: List<ReportListItem> = emptyList(),
    val totalWorkHours: Long = 0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val workTypeFilter: String? = null,
    val incomeCategoryFilter: String? = null,
    val expenseCategoryFilter: String? = null,
    val sortOption: SortOption = SortOption.DateNewest
)