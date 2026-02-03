package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "life_plan_goals")
data class Goal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: GoalCategory,
    val targetDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val totalTargets: Int = 0,
    val completedTargets: Int = 0,
    val isCompleted: Boolean = false
)

enum class GoalCategory {
    CAREER, HEALTH, FINANCE, LEARNING, PERSONAL, OTHER
}