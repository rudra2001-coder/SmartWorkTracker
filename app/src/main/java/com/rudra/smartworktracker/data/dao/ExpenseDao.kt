package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rudra.smartworktracker.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = 'MEAL' AND timestamp BETWEEN :startTime AND :endTime")
    fun getMealExpensesBetween(startTime: Long, endTime: Long): Flow<Double>
}
