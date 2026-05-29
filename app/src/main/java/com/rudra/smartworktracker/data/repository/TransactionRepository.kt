package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.model.IncomeByCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: FinancialTransactionDao) {

    fun getAllTransactions(): Flow<List<FinancialTransaction>> = transactionDao.getAllTransactions()

    fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalIncome().map { it ?: 0.0 }
    }

    fun getTotalExpenses(): Flow<Double> {
        return transactionDao.getTotalExpenses().map { it ?: 0.0 }
    }

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalIncomeBetween(startTime, endTime).map { it ?: 0.0 }
    }

    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getTotalExpensesBetween(startTime, endTime).map { it ?: 0.0 }
    }

    fun getExpenseByCategoryBetween(startTime: Long, endTime: Long): Flow<List<IncomeByCategory>> {
        return transactionDao.getExpenseByCategoryBetween(startTime, endTime)
    }

    fun getIncomeByCategoryBetween(startTime: Long, endTime: Long): Flow<List<IncomeByCategory>> {
        return transactionDao.getIncomeByCategoryBetween(startTime, endTime)
    }

    suspend fun deleteTransaction(transaction: FinancialTransaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteTransactionById(transactionId: Int) {
        transactionDao.deleteTransactionById(transactionId)
    }

    suspend fun getTransactionById(transactionId: Int): FinancialTransaction? {
        return transactionDao.getTransactionById(transactionId)
    }
}
