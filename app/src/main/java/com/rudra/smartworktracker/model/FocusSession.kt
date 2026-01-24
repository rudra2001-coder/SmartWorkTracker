package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey val id: String,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,
    val type: FocusType,
    val duration: Long,
    val interruptions: Int,
    val focusScore: Int,
    val timestamp: Long,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class FocusType {
    DEEP_WORK,
    POMODORO
}
