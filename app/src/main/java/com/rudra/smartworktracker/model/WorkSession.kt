package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus

@Entity(tableName = "work_sessions")
data class WorkSession(
    @PrimaryKey val id: String,
    
    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val startTime: Long,
    val endTime: Long?,
    val type: SessionType,
    val breaks: List<BreakPeriod>,
    val productivityScore: Int?,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

data class BreakPeriod(
    val startTime: Long,
    val endTime: Long
)

enum class SessionType {
    WORK,
    BREAK,
    LUNCH
}
