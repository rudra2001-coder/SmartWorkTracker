package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.IncomeByCategory
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {

    fun getIncomes(page: Int, pageSize: Int): Flow<List<Income>> {
        val offset = (page - 1) * pageSize
        return incomeDao.getPaginatedIncomes(offset, pageSize)
    }

    suspend fun insertIncome(income: Income) {
        incomeDao.insertIncome(income)
    }

    suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

    fun getIncomesBetween(startTime: Long, endTime: Long): Flow<List<Income>> {
        return incomeDao.getIncomesBetween(startTime, endTime)
    }

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return incomeDao.getTotalIncomeBetween(startTime, endTime)
    }

    fun getIncomesByCategoryBetween(startTime: Long, endTime: Long): Flow<List<IncomeByCategory>> {
        return incomeDao.getIncomesByCategoryBetween(startTime, endTime)
    }
    
    fun getAllIncomes(): Flow<List<Income>> {
        return incomeDao.getAllIncomes()
    }

    suspend fun clearAll() {
        incomeDao.deleteAll()
    }
}
