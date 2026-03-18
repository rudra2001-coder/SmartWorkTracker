package com.rudra.smartworktracker.ui.screens.realitytracker

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.RealityTrackerRepository
import com.rudra.smartworktracker.model.RealityCategory
import com.rudra.smartworktracker.model.RealityEntry
import com.rudra.smartworktracker.model.RealityEntryType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RealityTrackerViewModel(private val repository: RealityTrackerRepository) : ViewModel() {

    val entries: StateFlow<List<RealityEntry>> = repository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTimeRange = MutableStateFlow(TimeRange.WEEK)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    val stats: StateFlow<RealityStats> = _selectedTimeRange.flatMapLatest { range ->
        val (start, end) = when (range) {
            TimeRange.TODAY -> repository.getTodayStats()
            TimeRange.WEEK -> repository.getWeekStats()
            TimeRange.MONTH -> repository.getMonthStats()
        }
        combine(
            repository.getTotalPlannedCount(start, end),
            repository.getCompletedCount(start, end),
            repository.getPlannedCountByType(RealityEntryType.GOAL.name, start, end),
            repository.getCompletedCountByType(RealityEntryType.GOAL.name, start, end),
            repository.getPlannedCountByType(RealityEntryType.PROMISE.name, start, end),
            repository.getCompletedCountByType(RealityEntryType.PROMISE.name, start, end),
            repository.getPlannedCountByType(RealityEntryType.PLAN.name, start, end),
            repository.getCompletedCountByType(RealityEntryType.PLAN.name, start, end)
        ) { values ->
            val totalPlanned = values[0] as Int
            val totalCompleted = values[1] as Int
            val goalsPlanned = values[2] as Int
            val goalsCompleted = values[3] as Int
            val promisesPlanned = values[4] as Int
            val promisesCompleted = values[5] as Int
            val plansPlanned = values[6] as Int
            val plansCompleted = values[7] as Int

            RealityStats(
                totalPlanned = totalPlanned,
                totalCompleted = totalCompleted,
                goalsPlanned = goalsPlanned,
                goalsCompleted = goalsCompleted,
                promisesPlanned = promisesPlanned,
                promisesCompleted = promisesCompleted,
                plansPlanned = plansPlanned,
                plansCompleted = plansCompleted
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RealityStats())

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun addEntry(type: RealityEntryType, title: String, description: String, category: RealityCategory) {
        viewModelScope.launch {
            val entry = RealityEntry(
                type = type,
                title = title,
                description = description,
                category = category
            )
            repository.addEntry(entry)
        }
    }

    fun toggleCompletion(entry: RealityEntry) {
        viewModelScope.launch {
            repository.markAsCompleted(entry.id, !entry.isCompleted)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }
}

data class RealityStats(
    val totalPlanned: Int = 0,
    val totalCompleted: Int = 0,
    val goalsPlanned: Int = 0,
    val goalsCompleted: Int = 0,
    val promisesPlanned: Int = 0,
    val promisesCompleted: Int = 0,
    val plansPlanned: Int = 0,
    val plansCompleted: Int = 0
) {
    val completionRate: Float
        get() = if (totalPlanned > 0) (totalCompleted.toFloat() / totalPlanned) * 100 else 0f

    val overestimationPercentage: Float
        get() = if (totalPlanned > 0) ((totalPlanned - totalCompleted).toFloat() / totalPlanned) * 100 else 0f

    fun getStatsByType(type: RealityEntryType): Pair<Int, Int> = when (type) {
        RealityEntryType.GOAL -> Pair(goalsPlanned, goalsCompleted)
        RealityEntryType.PROMISE -> Pair(promisesPlanned, promisesCompleted)
        RealityEntryType.PLAN -> Pair(plansPlanned, plansCompleted)
    }
}

enum class TimeRange {
    TODAY, WEEK, MONTH
}

class RealityTrackerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealityTrackerViewModel::class.java)) {
            val realityTrackerDao = AppDatabase.getDatabase(application).realityTrackerDao()
            val repository = RealityTrackerRepository(realityTrackerDao)
            @Suppress("UNCHECKED_CAST")
            return RealityTrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
