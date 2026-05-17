package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM financial_transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

    @Query("DELETE FROM financial_transactions WHERE relatedLoanId = :loanId")
    suspend fun deleteTransactionsByLoanId(loanId: Int)

    @Query("SELECT * FROM financial_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Query("SELECT * FROM financial_transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): FinancialTransaction?

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type IN ('INCOME', 'LOAN_RECEIVE')")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type IN ('EXPENSE', 'EMI_PAID')")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type IN ('INCOME', 'LOAN_RECEIVE') AND date BETWEEN :startTime AND :endTime")
    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM financial_transactions WHERE type IN ('EXPENSE', 'EMI_PAID') AND date BETWEEN :startTime AND :endTime")
    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double?>

    @Delete
    suspend fun delete(financialTransaction: FinancialTransaction)
}
