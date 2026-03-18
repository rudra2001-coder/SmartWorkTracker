package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val checkInType: CheckInType,
    val morningAnswer: String = "",
    val nightAnswer: String = "",
    val morningMood: Int = 0, // 1-5
    val nightMood: Int = 0,   // 1-5
    val completed: Boolean = false
)

enum class CheckInType {
    MORNING,  // "What kind of day will you have?"
    NIGHT     // "Did your actions match your future self?"
}

@Entity(tableName = "consequence_debt")
data class ConsequenceDebt(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: DecisionCategory,
    val debtAmount: Float = 0f,    // Positive = debt, Negative = surplus
    val lastUpdated: Long = System.currentTimeMillis(),
    val transactions: String = ""   // JSON of debt changes
)

@Entity(tableName = "weekly_reports")
data class WeeklyReport(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val weekStartDate: Long,
    val weekEndDate: Long,
    val totalDecisions: Int = 0,
    val positiveDecisions: Int = 0,
    val negativeDecisions: Int = 0,
    val averageDailyScore: Int = 50,
    val bestDay: String = "",
    val worstDay: String = "",
    val biggestMistake: String = "",
    val bestImprovement: String = "",
    val identityAlignment: Int = 0,
    val disciplineStreak: Int = 0,
    val damageStreak: Int = 0,
    val totalDebtPaid: Float = 0f,
    val summary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_history")
data class UserHistory(
    @PrimaryKey val id: String = "user_stats",
    val totalDaysActive: Int = 0,
    val totalDecisions: Int = 0,
    val totalPositiveDecisions: Int = 0,
    val totalNegativeDecisions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val firstEntryDate: Long = System.currentTimeMillis(),
    val lastActiveDate: Long = System.currentTimeMillis()
)
