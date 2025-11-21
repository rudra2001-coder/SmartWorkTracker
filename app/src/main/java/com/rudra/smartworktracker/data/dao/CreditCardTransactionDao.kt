package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: CreditCardTransaction)

    @Query("SELECT * FROM credit_card_transactions WHERE cardId = :cardId ORDER BY date DESC")
    fun getTransactionsForCard(cardId: Int): Flow<List<CreditCardTransaction>>
}
