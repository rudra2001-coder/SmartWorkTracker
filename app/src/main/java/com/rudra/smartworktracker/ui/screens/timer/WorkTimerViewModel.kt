package com.rudra.smartworktracker.ui.screens.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.BreakPeriod
import com.rudra.smartworktracker.model.SessionType
import com.rudra.smartworktracker.model.WorkSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TimerMode { RUNNING, PAUSED, STOPPED, ON_BREAK }

data class TimerState(
    val mode: TimerMode = TimerMode.STOPPED,
    val totalSeconds: Int = 0,
    val currentSessionId: String? = null,
    val currentBreakStartTime: Long? = null
)

class WorkTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val workSessionDao = AppDatabase.getDatabase(application).workSessionDao()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun startWorkSession() {
        val newSessionId = UUID.randomUUID().toString()
        val workSession = WorkSession(
            id = newSessionId,
            startTime = System.currentTimeMillis(),
            endTime = null,
            type = SessionType.WORK,
            breaks = emptyList(),
            productivityScore = null
        )
        viewModelScope.launch {
            workSessionDao.insertWorkSession(workSession)
            _timerState.value = TimerState(mode = TimerMode.RUNNING, currentSessionId = newSessionId)
        }
    }

    fun stopWorkSession() {
        // Logic to update endTime for the work session will be added here.
        _timerState.value = TimerState(mode = TimerMode.STOPPED)
    }

    fun startBreak() {
        _timerState.value = _timerState.value.copy(
            mode = TimerMode.ON_BREAK,
            currentBreakStartTime = System.currentTimeMillis()
        )
    }

    fun endBreak() {
        val state = _timerState.value
        val breakStartTime = state.currentBreakStartTime
        if (state.currentSessionId != null && breakStartTime != null) {
            val breakPeriod = BreakPeriod(startTime = breakStartTime, endTime = System.currentTimeMillis())
            // Logic to add the break to the current work session will be added here.
            _timerState.value = state.copy(mode = TimerMode.RUNNING, currentBreakStartTime = null)
        }
    }
}
