package com.rudra.smartworktracker.ui.screens.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.FocusType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val focusSessionDao = AppDatabase.getDatabase(application).focusSessionDao()

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun startFocusSession(type: FocusType, duration: Long) {
        _timerState.value = TimerState.Running(type, duration, 0)
        viewModelScope.launch {
            // Timer logic will go here
        }
    }

    fun stopFocusSession() {
        if (_timerState.value is TimerState.Running) {
            val runningState = _timerState.value as TimerState.Running
            viewModelScope.launch {
                val focusSession = FocusSession(
                    id = UUID.randomUUID().toString(),
                    type = runningState.type,
                    duration = runningState.duration,
                    interruptions = 0, // Not implemented yet
                    focusScore = 0, // Not implemented yet
                    timestamp = System.currentTimeMillis()
                )
                focusSessionDao.insertFocusSession(focusSession)
                _timerState.value = TimerState.Idle
            }
        }
    }
}

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val type: FocusType, val duration: Long, val elapsed: Long) : TimerState()
}
