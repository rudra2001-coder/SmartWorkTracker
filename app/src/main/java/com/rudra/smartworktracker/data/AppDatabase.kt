package com.rudra.smartworktracker.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.smartworktracker.data.dao.MealDao
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.entity.Meal
import com.rudra.smartworktracker.data.entity.WorkLog

@Database(entities = [WorkLog::class, Meal::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workLogDao(): WorkLogDao
    abstract fun mealDao(): MealDao
}
