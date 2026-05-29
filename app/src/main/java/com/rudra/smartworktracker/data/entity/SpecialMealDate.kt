package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "special_meal_dates",
    indices = [Index(value = ["date"], unique = true)]
)
data class SpecialMealDate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
