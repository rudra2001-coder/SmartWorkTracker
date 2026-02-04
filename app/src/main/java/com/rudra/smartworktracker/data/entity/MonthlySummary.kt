package com.rudra.smartworktracker.data.entity

import androidx.room.DatabaseView
import androidx.room.Ignore

@DatabaseView("""
    SELECT
        strftime('%Y-%m', wl.date / 1000, 'unixepoch') AS month,
        SUM(CASE WHEN wl.workType = 'OFFICE' THEN 1 ELSE 0 END) AS totalWorkDays,
        COUNT(DISTINCT CASE WHEN ex.category = 'MEAL' THEN ex.id END) AS totalMeals,
        SUM(CASE WHEN ex.category = 'MEAL' THEN ex.amount ELSE 0 END) AS totalMealCost,
        SUM(CASE WHEN wl.isOvertime = 1 THEN 
            (strftime('%s', wl.endTime) - strftime('%s', wl.startTime)) / 3600.0 
            ELSE 0 END) AS totalOvertimeHours,
        SUM(CASE WHEN wl.isOvertime = 1 THEN 
            ((strftime('%s', wl.endTime) - strftime('%s', wl.startTime)) / 3600.0) * wl.overtimeRate 
            ELSE 0 END) AS totalOvertimePay,
        SUM(IFNULL(ex.amount, 0)) AS totalExpense
    FROM
        work_logs wl
    LEFT JOIN
        expenses ex ON strftime('%Y-%m-%d', wl.date / 1000, 'unixepoch') = strftime('%Y-%m-%d', ex.timestamp / 1000, 'unixepoch')
    GROUP BY
        month
""")
data class MonthlySummary(
    val month: String,
    val totalWorkDays: Int,
    val totalMeals: Int,
    val totalMealCost: Double,
    val totalOvertimeHours: Double,
    val totalOvertimePay: Double,
    
    @Ignore
    val uuid: String? = null,

    val totalExpense: Double
) {
    constructor(
        month: String,
        totalWorkDays: Int,
        totalMeals: Int,
        totalMealCost: Double,
        totalOvertimeHours: Double,
        totalOvertimePay: Double,
        totalExpense: Double
    ) : this(month, totalWorkDays, totalMeals, totalMealCost, totalOvertimeHours, totalOvertimePay, null, totalExpense)
}
