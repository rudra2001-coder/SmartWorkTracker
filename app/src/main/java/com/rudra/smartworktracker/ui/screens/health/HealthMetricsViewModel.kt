package com.rudra.smartworktracker.ui.screens.health

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.util.*
import kotlin.math.*

class HealthMetricsViewModel(application: Application) : AndroidViewModel(application) {

    private val healthMetricDao = AppDatabase.getDatabase(application).healthMetricDao()
    private val sharedPrefs = application.getSharedPreferences("work_health", Context.MODE_PRIVATE)

    // UI State
    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    // Analytics Data
    private val _healthAnalytics = MutableStateFlow(HealthAnalytics())
    val healthAnalytics: StateFlow<HealthAnalytics> = _healthAnalytics.asStateFlow()

    // Goals
    private val _goals = MutableStateFlow(
        mapOf(
            HealthMetricType.WEIGHT to 70.0,
            HealthMetricType.WATER to 3000.0,
            HealthMetricType.SLEEP to 7.5,
            HealthMetricType.BREAKS to 8.0,
            HealthMetricType.EXERCISE to 30.0
        )
    )
    val goals: StateFlow<Map<HealthMetricType, Double>> = _goals.asStateFlow()

    // Daily Work Routine
    private val _dailyWorkRoutine = MutableStateFlow(DailyWorkRoutine())
    val dailyWorkRoutine: StateFlow<DailyWorkRoutine> = _dailyWorkRoutine.asStateFlow()

