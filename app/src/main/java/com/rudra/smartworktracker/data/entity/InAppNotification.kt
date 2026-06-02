package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "in_app_notifications",
    indices = [
        Index(value = ["isRead"]),
        Index(value = ["type"]),
        Index(value = ["timestamp"])
    ]
)
data class InAppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: NotificationType,
    val source: String? = null,
    val referenceId: String? = null,
    val actionRoute: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType(val displayName: String) {
    RECURRING("Recurring"),
    BACKUP("Backup"),
    ALARM("Alarm"),
    FOCUS("Focus"),
    TEAM("Team"),
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer"),
    SYSTEM("System")
}
