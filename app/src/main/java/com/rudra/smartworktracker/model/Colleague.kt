package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.time.LocalDate

@Entity(tableName = "colleagues")
data class Colleague(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val fullName: String,
    val designation: String,
    val department: String,
    val avatar: String? = null,
    val workEmail: String,
    val phoneNumber: String,
    val workLocation: String,
    val joiningDate: LocalDate,
    val reportingManager: String,
    val workingShift: String,
    val skillTags: List<String>,
    val strengths: String,
    val relationshipType: String,
    val lastMeetingDate: LocalDate? = null,
    val totalMeetings: Int = 0,
    val lastCollaborationDate: LocalDate? = null,
    val meetingNotes: List<String> = emptyList(),
    val taskCollaborationHistory: List<String> = emptyList(),
    val sharedFiles: List<String> = emptyList(),
    val collaborationRating: Float = 0.0f,
    val interactionFrequency: Float = 0.0f,
    val productivityAlignment: Float = 0.0f,
    val trustScore: Float = 0.0f,
    val personalNotes: String = "",
    val reminderToFollowUp: String = "",
    val importantBehaviors: String = "",
    val isImportant: Boolean = false,
    val worksClosely: Boolean = false,
    val onLeave: Boolean = false,
    val needToFollowUpSoon: Boolean = false,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