    // Health Data Stream
    val healthData: StateFlow<HealthData> = healthMetricDao.getAllHealthMetrics()
        .map { metrics ->
            processHealthData(metrics)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HealthData()
        )

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            healthData.collect { data ->
                _healthAnalytics.value = calculateAnalytics(data)
                updateDailyWorkRoutine(data)
            }
        }
        startRealTimeTimer()
    }

    private fun startRealTimeTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(60000) // Update every minute
                updateDailyWorkRoutine(healthData.value)
            }
        }
    }

    fun saveHealthMetric(type: HealthMetricType, value: Double, notes: String = "") {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val isValid = when (type) {
                    HealthMetricType.WEIGHT -> value in 40.0..200.0
                    HealthMetricType.HEIGHT -> value in 100.0..250.0
                    HealthMetricType.WATER -> value in 0.0..10000.0
                    HealthMetricType.SLEEP -> value in 0.0..16.0
                    HealthMetricType.BREAKS -> value in 0.0..20.0
                    HealthMetricType.EXERCISE -> value in 0.0..480.0
                    HealthMetricType.SCREEN_TIME -> value in 0.0..1440.0 // minutes
                    HealthMetricType.POSTURE -> value in 0.0..1.0
                    HealthMetricType.FOCUS -> value in 0.0..1440.0
                    HealthMetricType.CALORIES -> value in 0.0..10000.0
                    HealthMetricType.PROTEIN -> value in 0.0..500.0
                    HealthMetricType.CARBS -> value in 0.0..1000.0
                    HealthMetricType.FIBER -> value in 0.0..200.0
                }

                if (!isValid) {
                    _uiState.update { it.copy(error = "Please enter a valid ${type.displayName} value.") }
                    return@launch
                }

                val metric = HealthMetric(
                    type = type,
                    value = value,
                    timestamp = System.currentTimeMillis(),
                    notes = if (notes.isNotEmpty()) notes else null
                )
                healthMetricDao.insertHealthMetric(metric)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        lastSavedMetric = type to value,
                        showConfetti = type == HealthMetricType.BREAKS || type == HealthMetricType.WATER
                    )
                }

                launch {
                    delay(3000)
                    _uiState.update { it.copy(saveSuccess = false, showConfetti = false) }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save ${type.displayName}: ${e.message}"
                    )
                }
            }
        }
    }

    fun logBreakTime(breakType: BreakType = BreakType.SHORT) {
        viewModelScope.launch {
            val metric = HealthMetric(
                type = HealthMetricType.BREAKS,
                value = 1.0,
                timestamp = System.currentTimeMillis(),
                notes = breakType.name
            )
            healthMetricDao.insertHealthMetric(metric)
        }
    }

    fun logWaterIntake(amount: Double) {
        // Find today's existing water intake and add to it
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // Log as a new entry. The processHealthData will sum them up.
            saveHealthMetric(HealthMetricType.WATER, amount, "Water Intake")
        }
    }

    fun logWorkSession(durationMinutes: Double, taskType: String = "Work") {
        saveHealthMetric(HealthMetricType.SCREEN_TIME, durationMinutes, taskType)
        saveHealthMetric(HealthMetricType.FOCUS, durationMinutes * 0.8, taskType) // Est. 80% focus
    }

    fun logPostureCheck(isGoodPosture: Boolean) {
        viewModelScope.launch {
            val metric = HealthMetric(
                type = HealthMetricType.POSTURE,
                value = if (isGoodPosture) 1.0 else 0.0,
                timestamp = System.currentTimeMillis()
            )
            healthMetricDao.insertHealthMetric(metric)
        }
    }

    fun logMeal(calories: Double, protein: Double, carbs: Double, fiber: Double) {
        viewModelScope.launch {
            if (calories > 0) saveHealthMetric(HealthMetricType.CALORIES, calories, "Meal")
            if (protein > 0) saveHealthMetric(HealthMetricType.PROTEIN, protein, "Meal")
            if (carbs > 0) saveHealthMetric(HealthMetricType.CARBS, carbs, "Meal")
            if (fiber > 0) saveHealthMetric(HealthMetricType.FIBER, fiber, "Meal")
        }
    }

    fun updateGoal(type: HealthMetricType, value: Double) {
        viewModelScope.launch {
            _goals.update { current ->
                current.toMutableMap().apply {
                    this[type] = value
                }
            }
            _uiState.update { it.copy(goalUpdated = true) }

            launch {
                delay(2000)
                _uiState.update { it.copy(goalUpdated = false) }
            }
        }
    }

    private fun processHealthData(metrics: List<HealthMetric>): HealthData {
        val groupedMetrics = metrics.groupBy { it.type }
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Sum values for the current day
        val todaysMetrics = metrics.filter { it.timestamp >= startOfDay }
        val todaysGrouped = todaysMetrics.groupBy { it.type }

        val currentValues = HealthMetricType.entries.associateWith { type ->
            val entriesForType = todaysGrouped[type] ?: emptyList()
            if (type == HealthMetricType.WATER || type == HealthMetricType.CALORIES || 
                type == HealthMetricType.PROTEIN || type == HealthMetricType.CARBS || 
                type == HealthMetricType.FIBER || type == HealthMetricType.EXERCISE ||
                type == HealthMetricType.SCREEN_TIME || type == HealthMetricType.FOCUS) {
                entriesForType.sumOf { it.value }
            } else {
                entriesForType.maxByOrNull { it.timestamp }?.value
            }
        }

        val weightProgress = groupedMetrics[HealthMetricType.WEIGHT]
            ?.sortedBy { it.timestamp }
            ?.takeLast(30)
            ?.map {
                val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate()
                date to it.value
            } ?: emptyList()

        val recentEntries = metrics
            .sortedByDescending { it.timestamp }
            .take(10)
            .map { metric ->
                HealthMetricEntry(
                    type = metric.type,
                    value = metric.value,
                    timestamp = metric.timestamp,
                    notes = metric.notes
                )
            }

        val workSessionStats = calculateWorkSessionStats(todaysMetrics)
        val sleepPattern = calculateSleepPattern(groupedMetrics[HealthMetricType.SLEEP])
        val workHoursTrend = calculateWorkHoursTrend(metrics)
        val productivityTrend = calculateProductivityTrend(metrics)

        return HealthData(
            currentValues = currentValues,
            weightProgress = weightProgress,
            recentEntries = recentEntries,
            workSessionStats = workSessionStats,
            sleepPattern = sleepPattern,
            workHoursTrend = workHoursTrend,
            productivityTrend = productivityTrend,
            nutritionData = calculateNutritionData(todaysMetrics),
            lastUpdated = metrics.maxOfOrNull { it.timestamp }
        )
    }

    private fun calculateWorkSessionStats(metrics: List<HealthMetric>): WorkSessionStats {
        val screenTime = metrics.filter { it.type == HealthMetricType.SCREEN_TIME }
            .sumOf { it.value }
        val breaks = metrics.filter { it.type == HealthMetricType.BREAKS }.size
        val focusTime = metrics.filter { it.type == HealthMetricType.FOCUS }.sumOf { it.value }

        return WorkSessionStats(
            totalScreenTime = screenTime,
            totalBreaks = breaks,
            totalFocusTime = focusTime,
            averageFocusSession = if (metrics.any { it.type == HealthMetricType.FOCUS }) focusTime / metrics.filter { it.type == HealthMetricType.FOCUS }.size else 0.0
        )
    }

    private fun calculateSleepPattern(metrics: List<HealthMetric>?): List<Pair<LocalDate, Double>> {
        return metrics?.sortedBy { it.timestamp }?.takeLast(7)?.map {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() to it.value
        } ?: emptyList()
    }

    private fun calculateWorkHoursTrend(metrics: List<HealthMetric>): List<Pair<LocalDate, Double>> {
        return metrics.filter { it.type == HealthMetricType.SCREEN_TIME }
            .groupBy { LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() }
            .map { (date, metrics) -> date to metrics.sumOf { it.value } / 60.0 }
            .sortedBy { it.first }
            .takeLast(7)
    }

    private fun calculateProductivityTrend(metrics: List<HealthMetric>): List<Pair<LocalDate, Double>> {
        return metrics.filter { it.type == HealthMetricType.FOCUS }
            .groupBy { LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate() }
            .map { (date, metrics) -> date to metrics.sumOf { it.value } }
            .sortedBy { it.first }
            .takeLast(7)
    }

    private fun calculateNutritionData(metrics: List<HealthMetric>): NutritionData {
        return NutritionData(
            calories = metrics.filter { it.type == HealthMetricType.CALORIES }.sumOf { it.value },
            protein = metrics.filter { it.type == HealthMetricType.PROTEIN }.sumOf { it.value },
            carbs = metrics.filter { it.type == HealthMetricType.CARBS }.sumOf { it.value },
            fiber = metrics.filter { it.type == HealthMetricType.FIBER }.sumOf { it.value }
        )
    }

    private fun calculateEyeStrainLevel(screenTimeHours: Double): Int {
        return when {
            screenTimeHours < 4 -> 1
            screenTimeHours < 8 -> 2
            else -> 3
        }
    }

    private fun calculateAnalytics(data: HealthData): HealthAnalytics {
        val weight = data.currentValues[HealthMetricType.WEIGHT]
        val height = data.currentValues[HealthMetricType.HEIGHT]
        val bmi = calculateBMI(weight, height)

        val bmiCategory = when {
            bmi == null -> BMICategory.UNKNOWN
            bmi < 18.5 -> BMICategory.UNDERWEIGHT
            bmi < 25 -> BMICategory.NORMAL
            bmi < 30 -> BMICategory.OVERWEIGHT
            else -> BMICategory.OBESE
        }

        val productivityScore = calculateProductivityScore(data.workSessionStats)
        val eyeStrainLevel = calculateEyeStrainLevel((data.currentValues[HealthMetricType.SCREEN_TIME] ?: 0.0) / 60.0)

        return HealthAnalytics(
            bmi = bmi,
            bmiCategory = bmiCategory,
            waterConsistency = calculateConsistency(data.recentEntries, HealthMetricType.WATER),
            sleepConsistency = calculateConsistency(data.recentEntries, HealthMetricType.SLEEP),
            dailyStreak = calculateDailyStreak(data.recentEntries),
            productivityScore = productivityScore,
            eyeStrainLevel = eyeStrainLevel,
            nutritionGoals = NutritionGoals(
                caloriesTarget = 2500.0,
                proteinTarget = 80.0,
                fiberTarget = 30.0
            )
        )
    }

    private fun calculateConsistency(entries: List<HealthMetricEntry>, type: HealthMetricType): Int {
        val typeEntries = entries.filter { it.type == type }
        if (typeEntries.isEmpty()) return 0
        return min(100, (typeEntries.size * 10))
    }

    private fun calculateDailyStreak(entries: List<HealthMetricEntry>): Int {
        val dates = entries.map {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it.timestamp), ZoneId.systemDefault()).toLocalDate()
        }.distinct().sortedDescending()

        var streak = 0
        var currentDate = LocalDate.now()
        for (date in dates) {
            if (date == currentDate || date == currentDate.minusDays(1)) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateBMI(weight: Double?, height: Double?): Double? {
        return if (weight != null && height != null && height > 0) {
            val heightInMeters = height / 100
            weight / (heightInMeters * heightInMeters)
        } else {
            null
        }
    }

    private fun calculateProductivityScore(stats: WorkSessionStats): Int {
        val breakScore = min(100, stats.totalBreaks * 10)
        val focusScore = min(100, (stats.totalFocusTime / 480 * 100).toInt()) // Assume 8h focus is 100%
        return (breakScore * 0.4 + focusScore * 0.6).toInt()
    }

    private fun updateDailyWorkRoutine(data: HealthData) {
        val currentTime = LocalTime.now()
        val stats = data.workSessionStats
        val lastBreakTime = data.recentEntries.find { it.type == HealthMetricType.BREAKS }?.timestamp ?: 0L
        
        val minutesSinceBreak = if (lastBreakTime == 0L) 60 else (System.currentTimeMillis() - lastBreakTime) / (1000 * 60)
        val nextBreakIn = max(0, 50 - minutesSinceBreak.toInt())

        val waterConsumed = data.currentValues[HealthMetricType.WATER] ?: 0.0
        val waterGoal = _goals.value[HealthMetricType.WATER] ?: 3000.0

        _dailyWorkRoutine.update { current ->
            current.copy(
                startTime = LocalTime.of(9, 0),
                currentWorkHours = calculateWorkHoursToday(LocalTime.of(9, 0), currentTime),
                nextBreakInMinutes = nextBreakIn,
                shouldTakeBreak = nextBreakIn <= 5,
                nextBreakType = if (stats.totalBreaks < 2) "Eye Break" else "Physical Break",
                breaksTaken = stats.totalBreaks,
                waterConsumed = waterConsumed,
                screenTimeHours = (data.currentValues[HealthMetricType.SCREEN_TIME] ?: 0.0) / 60.0,
                focusHours = (data.currentValues[HealthMetricType.FOCUS] ?: 0.0) / 60.0,
                productivityScore = calculateProductivityScore(stats),
                completedHealthGoals = calculateCompletedGoals(data),
                lastBreakTime = lastBreakTime,
                lastEyeBreakMinutes = minutesSinceBreak.toInt()
            )
        }
    }

    private fun calculateCompletedGoals(data: HealthData): Int {
        var completed = 0
        val current = data.currentValues
        
        if ((current[HealthMetricType.WATER] ?: 0.0) >= 2000) completed++
        if (data.workSessionStats.totalBreaks >= 5) completed++
        if ((current[HealthMetricType.EXERCISE] ?: 0.0) >= 30) completed++
        if ((current[HealthMetricType.SLEEP] ?: 0.0) >= 7) completed++
        if (data.nutritionData.calories in 1800.0..2800.0) completed++
        
        return completed
    }

    private fun calculateWorkHoursToday(startTime: LocalTime, currentTime: LocalTime): Int {
        return if (currentTime.isAfter(startTime)) {
            val duration = java.time.Duration.between(startTime, currentTime)
            duration.toHours().toInt()
        } else {
            0
        }
    }

    fun showMetricInput(type: HealthMetricType) {
        _uiState.update { it.copy(showInputDialog = !it.showInputDialog, selectedMetric = type) }
    }

    fun showMealInput() {
        _uiState.update { it.copy(showMealDialog = !it.showMealDialog) }
    }

    fun getHealthTips(): List<String> = listOf(
        "💧 Drink water every hour",
        "👀 Follow 20-20-20 rule",
        "🏃‍♂️ Take a walk every 2 hours"
    )
}

