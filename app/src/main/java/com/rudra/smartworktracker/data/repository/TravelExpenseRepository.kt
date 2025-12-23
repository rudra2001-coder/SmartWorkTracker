package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.TravelExpenseDao
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import kotlinx.coroutines.flow.Flow

class TravelExpenseRepository(
    private val travelExpenseDao: TravelExpenseDao
) {
    fun getTravelExpense(): Flow<TravelAndExpense?> = travelExpenseDao.getTravelExpense()

    suspend fun saveTravelExpense(travelAndExpense: TravelAndExpense) {
        travelExpenseDao.insert(travelAndExpense)
    }

    suspend fun updateTravelExpense(travelAndExpense: TravelAndExpense) {
        travelExpenseDao.update(travelAndExpense)
    }

    suspend fun deleteTravelExpense(travelAndExpense: TravelAndExpense) {
        travelExpenseDao.delete(travelAndExpense)
    }

    suspend fun deleteAllTravelExpenses() {
        travelExpenseDao.deleteAll()
    }

    suspend fun getTravelExpenseCount(): Int {
        return travelExpenseDao.getCount()
    }
}