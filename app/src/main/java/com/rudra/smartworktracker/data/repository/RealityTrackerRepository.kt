package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.RealityTrackerDao
import com.rudra.smartworktracker.model.RealityEntry
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class RealityTrackerRepository(private val dao: RealityTrackerDao) {

    fun getAllEntries(): Flow<List<RealityEntry>> = dao.getAllEntries()

    fun getEntriesByType(type: String): Flow<List<RealityEntry>> = dao.getEntriesByType(type)

    fun getEntriesByCategory(category: String): Flow<List<RealityEntry>> = dao.getEntriesByCategory(category)

    fun getEntriesInRange(startTime: Long, endTime: Long): Flow<List<RealityEntry>> = 
        dao.getEntriesInRange(startTime, endTime)

    fun getTodayStats(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    fun getWeekStats(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.timeInMillis

        return Pair(startOfWeek, endOfWeek)
    }

    fun getMonthStats(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis

        return Pair(startOfMonth, endOfMonth)
    }

    fun getTotalPlannedCount(startTime: Long, endTime: Long): Flow<Int> = 
        dao.getTotalPlannedCount(startTime, endTime)

    fun getCompletedCount(startTime: Long, endTime: Long): Flow<Int> = 
        dao.getCompletedCount(startTime, endTime)

    fun getPlannedCountByType(type: String, startTime: Long, endTime: Long): Flow<Int> =
        dao.getPlannedCountByType(type, startTime, endTime)

    fun getCompletedCountByType(type: String, startTime: Long, endTime: Long): Flow<Int> =
        dao.getCompletedCountByType(type, startTime, endTime)

    suspend fun addEntry(entry: RealityEntry) = dao.insertEntry(entry)

    suspend fun updateEntry(entry: RealityEntry) = dao.updateEntry(entry)

    suspend fun deleteEntry(id: String) = dao.deleteEntry(id)

    suspend fun markAsCompleted(id: String, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        dao.markAsCompleted(id, isCompleted, completedAt)
    }
}
