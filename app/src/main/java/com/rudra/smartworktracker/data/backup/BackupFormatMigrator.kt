package com.rudra.smartworktracker.data.backup

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Migrates old backup JSON to the latest format by injecting default values
 * for any missing fields. This ensures backups from any previous app version
 * can be restored after schema changes (new entities, new columns, etc.).
 *
 * When adding a new field to [AppBackup], add its name to [ARRAY_FIELDS]
 * (or [OBJECT_FIELDS]) so old backups get a valid default instead of crashing.
 */
object BackupFormatMigrator {

    const val CURRENT_BACKUP_VERSION = 35

    /** All top-level array/list fields in AppBackup. */
    private val ARRAY_FIELDS = setOf(
        "accounts", "expenses", "incomes", "workLogs", "loans", "emis",
        "creditCards", "creditCardTransactions", "savings", "financialTransactions",
        "habits", "focusSessions", "workSessions", "healthMetrics",
        "workDays", "achievements", "colleagues", "dailyJournals",
        "schedules", "monthlyInputs", "calculations", "meals",
        "recurringRules", "recurringTransactions", "realityEntries",
        "decisions", "dailyCheckIns", "consequenceDebts", "weeklyReports",
        "userHistories", "mealTypes", "weeklyMealRates", "dailyMealRates",
        "mealSettings", "specialMealDates", "mealRateSettings",
        "travelExpenses", "settings", "userProfile",
        "inAppNotifications", "manualMealEntries"
    )

    /** Top-level object fields and their default JSON. */
    private val OBJECT_FIELDS = mapOf(
        "metadata" to """{"dbVersion":0,"totalEntities":0,"totalRows":0,"entityCounts":{},"exportDurationMs":0,"fileSizeBytes":0}"""
    )

    /**
     * Parses a raw backup JSON string, injects any missing top-level fields
     * with safe defaults, and returns the normalized JSON string.
     *
     * Safe to call on already-current backups — no-op for existing fields.
     */
    fun migrateToCurrent(jsonString: String): String {
        val root = JsonParser.parseString(jsonString).asJsonObject

        // Inject missing array fields with empty arrays
        for (field in ARRAY_FIELDS) {
            if (!root.has(field) || root.get(field).isJsonNull) {
                root.add(field, JsonArray())
            }
        }

        // Inject missing object fields
        for ((field, defaultJson) in OBJECT_FIELDS) {
            if (!root.has(field) || root.get(field).isJsonNull) {
                root.add(field, JsonParser.parseString(defaultJson))
            }
        }

        // Ensure version is current
        root.addProperty("version", CURRENT_BACKUP_VERSION)

        return root.toString()
    }

    /**
     * Returns a human-readable summary of what was migrated, for UI display.
     */
    fun describeMigration(originalVersion: Int): String? {
        if (originalVersion < CURRENT_BACKUP_VERSION) {
            return "Migrated from backup format v$originalVersion to v$CURRENT_BACKUP_VERSION"
        }
        return null
    }
}
