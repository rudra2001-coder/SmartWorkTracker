package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_meal_rates",
    indices = [Index(value = ["mealTypeId", "weekNumber", "year"], unique = true)]
)
data class WeeklyMealRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealTypeId: Int,
    val rate: Double,
    val weekNumber: Int,
    val year: Int,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
