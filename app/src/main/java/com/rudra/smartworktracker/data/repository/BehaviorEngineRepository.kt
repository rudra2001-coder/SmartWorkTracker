package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.CheckInDao
import com.rudra.smartworktracker.data.dao.ConsequenceDebtDao
import com.rudra.smartworktracker.data.dao.DecisionDao
import com.rudra.smartworktracker.data.dao.WeeklyReportDao
import com.rudra.smartworktracker.data.dao.UserHistoryDao
import com.rudra.smartworktracker.model.ConsequenceDebt
import com.rudra.smartworktracker.model.DailyCheckIn
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.DecisionCategory
import com.rudra.smartworktracker.model.DecisionType
import com.rudra.smartworktracker.model.UserHistory
import com.rudra.smartworktracker.model.WeeklyReport
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BehaviorEngineRepository(
    private val decisionDao: DecisionDao,
    private val checkInDao: CheckInDao,
    private val consequenceDebtDao: ConsequenceDebtDao,
    private val weeklyReportDao: WeeklyReportDao,
    private val userHistoryDao: UserHistoryDao
) {
    // Decision operations
    fun getAllDecisions(): Flow<List<Decision>> = decisionDao.getAllDecisions()
    
    fun getDecisionsInRange(startTime: Long, endTime: Long): Flow<List<Decision>> = 
        decisionDao.getDecisionsInRange(startTime, endTime)
    
    suspend fun addDecision(decision: Decision) {
        decisionDao.insertDecision(decision)
        updateDebt(decision)
        updateUserHistory(decision)
    }
    
    suspend fun deleteDecision(id: String) = decisionDao.deleteDecisionById(id)

    // Check-in operations
    fun getTodayCheckIn(type: String): Flow<DailyCheckIn?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        return checkInDao.getCheckInByType(type, startOfDay, endOfDay)
    }
    
    suspend fun saveCheckIn(checkIn: DailyCheckIn) = checkInDao.insertCheckIn(checkIn)

    // Consequence Debt operations
    fun getAllDebts(): Flow<List<ConsequenceDebt>> = consequenceDebtDao.getAllDebts()
    fun getTotalDebt(): Flow<Float?> = consequenceDebtDao.getTotalDebt()
    
    private suspend fun updateDebt(decision: Decision) {
        val category = decision.decisionType.category
        val impact = if (decision.isPositive) {
            -decision.decisionType.defaultImpact // Negative debt = good
        } else {
            decision.decisionType.defaultImpact // Positive debt = bad
        }
        
        // Check if debt exists for this category
        val existingDebt = ConsequenceDebt(
            id = category.name,
            category = category,
            debtAmount = 0f
        )
        consequenceDebtDao.insertDebt(existingDebt)
        consequenceDebtDao.addToDebt(category.name, impact, System.currentTimeMillis())
    }

    suspend fun reduceDebt(category: DecisionCategory, amount: Float) {
        val existingDebt = ConsequenceDebt(
            id = category.name,
            category = category,
            debtAmount = 0f
        )
        consequenceDebtDao.insertDebt(existingDebt)
        consequenceDebtDao.addToDebt(category.name, -amount, System.currentTimeMillis()) // Negative = reduces debt
    }

    // User History operations
    fun getUserHistory(): Flow<UserHistory?> = userHistoryDao.getUserHistory()
    
    private suspend fun updateUserHistory(decision: Decision) {
        val history = UserHistory()
        userHistoryDao.insertHistory(history)
        
        val positive = if (decision.isPositive) 1 else 0
        val negative = if (decision.isPositive) 0 else 1
        userHistoryDao.addDecision(positive, negative)
    }

    suspend fun initUserHistory() {
        userHistoryDao.insertHistory(UserHistory())
    }

    // Weekly Report operations
    fun getLatestReport(): Flow<WeeklyReport?> = weeklyReportDao.getLatestReport()
    fun getAllReports(): Flow<List<WeeklyReport>> = weeklyReportDao.getAllReports()
    
    suspend fun generateWeeklyReport(
        weekStart: Long,
        weekEnd: Long,
        decisions: List<Decision>,
        avgScore: Int,
        identityAlignment: Int,
        disciplineStreak: Int,
        damageStreak: Int
    ): WeeklyReport {
        val positive = decisions.count { it.isPositive }
        val negative = decisions.count { !it.isPositive }
        
        val categoryCounts = decisions.groupBy { it.decisionType.category }
        val worstCategory = categoryCounts.maxByOrNull { it.value.size }?.key
        val bestCategory = categoryCounts.minByOrNull { it.value.size }?.key
        
        val worstType = decisions
            .filter { !it.isPositive }
            .groupBy { it.decisionType }
            .maxByOrNull { it.value.size }
            ?.key
            
        val bestType = decisions
            .filter { it.isPositive }
            .groupBy { it.decisionType }
            .maxByOrNull { it.value.size }
            ?.key
        
        val summary = when {
            positive > negative * 2 -> "Excellent week! You're building strong positive habits."
            positive > negative -> "Good week! You're moving in the right direction."
            negative > positive * 2 -> "Destructive pattern detected. This is your wake-up call."
            else -> "Mixed week. Focus on consistency."
        }
        
        val report = WeeklyReport(
            weekStartDate = weekStart,
            weekEndDate = weekEnd,
            totalDecisions = decisions.size,
            positiveDecisions = positive,
            negativeDecisions = negative,
            averageDailyScore = avgScore,
            worstDay = worstCategory?.displayName ?: "None",
            bestDay = bestCategory?.displayName ?: "None",
            biggestMistake = worstType?.displayName ?: "None",
            bestImprovement = bestType?.displayName ?: "None",
            identityAlignment = identityAlignment,
            disciplineStreak = disciplineStreak,
            damageStreak = damageStreak,
            summary = summary
        )
        
        weeklyReportDao.insertReport(report)
        return report
    }

    // Helper functions
    fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun get7DaysAgo(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun get30DaysAgo(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
