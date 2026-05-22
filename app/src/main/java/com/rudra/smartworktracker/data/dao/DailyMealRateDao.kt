package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.DailyMealRate
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMealRateDao {
    @Query("SELECT * FROM daily_meal_rates WHERE date = :date AND isDeleted = 0")
    fun getRatesForDate(date: Long): Flow<List<DailyMealRate>>

    @Query("SELECT * FROM daily_meal_rates WHERE isDeleted = 0")
    suspend fun getAllDailyMealRates(): List<DailyMealRate>

    @Query("SELECT * FROM daily_meal_rates WHERE date = :date AND isDeleted = 0")
    suspend fun getRatesForDateList(date: Long): List<DailyMealRate>

    @Query("SELECT * FROM daily_meal_rates WHERE date IN (:dates) AND isDeleted = 0")
    suspend fun getRatesForDates(dates: List<Long>): List<DailyMealRate>

    @Query("SELECT * FROM daily_meal_rates WHERE mealTypeId = :mealTypeId AND date = :date AND isDeleted = 0")
    suspend fun getRate(mealTypeId: Int, date: Long): DailyMealRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: DailyMealRate): Long

    @Query("DELETE FROM daily_meal_rates WHERE mealTypeId = :mealTypeId AND date = :date")
    suspend fun deleteRate(mealTypeId: Int, date: Long)
}
