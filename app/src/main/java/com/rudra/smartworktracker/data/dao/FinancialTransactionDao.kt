package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: FinancialTransaction)

    @Query("DELETE FROM financial_transactions WHERE relatedLoanId = :loanId")
    suspend fun deleteTransactionsByLoanId(loanId: Int)

    @Query("SELECT * FROM financial_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>
}
