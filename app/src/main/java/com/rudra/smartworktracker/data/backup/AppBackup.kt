package com.rudra.smartworktracker.data.backup

import com.rudra.smartworktracker.data.entity.*
import com.rudra.smartworktracker.data.entity.InAppNotification
import com.rudra.smartworktracker.model.*

data class AppBackup(
    val version: Int,
    val appVersion: String,
    val timestamp: Long,
    val metadata: BackupMetadata = BackupMetadata(),
    val settings: List<Settings> = emptyList(),
    val userProfile: List<UserProfile> = emptyList(),
    val dailyJournals: List<DailyJournal> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val focusSessions: List<FocusSession> = emptyList(),
    val workSessions: List<WorkSession> = emptyList(),
    val healthMetrics: List<HealthMetric> = emptyList(),
    val workDays: List<WorkDay> = emptyList(),
    val workLogs: List<WorkLog> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val colleagues: List<Colleague> = emptyList(),
    val travelExpenses: List<TravelAndExpense> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val monthlyInputs: List<MonthlyInput> = emptyList(),
    val calculations: List<Calculation> = emptyList(),
    val financialTransactions: List<FinancialTransaction> = emptyList(),
    val loans: List<Loan> = emptyList(),
    val emis: List<Emi> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val creditCardTransactions: List<CreditCardTransaction> = emptyList(),
    val savings: List<Savings> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val mealRateSettings: List<MealRateSetting> = emptyList(),
    val recurringRules: List<RecurringRule> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val realityEntries: List<RealityEntry> = emptyList(),
    val decisions: List<Decision> = emptyList(),
    val dailyCheckIns: List<DailyCheckIn> = emptyList(),
    val consequenceDebts: List<ConsequenceDebt> = emptyList(),
    val weeklyReports: List<WeeklyReport> = emptyList(),
    val userHistories: List<UserHistory> = emptyList(),
    val mealTypes: List<MealType> = emptyList(),
    val weeklyMealRates: List<WeeklyMealRate> = emptyList(),
    val dailyMealRates: List<DailyMealRate> = emptyList(),
    val mealSettings: List<MealSettings> = emptyList(),
    val specialMealDates: List<SpecialMealDate> = emptyList(),
    val manualMealEntries: List<ManualMealEntry> = emptyList(),
    val inAppNotifications: List<InAppNotification> = emptyList()
)

data class BackupMetadata(
    val dbVersion: Int = 0,
    val totalEntities: Int = 0,
    val totalRows: Long = 0,
    val entityCounts: Map<String, Int> = emptyMap(),
    val exportDurationMs: Long = 0,
    val fileSizeBytes: Long = 0
) {
    fun displaySummary(): String = buildString {
        append("$totalRows records across $totalEntities entity types")
        if (entityCounts.isNotEmpty()) {
            append(" (")
            append(entityCounts.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .take(5)
                .joinToString(", ") { "${it.key}: ${it.value}" })
            append(")")
        }
    }
}