data class DailyWorkRoutine(
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(17, 0),
    val currentWorkHours: Int = 0,
    val nextBreakInMinutes: Int = 50,
    val shouldTakeBreak: Boolean = false,
    val nextBreakType: String = "Short Break",
    val breaksTaken: Int = 0,
    val waterConsumed: Double = 0.0,
    val screenTimeHours: Double = 0.0,
    val goodPostureTime: Int = 75,
    val focusHours: Double = 0.0,
    val completedTasks: Int = 0,
    val productivityScore: Int = 0,
    val completedHealthGoals: Int = 0,
    val totalHealthGoals: Int = 5,
    val lastBreakTime: Long = 0L,
    val lastEyeBreakMinutes: Int = 0,
    val screenTimeTrend: Float = 0f,
    val postureTrend: Float = 0f,
    val eyeStrainLevel: Int = 1
)

data class WorkSessionStats(
    val totalScreenTime: Double = 0.0,
    val totalBreaks: Int = 0,
    val totalFocusTime: Double = 0.0,
    val averageFocusSession: Double = 0.0
)

data class NutritionData(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fiber: Double = 0.0
)

data class NutritionGoals(
    val caloriesTarget: Double = 2500.0,
    val proteinTarget: Double = 80.0,
    val fiberTarget: Double = 30.0
)

