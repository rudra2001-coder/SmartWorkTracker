package com.rudra.smartworktracker.data.dao

import androidx.room.*
import com.rudra.smartworktracker.data.entity.TravelAndExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelExpenseDao {
    @Query("SELECT * FROM travel_expenses LIMIT 1")
    fun getTravelExpense(): Flow<TravelAndExpense?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: TravelAndExpense)

    @Update
    suspend fun update(expense: TravelAndExpense)

    @Delete
    suspend fun delete(expense: TravelAndExpense)

    @Query("DELETE FROM travel_expenses")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM travel_expenses")
    suspend fun getCount(): Int
}