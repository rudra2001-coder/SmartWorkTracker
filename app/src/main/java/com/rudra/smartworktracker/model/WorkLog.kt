package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.util.Date

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val date: Date,
    val workType: WorkType,
    val startTime: String?,
    val endTime: String?,
    val isOvertime: Boolean = false,
    val overtimeRate: Double? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class WorkType {
    OFFICE,
    HOME_OFFICE,
    OFF_DAY,
    EXTRA_WORK,
    OVERTIME
}
