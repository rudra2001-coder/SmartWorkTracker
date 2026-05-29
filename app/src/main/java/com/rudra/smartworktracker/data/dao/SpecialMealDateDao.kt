package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.SpecialMealDate
import kotlinx.coroutines.flow.Flow

@Dao
interface SpecialMealDateDao {
    @Query("SELECT * FROM special_meal_dates WHERE isDeleted = 0")
    fun getAllSpecialDates(): Flow<List<SpecialMealDate>>

    @Query("SELECT * FROM special_meal_dates WHERE isDeleted = 0")
    suspend fun getAllSpecialDatesList(): List<SpecialMealDate>

    @Query("SELECT * FROM special_meal_dates WHERE date = :date AND isDeleted = 0")
    suspend fun getSpecialDate(date: Long): SpecialMealDate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: SpecialMealDate): Long

    @Query("DELETE FROM special_meal_dates WHERE date = :date")
    suspend fun deleteByDate(date: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM special_meal_dates WHERE date = :date AND isDeleted = 0)")
    suspend fun isSpecialDate(date: Long): Boolean
}
