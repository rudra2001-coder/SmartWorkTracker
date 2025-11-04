package com.rudra.smartworktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.smartworktracker.data.dao.AchievementDao
import com.rudra.smartworktracker.data.dao.DailyJournalDao
import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.data.dao.FocusSessionDao
import com.rudra.smartworktracker.data.dao.HabitDao
import com.rudra.smartworktracker.data.dao.HealthMetricDao
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.dao.WorkSessionDao
import com.rudra.smartworktracker.data.local.TypeConverters as LocalTypeConverters
import com.rudra.smartworktracker.model.Achievement
import com.rudra.smartworktracker.model.DailyJournal
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkSession

@Database(
    entities = [WorkSession::class, Expense::class, Habit::class, FocusSession::class, HealthMetric::class, Achievement::class, DailyJournal::class, WorkLog::class],
    version = 7, // Incremented version for the new entity
    exportSchema = false
)
@TypeConverters(LocalTypeConverters::class, com.rudra.smartworktracker.data.local.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workSessionDao(): WorkSessionDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun habitDao(): HabitDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyJournalDao(): DailyJournalDao
    abstract fun workLogDao(): WorkLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_work_tracker_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
