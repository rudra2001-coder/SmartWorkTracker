package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long,
    val notes: String? = null,
    val tags: String? = null, // Comma-separated tags like "work", "exercise", "nutrition"

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class HealthMetricType {
    WEIGHT,
    HEIGHT,
    WATER,
    SLEEP,
    BREAKS,      // New: Number of breaks taken
    EXERCISE,    // New: Exercise minutes
    SCREEN_TIME, // New: Screen time in minutes
    POSTURE,     // New: Posture check (1 = good, 0 = bad)
    FOCUS,       // New: Focus time in minutes
    CALORIES,
    PROTEIN,
    CARBS,
    FIBER
}
