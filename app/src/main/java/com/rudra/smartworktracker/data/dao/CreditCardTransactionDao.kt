package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CreditCardTransaction)

    @Query("SELECT * FROM credit_card_transactions WHERE cardId = :cardId ORDER BY date DESC")
    fun getTransactionsForCard(cardId: Int): Flow<List<CreditCardTransaction>>

    @Query("SELECT * FROM credit_card_transactions")
    fun getAllTransactions(): Flow<List<CreditCardTransaction>>

    @Query("DELETE FROM credit_card_transactions WHERE cardId = :cardId")
    suspend fun deleteTransactionsByCardId(cardId: Int)
}
