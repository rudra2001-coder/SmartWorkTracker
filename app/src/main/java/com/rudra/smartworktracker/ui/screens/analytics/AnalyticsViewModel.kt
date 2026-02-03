package com.rudra.smartworktracker.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.*
import com.rudra.smartworktracker.data.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import kotlin.math.*

data class AnalyticsData(
    val productivityScore: Int = 0,
    val focusSessions: List<FocusSession> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val healthMetrics: List<HealthMetric> = emptyList(),
    val totalWaterToday: Double = 0.0,
    val sleepHours: Double = 0.0,
    val financialBalance: Double = 0.0,
    val totalSavings: Double = 0.0,
    val activeLoansCount: Int = 0,
    val totalCaloriesToday: Double = 0.0,
    val workHoursToday: Double = 0.0,
    val workLifeBalanceScore: Int = 0,
    val achievementsCount: Int = 0
)

class AnalyticsViewModel(private val db: AppDatabase) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    val analyticsData: StateFlow<AnalyticsData> = combine(
        db.focusSessionDao().getAllFocusSessions(),
        db.habitDao().getAllHabits(),
        db.incomeDao().getAllIncomes(),
        db.expenseDao().getAllExpenses(),
        db.healthMetricDao().getAllHealthMetrics(),
        db.savingsDao().getAllSavings(),
        db.loanDao().getAllLoans(),
        db.achievementDao().getAllAchievements()
    ) { args: Array<Any?> ->
        val focusSessions = args[0] as List<FocusSession>
        val habits = args[1] as List<Habit>
        val incomes = args[2] as List<Income>
        val expenses = args[3] as List<Expense>
        val healthMetrics = args[4] as List<HealthMetric>
        val savings = args[5] as List<Savings>
        val loans = args[6] as List<Loan>
        val achievements = args[7] as List<Achievement>
        
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // --- Health Stats for Today ---
        val todaysHealth = healthMetrics.filter { it.timestamp >= startOfDay }
        val waterToday = todaysHealth.filter { it.type == HealthMetricType.WATER }.sumOf { it.value }
        val sleep = todaysHealth.filter { it.type == HealthMetricType.SLEEP }.maxByOrNull { it.timestamp }?.value ?: 0.0
        val calories = todaysHealth.filter { it.type == HealthMetricType.CALORIES }.sumOf { it.value }

        // --- Financial Stats ---
        val totalIncome = incomes.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val totalSavingsAmount = savings.sumOf { it.amount }
        // Loans: Check remainingAmount to see if it's active
        val activeLoans = loans.count { it.remainingAmount > 0 }

        // --- Productivity Score Calculation ---
        val totalFocusMinutes = focusSessions.filter { 
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() == today
        }.sumOf { it.duration } / 60
        
        val habitPoints = habits.count { it.streak > 0 } * 5
        val healthPoints = if (waterToday >= 2000) 10 else (waterToday / 200).toInt()
        val baseScore = (totalFocusMinutes / 15).toInt() + habitPoints + healthPoints
        val productivityScore = baseScore.coerceIn(0, 100)

        // --- Work Hours ---
        val workHours = totalFocusMinutes / 60.0

        // --- Work-Life Balance Calculation ---
        val sleepScore = min(100.0, (sleep / 8.0) * 100).toInt()
        val workScore = if (workHours in 4.0..8.0) 100 else (100 - abs(6 - workHours) * 10).toInt().coerceAtLeast(0)
        val workLifeBalance = ((workScore + sleepScore) / 2).coerceIn(0, 100)

        AnalyticsData(
            productivityScore = productivityScore,
            focusSessions = focusSessions,
            habits = habits,
            incomes = incomes,
            expenses = expenses,
            healthMetrics = healthMetrics,
            totalWaterToday = waterToday,
            sleepHours = sleep,
            financialBalance = balance,
            totalSavings = totalSavingsAmount,
            activeLoansCount = activeLoans,
            totalCaloriesToday = calories,
            workHoursToday = workHours,
            workLifeBalanceScore = workLifeBalance,
            achievementsCount = achievements.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsData()
    )
}
