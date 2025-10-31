package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val workType: WorkType,
    val startTime: String?,
    val endTime: String?
)