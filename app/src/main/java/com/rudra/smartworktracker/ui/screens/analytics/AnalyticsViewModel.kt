package com.rudra.smartworktracker.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.*
import com.rudra.smartworktracker.data.entity.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    val achievementsCount: Int = 0,
    val focusScore: Long = 0,
    val stepsToday: Int = 0,
    val productivityTrend: Float = 0f,
    val workHoursTrend: Float = 0f,
    val caloriesTrend: Float = 0f,
    val achievementsTrend: Float = 0f,
    val focusTrend: Float = 0f,
    val habitCompletionRate: Float = 0f,
    val recentAchievements: List<Achievement> = emptyList(),
    val weeklyPerformance: List<WeeklyPerformance> = emptyList()
)

data class WeeklyPerformance(
    val day: String,
    val productivityScore: Int,
    val date: LocalDate
)

class AnalyticsViewModel(private val db: AppDatabase) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    fun setSelectedPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
    }

    @Suppress("UNCHECKED_CAST")
    val analyticsData: StateFlow<AnalyticsData> = combine(
        db.focusSessionDao().getAllFocusSessions(),
        db.habitDao().getAllHabits(),
        db.incomeDao().getAllIncomes(),
        db.expenseDao().getAllExpenses(),
        db.healthMetricDao().getAllHealthMetrics(),
        db.savingsDao().getAllSavings(),
        db.loanDao().getAllLoans(),
        db.achievementDao().getAllAchievements(),
        _selectedPeriod
    ) { args: Array<Any?> ->
        val focusSessions = args[0] as List<FocusSession>
        val habits = args[1] as List<Habit>
        val incomes = args[2] as List<Income>
        val expenses = args[3] as List<Expense>
        val healthMetrics = args[4] as List<HealthMetric>
        val savings = args[5] as List<Savings>
        val loans = args[6] as List<Loan>
        val achievements = args[7] as List<Achievement>
        val period = args[8] as AnalyticsPeriod

        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Get date range based on selected period
        val startDate = when (period) {
            AnalyticsPeriod.WEEK -> today.minusWeeks(1)
            AnalyticsPeriod.MONTH -> today.minusMonths(1)
            AnalyticsPeriod.QUARTER -> today.minusMonths(3)
            AnalyticsPeriod.YEAR -> today.minusYears(1)
        }
        val startTimestamp = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // --- Health Stats for Today ---
        val todaysHealth = healthMetrics.filter { it.timestamp >= startOfDay }
        val waterToday = todaysHealth.filter { it.type == HealthMetricType.WATER }.sumOf { it.value }
        val sleep = todaysHealth.filter { it.type == HealthMetricType.SLEEP }.maxByOrNull { it.timestamp }?.value ?: 0.0
        val calories = todaysHealth.filter { it.type == HealthMetricType.CALORIES }.sumOf { it.value }
        val steps = todaysHealth.filter { it.type == HealthMetricType.EXERCISE }.sumOf { it.value }.toInt()

        // --- Financial Stats ---
        val periodIncomes = incomes.filter { it.timestamp >= startTimestamp }
        val periodExpenses = expenses.filter { it.timestamp >= startTimestamp }
        val totalIncome = periodIncomes.sumOf { it.amount }
        val totalExpense = periodExpenses.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val totalSavingsAmount = savings.sumOf { it.amount }
        val activeLoans = loans.count { it.remainingAmount > 0 }

        // --- Productivity Score Calculation ---
        val todaysFocusSessions = focusSessions.filter {
            LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() == today
        }
        val totalFocusMinutes = todaysFocusSessions.sumOf { it.duration } / 60

        val habitPoints = habits.count { it.streak > 0 } * 5
        val healthPoints = if (waterToday >= 2000) 10 else (waterToday / 200).toInt()
        val baseScore = (totalFocusMinutes / 15).toInt() + habitPoints + healthPoints
        val productivityScore = baseScore.coerceIn(0, 100)

        // Focus score (based on deep work sessions)
        val deepWorkMinutes = todaysFocusSessions.filter { it.type == FocusType.DEEP_WORK }.sumOf { it.duration } / 60
        val focusScore = (deepWorkMinutes / 4 * 20).coerceIn(0, 100)

        // --- Work Hours ---
        val workHours = totalFocusMinutes / 60.0

        // --- Work-Life Balance Calculation ---
        val sleepScore = min(100.0, (sleep / 8.0) * 100).toInt()
        val workScore = if (workHours in 4.0..8.0) 100 else (100 - abs(6 - workHours) * 10).toInt().coerceAtLeast(0)
        val workLifeBalance = ((workScore + sleepScore) / 2).coerceIn(0, 100)

        // --- Trends Calculation (compare with previous period) ---
        val previousStartDate = when (period) {
            AnalyticsPeriod.WEEK -> today.minusWeeks(2)
            AnalyticsPeriod.MONTH -> today.minusMonths(2)
            AnalyticsPeriod.QUARTER -> today.minusMonths(6)
            AnalyticsPeriod.YEAR -> today.minusYears(2)
        }
        val previousStartTimestamp = previousStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val previousPeriodSessions = focusSessions.filter {
            it.timestamp in previousStartTimestamp until startTimestamp
        }
        val currentPeriodSessions = focusSessions.filter {
            it.timestamp >= startTimestamp
        }

        val previousProductivity = calculateProductivityScore(
            previousPeriodSessions,
            habits,
            healthMetrics.filter { it.timestamp in previousStartTimestamp until startTimestamp }
        )
        val currentProductivity = productivityScore
        val productivityTrend = if (previousProductivity > 0)
            ((currentProductivity - previousProductivity) / previousProductivity * 100).toFloat() else 0f

        // --- Habit Completion Rate ---
        val habitCompletionRate = if (habits.isNotEmpty())
            habits.count { it.streak > 0 }.toFloat() / habits.size else 0f

        // --- Recent Achievements (last 5) ---
        val recentAchievements = achievements
            .filter { it.unlockedTimestamp != null }
            .sortedByDescending { it.unlockedTimestamp }
            .take(5)

        // --- Weekly Performance ---
        val weeklyPerformance = (0..6).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val daySessions = focusSessions.filter {
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() == date
            }
            val dayHealth = healthMetrics.filter {
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() == date
            }
            val dayProductivity = calculateProductivityScore(daySessions, habits, dayHealth)
            WeeklyPerformance(
                day = date.format(DateTimeFormatter.ofPattern("EEE")),
                productivityScore = dayProductivity,
                date = date
            )
        }.reversed()

        AnalyticsData(
            productivityScore = productivityScore,
            focusSessions = focusSessions,
            habits = habits,
            incomes = periodIncomes,
            expenses = periodExpenses,
            healthMetrics = healthMetrics,
            totalWaterToday = waterToday,
            sleepHours = sleep,
            financialBalance = balance,
            totalSavings = totalSavingsAmount,
            activeLoansCount = activeLoans,
            totalCaloriesToday = calories,
            workHoursToday = workHours,
            workLifeBalanceScore = workLifeBalance,
            achievementsCount = achievements.size,
            focusScore = focusScore,
            stepsToday = steps,
            productivityTrend = productivityTrend,
            workHoursTrend = 0f, // Calculate as needed
            caloriesTrend = 0f, // Calculate as needed
            achievementsTrend = 0f, // Calculate as needed
            focusTrend = 0f, // Calculate as needed
            habitCompletionRate = habitCompletionRate,
            recentAchievements = recentAchievements,
            weeklyPerformance = weeklyPerformance
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsData()
    )

    private fun calculateProductivityScore(
        sessions: List<FocusSession>,
        habits: List<Habit>,
        healthMetrics: List<HealthMetric>
    ): Int {
        val totalMinutes = sessions.sumOf { it.duration } / 60
        val habitPoints = habits.count { it.streak > 0 } * 5
        val waterIntake = healthMetrics.filter { it.type == HealthMetricType.WATER }.sumOf { it.value }
        val healthPoints = if (waterIntake >= 2000) 10 else (waterIntake / 200).toInt()
        return ((totalMinutes / 15).toInt() + habitPoints + healthPoints).coerceIn(0, 100)
    }
}