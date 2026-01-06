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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val CHANNEL_NAME = "Alarm Notifications"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("SCHEDULE_ID", -1L)
        val title = intent.getStringExtra("SCHEDULE_TITLE") ?: "Schedule Alarm"
        val isRepeating = intent.getBooleanExtra("IS_REPEATING", false)

        // Show notification
        showAlarmNotification(context, title, scheduleId)

        // Play alarm sound
        playAlarmSound(context)

        // Vibrate
        vibratePhone(context)

        // If repeating, reschedule for next time
        if (isRepeating) {
            CoroutineScope(Dispatchers.IO).launch {
                // You would need to fetch the schedule from database here
                // and reschedule it using AlarmScheduler
            }
        }
    }

    private fun showAlarmNotification(
        context: Context,
        title: String,
        scheduleId: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for when notification is tapped
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.playstore) // Make sure you have this icon
            .setContentTitle("⏰ $title")
            .setContentText("It's time for your schedule!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // Show on lock screen
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }

    private fun playAlarmSound(context: Context) {
        try {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, alarmSound)
            ringtone.play()

            // Stop alarm after 1 minute
            CoroutineScope(Dispatchers.IO).launch {
                kotlinx.coroutines.delay(60000)
                ringtone.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationPattern = longArrayOf(0, 1000, 1000, 1000, 1000, 1000)
                it.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 1000, 1000, 1000, 1000, 1000), 0)
            }

            // Stop vibration after 1 minute
            CoroutineScope(Dispatchers.IO).launch {
                kotlinx.coroutines.delay(60000)
                it.cancel()
            }
        }
    }
}
