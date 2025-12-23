package com.rudra.smartworktracker.ui.screens.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

class HealthMetricsViewModel(application: Application) : AndroidViewModel(application) {

    private val healthMetricDao = AppDatabase.getDatabase(application).healthMetricDao()

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
            HealthMetricType.WATER to 2500.0, // ml
            HealthMetricType.SLEEP to 8.0 // hours
        )
    )
    val goals: StateFlow<Map<HealthMetricType, Double>> = _goals.asStateFlow()

    // Health Data Stream with enhanced processing
    val healthData: StateFlow<HealthData> = healthMetricDao.getAllHealthMetrics()
        .map { metrics ->
            processHealthData(metrics)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            HealthData()
        )

    init {
        // Initialize analytics
        viewModelScope.launch {
            healthData.collect { data ->
                _healthAnalytics.value = calculateAnalytics(data)
            }
        }
    }

    fun saveHealthMetric(type: HealthMetricType, value: Double) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Enhanced validation
                val isValid = when (type) {
                    HealthMetricType.WEIGHT -> value in 20.0..300.0
                    HealthMetricType.HEIGHT -> value in 50.0..250.0
                    HealthMetricType.WATER -> value in 0.0..10000.0
                    HealthMetricType.SLEEP -> value in 0.0..24.0
                }

                if (!isValid) {
                    _uiState.update { it.copy(error = "Please enter a valid ${type.displayName} value.") }
                    return@launch
                }

                val metric = HealthMetric(
                    type = type,
                    value = value,
                    timestamp = System.currentTimeMillis()
                )
                healthMetricDao.insertHealthMetric(metric)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        lastSavedMetric = type to value,
                        showConfetti = true
                    )
                }

                // Reset success state
                launch {
                    kotlinx.coroutines.delay(3000)
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

    fun updateGoal(type: HealthMetricType, value: Double) {
        viewModelScope.launch {
            _goals.update { current ->
                current.toMutableMap().apply {
                    this[type] = value
                }
            }
            _uiState.update { it.copy(goalUpdated = true) }

            launch {
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(goalUpdated = false) }
            }
        }
    }

    private fun processHealthData(metrics: List<HealthMetric>): HealthData {
        val groupedMetrics = metrics.groupBy { it.type }

        val currentValues = HealthMetricType.entries.associateWith { type ->
            groupedMetrics[type]?.maxByOrNull { it.timestamp }?.value
        }

        val weightProgress = groupedMetrics[HealthMetricType.WEIGHT]
            ?.sortedBy { it.timestamp }
            ?.takeLast(30)
            ?.map {
                val date = LocalDate.ofEpochDay(it.timestamp / (1000 * 60 * 60 * 24))
                date to it.value
            } ?: emptyList()

        val recentEntries = metrics
            .sortedByDescending { it.timestamp }
            .take(10)
            .map { metric ->
                HealthMetricEntry(
                    type = metric.type,
                    value = metric.value,
                    timestamp = metric.timestamp
                )
            }

        // Calculate streaks and consistency
        val waterConsistency = calculateConsistency(groupedMetrics[HealthMetricType.WATER])
        val sleepConsistency = calculateConsistency(groupedMetrics[HealthMetricType.SLEEP])

        return HealthData(
            currentValues = currentValues,
            weightProgress = weightProgress,
            recentEntries = recentEntries,
            waterConsistency = waterConsistency,
            sleepConsistency = sleepConsistency,
            lastUpdated = metrics.maxOfOrNull { it.timestamp }
        )
    }

    private fun calculateConsistency(metrics: List<HealthMetric>?): Int {
        if (metrics.isNullOrEmpty()) return 0
        val recentMetrics = metrics.takeLast(7)
        return if (recentMetrics.size >= 5) 100 else (recentMetrics.size * 100 / 7)
    }

    private fun calculateBMI(weight: Double?, height: Double?): Double? {
        return if (weight != null && height != null && height > 0) {
            val heightInMeters = height / 100
            weight / (heightInMeters * heightInMeters)
        } else {
            null
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

        val weightTrend = if (data.weightProgress.size >= 2) {
            val first = data.weightProgress.first().second
            val last = data.weightProgress.last().second
            ((last - first) / first * 100).toFloat()
        } else 0f

        return HealthAnalytics(
            bmi = bmi,
            bmiCategory = bmiCategory,
            weightTrend = weightTrend,
            waterConsistency = data.waterConsistency,
            sleepConsistency = data.sleepConsistency,
            dailyStreak = calculateDailyStreak(data.recentEntries)
        )
    }

    private fun calculateDailyStreak(entries: List<HealthMetricEntry>): Int {
        val dates = entries.map {
            LocalDate.ofEpochDay(it.timestamp / (1000 * 60 * 60 * 24))
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
}

// Enhanced Data Classes
data class HealthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val goalUpdated: Boolean = false,
    val showConfetti: Boolean = false,
    val lastSavedMetric: Pair<HealthMetricType, Double>? = null
)

data class HealthData(
    val currentValues: Map<HealthMetricType, Double?> = emptyMap(),
    val weightProgress: List<Pair<LocalDate, Double>> = emptyList(),
    val recentEntries: List<HealthMetricEntry> = emptyList(),
    val waterConsistency: Int = 0,
    val sleepConsistency: Int = 0,
    val lastUpdated: Long? = null
)

data class HealthMetricEntry(
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long
)

data class HealthAnalytics(
    val bmi: Double? = null,
    val bmiCategory: BMICategory = BMICategory.UNKNOWN,
    val weightTrend: Float = 0f,
    val waterConsistency: Int = 0,
    val sleepConsistency: Int = 0,
    val dailyStreak: Int = 0,
    val recommendations: List<String> = emptyList()
)

enum class BMICategory {
    UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE, UNKNOWN
}

// Extension properties for HealthMetricType
val HealthMetricType.displayName: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "Weight"
        HealthMetricType.HEIGHT -> "Height"
        HealthMetricType.WATER -> "Water"
        HealthMetricType.SLEEP -> "Sleep"
    }

val HealthMetricType.unit: String
    get() = when (this) {
        HealthMetricType.WEIGHT -> "kg"
        HealthMetricType.HEIGHT -> "cm"
        HealthMetricType.WATER -> "ml"
        HealthMetricType.SLEEP -> "hrs"
    }