package com.rudra.smartworktracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rudra.smartworktracker.data.entity.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTypeDao {
    @Query("SELECT * FROM meal_types WHERE isDeleted = 0 ORDER BY sortOrder ASC")
    fun getAllMealTypes(): Flow<List<MealType>>

    @Query("SELECT * FROM meal_types WHERE isDeleted = 0 ORDER BY sortOrder ASC")
    suspend fun getAllMealTypesList(): List<MealType>

    @Query("SELECT * FROM meal_types WHERE id = :id")
    suspend fun getMealTypeById(id: Int): MealType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealType: MealType): Long

    @Update
    suspend fun update(mealType: MealType)

    @Query("UPDATE meal_types SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Int, timestamp: Long = System.currentTimeMillis())
}
