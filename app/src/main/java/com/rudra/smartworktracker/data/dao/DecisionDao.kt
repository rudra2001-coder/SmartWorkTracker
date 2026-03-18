package com.rudra.smartworktracker.data.dao

import androidx.room.*
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.DecisionCategory
import com.rudra.smartworktracker.model.DecisionType
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions ORDER BY createdAt DESC")
    fun getAllDecisions(): Flow<List<Decision>>

    @Query("SELECT * FROM decisions WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getDecisionsInRange(startTime: Long, endTime: Long): Flow<List<Decision>>

    @Query("SELECT * FROM decisions WHERE decisionType = :type ORDER BY createdAt DESC")
    fun getDecisionsByType(type: String): Flow<List<Decision>>

    @Query("SELECT * FROM decisions WHERE category = :category ORDER BY createdAt DESC")
    fun getDecisionsByCategory(category: String): Flow<List<Decision>>

    @Query("SELECT * FROM decisions WHERE createdAt >= :startTime ORDER BY createdAt DESC")
    fun getRecentDecisions(startTime: Long): Flow<List<Decision>>

    @Query("SELECT COUNT(*) FROM decisions WHERE decisionType = :type AND createdAt >= :startTime")
    fun getDecisionCountByType(type: String, startTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM decisions WHERE category = :category AND createdAt >= :startTime")
    fun getDecisionCountByCategory(category: String, startTime: Long): Flow<Int>

    @Query("SELECT SUM(CASE WHEN isPositive = 1 THEN 1 ELSE 0 END) FROM decisions WHERE createdAt >= :startTime")
    fun getPositiveDecisionCount(startTime: Long): Flow<Int>

    @Query("SELECT SUM(CASE WHEN isPositive = 0 THEN 1 ELSE 0 END) FROM decisions WHERE createdAt >= :startTime")
    fun getNegativeDecisionCount(startTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: Decision)

    @Delete
    suspend fun deleteDecision(decision: Decision)

    @Query("DELETE FROM decisions WHERE id = :id")
    suspend fun deleteDecisionById(id: String)

    @Query("DELETE FROM decisions WHERE createdAt < :timestamp")
    suspend fun deleteOldDecisions(timestamp: Long)
}
