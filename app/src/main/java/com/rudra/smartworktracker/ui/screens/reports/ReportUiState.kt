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
    val sortOption: SortOption = SortOption.DateNewest,
    val fullReport: ReportsFullData = ReportsFullData()
)

data class ReportsFullData(
    val workSessionCount: Int = 0,
    val workSessionHours: Double = 0.0,
    val workDayCount: Int = 0,
    val habitCount: Int = 0,
    val focusSessionCount: Int = 0,
    val focusSessionMinutes: Long = 0,
    val healthMetricCount: Int = 0,
    val achievementCount: Int = 0,
    val achievementsUnlocked: Int = 0,
    val journalCount: Int = 0,
    val checkInCount: Int = 0,
    val decisionCount: Int = 0,
    val positiveDecisions: Int = 0,
    val negativeDecisions: Int = 0,
    val realityPlanned: Int = 0,
    val realityCompleted: Int = 0,
    val activeLoanCount: Int = 0,
    val totalLoanAmount: Double = 0.0,
    val totalRemainingLoan: Double = 0.0,
    val borrowedLoanCount: Int = 0,
    val lentLoanCount: Int = 0,
    val totalBorrowedRemaining: Double = 0.0,
    val totalLentRemaining: Double = 0.0,
    val activeEmiCount: Int = 0,
    val pendingEmiCount: Int = 0,
    val overdueEmiCount: Int = 0,
    val totalPendingEmiAmount: Double = 0.0,
    val creditCardCount: Int = 0,
    val totalCreditCardDebt: Double = 0.0,
    val totalCreditCardLimit: Double = 0.0,
    val financialTxCount: Int = 0,
    val financialTxIncome: Double = 0.0,
    val financialTxExpense: Double = 0.0,
    val activeRecurringRules: Int = 0,
    val recurringTxCount: Int = 0,
    val pendingRecurringTxCount: Int = 0,
    val mealCount: Int = 0,
    val mealTotalCost: Double = 0.0,
    val specialMealDateCount: Int = 0,
    val colleagueCount: Int = 0,
    val scheduleCount: Int = 0,
    val travelExpenseAmount: Double = 0.0,
    val totalDebtAmount: Double = 0.0,
    val weeklyReportCount: Int = 0,
    val monthlyInputCount: Int = 0
)