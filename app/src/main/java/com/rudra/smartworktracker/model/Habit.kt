package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val streak: Int,
    val difficulty: HabitDifficulty,
    val triggerHabitId: String?,
    val createdAt: Long,
    val lastCompleted: Long? = null
)

enum class HabitDifficulty {
    EASY,
    MEDIUM,
    HARD
}
