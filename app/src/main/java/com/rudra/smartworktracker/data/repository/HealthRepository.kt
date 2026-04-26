package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.HealthMetricDao
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import com.rudra.smartworktracker.model.MoodType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class HealthRepository(private val healthMetricDao: HealthMetricDao) {

    fun getAllMetrics(): Flow<List<HealthMetric>> = healthMetricDao.getAllHealthMetrics()

    fun getMetricsBetween(start: Long, end: Long): Flow<List<HealthMetric>> = 
        healthMetricDao.getMetricsBetweenTimestamps(start, end)

    fun getMetricsByType(type: HealthMetricType): Flow<List<HealthMetric>> = 
        healthMetricDao.getMetricsByType(type)

    fun getMetricsByTypeAndTimeRange(type: HealthMetricType, start: Long, end: Long): Flow<List<HealthMetric>> = 
        healthMetricDao.getMetricsByTypeAndTimeRange(type, start, end)

    fun getTodaysMetrics(): Flow<List<HealthMetric>> {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return healthMetricDao.getTodaysMetrics(startOfDay, endOfDay)
    }

    fun getRecentMetrics(limit: Int = 20): Flow<List<HealthMetric>> = 
        healthMetricDao.getRecentMetrics(limit)

    fun getLatestMetricByType(type: HealthMetricType): Flow<HealthMetric?> = 
        healthMetricDao.getLatestMetricByType(type)

    fun getMetricsByTag(tag: String): Flow<List<HealthMetric>> = 
        healthMetricDao.getMetricsByTag(tag)

    fun getAllTags(): Flow<List<String>> = healthMetricDao.getAllTags()

    fun searchMetrics(query: String): Flow<List<HealthMetric>> = 
        healthMetricDao.searchMetrics(query)

    fun getWeeklyMetrics(type: HealthMetricType): Flow<List<HealthMetric>> {
        val now = LocalDateTime.now()
        val weekAgo = now.minusDays(7)
        val start = weekAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return healthMetricDao.getMetricsByTypeAndTimeRange(type, start, end)
    }

    fun getMonthlyMetrics(type: HealthMetricType): Flow<List<HealthMetric>> {
        val now = LocalDateTime.now()
        val monthAgo = now.minusDays(30)
        val start = monthAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return healthMetricDao.getMetricsByTypeAndTimeRange(type, start, end)
    }

    suspend fun insertMetric(metric: HealthMetric): Long = 
        healthMetricDao.insertHealthMetric(metric)

    suspend fun updateMetric(metric: HealthMetric) = 
        healthMetricDao.updateHealthMetric(metric.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteMetric(metric: HealthMetric) = 
        healthMetricDao.deleteHealthMetric(metric)

    suspend fun softDeleteMetric(id: Int) = 
        healthMetricDao.softDeleteMetric(id)

    suspend fun logWater(amount: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.WATER,
            value = amount,
            timestamp = System.currentTimeMillis(),
            notes = notes ?: "Water intake"
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logSleep(hours: Double, quality: Int? = null, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.SLEEP,
            value = hours,
            secondaryValue = quality?.toDouble(),
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logExercise(minutes: Double, exerciseType: String? = null, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.EXERCISE,
            value = minutes,
            timestamp = System.currentTimeMillis(),
            notes = notes ?: exerciseType
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logMood(mood: MoodType, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.MOOD,
            value = mood.ordinal.toDouble(),
            timestamp = System.currentTimeMillis(),
            notes = notes,
            mood = mood
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logStress(level: Int, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.STRESS,
            value = level.toDouble(),
            timestamp = System.currentTimeMillis(),
            notes = notes,
            stressLevel = level
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logWeight(weight: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.WEIGHT,
            value = weight,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logHeartRate(rate: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.HEART_RATE,
            value = rate,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logBloodPressure(systolic: Double, diastolic: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.BLOOD_PRESSURE,
            value = systolic,
            secondaryValue = diastolic,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logSteps(steps: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.STEPS,
            value = steps,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logCalories(calories: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.CALORIES,
            value = calories,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logNutrition(calories: Double, protein: Double, carbs: Double, fiber: Double, fat: Double? = null): List<Long> {
        val ids = mutableListOf<Long>()
        ids.add(logCalories(calories, "Meal"))
        
        if (protein > 0) {
            val proteinMetric = HealthMetric(
                type = HealthMetricType.PROTEIN,
                value = protein,
                timestamp = System.currentTimeMillis(),
                notes = "Protein intake"
            )
            ids.add(healthMetricDao.insertHealthMetric(proteinMetric))
        }
        
        if (carbs > 0) {
            val carbsMetric = HealthMetric(
                type = HealthMetricType.CARBS,
                value = carbs,
                timestamp = System.currentTimeMillis(),
                notes = "Carbs intake"
            )
            ids.add(healthMetricDao.insertHealthMetric(carbsMetric))
        }
        
        if (fiber > 0) {
            val fiberMetric = HealthMetric(
                type = HealthMetricType.FIBER,
                value = fiber,
                timestamp = System.currentTimeMillis(),
                notes = "Fiber intake"
            )
            ids.add(healthMetricDao.insertHealthMetric(fiberMetric))
        }
        
        if (fat != null && fat > 0) {
            val fatMetric = HealthMetric(
                type = HealthMetricType.FAT,
                value = fat,
                timestamp = System.currentTimeMillis(),
                notes = "Fat intake"
            )
            ids.add(healthMetricDao.insertHealthMetric(fatMetric))
        }
        
        return ids
    }

    suspend fun logMeditation(minutes: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.MEDITATION,
            value = minutes,
            timestamp = System.currentTimeMillis(),
            notes = notes,
            duration = (minutes * 60 * 1000).toLong()
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logBreak(breakType: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.BREAKS,
            value = 1.0,
            timestamp = System.currentTimeMillis(),
            notes = breakType ?: "Break taken"
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logSkinCare(product: String, applied: Boolean = true): Long {
        val metric = HealthMetric(
            type = HealthMetricType.SKIN_CARE,
            value = if (applied) 1.0 else 0.0,
            timestamp = System.currentTimeMillis(),
            notes = product
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun logScreenTime(minutes: Double, notes: String? = null): Long {
        val metric = HealthMetric(
            type = HealthMetricType.SCREEN_TIME,
            value = minutes,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )
        return healthMetricDao.insertHealthMetric(metric)
    }

    suspend fun getTodayWaterIntake(): Double {
        return getTodaysMetrics().first()
            .filter { it.type == HealthMetricType.WATER }
            .sumOf { it.value }
    }

    suspend fun getTodayExerciseMinutes(): Double {
        return getTodaysMetrics().first()
            .filter { it.type == HealthMetricType.EXERCISE }
            .sumOf { it.value }
    }

    suspend fun getTodayBreaksCount(): Int {
        return getTodaysMetrics().first()
            .count { it.type == HealthMetricType.BREAKS }
    }

    suspend fun getWeeklyAverage(type: HealthMetricType): Double {
        return getWeeklyMetrics(type).first()
            .takeIf { it.isNotEmpty() }
            ?.map { it.value }
            ?.average() ?: 0.0
    }

    suspend fun getMoodTrend(): List<Pair<LocalDate, MoodType>> {
        return getWeeklyMetrics(HealthMetricType.MOOD).first()
            .mapNotNull { metric ->
                metric.mood?.let {
                    val date = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(metric.timestamp),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                    date to it
                }
            }
    }

    suspend fun getStressTrend(): List<Pair<LocalDate, Int>> {
        return getWeeklyMetrics(HealthMetricType.STRESS).first()
            .mapNotNull { metric ->
                metric.stressLevel?.let {
                    val date = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(metric.timestamp),
                        ZoneId.systemDefault()
                    ).toLocalDate()
                    date to it
                }
            }
    }
}
