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

/**
 * Manager for handling database backups and restores in JSON format.
 * Rule 3.1: Primary backup = JSON export (SAFE).
 * Rule 3.2: Restore logic via Room transactions.
 */
class BackupManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = AppBackup(
                version = 32,
                appVersion = "1.0.0",
                timestamp = System.currentTimeMillis(),
                settings = db.settingsDao().getAllSettings().first(),
                userProfile = db.userProfileDao().getUserProfile().first()?.let { listOf(it) } ?: emptyList(),
                dailyJournals = db.dailyJournalDao().getAllJournals().first(),
                habits = db.habitDao().getAllHabits().first(),
                focusSessions = db.focusSessionDao().getAllFocusSessions().first(),
                workSessions = db.workSessionDao().getAllWorkSessions().first(),
                healthMetrics = db.healthMetricDao().getAllHealthMetrics().first(),
                workDays = db.workDayDao().getAllWorkDays().first(),
                workLogs = db.workLogDao().getAllWorkLogs().first(),
                achievements = db.achievementDao().getAllAchievements().first(),
                colleagues = db.colleagueDao().getAllColleagues().first(),
                expenses = db.expenseDao().getAllExpenses().first(),
                incomes = db.incomeDao().getAllIncomes().first(),
                monthlyInputs = db.monthlyInputDao().getAllMonthlyInputs().first(),
                calculations = db.calculationDao().getCalculations().first(),
                financialTransactions = db.financialTransactionDao().getAllTransactions().first(),
                loans = db.loanDao().getAllLoans().first(),
                emis = db.emiDao().getAllEmis().first(),
                creditCards = db.creditCardDao().getAllCreditCards().first(),
                creditCardTransactions = db.creditCardTransactionDao().getAllTransactions().first(),
                savings = db.savingsDao().getAllSavings().first(),
                schedules = db.scheduleDao().getAllSchedules().first(),
                meals = db.mealDao().getAllMeals().first(),
                travelExpenses = db.travelExpenseDao().getTravelExpense().first()?.let { listOf(it) } ?: emptyList(),
                accounts = db.accountDao().getAllAccounts().first(),
                mealRateSettings = db.mealRateSettingDao().getAllMealRateSettings().first(),
                recurringRules = db.recurringRuleDao().getAllRules().first(),
                recurringTransactions = db.recurringTransactionDao().getAllTransactions().first(),
                realityEntries = db.realityTrackerDao().getAllEntries().first(),
                decisions = db.decisionDao().getAllDecisions().first(),
                dailyCheckIns = db.checkInDao().getAllCheckIns().first(),
                consequenceDebts = db.consequenceDebtDao().getAllDebts().first(),
                weeklyReports = db.weeklyReportDao().getAllReports().first(),
                userHistories = db.userHistoryDao().getUserHistory().first()?.let { listOf(it) } ?: emptyList()
            )

            val jsonString = gson.toJson(backup)
            BufferedWriter(OutputStreamWriter(outputStream)).use { it.write(jsonString) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importFromJson(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = BufferedReader(InputStreamReader(inputStream)).use {
                gson.fromJson(it, AppBackup::class.java)
            } ?: return@withContext Result.failure(Exception("Failed to parse backup file"))

            // Rule 3.2: Safe import via transaction
            db.withTransaction {
                backup.settings.forEach { db.settingsDao().saveSettings(it) }
                backup.userProfile.forEach { db.userProfileDao().insertUserProfile(it) }
                backup.expenses.forEach { db.expenseDao().insertExpense(it) }
                backup.incomes.forEach { db.incomeDao().insertIncome(it) }
                backup.workLogs.forEach { db.workLogDao().insertWorkLog(it) }
                backup.habits.forEach { db.habitDao().insertHabit(it) }
                backup.focusSessions.forEach { db.focusSessionDao().insertFocusSession(it) }
                backup.workSessions.forEach { db.workSessionDao().insertWorkSession(it) }
                backup.dailyJournals.forEach { db.dailyJournalDao().insertJournal(it) }
                backup.healthMetrics.forEach { db.healthMetricDao().insertHealthMetric(it) }
                backup.workDays.forEach { db.workDayDao().insertWorkDay(it) }
                backup.achievements.forEach { db.achievementDao().insertAchievement(it) }
                backup.colleagues.forEach { db.colleagueDao().insertColleague(it) }
                backup.monthlyInputs.forEach { db.monthlyInputDao().insertMonthlyInput(it) }
                backup.calculations.forEach { db.calculationDao().insert(it) }
                backup.loans.forEach { db.loanDao().insertLoan(it) }
                backup.emis.forEach { db.emiDao().insertEmi(it) }
                backup.financialTransactions.forEach { db.financialTransactionDao().insertTransaction(it) }
                backup.creditCards.forEach { db.creditCardDao().insertCard(it) }
                backup.creditCardTransactions.forEach { db.creditCardTransactionDao().insertTransaction(it) }
                backup.savings.forEach { db.savingsDao().insert(it) }
                backup.schedules.forEach { db.scheduleDao().insertSchedule(it) }
                backup.meals.forEach { db.mealDao().insertMeal(it) }
                backup.travelExpenses.forEach { db.travelExpenseDao().insert(it) }
                backup.accounts.forEach { db.accountDao().insertAccount(it) }
                backup.mealRateSettings.forEach { db.mealRateSettingDao().insert(it) }
                backup.recurringRules.forEach { db.recurringRuleDao().insertRule(it) }
                backup.recurringTransactions.forEach { db.recurringTransactionDao().insertTransaction(it) }
                backup.realityEntries.forEach { db.realityTrackerDao().insertEntry(it) }
                backup.decisions.forEach { db.decisionDao().insertDecision(it) }
                backup.dailyCheckIns.forEach { db.checkInDao().insertCheckIn(it) }
                backup.consequenceDebts.forEach { db.consequenceDebtDao().insertDebt(it) }
                backup.weeklyReports.forEach { db.weeklyReportDao().insertReport(it) }
                backup.userHistories.forEach { db.userHistoryDao().insertHistory(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
