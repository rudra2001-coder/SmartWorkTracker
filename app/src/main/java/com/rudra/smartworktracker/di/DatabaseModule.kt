package com.rudra.smartworktracker.di

import android.content.Context
import androidx.room.Room
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.dao.MealDao
import com.rudra.smartworktracker.data.dao.WorkLogDao

object DatabaseModule {

    private var database: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "smart_work_tracker.db"
                    ).fallbackToDestructiveMigration(false).build().also {
                database = it
            }
        }
    }

    fun provideWorkLogDao(database: AppDatabase): WorkLogDao {
        return database.workLogDao()
    }

    fun provideMealDao(database: AppDatabase): MealDao {
        return database.mealDao()
    }
}
