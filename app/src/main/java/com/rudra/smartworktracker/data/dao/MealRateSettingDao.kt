package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.MealRateSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface MealRateSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealRateSetting: MealRateSetting)

    @Query("SELECT * FROM meal_rate_settings ORDER BY id ASC")
    fun getAllMealRateSettings(): Flow<List<MealRateSetting>>

    @Query("DELETE FROM meal_rate_settings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_rate_settings")
    suspend fun clearAll()
}
