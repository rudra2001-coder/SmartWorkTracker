package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_inputs")
data class MonthlyInput(
    @PrimaryKey
    val month: String, // YYYY-MM
    var totalWorkingDays: Int,
    var totalMeals: Int,
    var totalOvertimeHours: Double,
    var isAutoCalculated: Boolean
)
