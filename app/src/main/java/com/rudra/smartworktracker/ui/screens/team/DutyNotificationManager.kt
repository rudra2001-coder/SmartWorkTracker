package com.rudra.smartworktracker.ui.screens.team

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.rudra.smartworktracker.engine.InAppNotificationManager

class DutyNotificationManager(private val context: Context) {
    private val channelId = "team_duty_ops"
    private val channelName = "Team Duty Operations"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for duty swaps and team assignments"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendSwapRequestNotification(requesterName: String, date: String) {
        InAppNotificationManager.getInstance(context).showTeam(
            title = "New Duty Swap Request",
            message = "$requesterName wants to swap their duty on $date",
            actionRoute = "team"
        )
        if (checkPermission()) {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Duty Swap Request")
                .setContentText("$requesterName wants to swap their duty on $date")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }

    fun sendSwapApprovalNotification(date: String) {
        InAppNotificationManager.getInstance(context).showTeam(
            title = "Duty Swap Approved",
            message = "The duty swap for $date has been finalized.",
            actionRoute = "team"
        )
        if (checkPermission()) {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Duty Swap Approved")
                .setContentText("The duty swap for $date has been finalized.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } catch (e: SecurityException) {
                // Permission not granted
            }
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
