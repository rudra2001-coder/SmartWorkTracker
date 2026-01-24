package com.rudra.smartworktracker

import android.app.Application
import androidx.work.*
import com.rudra.smartworktracker.data.backup.AutoBackupWorker
import java.util.*
import java.util.concurrent.TimeUnit

class SmartWorkTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleDailyBackup()
    }

    private fun scheduleDailyBackup() {
        val constraints = Constraints.Builder()
            .setRequiresStorageNotLow(true)
            .build()

        // Calculate time until 12:05 AM to avoid overlap with midnight system tasks
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 0)
        dueDate.set(Calendar.MINUTE, 5)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val initialDelay = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyBackupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag("daily_backup")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_backup_work",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyBackupRequest
        )
    }
}
