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
import java.time.ZoneId

class HealthMetricsViewModel(application: Application) : AndroidViewModel(application) {

    private val healthMetricDao = AppDatabase.getDatabase(application).healthMetricDao()

    // UI State
    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

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

    // Goals
    private val _weightGoal = MutableStateFlow(70.0) // Default goal

    fun saveHealthMetric(type: HealthMetricType, value: Double) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Validate input
                val isValid = when (type) {
                    HealthMetricType.WEIGHT -> value in 20.0..300.0
                    HealthMetricType.HEIGHT -> value in 50.0..250.0
                    HealthMetricType.WATER -> value in 0.0..10000.0
                    HealthMetricType.SLEEP -> value in 0.0..24.0
                }

                if (!isValid) {
                    _uiState.update { it.copy(error = "Please enter a valid value.") }
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
                        lastSavedMetric = type to value
                    )
                }

                // Reset success state after 3 seconds
                launch {
                    kotlinx.coroutines.delay(3000)
                    _uiState.update { it.copy(saveSuccess = false) }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save ${type.name}: ${e.message}"
                    )
                }
            }
        }
    }

    private fun processHealthData(metrics: List<HealthMetric>): HealthData {
        val weightMetrics = metrics.filter { it.type == HealthMetricType.WEIGHT }
        val heightMetrics = metrics.filter { it.type == HealthMetricType.HEIGHT }

        val currentWeight = weightMetrics.maxByOrNull { it.timestamp }?.value
        val currentHeight = heightMetrics.maxByOrNull { it.timestamp }?.value
        val currentBMI = calculateBMI(currentWeight, currentHeight)

        val weightProgress = weightMetrics
            .sortedBy { it.timestamp }
            .map {
                val date = LocalDate.ofEpochDay(it.timestamp / (1000 * 60 * 60 * 24))
                date to it.value
            }
            .takeLast(30) // Last 30 entries

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

        return HealthData(
            currentWeight = currentWeight,
            currentHeight = currentHeight,
            currentBMI = currentBMI,
            weightGoal = _weightGoal.value,
            weightProgress = weightProgress,
            recentEntries = recentEntries
        )
    }

    private fun calculateBMI(weight: Double?, height: Double?): Double? {
        return if (weight != null && height != null && height > 0) {
            // Convert height from cm to meters and calculate BMI
            val heightInMeters = height / 100
            weight / (heightInMeters * heightInMeters)
        } else {
            null
        }
    }
}

// Data Classes
data class HealthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val lastSavedMetric: Pair<HealthMetricType, Double>? = null
)

data class HealthData(
    val currentWeight: Double? = null,
    val currentHeight: Double? = null,
    val currentBMI: Double? = null,
    val weightGoal: Double? = null,
    val weightProgress: List<Pair<LocalDate, Double>> = emptyList(),
    val recentEntries: List<HealthMetricEntry> = emptyList()
)

data class HealthMetricEntry(
    val type: HealthMetricType,
    val value: Double,
    val timestamp: Long
)

data class HealthInsights(
    val currentWeight: Double? = null,
    val weightTrend: Double = 0.0,
    val sleepQuality: Int = 0,
    val hydrationScore: Int = 0,
    val consistencyScore: Int = 0,
    val recommendations: List<String> = emptyList()
)
