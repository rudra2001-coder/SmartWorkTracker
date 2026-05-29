package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_settings")
data class MealSettings(
    @PrimaryKey
    val id: String = "meal_settings",
    val normalMealRate: Double = 70.0,
    val specialMealRate: Double = 90.0,
    val mealDays: Set<Int>? = null,
    val isEnabled: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastUpdated: Long = System.currentTimeMillis()
) : BaseEntity
