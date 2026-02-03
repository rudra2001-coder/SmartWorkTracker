package com.rudra.smartworktracker.model

import java.util.Date
import java.util.UUID

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: GoalCategory,
    val targetDate: Date? = null,
    val createdAt: Date = Date(),
    val totalTargets: Int = 0,
    val completedTargets: Int = 0,
    val isCompleted: Boolean = false
)

enum class GoalCategory {
    CAREER, HEALTH, FINANCE, LEARNING, PERSONAL, OTHER
}