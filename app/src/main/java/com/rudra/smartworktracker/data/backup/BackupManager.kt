package com.rudra.smartworktracker.data.backup

import android.content.Context
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rudra.smartworktracker.data.AppDatabase
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

class BackupManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val historyStore = BackupHistoryStore(context)

    suspend fun exportToJson(
        outputStream: OutputStream,
        onProgress: ((String) -> Unit)? = null
    ): ExportResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            onProgress?.invoke("Reading accounts...")
            val accounts = db.accountDao().getAllAccounts().first()
            onProgress?.invoke("Reading expenses...")
            val expenses = db.expenseDao().getAllExpenses().first()
            onProgress?.invoke("Reading incomes...")
            val incomes = db.incomeDao().getAllIncomes().first()
            onProgress?.invoke("Reading work logs...")
            val workLogs = db.workLogDao().getAllWorkLogs().first()
            onProgress?.invoke("Reading financial records...")
            val loans = db.loanDao().getAllLoans().first()
            val emis = db.emiDao().getAllEmis().first()
            val creditCards = db.creditCardDao().getAllCreditCards().first()
            val creditCardTransactions = db.creditCardTransactionDao().getAllTransactions().first()
            val savings = db.savingsDao().getAllSavings().first()
            val financialTransactions = db.financialTransactionDao().getAllTransactions().first()
            val recurringRules = db.recurringRuleDao().getAllRules().first()
            val recurringTransactions = db.recurringTransactionDao().getAllTransactions().first()

            onProgress?.invoke("Reading personal data...")
            val habits = db.habitDao().getAllHabits().first()
            val focusSessions = db.focusSessionDao().getAllFocusSessions().first()
            val workSessions = db.workSessionDao().getAllWorkSessions().first()
            val healthMetrics = db.healthMetricDao().getAllHealthMetrics().first()
            val dailyJournals = db.dailyJournalDao().getAllJournals().first()
            val workDays = db.workDayDao().getAllWorkDays().first()
            val achievements = db.achievementDao().getAllAchievements().first()
            val colleagues = db.colleagueDao().getAllColleagues().first()
            val schedules = db.scheduleDao().getAllSchedules().first()

            onProgress?.invoke("Reading system data...")
            val settings = db.settingsDao().getAllSettings().first()
            val userProfile = db.userProfileDao().getUserProfile().first()
            val monthlyInputs = db.monthlyInputDao().getAllMonthlyInputs().first()
            val calculations = db.calculationDao().getCalculations().first()
            val meals = db.mealDao().getAllMeals().first()
            val travelExpenses = db.travelExpenseDao().getTravelExpense().first()
            val mealRateSettings = db.mealRateSettingDao().getAllMealRateSettings().first()
            val realityEntries = db.realityTrackerDao().getAllEntries().first()
            val decisions = db.decisionDao().getAllDecisions().first()
            val dailyCheckIns = db.checkInDao().getAllCheckIns().first()
            val consequenceDebts = db.consequenceDebtDao().getAllDebts().first()
            val weeklyReports = db.weeklyReportDao().getAllReports().first()
            val userHistories = db.userHistoryDao().getUserHistory().first()

            onProgress?.invoke("Reading meal config...")
            val mealTypes = db.mealTypeDao().getAllMealTypesList()
            val weeklyMealRates = db.weeklyMealRateDao().getAllWeeklyMealRates()
            val dailyMealRates = db.dailyMealRateDao().getAllDailyMealRates()
            val mealSettings = db.mealSettingsDao().getMealSettingsOnce()
            val specialMealDates = db.specialMealDateDao().getAllSpecialDatesList()

            onProgress?.invoke("Reading notifications...")
            val inAppNotifications = db.inAppNotificationDao().getAllNotifications().first()

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
                "Notifications" to inAppNotifications.size
            ).filter { it.value > 0 }

            val totalRows = entityCounts.values.sum().toLong()
            val durationMs = System.currentTimeMillis() - startTime

            val backup = AppBackup(
                version = 34,
                appVersion = "1.0.0",
                timestamp = System.currentTimeMillis(),
                metadata = BackupMetadata(
                    dbVersion = 14,
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
                travelExpenses = travelExpenses?.let { listOf(it) } ?: emptyList(),
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
                inAppNotifications = inAppNotifications
            )

            onProgress?.invoke("Writing JSON...")
            val jsonString = gson.toJson(backup)
            val bytes = jsonString.toByteArray()
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

    suspend fun previewBackup(inputStream: InputStream): RestorePreview = withContext(Dispatchers.IO) {
        try {
            val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val backup = gson.fromJson(jsonString, AppBackup::class.java)
                ?: return@withContext RestorePreview(
                    version = 0, appVersion = "", timestamp = 0,
                    totalRows = 0, entityCounts = emptyMap(),
                    isValid = false, validationMessage = "Failed to parse backup file"
                )

            val entityCounts = buildEntityCounts(backup)
            val totalRows = entityCounts.values.sum().toLong()

            if (backup.version > 34) {
                RestorePreview(
                    version = backup.version, appVersion = backup.appVersion,
                    timestamp = backup.timestamp, totalRows = totalRows,
                    entityCounts = entityCounts, isValid = true,
                    validationMessage = "Newer backup version (${backup.version}). Some data may not restore correctly."
                )
            } else {
                RestorePreview(
                    version = backup.version, appVersion = backup.appVersion,
                    timestamp = backup.timestamp, totalRows = totalRows,
                    entityCounts = entityCounts, isValid = true
                )
            }
        } catch (e: Exception) {
            RestorePreview(
                version = 0, appVersion = "", timestamp = 0,
                totalRows = 0, entityCounts = emptyMap(),
                isValid = false, validationMessage = "Invalid backup file: ${e.localizedMessage}"
            )
        }
    }

    suspend fun importFromJson(
        inputStream: InputStream,
        onProgress: ((String) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            onProgress?.invoke("Parsing backup file...")
            val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            val backup = gson.fromJson(jsonString, AppBackup::class.java)
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
            Result.failure(Exception("Restore failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
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

    fun enforceRetention() {
        val excess = historyStore.getExcessEntries()
        for (entry in excess) {
            BackupHistoryStore.deleteFile(context, entry)
            historyStore.remove(entry.id)
        }
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
        "Settings" to backup.settings.size
    ).filter { it.value > 0 }
}
