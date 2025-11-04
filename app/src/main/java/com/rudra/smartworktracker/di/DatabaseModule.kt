package com.rudra.smartworktracker.di

import android.content.Context
import androidx.room.Room
import com.rudra.smartworktracker.data.AppDatabase

object DatabaseModule {
    private var instance: AppDatabase? = null

    fun provideDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "smart_work_tracker_database"
        ).build()
    }
}
