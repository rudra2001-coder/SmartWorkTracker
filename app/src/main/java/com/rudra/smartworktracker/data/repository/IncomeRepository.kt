package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.data.entity.Income
import kotlinx.coroutines.flow.Flow

class IncomeRepository(private val incomeDao: IncomeDao) {

    fun getLatestIncome(): Flow<Income?> {
        return incomeDao.getLatestIncome()
    }

    fun getIncomesBetween(startTime: Long, endTime: Long): Flow<List<Income>> {
        return incomeDao.getIncomesBetween(startTime, endTime)
    }

    fun getAllIncomes(): Flow<List<Income>> {
        return incomeDao.getAllIncomes()
    }

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return incomeDao.getTotalIncomeBetween(startTime, endTime)
    }

    suspend fun insertIncome(income: Income) {
        incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: Income) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: Income) {
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(incomeId: Long) {
        incomeDao.deleteIncomeById(incomeId)
    }
}
