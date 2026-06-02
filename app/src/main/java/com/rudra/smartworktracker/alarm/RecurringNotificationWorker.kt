package com.rudra.smartworktracker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rudra.smartworktracker.MainActivity
import com.rudra.smartworktracker.R
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.data.repository.SavingsRepository
import com.rudra.smartworktracker.engine.FusionEngine
import com.rudra.smartworktracker.engine.InAppNotificationManager
import com.rudra.smartworktracker.engine.RecurringEngine
import java.util.concurrent.TimeUnit

class RecurringNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "recurring_notification_worker"
        const val CHANNEL_ID = "recurring_transactions"
        const val NOTIFICATION_ID = 1001

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<RecurringNotificationWorker>(
                1, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel()

            val database = AppDatabase.getDatabase(context)
            val repository = RecurringRepository(
                database.recurringRuleDao(),
                database.recurringTransactionDao()
            )
            val incomeRepository = IncomeRepository(database.incomeDao(), database.accountDao())
            val expenseRepository = ExpenseRepository(database.expenseDao(), database.accountDao())
            val savingsRepository = SavingsRepository(database.savingsDao(), database.accountDao(), database.financialTransactionDao())
            val fusionEngine = FusionEngine(database.accountDao(), database.financialTransactionDao())
            val engine = RecurringEngine(
                repository, incomeRepository, expenseRepository,
                database.accountDao(), savingsRepository, fusionEngine
            )

            val results = engine.processDueRules()

            val upcomingTransactions = engine.getUpcomingTransactions(1)

            if (upcomingTransactions.isNotEmpty()) {
                sendUpcomingNotification(upcomingTransactions.size, upcomingTransactions.first().amount)
            }

            val failedCount = results.count { !it.success }
            if (failedCount > 0) {
                val failedReasons = results.filter { !it.success }.mapNotNull { it.reason }
                sendFailureNotification(failedCount, failedReasons.take(3))
            }

            val successCount = results.count { it.success }
            if (successCount > 0) {
                val totalAmount = results.filter { it.success }.sumOf { 0.0 }
                sendSuccessNotification(successCount)
            }

            val notifManager = InAppNotificationManager.getInstance(context)
            if (successCount > 0) {
                notifManager.showRecurring(
                    "Recurring Transactions Executed",
                    "$successCount recurring transaction(s) processed successfully"
                )
            }
            if (failedCount > 0) {
                notifManager.showRecurring(
                    "Recurring Transactions Failed",
                    "$failedCount recurring transaction(s) failed. Check rules for details."
                )
            }
            if (upcomingTransactions.isNotEmpty()) {
                notifManager.showRecurring(
                    "Upcoming Recurring Transactions",
                    "${upcomingTransactions.size} transaction(s) scheduled for today"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendSuccessNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Recurring Transactions Executed")
            .setContentText("$count recurring transaction(s) processed successfully")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$count recurring transaction(s) have been executed automatically."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 3, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recurring Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for recurring income, expenses, and transfers"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendUpcomingNotification(count: Int, sampleAmount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming Recurring Transactions")
            .setContentText("$count transaction(s) scheduled for today")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You have $count recurring transaction(s) scheduled for today. Total approximate amount: ৳${"%,.0f".format(sampleAmount * count)}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun sendFailureNotification(count: Int, reasons: List<String>) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = buildString {
            append("$count recurring transaction(s) failed to execute")
            if (reasons.isNotEmpty()) {
                append("\nReasons:")
                reasons.take(3).forEach { reason ->
                    append("\n• $reason")
                }
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Recurring Transaction Failed")
            .setContentText("$count transaction(s) failed")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
}

object RecurringNotificationHelper {

    fun sendRuleNotification(context: Context, rule: RecurringRule, daysUntil: Int) {
        InAppNotificationManager.getInstance(context).showRecurring(
            title = "Upcoming: ${rule.name}",
            message = "৳${"%,.0f".format(rule.amount)} ${rule.transactionType.name.lowercase()} scheduled ${if (daysUntil == 0) "today" else "in $daysUntil days"}",
            referenceId = rule.id.toString(),
            actionRoute = "recurring"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RecurringNotificationWorker.CHANNEL_ID,
                "Recurring Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, rule.id.toInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )

        val amountText = "৳${String.format("%.2f", rule.amount)}"
        val timeText = when (daysUntil) {
            0 -> "today"
            1 -> "tomorrow"
            else -> "in $daysUntil days"
        }

        val notification = NotificationCompat.Builder(context, RecurringNotificationWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming: ${rule.name}")
            .setContentText("$amountText ${rule.transactionType.name.lowercase()} scheduled $timeText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${rule.name}\nAmount: $amountText\nType: ${rule.transactionType.name.lowercase()}\nFrequency: ${rule.frequency.name.lowercase()}\nScheduled: $timeText"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(rule.id.toInt() + 10000, notification)
    }

    fun sendExecutionNotification(context: Context, rule: RecurringRule, success: Boolean) {
        InAppNotificationManager.getInstance(context).showRecurring(
            title = if (success) "Executed: ${rule.name}" else "Failed: ${rule.name}",
            message = "৳${"%,.0f".format(rule.amount)} - ${if (success) "executed successfully" else "execution failed"}",
            referenceId = rule.id.toString(),
            actionRoute = "recurring"
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RecurringNotificationWorker.CHANNEL_ID,
                "Recurring Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, rule.id.toInt() + 1000, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val amountText = "৳${String.format("%.2f", rule.amount)}"

        val (title, text) = if (success) {
            "Recurring Transaction Executed" to "$amountText ${rule.name} has been processed (${rule.transactionType.name.lowercase()})"
        } else {
            "Recurring Transaction Failed" to "$amountText ${rule.name} could not be processed"
        }

        val notification = NotificationCompat.Builder(context, RecurringNotificationWorker.CHANNEL_ID)
            .setSmallIcon(
                if (success) android.R.drawable.ic_popup_reminder
                else android.R.drawable.ic_dialog_alert
            )
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(
                if (success) NotificationCompat.PRIORITY_DEFAULT
                else NotificationCompat.PRIORITY_HIGH
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(rule.id.toInt() + 2000, notification)
    }
}
