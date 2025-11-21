package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: FinancialTransactionDao) {

    fun getAllTransactions(): Flow<List<FinancialTransaction>> = transactionDao.getAllTransactions()

    fun getTotalIncome(): Flow<Double> {
        return transactionDao.getAllTransactions().map {
            it.filter { it.type == TransactionType.INCOME || it.type == TransactionType.LOAN_RECEIVE }.sumOf { it.amount }
        }
    }

    fun getTotalExpenses(): Flow<Double> {
        return transactionDao.getAllTransactions().map {
            it.filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.EMI_PAID }.sumOf { it.amount }
        }
    }
}
