package com.rudra.smartworktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.data.entity.WorkLog

@Database(entities = [WorkLog::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workLogDao(): WorkLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_work_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}