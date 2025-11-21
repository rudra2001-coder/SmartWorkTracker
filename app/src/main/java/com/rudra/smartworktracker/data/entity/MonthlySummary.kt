package com.rudra.smartworktracker.data.entity

import androidx.room.DatabaseView

@DatabaseView("""
    SELECT
        strftime('%Y-%m', date / 1000, 'unixepoch') AS month,
        SUM(CASE WHEN workType = 'OFFICE' THEN 1 ELSE 0 END) AS totalWorkDays,
        SUM(CASE WHEN category = 'MEAL' THEN 1 ELSE 0 END) AS totalMeals,
        SUM(CASE WHEN category = 'MEAL' THEN amount ELSE 0 END) AS totalMealCost,
        0.0 AS totalOvertimeHours, -- Placeholder
        0.0 AS totalOvertimePay, -- Placeholder
        SUM(amount) AS totalExpense
    FROM
        work_logs
    LEFT JOIN
        expenses ON strftime('%Y-%m-%d', work_logs.date / 1000, 'unixepoch') = strftime('%Y-%m-%d', expenses.timestamp / 1000, 'unixepoch')
    GROUP BY
        month
""")
data class MonthlySummary(
    val month: String, // YYYY-MM
    val totalWorkDays: Int,
    val totalMeals: Int,
    val totalMealCost: Double,
    val totalOvertimeHours: Double,
    val totalOvertimePay: Double,
    val totalExpense: Double
)
