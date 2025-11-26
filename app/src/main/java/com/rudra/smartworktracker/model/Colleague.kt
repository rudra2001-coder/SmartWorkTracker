package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "colleagues")
data class Colleague(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
    val needToFollowUpSoon: Boolean = false
)
