package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.time.LocalDate

@Entity(
    tableName = "daily_journals",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyJournal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val date: LocalDate,
    val morningIntention: String = "",
    val eveningReflection: String = "",
    val gratitude: String = "",

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
