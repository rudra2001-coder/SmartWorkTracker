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
    var isAutoCalculated: Boolean,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