enum class BreakType { SHORT, LONG, EYE, PHYSICAL }

data class HealthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val goalUpdated: Boolean = false,
    val showConfetti: Boolean = false,
    val showInputDialog: Boolean = false,
    val showMealDialog: Boolean = false,
    val selectedMetric: HealthMetricType? = null,
    val lastSavedMetric: Pair<HealthMetricType, Double>? = null
)

data class HealthData(
    val currentValues: Map<HealthMetricType, Double?> = emptyMap(),
    val weightProgress: List<Pair<LocalDate, Double>> = emptyList(),
    val recentEntries: List<HealthMetricEntry> = emptyList(),
    val waterConsistency: Int = 0,
    val sleepConsistency: Int = 0,
    val workSessionStats: WorkSessionStats = WorkSessionStats(),
    val sleepPattern: List<Pair<LocalDate, Double>> = emptyList(),
    val workHoursTrend: List<Pair<LocalDate, Double>> = emptyList(),
    val productivityTrend: List<Pair<LocalDate, Double>> = emptyList(),
    val nutritionData: NutritionData = NutritionData(),
    val lastUpdated: Long? = null
)

data class HealthMetricEntry(
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long,
    val notes: String? = null
)

data class HealthAnalytics(
    val bmi: Double? = null,
    val bmiCategory: BMICategory = BMICategory.UNKNOWN,
    val waterConsistency: Int = 0,
    val sleepConsistency: Int = 0,
    val dailyStreak: Int = 0,
    val productivityScore: Int = 0,
    val eyeStrainLevel: Int = 1,
    val nutritionGoals: NutritionGoals = NutritionGoals(),
    val recommendations: List<String> = emptyList()
)

