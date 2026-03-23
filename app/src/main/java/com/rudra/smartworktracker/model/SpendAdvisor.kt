package com.rudra.smartworktracker.model

import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.Expense

data class SpendAdvisor(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0,
    val dailyAverageExpense: Double = 0.0,
    val monthlyGoal: Double = 0.0,
    val spendingTrend: SpendingTrend = SpendingTrend.STABLE,
    val recentExpenses: List<Expense> = emptyList(),
    val recentIncomes: List<Income> = emptyList()
)

enum class SpendingTrend(val displayName: String) {
    INCREASING("Increasing"),
    DECREASING("Decreasing"),
    STABLE("Stable")
}

enum class ExpenseAdvice(val title: String, val description: String, val severity: AdviceSeverity) {
    SAFE(
        title = "Good to Go",
        description = "This expense is within your budget. You can proceed!",
        severity = AdviceSeverity.GOOD
    ),
    RISKY(
        title = "Caution",
        description = "This is slightly high. It may reduce your savings.",
        severity = AdviceSeverity.WARNING
    ),
    NOT_ACCEPTABLE(
        title = "Not Recommended",
        description = "This expense is too high and may affect other expenses.",
        severity = AdviceSeverity.DANGER
    )
}

enum class AdviceSeverity {
    GOOD,
    WARNING,
    DANGER
}

data class PlannedExpense(
    val amount: Double,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)

data class ExpenseAnalysis(
    val advice: ExpenseAdvice,
    val remainingAfterExpense: Double,
    val safeLimit: Double,
    val warningLimit: Double,
    val suggestion: String? = null,
    val confidenceScore: Int = 0
)
