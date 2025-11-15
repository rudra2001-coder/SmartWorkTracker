package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val mealRate: Double,
    val overtimeRate: Double,
    val dailyWorkHours: Double,
    val workingDaysPerWeek: Int,
    val isDarkTheme: Boolean = false,
    val language: Language = Language.ENGLISH
)
