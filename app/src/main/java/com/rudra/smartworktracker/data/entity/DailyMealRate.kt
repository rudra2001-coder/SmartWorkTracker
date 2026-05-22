package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_meal_rates",
    indices = [Index(value = ["mealTypeId", "date"], unique = true)]
)
data class DailyMealRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealTypeId: Int,
    val rate: Double,
    val date: Long,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
