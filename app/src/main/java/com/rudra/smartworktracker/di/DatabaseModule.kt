package com.rudra.smartworktracker.di

import android.content.Context
import androidx.room.Room
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.dao.MealDao
import com.rudra.smartworktracker.data.dao.WorkLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_work_tracker.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWorkLogDao(database: AppDatabase): WorkLogDao {
        return database.workLogDao()
    }

    @Provides
    fun provideMealDao(database: AppDatabase): MealDao {
        return database.mealDao()
    }
}
