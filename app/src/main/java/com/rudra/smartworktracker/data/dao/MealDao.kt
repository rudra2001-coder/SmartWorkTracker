package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rudra.smartworktracker.data.entity.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Query("SELECT * FROM meals")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("DELETE FROM meals")
    suspend fun clearAllMeals()
    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: Int)
    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: Int): Meal?
    @Query("SELECT * FROM meals WHERE date = :date")
    fun getMealsByDate(date: String): Flow<List<Meal>>




}
