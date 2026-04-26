package com.rudra.smartworktracker.ui.screens.financials

import java.time.LocalDate

enum class TransactionFilter(val displayName: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense"),
    DATE_RANGE("Date Range")
}

data class FinancialsUiState(
    val transactions: List<UnifiedTransaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netFlow: Double = 0.0,
    val isLoading: Boolean = false,
    val filter: TransactionFilter = TransactionFilter.ALL,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val limit: Int = 100,
    val errorMessage: String? = null
)
