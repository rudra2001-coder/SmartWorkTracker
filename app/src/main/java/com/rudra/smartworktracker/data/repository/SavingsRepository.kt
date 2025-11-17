package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.SavingsDao
import com.rudra.smartworktracker.data.entity.Savings
import kotlinx.coroutines.flow.Flow

class SavingsRepository(private val savingsDao: SavingsDao) {

    fun getSavings(): Flow<Double> = savingsDao.getTotalSavings()

    fun getSavingsHistory(): Flow<List<Savings>> = savingsDao.getSavingsHistory()

    suspend fun addToSavings(amount: Double) {
        val savings = Savings(amount = amount, timestamp = System.currentTimeMillis())
        savingsDao.insert(savings)
    }

    suspend fun withdrawFromSavings(amount: Double) {
        val savings = Savings(amount = -amount, timestamp = System.currentTimeMillis())
        savingsDao.insert(savings)
    }
}
