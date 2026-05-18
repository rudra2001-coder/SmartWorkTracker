package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_rate_settings")
data class MealRateSetting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String = "",
    val rate: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis(),
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
