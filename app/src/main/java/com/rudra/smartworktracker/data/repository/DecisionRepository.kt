package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.DecisionDao
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.DecisionCategory
import com.rudra.smartworktracker.model.DecisionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class DecisionRepository(private val dao: DecisionDao) {

    fun getAllDecisions(): Flow<List<Decision>> = dao.getAllDecisions()

    fun getDecisionsInRange(startTime: Long, endTime: Long): Flow<List<Decision>> = 
        dao.getDecisionsInRange(startTime, endTime)

    fun getDecisionsByType(type: DecisionType): Flow<List<Decision>> = 
        dao.getDecisionsByType(type.name)

    fun getDecisionsByCategory(category: DecisionCategory): Flow<List<Decision>> = 
        dao.getDecisionsByCategory(category.name)

    fun getRecentDecisions(startTime: Long): Flow<List<Decision>> = 
        dao.getRecentDecisions(startTime)

    fun getDecisionCountByType(type: DecisionType, startTime: Long): Flow<Int> = 
        dao.getDecisionCountByType(type.name, startTime)

    fun getDecisionCountByCategory(category: DecisionCategory, startTime: Long): Flow<Int> =
        dao.getDecisionCountByCategory(category.name, startTime)

    fun getPositiveDecisionCount(startTime: Long): Flow<Int> =
        dao.getPositiveDecisionCount(startTime)

    fun getNegativeDecisionCount(startTime: Long): Flow<Int> =
        dao.getNegativeDecisionCount(startTime)

    suspend fun addDecision(decision: Decision) = dao.insertDecision(decision)

    suspend fun deleteDecision(decision: Decision) = dao.deleteDecision(decision)

    suspend fun deleteDecisionById(id: String) = dao.deleteDecisionById(id)

    suspend fun deleteOldDecisions(olderThanDays: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -olderThanDays)
        dao.deleteOldDecisions(calendar.timeInMillis)
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

    fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
