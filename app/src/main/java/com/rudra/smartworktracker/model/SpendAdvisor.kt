package com.rudra.smartworktracker.model

import androidx.compose.ui.graphics.Color

data class SpendAdvisor(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val currentBalance: Double = 0.0,
    val dailyAverageExpense: Double = 0.0,
    val monthlyGoal: Double = 30000.0,
    val spendingTrend: SpendingTrend = SpendingTrend.STABLE,
    val trendData: List<Double> = emptyList(),
    val categoryBreakdown: Map<ExpenseCategory, Double> = emptyMap()
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

data class ExpenseAnalysis(
    val advice: ExpenseAdvice,
    val remainingAfterExpense: Double,
    val safeLimit: Double,
    val warningLimit: Double,
    val suggestion: String? = null,
    val confidenceScore: Int = 0,
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val timestamp: Long = System.currentTimeMillis()
)

data class SavingsTip(
    val id: String,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
