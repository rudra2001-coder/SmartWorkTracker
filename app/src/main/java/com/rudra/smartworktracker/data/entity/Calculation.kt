package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class Calculation(
    @PrimaryKey val id: Int = 1,
    val dailyMealRate: Double = 60.0, // Default meal rate is 60 Taka
    val overtimeRate: Double = 0.0
)