enum class BMICategory { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE, UNKNOWN }

val HealthMetricType.displayName: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "Weight"
        HealthMetricType.HEIGHT -> "Height"
        HealthMetricType.WATER -> "Water"
        HealthMetricType.SLEEP -> "Sleep"
        HealthMetricType.BREAKS -> "Breaks"
        HealthMetricType.EXERCISE -> "Exercise"
        HealthMetricType.SCREEN_TIME -> "Screen Time"
        HealthMetricType.POSTURE -> "Posture"
        HealthMetricType.FOCUS -> "Focus"
        HealthMetricType.CALORIES -> "Calories"
        HealthMetricType.PROTEIN -> "Protein"
        HealthMetricType.CARBS -> "Carbs"
        HealthMetricType.FIBER -> "Fiber"
    }

val HealthMetricType.unit: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "kg"
        HealthMetricType.HEIGHT -> "cm"
        HealthMetricType.WATER -> "ml"
        HealthMetricType.SLEEP -> "hrs"
        HealthMetricType.BREAKS -> "breaks"
        HealthMetricType.EXERCISE -> "min"
        HealthMetricType.SCREEN_TIME -> "min"
        HealthMetricType.POSTURE -> ""
        HealthMetricType.FOCUS -> "min"
        HealthMetricType.CALORIES -> "kcal"
        HealthMetricType.PROTEIN -> "g"
        HealthMetricType.CARBS -> "g"
        HealthMetricType.FIBER -> "g"
    }
