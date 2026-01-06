package com.rudra.smartworktracker.ui.screens.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.ScheduleRepository
import com.rudra.smartworktracker.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Add these data classes at the top level of the file
data class ScheduleHistory(
    val id: String = System.currentTimeMillis().toString() + (0..1000).random(),
    val scheduleId: Long?,
    val type: HistoryType,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: String? = null,
    val scheduleTitle: String? = null,
    val scheduleTime: String? = null
) {
    fun formattedTime(): String {
        val now = LocalDateTime.now()
        val daysAgo = java.time.Duration.between(timestamp, now).toDays()
        return when {
            daysAgo == 0L -> "Today"
            daysAgo == 1L -> "Yesterday"
            daysAgo < 7L -> "$daysAgo days ago"
            else -> timestamp.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }
    }

    fun formattedTimestamp(): String {
        return timestamp.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
    }
}

enum class HistoryType {
    CREATED, UPDATED, TRIGGERED, DELETED
}

data class SchedulerUiState(
    val schedules: List<Schedule> = emptyList(),
    val history: List<ScheduleHistory> = emptyList(),
    val newScheduleTitle: String = "",
    val newScheduleHour: Int = 0,
    val newScheduleMinute: Int = 0,
    val newScheduleIsRepeating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SchedulerViewModel(private val scheduleRepository: ScheduleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulerUiState())
    val uiState: StateFlow<SchedulerUiState> = _uiState.asStateFlow()

    private val _history = mutableListOf<ScheduleHistory>()

    init {
        loadSchedules()
        loadInitialHistory()
    }

    private fun loadSchedules() {
        scheduleRepository.getAllSchedules()
            .onEach { schedules ->
                _uiState.value = _uiState.value.copy(
                    schedules = schedules,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialHistory() {
        // In a real app, you would load from database
        // For now, we'll create some sample history
        val sampleHistory = listOf(
            ScheduleHistory(
                scheduleId = null,
                type = HistoryType.CREATED,
                details = "Welcome to Schedule Manager!",
                scheduleTitle = "Getting Started",
                scheduleTime = "Now"
            )
        )
        _history.addAll(sampleHistory)
        _uiState.value = _uiState.value.copy(history = _history.toList())
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(newScheduleTitle = title)
    }

    fun onTimeChange(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            newScheduleHour = hour,
            newScheduleMinute = minute
        )
    }

    fun onIsRepeatingChange(isRepeating: Boolean) {
        _uiState.value = _uiState.value.copy(newScheduleIsRepeating = isRepeating)
    }

    fun addSchedule() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val newSchedule = Schedule(
                    title = _uiState.value.newScheduleTitle,
                    hour = _uiState.value.newScheduleHour,
                    minute = _uiState.value.newScheduleMinute,
                    isRepeating = _uiState.value.newScheduleIsRepeating,
                    createdAt = LocalDateTime.now()
                )

                scheduleRepository.insertSchedule(newSchedule)

                // Add to history
                addHistory(
                    ScheduleHistory(
                        scheduleId = newSchedule.id,
                        type = HistoryType.CREATED,
                        details = "New schedule created",
                        scheduleTitle = newSchedule.title,
                        scheduleTime = "${newSchedule.hour.toString().padStart(2, '0')}:${newSchedule.minute.toString().padStart(2, '0')}"
                    )
                )

                // Reset input fields
                _uiState.value = _uiState.value.copy(
                    newScheduleTitle = "",
                    newScheduleHour = 0,
                    newScheduleMinute = 0,
                    newScheduleIsRepeating = false,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to add schedule: ${e.message}"
                )
            }
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                // First add to history before deleting
                addHistory(
                    ScheduleHistory(
                        scheduleId = schedule.id,
                        type = HistoryType.DELETED,
                        details = "Schedule deleted permanently",
                        scheduleTitle = schedule.title,
                        scheduleTime = "${schedule.hour.toString().padStart(2, '0')}:${schedule.minute.toString().padStart(2, '0')}"
                    )
                )

                scheduleRepository.deleteSchedule(schedule)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete schedule: ${e.message}"
                )
            }
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                val updatedSchedule = schedule.copy(isEnabled = !schedule.isEnabled)
                scheduleRepository.updateSchedule(updatedSchedule)

                // Add to history
                addHistory(
                    ScheduleHistory(
                        scheduleId = schedule.id,
                        type = HistoryType.UPDATED,
                        details = if (updatedSchedule.isEnabled)
                            "Schedule enabled"
                        else
                            "Schedule disabled",
                        scheduleTitle = schedule.title,
                        scheduleTime = "${schedule.hour.toString().padStart(2, '0')}:${schedule.minute.toString().padStart(2, '0')}"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update schedule: ${e.message}"
                )
            }
        }
    }

    fun deleteHistory(history: ScheduleHistory) {
        viewModelScope.launch {
            _history.remove(history)
            _uiState.value = _uiState.value.copy(history = _history.toList())
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _history.clear()
            _uiState.value = _uiState.value.copy(history = emptyList())

            // Add a history entry for clearing
            addHistory(
                ScheduleHistory(
                    scheduleId = null,
                    type = HistoryType.UPDATED,
                    details = "All history cleared",
                    scheduleTitle = "System",
                    scheduleTime = "Now"
                )
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun triggerSchedule(schedule: Schedule) {
        viewModelScope.launch {
            addHistory(
                ScheduleHistory(
                    scheduleId = schedule.id,
                    type = HistoryType.TRIGGERED,
                    details = "Schedule triggered manually",
                    scheduleTitle = schedule.title,
                    scheduleTime = "${schedule.hour.toString().padStart(2, '0')}:${schedule.minute.toString().padStart(2, '0')}"
                )
            )
        }
    }

    private fun addHistory(history: ScheduleHistory) {
        // Keep only last 50 history items to prevent memory issues
        if (_history.size >= 50) {
            _history.removeAt(_history.size - 1)
        }
        _history.add(0, history) // Add to beginning for chronological order
        _uiState.value = _uiState.value.copy(history = _history.toList())
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadSchedules()
    }
}

