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
    suspend fun deleteMealById(mealId: Long)

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: Long): Meal?

    @Query("SELECT * FROM meals WHERE strftime('%Y-%m-%d', date / 1000, 'unixepoch') = :date")
    fun getMealsByDate(date: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear")
    suspend fun getMealsByMonth(monthYear: String): List<Meal>

    @Query("SELECT * FROM meals WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear")
    fun getMealsByMonthFlow(monthYear: String): Flow<List<Meal>>

    @Query("SELECT SUM(mealCount * costPerMeal) FROM meals WHERE strftime('%Y-%m', date / 1000, 'unixepoch') = :monthYear")
    suspend fun getMonthlyTotal(monthYear: String): Double?

    @Query("SELECT SUM(mealCount * costPerMeal) FROM meals WHERE strftime('%Y', date / 1000, 'unixepoch') = :year")
    suspend fun getYearlyTotal(year: String): Double?
}
