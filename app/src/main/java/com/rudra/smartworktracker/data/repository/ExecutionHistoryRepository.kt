package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.ExecutionHistoryDao
import com.rudra.smartworktracker.data.entity.ExecutionHistoryEntity
import kotlinx.coroutines.flow.Flow

class ExecutionHistoryRepository(
    private val dao: ExecutionHistoryDao
) {
    fun getAll(): Flow<List<ExecutionHistoryEntity>> = dao.getAll()

    fun getRecent(limit: Int = 50): Flow<List<ExecutionHistoryEntity>> = dao.getRecent(limit)

    fun getByRuleId(ruleId: Long): Flow<List<ExecutionHistoryEntity>> = dao.getByRuleId(ruleId)

    suspend fun insert(entry: ExecutionHistoryEntity): Long = dao.insert(entry)

    suspend fun deleteAll() = dao.deleteAll()
}
