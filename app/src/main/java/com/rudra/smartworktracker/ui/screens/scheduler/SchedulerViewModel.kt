package com.rudra.smartworktracker.ui.screens.scheduler

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.alarm.AlarmScheduler
import com.rudra.smartworktracker.data.repository.ScheduleRepository
import com.rudra.smartworktracker.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalTime

data class SchedulerUiState(
    val schedules: List<Schedule> = emptyList(),
    val newScheduleTitle: String = "",
    val newScheduleHour: Int = 0,
    val newScheduleMinute: Int = 0,
    val newScheduleIsRepeating: Boolean = false,
    val selectedDays: List<Int> = emptyList(),
    val history: List<ScheduleHistory> = emptyList()
)

class SchedulerViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulerUiState())
    val uiState: StateFlow<SchedulerUiState> = _uiState.asStateFlow()

    private var alarmScheduler: AlarmScheduler? = null

    init {
        context?.let {
            alarmScheduler = AlarmScheduler(it)
        }

        scheduleRepository.getAllSchedules()
            .onEach { schedules ->
                _uiState.value = _uiState.value.copy(schedules = schedules)

                // Reschedule all alarms when schedules are loaded
                alarmScheduler?.rescheduleAll(schedules.filter { it.isEnabled })
            }
            .launchIn(viewModelScope)
    }

    fun addSchedule() {
        viewModelScope.launch {
            val time = LocalTime.of(
                _uiState.value.newScheduleHour,
                _uiState.value.newScheduleMinute
            )

            val newSchedule = Schedule(
                title = _uiState.value.newScheduleTitle,
                time = time,
                isRepeating = _uiState.value.newScheduleIsRepeating,
                repeatingDays = if (_uiState.value.newScheduleIsRepeating) {
                    _uiState.value.selectedDays.toSet()
                } else {
                    emptySet()
                }
            )

            val id: Unit = scheduleRepository.insertSchedule(newSchedule)
            val finalSchedule = newSchedule.copy()
            alarmScheduler?.schedule(finalSchedule)

            // Reset form
            _uiState.value = _uiState.value.copy(
                newScheduleTitle = "",
                newScheduleHour = 0,
                newScheduleMinute = 0,
                newScheduleIsRepeating = false,
                selectedDays = emptyList()
            )
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            // Cancel alarm first
            alarmScheduler?.cancel(schedule)

            scheduleRepository.deleteSchedule(schedule)
        }
    }

    fun toggleSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val updatedSchedule = schedule.copy(isEnabled = !schedule.isEnabled)
            scheduleRepository.updateSchedule(updatedSchedule)

            // Schedule or cancel alarm based on enabled state
            if (updatedSchedule.isEnabled) {
                alarmScheduler?.schedule(updatedSchedule)
            } else {
                alarmScheduler?.cancel(updatedSchedule)
            }
        }
    }

    fun onDaySelected(day: Int) {
        val currentDays = _uiState.value.selectedDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(selectedDays = currentDays)
    }

    // Add method to test alarm immediately
    fun testAlarmNow(schedule: Schedule) {
        viewModelScope.launch {
            // Create a test schedule with current time + 10 seconds
            val testTime = LocalTime.now().plusSeconds(10)
            val testSchedule = schedule.copy(time = testTime, isRepeating = false)

            alarmScheduler?.schedule(testSchedule)
        }
    }
    fun clearHistory() {
        // Dummy function, not implemented
    }

    fun deleteHistory(history: ScheduleHistory) {
        // Dummy function, not implemented
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(newScheduleTitle = title)
    }

    fun onTimeChange(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(newScheduleHour = hour, newScheduleMinute = minute)
    }

    fun onIsRepeatingChange(isRepeating: Boolean) {
        _uiState.value = _uiState.value.copy(newScheduleIsRepeating = isRepeating)
    }
}
