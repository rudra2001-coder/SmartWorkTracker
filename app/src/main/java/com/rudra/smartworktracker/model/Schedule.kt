package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rudra.smartworktracker.data.entity.BaseEntity
import com.rudra.smartworktracker.data.entity.SyncStatus
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String,
    val time: LocalTime,
    val isEnabled: Boolean = true,
    val isRepeating: Boolean = false,
    val repeatingDays: Set<Int> = emptySet(),
    val ringtoneUri: String? = null,
    val ringtoneName: String = "Default",
    val vibrationPattern: String = "default",
    val volumeLevel: Int = 80,
    val description: String? = null,
    val category: String = "General",
    val snoozeDuration: Int = 5, // minutes
    val maxSnoozeCount: Int = 3,
    val isImportant: Boolean = false,
    val colorTag: Int? = null,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity {
    fun getNextTriggerTime(): LocalDateTime {
        val now = LocalDateTime.now()
        val todayAtTime = now.with(time)
        
        return if (todayAtTime.isAfter(now)) {
            todayAtTime
        } else {
            todayAtTime.plusDays(1)
        }
    }
    
    fun getRepeatingDaysText(): String {
        if (!isRepeating) return "Once"
        if (repeatingDays.isEmpty()) return "Daily"
        if (repeatingDays.size == 7) return "Every day"
        
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val selectedDays = repeatingDays.sorted().map { dayNames[it - 1] }
        return selectedDays.joinToString(", ")
    }
}
