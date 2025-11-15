package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_days")
data class WorkDay(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val meals: Int,
    val overtimeHours: Double
)
