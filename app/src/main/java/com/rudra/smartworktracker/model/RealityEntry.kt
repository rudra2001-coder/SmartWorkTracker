package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reality_entries")
data class RealityEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: RealityEntryType,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val targetDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val category: RealityCategory = RealityCategory.GENERAL
)

enum class RealityEntryType {
    GOAL,       // Things user wants to achieve
    PROMISE,    // Commitments made to others
    PLAN        // Planned tasks/actions
}

enum class RealityCategory {
    WORK, HEALTH, LEARNING, PERSONAL, FINANCE, SOCIAL, OTHER, GENERAL
}
