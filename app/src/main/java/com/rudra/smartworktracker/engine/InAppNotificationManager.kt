package com.rudra.smartworktracker.engine

import android.content.Context
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.InAppNotification
import com.rudra.smartworktracker.data.entity.NotificationType
import com.rudra.smartworktracker.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InAppNotificationManager private constructor(context: Context) {

    private val repository: NotificationRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val db = AppDatabase.getDatabase(context)
        repository = NotificationRepository(db.inAppNotificationDao())
    }

    fun show(
        title: String,
        message: String,
        type: NotificationType,
        source: String? = null,
        referenceId: String? = null,
        actionRoute: String? = null
    ) {
        scope.launch {
            repository.insert(
                InAppNotification(
                    title = title,
                    message = message,
                    type = type,
                    source = source,
                    referenceId = referenceId,
                    actionRoute = actionRoute,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun showRecurring(title: String, message: String, referenceId: String? = null, actionRoute: String? = "recurring") {
        show(title, message, NotificationType.RECURRING, "Recurring System", referenceId, actionRoute)
    }

    fun showBackup(title: String, message: String, referenceId: String? = null, actionRoute: String? = "backup") {
        show(title, message, NotificationType.BACKUP, "Backup System", referenceId, actionRoute)
    }

    fun showAlarm(title: String, message: String, referenceId: String? = null, actionRoute: String? = "scheduler") {
        show(title, message, NotificationType.ALARM, "Alarm", referenceId, actionRoute)
    }

    fun showFocus(title: String, message: String, referenceId: String? = null, actionRoute: String? = "focus") {
        show(title, message, NotificationType.FOCUS, "Focus", referenceId, actionRoute)
    }

    fun showTeam(title: String, message: String, referenceId: String? = null, actionRoute: String? = "team") {
        show(title, message, NotificationType.TEAM, "Team", referenceId, actionRoute)
    }

    fun showSystem(title: String, message: String, actionRoute: String? = null) {
        show(title, message, NotificationType.SYSTEM, "System", null, actionRoute)
    }

    companion object {
        @Volatile
        private var INSTANCE: InAppNotificationManager? = null

        fun getInstance(context: Context): InAppNotificationManager {
            return INSTANCE ?: synchronized(this) {
                val instance = InAppNotificationManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
