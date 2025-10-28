package com.rudra.smartworktracker.di

import android.content.Context
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.repository.WorkLogRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun provideWorkLogDao(database: AppDatabase): WorkLogDao {
        return database.workLogDao()
    }
    
    @Provides
    @Singleton
    fun provideWorkLogRepository(workLogDao: WorkLogDao): WorkLogRepository {
        return WorkLogRepository(workLogDao)
    }
}