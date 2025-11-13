package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.model.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetween(startTime, endTime)
    }

    fun getMealExpensesBetween(startTime: Long, endTime: Long): Flow<Double> {
        return expenseDao.getMealExpensesBetween(startTime, endTime)
    }

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
}
