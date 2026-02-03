package com.rudra.smartworktracker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.rudra.smartworktracker.MainActivity
import com.rudra.smartworktracker.R
import com.rudra.smartworktracker.model.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val CHANNEL_NAME = "Alarm Notifications"
        const val ACTION_SNOOZE = "com.rudra.smartworktracker.ALARM_SNOOZE"
        const val ACTION_DISMISS = "com.rudra.smartworktracker.ALARM_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("SCHEDULE_ID", -1L)
        val title = intent.getStringExtra("SCHEDULE_TITLE") ?: "Schedule Alarm"

        when (intent.action) {
            ACTION_SNOOZE -> {
                snoozeAlarm(context, scheduleId, title, 5)
                dismissAlarm(context, scheduleId)
            }
            ACTION_DISMISS -> {
                dismissAlarm(context, scheduleId)
            }
            else -> {
                // Regular alarm trigger
                showAlarmNotification(context, title, scheduleId)
                // The sound and vibration are now handled by AlarmActivity or the Notification itself
            }
        }
    }

    private fun dismissAlarm(context: Context, scheduleId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(scheduleId.toInt())
        
        // Broadcast to AlarmActivity to stop if it's running
        val stopIntent = Intent("com.rudra.smartworktracker.STOP_ALARM")
        context.sendBroadcast(stopIntent)
    }

    private fun snoozeAlarm(context: Context, scheduleId: Long, title: String, minutes: Int) {
        val snoozeTime = LocalTime.now().plusMinutes(minutes.toLong())
        val alarmScheduler = AlarmScheduler(context)
        
        val snoozeSchedule = Schedule(
            id = if (scheduleId != -1L) scheduleId + 1000000 else System.currentTimeMillis(),
            title = "Snooze: $title",
            time = snoozeTime,
            isEnabled = true,
            isRepeating = false
        )
        
        alarmScheduler.schedule(snoozeSchedule)
    }

    private fun showAlarmNotification(
        context: Context,
        title: String,
        scheduleId: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                enableVibration(true)
                setShowBadge(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 1. Full Screen Intent (AlarmActivity)
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("SCHEDULE_TITLE", title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Dismiss Action
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra("SCHEDULE_ID", scheduleId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt() + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Snooze Action
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("SCHEDULE_ID", scheduleId)
            putExtra("SCHEDULE_TITLE", title)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.playstore)
            .setContentTitle("⏰ $title")
            .setContentText("It's time for your schedule!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 5m", snoozePendingIntent)
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}
