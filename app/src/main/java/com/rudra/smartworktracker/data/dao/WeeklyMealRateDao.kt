package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.WeeklyMealRate
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyMealRateDao {
    @Query("SELECT * FROM weekly_meal_rates WHERE weekNumber = :weekNumber AND year = :year AND isDeleted = 0")
    fun getRatesForWeek(weekNumber: Int, year: Int): Flow<List<WeeklyMealRate>>

    @Query("SELECT * FROM weekly_meal_rates WHERE weekNumber = :weekNumber AND year = :year AND isDeleted = 0")
    suspend fun getRatesForWeekList(weekNumber: Int, year: Int): List<WeeklyMealRate>

    @Query("SELECT * FROM weekly_meal_rates WHERE mealTypeId = :mealTypeId AND weekNumber = :weekNumber AND year = :year AND isDeleted = 0")
    suspend fun getRate(mealTypeId: Int, weekNumber: Int, year: Int): WeeklyMealRate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: WeeklyMealRate): Long

    @Query("DELETE FROM weekly_meal_rates WHERE mealTypeId = :mealTypeId AND weekNumber = :weekNumber AND year = :year")
    suspend fun deleteRate(mealTypeId: Int, weekNumber: Int, year: Int)
}
