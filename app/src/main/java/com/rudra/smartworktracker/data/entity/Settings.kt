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
    val language: Language = Language.ENGLISH,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    // Audit fields - Rule 1.2
    // Nullable for safe migration from version 25 to 26
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
