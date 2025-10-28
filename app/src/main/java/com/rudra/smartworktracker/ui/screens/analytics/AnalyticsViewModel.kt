package com.rudra.smartworktracker.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: WorkLogRepository
) : ViewModel() {
    
    private val _analyticsData = MutableStateFlow(AnalyticsData())
    
    val uiState: StateFlow<AnalyticsUiState> = _analyticsData.map { analyticsData ->
        AnalyticsUiState(analyticsData = analyticsData)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
    
    init {
        loadAnalyticsForMonth(YearMonth.now())
    }
    
    fun loadAnalyticsForMonth(month: YearMonth) {
        viewModelScope.launch {
            // Simulate analytics calculation
            // In real app, you'd calculate this from actual work logs
            val analyticsData = AnalyticsData(
                totalWorkDays = 22,
                officeDays = 12,
                homeOfficeDays = 8,
                offDays = 6,
                extraWorkDays = 4,
                weeklyTrend = listOf(5f, 4f, 3f, 5f),
                consistencyScore = 85,
                averageHours = 8.5,
                productivityScore = 78
            )
            
            _analyticsData.value = analyticsData
        }
    }
}

data class AnalyticsUiState(
    val analyticsData: AnalyticsData = AnalyticsData()
)

data class AnalyticsData(
    val totalWorkDays: Int = 0,
    val officeDays: Int = 0,
    val homeOfficeDays: Int = 0,
    val offDays: Int = 0,
    val extraWorkDays: Int = 0,
    val weeklyTrend: List<Float> = emptyList(),
    val consistencyScore: Int = 0,
    val averageHours: Double = 0.0,
    val productivityScore: Int = 0
)