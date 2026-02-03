package com.rudra.smartworktracker.ui.screens.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.BreakPeriod
import com.rudra.smartworktracker.model.SessionType
import com.rudra.smartworktracker.model.WorkSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TimerMode { RUNNING, PAUSED, STOPPED, ON_BREAK, ON_LUNCH }

data class TimerState(
    val mode: TimerMode = TimerMode.STOPPED,
    val totalSeconds: Int = 0,
    val workSeconds: Int = 0,
    val breakSeconds: Int = 0,
    val currentSessionId: String? = null,
    val currentBreakStartTime: Long? = null,
    val todayStats: TodayStats = TodayStats(),
    val sessionHistory: List<SessionItem> = emptyList()
)

data class TodayStats(
    val totalWorkTime: Int = 0,
    val totalBreaks: Int = 0,
    val productivityScore: Float = 0f,
    val sessionsCompleted: Int = 0
)

data class SessionItem(
    val id: String,
    val type: SessionType,
    val startTime: String,
    val duration: String,
    val productivityScore: Int? = null
)

class WorkTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val workSessionDao = AppDatabase.getDatabase(application).workSessionDao()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Load history or initial stats here if needed
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerState.value = _timerState.value.let { state ->
                    when (state.mode) {
                        TimerMode.RUNNING -> state.copy(
                            totalSeconds = state.totalSeconds + 1,
                            workSeconds = state.workSeconds + 1
                        )
                        TimerMode.ON_BREAK, TimerMode.ON_LUNCH -> state.copy(
                            totalSeconds = state.totalSeconds + 1,
                            breakSeconds = state.breakSeconds + 1
                        )
                        else -> state
                    }
                }
            }
        }
    }

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
            _timerState.value = _timerState.value.copy(
                mode = TimerMode.RUNNING,
                currentSessionId = newSessionId,
                workSeconds = 0,
                breakSeconds = 0,
                totalSeconds = 0
            )
            startTimer()
        }
    }

    fun pauseWorkSession() {
        _timerState.value = _timerState.value.copy(mode = TimerMode.PAUSED)
    }

    fun resumeWorkSession() {
        _timerState.value = _timerState.value.copy(mode = TimerMode.RUNNING)
    }

    fun stopWorkSession() {
        timerJob?.cancel()
        // Logic to update endTime for the work session will be added here.
        _timerState.value = _timerState.value.copy(mode = TimerMode.STOPPED)
    }

    fun startBreak() {
        _timerState.value = _timerState.value.copy(
            mode = TimerMode.ON_BREAK,
            currentBreakStartTime = System.currentTimeMillis()
        )
    }

    fun startLunch() {
        _timerState.value = _timerState.value.copy(
            mode = TimerMode.ON_LUNCH,
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
