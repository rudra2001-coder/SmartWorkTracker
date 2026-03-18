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
import com.rudra.smartworktracker.engine.RecurringEngine
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Worker that processes recurring transactions in the background.
 * Runs periodically to check for due transactions and send notifications.
 */
class RecurringNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "recurring_notification_worker"
        const val CHANNEL_ID = "recurring_transactions"
        const val NOTIFICATION_ID = 1001
        
        /**
         * Schedule the recurring notification worker
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<RecurringNotificationWorker>(
                1, TimeUnit.HOURS // Run every hour
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
            val incomeRepository = IncomeRepository(database.incomeDao())
            val expenseRepository = ExpenseRepository(database.expenseDao())
            val engine = RecurringEngine(repository, incomeRepository, expenseRepository)
            
            // Get current balance for balance protection
            val currentBalance = calculateCurrentBalance(incomeRepository, expenseRepository)
            
            // Process due rules with balance check
            val results = engine.processDueRules(currentBalance)
            
            // Check for upcoming transactions in the next 24 hours
            val upcomingTransactions = engine.getUpcomingTransactions(1)
            
            // Send notification for upcoming transactions
            if (upcomingTransactions.isNotEmpty()) {
                sendUpcomingNotification(upcomingTransactions.size)
            }
            
            // Handle failed transactions
            val failedCount = results.count { !it.success }
            if (failedCount > 0) {
                sendFailureNotification(failedCount)
            }
            
            // Send success notification if transactions were executed
            val successCount = results.count { it.success }
            if (successCount > 0) {
                sendSuccessNotification(successCount)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun calculateCurrentBalance(
        incomeRepository: IncomeRepository,
        expenseRepository: ExpenseRepository
    ): Double {
        return try {
            val now = System.currentTimeMillis()
            val startOfMonth = getStartOfMonth()
            
            val totalIncome = incomeRepository.getTotalIncomeBetween(startOfMonth, now).first() ?: 0.0
            val totalExpenses = expenseRepository.getTotalExpensesBetween(startOfMonth, now).first() ?: 0.0
            
            totalIncome - totalExpenses
        } catch (e: Exception) {
            0.0
        }
    }
    
    private fun getStartOfMonth(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun sendSuccessNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Transactions Executed")
            .setContentText("$count recurring transaction(s) added successfully")
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
                description = "Notifications for recurring income and expenses"
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendUpcomingNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming Transactions")
            .setContentText("You have $count recurring transaction(s) scheduled for today")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun sendFailureNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Transaction Failed")
            .setContentText("$count recurring transaction(s) failed to execute")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }
}

/**
 * Notification helper for recurring transactions
 */
object RecurringNotificationHelper {
    
    /**
     * Send a notification for a specific upcoming recurring rule
     */
    fun sendRuleNotification(context: Context, rule: RecurringRule, daysUntil: Int) {
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
            context,
            rule.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val amountText = "$${String.format("%.2f", rule.amount)}"
        val timeText = when (daysUntil) {
            0 -> "today"
            1 -> "tomorrow"
            else -> "in $daysUntil days"
        }
        
        val notification = NotificationCompat.Builder(context, RecurringNotificationWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Upcoming: ${rule.name}")
            .setContentText("$amountText scheduled $timeText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(rule.id.toInt() + 10000, notification)
    }
    
    /**
     * Send a notification when a transaction is executed
     */
    fun sendExecutionNotification(context: Context, rule: RecurringRule, success: Boolean) {
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
            context,
            rule.id.toInt() + 1000,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val amountText = "$${String.format("%.2f", rule.amount)}"
        
        val (title, text) = if (success) {
            "Transaction Executed" to "$amountText ${rule.name} has been processed"
        } else {
            "Transaction Failed" to "$amountText ${rule.name} could not be processed"
        }
        
        val notification = NotificationCompat.Builder(context, RecurringNotificationWorker.CHANNEL_ID)
            .setSmallIcon(
                if (success) android.R.drawable.ic_popup_reminder 
                else android.R.drawable.ic_dialog_alert
            )
            .setContentTitle(title)
            .setContentText(text)
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
