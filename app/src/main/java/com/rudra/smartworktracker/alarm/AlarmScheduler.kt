package com.rudra.smartworktracker.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rudra.smartworktracker.model.Schedule
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(schedule: Schedule) {
        if (!schedule.isEnabled) return

        if (schedule.isRepeating && schedule.repeatingDays.isNotEmpty()) {
            scheduleRepeatingAlarm(schedule)
        } else {
            scheduleOneTimeAlarm(schedule)
        }
    }

    private fun scheduleOneTimeAlarm(schedule: Schedule) {
        val alarmTime = calculateNextAlarmTime(schedule.time, false)

        val intent = createAlarmIntent(schedule)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        }
    }

    private fun scheduleRepeatingAlarm(schedule: Schedule) {
        // Schedule separate alarm for each repeating day
        schedule.repeatingDays.forEach { dayOfWeek ->
            scheduleAlarmForDay(schedule, dayOfWeek)
        }
    }

    private fun scheduleAlarmForDay(schedule: Schedule, dayOfWeek: Int) {
        val alarmTime = calculateNextAlarmTimeForDay(schedule.time, dayOfWeek)

        // Create unique request code for each day
        val requestCode = (schedule.id * 10 + dayOfWeek).toInt()

        val intent = createAlarmIntent(schedule)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        }
    }

    private fun calculateNextAlarmTime(alarmTime: LocalTime, isRepeating: Boolean): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            set(Calendar.MINUTE, alarmTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time has passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun calculateNextAlarmTimeForDay(alarmTime: LocalTime, dayOfWeek: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            set(Calendar.MINUTE, alarmTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)

            // If time has passed for this day this week, schedule for next week
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_WEEK, 7)
            }
        }
        return calendar.timeInMillis
    }

    private fun createAlarmIntent(schedule: Schedule): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_TITLE", schedule.title)
            putExtra("IS_REPEATING", schedule.isRepeating)
            putExtra("REPEATING_DAYS", schedule.repeatingDays.toIntArray())
        }
    }

    fun cancel(schedule: Schedule) {
        if (schedule.isRepeating && schedule.repeatingDays.isNotEmpty()) {
            // Cancel all alarms for each day
            schedule.repeatingDays.forEach { dayOfWeek ->
                val requestCode = (schedule.id * 10 + dayOfWeek).toInt()
                cancelAlarmWithRequestCode(requestCode)
            }
        } else {
            // Cancel single alarm
            cancelAlarmWithRequestCode(schedule.id.toInt())
        }
    }

    private fun cancelAlarmWithRequestCode(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAll(schedules: List<Schedule>) {
        // Cancel all existing alarms first
        schedules.forEach { cancel(it) }

        // Schedule all enabled alarms
        schedules.filter { it.isEnabled }.forEach { schedule(it) }
    }
}
