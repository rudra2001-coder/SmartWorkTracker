package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.CreditCard
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Insert
    suspend fun insertCard(creditCard: CreditCard)

    @Update
    suspend fun updateCard(creditCard: CreditCard)

    @Query("SELECT * FROM credit_cards ORDER BY cardName ASC")
    fun getAllCards(): Flow<List<CreditCard>>

    @Query("SELECT * FROM credit_cards WHERE id = :cardId")
    fun getCardById(cardId: Int): Flow<CreditCard?>
}
