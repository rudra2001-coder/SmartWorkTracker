package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(creditCard: CreditCard)

    @Update
    suspend fun updateCard(creditCard: CreditCard)

    @Query("SELECT * FROM credit_cards WHERE isDeleted = 0 ORDER BY cardName ASC")
    fun getAllCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :cardId")
    fun getCardById(cardId: Int): Flow<CreditCard?>

    @Query("SELECT * FROM credit_cards WHERE isDeleted = 0")
    fun getAllCreditCards(): Flow<List<CreditCard>>

    @Query("UPDATE credit_cards SET isDeleted = 1, updatedAt = :timestamp WHERE id = :cardId")
    suspend fun softDeleteCard(cardId: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM credit_cards WHERE id = :cardId")
    suspend fun deleteCard(cardId: Int)
}
