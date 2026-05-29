package com.rudra.smartworktracker.data.backup

import com.rudra.smartworktracker.data.entity.*
import com.rudra.smartworktracker.model.*

/**
 * Data class representing the entire database for backup/restore purposes.
 * Rule 3.1: Export all tables to JSON, including schema and app version.
 */
data class AppBackup(
    val version: Int,
    val appVersion: String,
    val timestamp: Long,
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
    val specialMealDates: List<SpecialMealDate> = emptyList()
)
