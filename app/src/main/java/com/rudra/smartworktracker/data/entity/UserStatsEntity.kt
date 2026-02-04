package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_gamification_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val experiencePoints: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val totalGoalsCompleted: Int = 0,
    val lastActiveDate: String = "", // Store as ISO-8601 string
    val streakProtectionAvailable: Int = 0,
    val xpMultiplier: Float = 1.0f
)