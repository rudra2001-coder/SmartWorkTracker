package com.rudra.smartworktracker.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all alarms after device reboot
            CoroutineScope(Dispatchers.IO).launch {
                // You would need to fetch all schedules from database
                // and reschedule them using AlarmScheduler
                val alarmScheduler = AlarmScheduler(context)
                // alarmScheduler.rescheduleAll(schedules)
            }
        }
    }
}
