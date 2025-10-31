package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.MealDao
import com.rudra.smartworktracker.data.entity.Meal
import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {

    fun getAllMeals(): Flow<List<Meal>> {
        return mealDao.getAllMeals()
    }

    suspend fun insertMeal(meal: Meal) {
        mealDao.insertMeal(meal)
    }

    suspend fun clearAllMeals() {
        mealDao.clearAllMeals()
    }
}
