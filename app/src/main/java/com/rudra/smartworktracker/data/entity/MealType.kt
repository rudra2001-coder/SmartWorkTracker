package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_types")
data class MealType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val defaultRate: Double = 0.0,
    val sortOrder: Int = 0,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
