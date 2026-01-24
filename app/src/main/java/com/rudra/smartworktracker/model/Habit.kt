package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.util.UUID

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val streak: Int = 0,
    val difficulty: HabitDifficulty = HabitDifficulty.MEDIUM,
    val triggerHabitId: String? = null,
    val lastCompleted: Long? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class HabitDifficulty {
    EASY,
    MEDIUM,
    HARD
}
