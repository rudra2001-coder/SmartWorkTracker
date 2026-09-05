package com.rudra.smartworktracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarExporter {
    
    fun exportToIcs(context: Context, rules: List<RecurringRule>): Uri? {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
            val now = dateFormat.format(Calendar.getInstance().time)
            
            val icsContent = buildString {
                appendLine("BEGIN:VCALENDAR")
                appendLine("VERSION:2.0")
                appendLine("PRODID:-//SmartWorkTracker//Recurring//EN")
                appendLine("CALSCALE:GREGORIAN")
                appendLine("METHOD:PUBLISH")
                
                rules.filter { it.isActive }.forEach { rule ->
                    val startDate = dateFormat.format(rule.startDate)
                    val nextExec = dateFormat.format(rule.nextExecutionDate)
                    
                    appendLine("BEGIN:VEVENT")
                    appendLine("DTSTART:$startDate")
                    appendLine("DTEND:$startDate")
                    appendLine("DTSTAMP:$now")
                    appendLine("UID:${rule.id}@smartworktracker")
                    appendLine("SUMMARY:${rule.name}")
                    appendLine("DESCRIPTION:${rule.description ?: ""} | Amount: $${rule.amount} | Type: ${rule.transactionType}")
                    
                    val rrule = getRRule(rule)
                    if (rrule != null) {
                        appendLine("RRULE:$rrule")
                    }
                    
                    appendLine("END:VEVENT")
                }
                
                appendLine("END:VCALENDAR")
            }
            
            val file = File(context.cacheDir, "recurring_transactions.ics")
            file.writeText(icsContent)
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getRRule(rule: RecurringRule): String? {
        return when (rule.frequency) {
            RecurringFrequency.DAILY -> "FREQ=DAILY;INTERVAL=${rule.interval}"
            RecurringFrequency.WEEKLY -> "FREQ=WEEKLY;INTERVAL=${rule.interval}"
            RecurringFrequency.BIWEEKLY -> "FREQ=WEEKLY;INTERVAL=2"
            RecurringFrequency.MONTHLY -> "FREQ=MONTHLY;INTERVAL=${rule.interval}"
            RecurringFrequency.QUARTERLY -> "FREQ=MONTHLY;INTERVAL=3"
            RecurringFrequency.YEARLY -> "FREQ=YEARLY;INTERVAL=1"
            RecurringFrequency.CUSTOM -> "FREQ=DAILY;INTERVAL=${rule.interval}"
            RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> {
                val days = rule.selectedDaysOfWeek?.joinToString(",") { it.name.take(2) } ?: ""
                if (days.isNotEmpty()) "FREQ=WEEKLY;BYDAY=$days" else null
            }
        }
    }
    
    fun shareCalendar(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Recurring Transactions")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Calendar"))
    }
}
