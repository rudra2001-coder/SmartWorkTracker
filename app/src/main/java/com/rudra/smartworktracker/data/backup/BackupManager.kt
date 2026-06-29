package com.rudra.smartworktracker.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.*

data class ExportResult(
    val success: Boolean,
    val totalRows: Long = 0,
    val entityCounts: Map<String, Int> = emptyMap(),
    val durationMs: Long = 0,
    val fileSizeBytes: Long = 0,
    val errorMessage: String? = null
)

data class RestorePreview(
    val version: Int,
    val appVersion: String,
    val timestamp: Long,
    val totalRows: Long,
    val entityCounts: Map<String, Int>,
    val isValid: Boolean,
    val validationMessage: String? = null
)

data class BackupOptions(
    val compress: Boolean = false,
    val password: String? = null,
    val selectedTypes: Set<String>? = null
)

class BackupManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val historyStore = BackupHistoryStore(context)

    suspend fun exportToJson(
        outputStream: OutputStream,
        onProgress: ((String) -> Unit)? = null,
        options: BackupOptions = BackupOptions()
    ): ExportResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val sel = options.selectedTypes

            onProgress?.invoke("Reading accounts...")
            val accounts = if (sel == null || "Accounts" in sel) db.accountDao().getAllAccounts().first() else emptyList()
            onProgress?.invoke("Reading expenses...")
            val expenses = if (sel == null || "Expenses" in sel) db.expenseDao().getAllExpenses().first() else emptyList()
            onProgress?.invoke("Reading incomes...")
            val incomes = if (sel == null || "Incomes" in sel) db.incomeDao().getAllIncomes().first() else emptyList()
            onProgress?.invoke("Reading work logs...")
            val workLogs = if (sel == null || "Work Logs" in sel) db.workLogDao().getAllWorkLogs().first() else emptyList()
            onProgress?.invoke("Reading financial records...")
            val loans = if (sel == null || "Loans" in sel) db.loanDao().getAllLoans().first() else emptyList()
            val emis = if (sel == null || "EMIs" in sel) db.emiDao().getAllEmis().first() else emptyList()
            val creditCards = if (sel == null || "Credit Cards" in sel) db.creditCardDao().getAllCreditCards().first() else emptyList()
            val creditCardTransactions = if (sel == null || "Credit Card Tx" in sel) db.creditCardTransactionDao().getAllTransactions().first() else emptyList()
            val savings = if (sel == null || "Savings" in sel) db.savingsDao().getAllSavings().first() else emptyList()
            val financialTransactions = if (sel == null || "Fin. Transactions" in sel) db.financialTransactionDao().getAllTransactions().first() else emptyList()
            val recurringRules = if (sel == null || "Recurring Rules" in sel) db.recurringRuleDao().getAllRules().first() else emptyList()
            val recurringTransactions = if (sel == null || "Recurring Tx" in sel) db.recurringTransactionDao().getAllTransactions().first() else emptyList()

            onProgress?.invoke("Reading personal data...")
            val habits = if (sel == null || "Habits" in sel) db.habitDao().getAllHabits().first() else emptyList()
            val focusSessions = if (sel == null || "Focus Sessions" in sel) db.focusSessionDao().getAllFocusSessions().first() else emptyList()
            val workSessions = if (sel == null || "Work Sessions" in sel) db.workSessionDao().getAllWorkSessions().first() else emptyList()
            val healthMetrics = if (sel == null || "Health Metrics" in sel) db.healthMetricDao().getAllHealthMetrics().first() else emptyList()
            val dailyJournals = if (sel == null || "Journals" in sel) db.dailyJournalDao().getAllJournals().first() else emptyList()
            val workDays = if (sel == null || "Work Days" in sel) db.workDayDao().getAllWorkDays().first() else emptyList()
            val achievements = if (sel == null || "Achievements" in sel) db.achievementDao().getAllAchievements().first() else emptyList()
            val colleagues = if (sel == null || "Colleagues" in sel) db.colleagueDao().getAllColleagues().first() else emptyList()
            val schedules = if (sel == null || "Schedules" in sel) db.scheduleDao().getAllSchedules().first() else emptyList()

            onProgress?.invoke("Reading system data...")
            val settings = if (sel == null || "Settings" in sel) db.settingsDao().getAllSettings().first() else emptyList()
            val userProfile = if (sel == null || "User Profile" in sel) db.userProfileDao().getUserProfile().first() else null
            val monthlyInputs = if (sel == null || "Monthly Inputs" in sel) db.monthlyInputDao().getAllMonthlyInputs().first() else emptyList()
            val calculations = if (sel == null || "Calculations" in sel) db.calculationDao().getCalculations().first() else emptyList()
            val meals = if (sel == null || "Meals" in sel) db.mealDao().getAllMeals().first() else emptyList()
            val travelExpense = if (sel == null || "Travel" in sel) db.travelExpenseDao().getTravelExpense().first() else null
            val travelExpenses: List<TravelAndExpense> = travelExpense?.let { listOf(it) } ?: emptyList()
            val mealRateSettings = if (sel == null || "Meal Rates" in sel) db.mealRateSettingDao().getAllMealRateSettings().first() else emptyList()
            val realityEntries = if (sel == null || "Reality" in sel) db.realityTrackerDao().getAllEntries().first() else emptyList()
            val decisions = if (sel == null || "Decisions" in sel) db.decisionDao().getAllDecisions().first() else emptyList()
            val dailyCheckIns = if (sel == null || "Check-ins" in sel) db.checkInDao().getAllCheckIns().first() else emptyList()
            val consequenceDebts = if (sel == null || "Debts" in sel) db.consequenceDebtDao().getAllDebts().first() else emptyList()
            val weeklyReports = if (sel == null || "Weekly Reports" in sel) db.weeklyReportDao().getAllReports().first() else emptyList()
            val userHistories = if (sel == null || "User History" in sel) db.userHistoryDao().getUserHistory().first() else null

            onProgress?.invoke("Reading meal config...")
            val mealTypes = if (sel == null || "Meal Types" in sel) db.mealTypeDao().getAllMealTypesList() else emptyList()
            val weeklyMealRates = if (sel == null || "Meal Rates" in sel) db.weeklyMealRateDao().getAllWeeklyMealRates() else emptyList()
            val dailyMealRates = if (sel == null || "Meal Rates" in sel) db.dailyMealRateDao().getAllDailyMealRates() else emptyList()
            val mealSettings = if (sel == null || "Meal Config" in sel) db.mealSettingsDao().getMealSettingsOnce() else null
            val specialMealDates = if (sel == null || "Meal Config" in sel) db.specialMealDateDao().getAllSpecialDatesList() else emptyList()

            onProgress?.invoke("Reading manual meal entries...")
            val manualMealEntries = if (sel == null || "Manual Meals" in sel) db.manualMealEntryDao().getAllEntriesList() else emptyList()

            onProgress?.invoke("Reading notifications...")
            val inAppNotifications = if (sel == null || "Notifications" in sel) db.inAppNotificationDao().getAllNotifications().first() else emptyList()

            val entityCounts = linkedMapOf(
                "Accounts" to accounts.size,
                "Expenses" to expenses.size,
                "Incomes" to incomes.size,
                "Work Logs" to workLogs.size,
                "Loans" to loans.size,
                "EMIs" to emis.size,
                "Credit Cards" to creditCards.size,
                "Credit Card Tx" to creditCardTransactions.size,
                "Savings" to savings.size,
                "Fin. Transactions" to financialTransactions.size,
                "Habits" to habits.size,
                "Focus Sessions" to focusSessions.size,
                "Work Sessions" to workSessions.size,
                "Health Metrics" to healthMetrics.size,
                "Journals" to dailyJournals.size,
                "Work Days" to workDays.size,
                "Achievements" to achievements.size,
                "Colleagues" to colleagues.size,
                "Schedules" to schedules.size,
                "Recurring Rules" to recurringRules.size,
                "Recurring Tx" to recurringTransactions.size,
                "Reality Entries" to realityEntries.size,
                "Decisions" to decisions.size,
                "Check-ins" to dailyCheckIns.size,
                "Debts" to consequenceDebts.size,
                "Weekly Reports" to weeklyReports.size,
                "Meal Settings" to (if (mealSettings != null) 1 else 0),
                "Manual Meals" to manualMealEntries.size,
                "Notifications" to inAppNotifications.size
            ).filter { it.value > 0 }

            val totalRows = entityCounts.values.sum().toLong()
            val durationMs = System.currentTimeMillis() - startTime

            val backup = AppBackup(
                version = BackupFormatMigrator.CURRENT_BACKUP_VERSION,
                appVersion = "1.0.0",
                timestamp = System.currentTimeMillis(),
                metadata = BackupMetadata(
                    dbVersion = 17,
                    totalEntities = entityCounts.size,
                    totalRows = totalRows,
                    entityCounts = entityCounts,
                    exportDurationMs = durationMs
                ),
                settings = settings,
                userProfile = userProfile?.let { listOf(it) } ?: emptyList(),
                dailyJournals = dailyJournals,
                habits = habits,
                focusSessions = focusSessions,
                workSessions = workSessions,
                healthMetrics = healthMetrics,
                workDays = workDays,
                workLogs = workLogs,
                achievements = achievements,
                colleagues = colleagues,
                expenses = expenses,
                incomes = incomes,
                monthlyInputs = monthlyInputs,
                calculations = calculations,
                financialTransactions = financialTransactions,
                loans = loans,
                emis = emis,
                creditCards = creditCards,
                creditCardTransactions = creditCardTransactions,
                savings = savings,
                schedules = schedules,
                meals = meals,
                travelExpenses = travelExpenses,
                accounts = accounts,
                mealRateSettings = mealRateSettings,
                recurringRules = recurringRules,
                recurringTransactions = recurringTransactions,
                realityEntries = realityEntries,
                decisions = decisions,
                dailyCheckIns = dailyCheckIns,
                consequenceDebts = consequenceDebts,
                weeklyReports = weeklyReports,
                userHistories = userHistories?.let { listOf(it) } ?: emptyList(),
                mealTypes = mealTypes,
                weeklyMealRates = weeklyMealRates,
                dailyMealRates = dailyMealRates,
                mealSettings = mealSettings?.let { listOf(it) } ?: emptyList(),
                specialMealDates = specialMealDates,
                manualMealEntries = manualMealEntries,
                inAppNotifications = inAppNotifications
            )

            onProgress?.invoke("Writing JSON...")
            var bytes = gson.toJson(backup).toByteArray()

            val ext = mutableListOf<String>()

            if (options.compress) {
                onProgress?.invoke("Compressing...")
                bytes = BackupCompression.compress(bytes)
                ext.add("gz")
            }

            if (!options.password.isNullOrBlank()) {
                onProgress?.invoke("Encrypting...")
                bytes = BackupCrypto.encrypt(bytes, options.password)
                ext.add("enc")
            }

            BufferedOutputStream(outputStream).use { it.write(bytes) }

            val elapsed = System.currentTimeMillis() - startTime
            onProgress?.invoke("Done!")

            ExportResult(
                success = true,
                totalRows = totalRows,
                entityCounts = entityCounts,
                durationMs = elapsed,
                fileSizeBytes = bytes.size.toLong()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Unknown error during export"
            )
        }
    }

    suspend fun previewBackup(
        inputStream: InputStream,
        password: String? = null
    ): RestorePreview = withContext(Dispatchers.IO) {
        try {
            val rawBytes = inputStream.readBytes()
            val decompressed = processInputBytes(rawBytes, password)

            val migratedJson = BackupFormatMigrator.migrateToCurrent(decompressed)
            val backup = gson.fromJson(migratedJson, AppBackup::class.java)
                ?: return@withContext RestorePreview(
                    version = 0, appVersion = "", timestamp = 0,
                    totalRows = 0, entityCounts = emptyMap(),
                    isValid = false, validationMessage = "Failed to parse backup file"
                )

            val entityCounts = buildEntityCounts(backup)
            val totalRows = entityCounts.values.sum().toLong()

            val messages = mutableListOf<String>()
            val originalVersion = try {
                com.google.gson.JsonParser.parseString(decompressed).asJsonObject.get("version")?.asInt ?: 1
            } catch (_: Exception) { 1 }
            BackupFormatMigrator.describeMigration(originalVersion)?.let { messages.add(it) }

            RestorePreview(
                version = backup.version, appVersion = backup.appVersion,
                timestamp = backup.timestamp, totalRows = totalRows,
                entityCounts = entityCounts, isValid = true,
                validationMessage = if (messages.isNotEmpty()) messages.joinToString("\n") else null
            )
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error"
            RestorePreview(
                version = 0, appVersion = "", timestamp = 0,
                totalRows = 0, entityCounts = emptyMap(),
                isValid = false,
                validationMessage = when {
                    msg.contains("AEADBadTagException") || msg.contains("Bad tag") ->
                        "Wrong password or corrupted backup file"
                    msg.contains("GZIP") || msg.contains("gzip") ->
                        "Invalid compressed backup file"
                    else -> "Invalid backup file: $msg"
                }
            )
        }
    }

    suspend fun importFromJson(
        inputStream: InputStream,
        onProgress: ((String) -> Unit)? = null,
        password: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke("Parsing backup file...")
            val rawBytes = inputStream.readBytes()
            val decompressed = processInputBytes(rawBytes, password)

            val migratedJson = BackupFormatMigrator.migrateToCurrent(decompressed)
            val backup = gson.fromJson(migratedJson, AppBackup::class.java)
                ?: return@withContext Result.failure(Exception("Failed to parse backup file"))

            val entityCounts = buildEntityCounts(backup)
            val totalRows = entityCounts.values.sum()

            onProgress?.invoke("Clearing existing data...")

            db.withTransaction {
                db.clearAllTables()

                onProgress?.invoke("Restoring accounts...")
                backup.accounts.forEach { db.accountDao().insertAccount(it) }
                onProgress?.invoke("Restoring expenses...")
                backup.expenses.forEach { db.expenseDao().insertExpense(it) }
                onProgress?.invoke("Restoring incomes...")
                backup.incomes.forEach { db.incomeDao().insertIncome(it) }
                onProgress?.invoke("Restoring work logs...")
                backup.workLogs.forEach { db.workLogDao().insertWorkLog(it) }
                backup.habits.forEach { db.habitDao().insertHabit(it) }
                backup.focusSessions.forEach { db.focusSessionDao().insertFocusSession(it) }
                backup.workSessions.forEach { db.workSessionDao().insertWorkSession(it) }
                onProgress?.invoke("Restoring journals...")
                backup.dailyJournals.forEach { db.dailyJournalDao().insertJournal(it) }
                backup.healthMetrics.forEach { db.healthMetricDao().insertHealthMetric(it) }
                backup.workDays.forEach { db.workDayDao().insertWorkDay(it) }
                backup.achievements.forEach { db.achievementDao().insertAchievement(it) }
                backup.colleagues.forEach { db.colleagueDao().insertColleague(it) }
                backup.monthlyInputs.forEach { db.monthlyInputDao().insertMonthlyInput(it) }
                backup.calculations.forEach { db.calculationDao().insert(it) }
                onProgress?.invoke("Restoring loans & cards...")
                backup.loans.forEach { db.loanDao().insertLoan(it) }
                backup.emis.forEach { db.emiDao().insertEmi(it) }
                backup.financialTransactions.forEach { db.financialTransactionDao().insertTransaction(it) }
                backup.creditCards.forEach { db.creditCardDao().insertCard(it) }
                backup.creditCardTransactions.forEach { db.creditCardTransactionDao().insertTransaction(it) }
                backup.savings.forEach { db.savingsDao().insert(it) }
                backup.schedules.forEach { db.scheduleDao().insertSchedule(it) }
                backup.meals.forEach { db.mealDao().insertMeal(it) }
                backup.travelExpenses.forEach { db.travelExpenseDao().insert(it) }
                backup.mealRateSettings.forEach { db.mealRateSettingDao().insert(it) }
                onProgress?.invoke("Restoring recurring...")
                backup.recurringRules.forEach { db.recurringRuleDao().insertRule(it) }
                backup.recurringTransactions.forEach { db.recurringTransactionDao().insertTransaction(it) }
                backup.realityEntries.forEach { db.realityTrackerDao().insertEntry(it) }
                backup.decisions.forEach { db.decisionDao().insertDecision(it) }
                backup.dailyCheckIns.forEach { db.checkInDao().insertCheckIn(it) }
                backup.consequenceDebts.forEach { db.consequenceDebtDao().insertDebt(it) }
                backup.weeklyReports.forEach { db.weeklyReportDao().insertReport(it) }
                backup.userHistories.forEach { db.userHistoryDao().insertHistory(it) }
                onProgress?.invoke("Restoring meal config...")
                backup.mealTypes.forEach { db.mealTypeDao().insert(it) }
                backup.weeklyMealRates.forEach { db.weeklyMealRateDao().insert(it) }
                backup.dailyMealRates.forEach { db.dailyMealRateDao().insert(it) }
                backup.mealSettings.forEach { db.mealSettingsDao().insert(it) }
                backup.specialMealDates.forEach { db.specialMealDateDao().insert(it) }
                backup.manualMealEntries.forEach { db.manualMealEntryDao().insert(it) }
                onProgress?.invoke("Restoring notifications...")
                backup.inAppNotifications.forEach { db.inAppNotificationDao().insert(it) }
                onProgress?.invoke("Restoring settings...")
                backup.settings.forEach { db.settingsDao().saveSettings(it) }
                backup.userProfile.forEach { db.userProfileDao().insertUserProfile(it) }
            }

            onProgress?.invoke("Restored $totalRows records successfully!")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.localizedMessage ?: "Unknown error"
            Result.failure(Exception(
                when {
                    msg.contains("AEADBadTagException") || msg.contains("Bad tag") ->
                        "Wrong password or corrupted backup file"
                    else -> "Restore failed: $msg"
                }
            ))
        }
    }

    private fun processInputBytes(raw: ByteArray, password: String?): String {
        var bytes = raw

        if (isGzip(bytes)) {
            bytes = BackupCompression.decompress(bytes)
        }

        if (!password.isNullOrBlank()) {
            bytes = BackupCrypto.decrypt(bytes, password)
        }

        return String(bytes)
    }

    private fun isGzip(data: ByteArray): Boolean {
        return data.size >= 2 && data[0].toInt() == 0x1F && data[1].toInt() == 0x8B
    }

    fun recordBackup(
        fileName: String,
        totalRows: Long,
        fileSizeBytes: Long,
        isManual: Boolean,
        fileUri: String = "",
        mediaStoreId: Long = 0
    ) {
        val entry = BackupEntry(
            fileName = fileName,
            totalRows = totalRows,
            fileSizeBytes = fileSizeBytes,
            isManual = isManual,
            fileUri = fileUri,
            mediaStoreId = mediaStoreId
        )
        historyStore.add(entry)
        enforceRetention()
    }

    fun getBackupHistory(): List<BackupEntry> = historyStore.getAll()

    fun deleteBackupEntry(entry: BackupEntry): Boolean {
        val fileDeleted = BackupHistoryStore.deleteFile(context, entry)
        historyStore.remove(entry.id)
        return fileDeleted
    }

    fun getRetentionLimit(): Int = historyStore.getRetentionLimit()

    fun setRetentionLimit(limit: Int) {
        historyStore.setRetentionLimit(limit)
        enforceRetention()
    }

    fun getRetentionDays(): Int = historyStore.getRetentionDays()

    fun setRetentionDays(days: Int) {
        historyStore.setRetentionDays(days)
        enforceRetention()
    }

    fun getBackupHour(): Int = historyStore.getBackupHour()

    fun getBackupMinute(): Int = historyStore.getBackupMinute()

    fun setBackupTime(hour: Int, minute: Int) {
        historyStore.setBackupTime(hour, minute)
    }

    fun getBackupTimeDisplay(): String {
        val h = getBackupHour()
        val m = getBackupMinute()
        val amPm = if (h < 12) "AM" else "PM"
        val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return "%d:%02d %s".format(hour12, m, amPm)
    }

    fun getBackupFrequency(): String = historyStore.getBackupFrequency()

    fun setBackupFrequency(frequency: String) {
        historyStore.setBackupFrequency(frequency)
    }

    fun enforceRetention() {
        val excess = historyStore.getExcessEntries()
        for (entry in excess) {
            BackupHistoryStore.deleteFile(context, entry)
            historyStore.remove(entry.id)
        }
        // Age-based cleanup
        historyStore.cleanupByAge()
    }

    private fun buildEntityCounts(backup: AppBackup): Map<String, Int> = linkedMapOf(
        "Accounts" to backup.accounts.size,
        "Expenses" to backup.expenses.size,
        "Incomes" to backup.incomes.size,
        "Work Logs" to backup.workLogs.size,
        "Loans" to backup.loans.size,
        "EMIs" to backup.emis.size,
        "Credit Cards" to backup.creditCards.size,
        "Savings" to backup.savings.size,
        "Fin. Transactions" to backup.financialTransactions.size,
        "Habits" to backup.habits.size,
        "Health Metrics" to backup.healthMetrics.size,
        "Journals" to backup.dailyJournals.size,
        "Recurring Rules" to backup.recurringRules.size,
        "Notifications" to backup.inAppNotifications.size,
        "Settings" to backup.settings.size,
        "Manual Meals" to backup.manualMealEntries.size
    ).filter { it.value > 0 }
}
