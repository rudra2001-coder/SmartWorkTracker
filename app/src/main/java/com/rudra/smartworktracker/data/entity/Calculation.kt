package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class Calculation(
    @PrimaryKey val id: Int = 1,
    val mealRate: Double = 0.0,
    val overtimeRate: Double = 0.0
)
