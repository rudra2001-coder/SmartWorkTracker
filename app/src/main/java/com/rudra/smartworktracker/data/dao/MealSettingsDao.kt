package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.MealSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface MealSettingsDao {
    @Query("SELECT * FROM meal_settings WHERE id = 'meal_settings'")
    fun getMealSettings(): Flow<MealSettings?>

    @Query("SELECT * FROM meal_settings WHERE id = 'meal_settings'")
    suspend fun getMealSettingsOnce(): MealSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: MealSettings)
}
