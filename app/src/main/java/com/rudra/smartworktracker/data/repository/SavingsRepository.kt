package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.SavingsDao
import com.rudra.smartworktracker.data.entity.Savings
import kotlinx.coroutines.flow.Flow

class SavingsRepository(private val savingsDao: SavingsDao) {

    fun getSavings(): Flow<Double> = savingsDao.getTotalSavings()

    fun getSavingsHistory(): Flow<List<Savings>> = savingsDao.getSavingsHistory()

    suspend fun addToSavings(amount: Double, note: String = "", category: String = "Deposit") {
        val savings = Savings(
            amount = amount,
            note = note,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        savingsDao.insert(savings)
    }

    suspend fun withdrawFromSavings(amount: Double, note: String = "", category: String = "Withdrawal") {
        val savings = Savings(
            amount = -amount,
            note = note,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        savingsDao.insert(savings)
    }

    suspend fun deleteTransaction(savings: Savings) {
        savingsDao.delete(savings)
    }

    fun getSavingsBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return savingsDao.getSavingsBetween(startTime, endTime)
    }

    fun getSavingsByCategory(category: String): Flow<List<Savings>> {
        return savingsDao.getSavingsByCategory(category)
    }

    fun searchSavings(query: String): Flow<List<Savings>> {
        return savingsDao.searchSavings(query)
    }

    fun getSavingsSince(startTime: Long): Flow<List<Savings>> {
        return savingsDao.getSavingsSince(startTime)
    }

    suspend fun clearAll() {
        savingsDao.deleteAll()
    }
}
