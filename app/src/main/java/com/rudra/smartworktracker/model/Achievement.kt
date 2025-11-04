package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val type: AchievementType,
    val threshold: Int, // e.g., 7 for a 7-day streak
    val unlocked: Boolean = false,
    val unlockedTimestamp: Long? = null
)

enum class AchievementType {
    STREAK,
    FOCUS
}
