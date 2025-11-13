package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_summaries")
data class MonthlySummary(
    @PrimaryKey
    val month: String, // YYYY-MM
    val totalWorkDays: Int,
    val totalMeals: Int,
    val totalMealCost: Double,
    val totalOvertimeHours: Double,
    val totalOvertimePay: Double,
    val totalExpense: Double
)
