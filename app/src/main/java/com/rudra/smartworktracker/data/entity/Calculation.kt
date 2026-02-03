package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "calculations")
data class Calculation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val dailyMealRate: Double = 60.0,
    val overtimeRate: Double = 0.0,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastUpdated: Long
) : BaseEntity
