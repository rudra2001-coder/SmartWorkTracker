package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: FinancialTransactionDao) {

    fun getAllTransactions(): Flow<List<FinancialTransaction>> = transactionDao.getAllTransactions()

    fun getTotalIncome(): Flow<Double> = transactionDao.getTotalIncome()

    fun getTotalExpenses(): Flow<Double> = transactionDao.getTotalExpenses()

    suspend fun deleteTransaction(transaction: FinancialTransaction) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteTransactionById(transactionId: Int) {
        transactionDao.deleteTransactionById(transactionId)
    }

    suspend fun getTransactionById(transactionId: Int): FinancialTransaction? {
        return transactionDao.getTransactionById(transactionId)
    }

    suspend fun insertTransaction(transaction: FinancialTransaction) {
        transactionDao.insertTransaction(transaction)
    }
}
