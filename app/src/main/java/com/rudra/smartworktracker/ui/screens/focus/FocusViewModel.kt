package com.rudra.smartworktracker.ui.screens.focus

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.R
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.FocusSession
import com.rudra.smartworktracker.model.FocusType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID




class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val focusSessionDao = AppDatabase.getDatabase(application).focusSessionDao()
    private var timerJob: Job? = null
    private var interruptionsCount = 0
    private var startTime = 0L

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    fun startFocusSession(type: FocusType, duration: Long) {
        startTime = System.currentTimeMillis()
        interruptionsCount = 0
        _timerState.value = TimerState.Running(type, duration, 0)
        _isPaused.value = false

        startTimer(duration)
        sendStartNotification(type, duration)
    }

    fun pauseResumeTimer() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            timerJob?.cancel()
            sendPauseNotification()
        } else {
            val currentState = _timerState.value
            if (currentState is TimerState.Running) {
                startTimer(currentState.duration, currentState.elapsed)
                sendResumeNotification()
            }
        }
    }

    fun recordInterruption() {
        interruptionsCount++
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            _timerState.value = currentState.copy(elapsed = currentState.elapsed + 30) // Add 30 seconds penalty
        }
    }

    fun stopFocusSession() {
        timerJob?.cancel()
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            saveSession(currentState)
        }
        _timerState.value = TimerState.Idle
        _isPaused.value = false
        cancelNotification()
    }

    private fun startTimer(totalDuration: Long, initialElapsed: Long = 0) {
        timerJob = viewModelScope.launch {
            var elapsed = initialElapsed
            while (elapsed < totalDuration && !_isPaused.value) {
                delay(1000)
                elapsed++
                val currentState = _timerState.value
                if (currentState is TimerState.Running) {
                    _timerState.value = currentState.copy(elapsed = elapsed)

                    // Send periodic updates
                    if (elapsed % 300 == 0L) { // Every 5 minutes
                        sendProgressNotification(currentState.type, elapsed, totalDuration)
                    }

                    // Check for completion
                    if (elapsed >= totalDuration) {
                        onTimerComplete(currentState)
                        break
                    }
                }
            }
        }
    }

    private fun onTimerComplete(state: TimerState.Running) {
        viewModelScope.launch {
            // Calculate focus score based on interruptions and time spent
            val focusScore = calculateFocusScore(state.duration, interruptionsCount)

            val focusSession = FocusSession(
                id = UUID.randomUUID().toString(),
                type = state.type,
                duration = state.duration,
                elapsedTime = state.elapsed,
                interruptions = interruptionsCount,
                focusScore = focusScore,
                timestamp = System.currentTimeMillis()
            )

            focusSessionDao.insertFocusSession(focusSession)

            sendCompletionNotification(state.type)
            _timerState.value = TimerState.Completed(state.type, focusScore)
        }
    }

    private fun saveSession(state: TimerState.Running) {
        viewModelScope.launch {
            val focusScore = calculateFocusScore(state.elapsed, interruptionsCount)

            val focusSession = FocusSession(
                id = UUID.randomUUID().toString(),
                type = state.type,
                duration = state.duration,
                elapsedTime = state.elapsed,
                interruptions = interruptionsCount,
                focusScore = focusScore,
                timestamp = startTime
            )

            focusSessionDao.insertFocusSession(focusSession)
        }
    }

    private fun calculateFocusScore(elapsedTime: Long, interruptions: Int): Int {
        val baseScore = (elapsedTime.toDouble() / 3600 * 100).toInt() // Score based on hours
        val interruptionPenalty = interruptions * 5 // 5 points penalty per interruption
        return maxOf(0, baseScore - interruptionPenalty)
    }

    private fun sendStartNotification(type: FocusType, duration: Long) {
        val notificationManager = getApplication<Application>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(getApplication(), "focus_channel")
            .setContentTitle("Focus Session Started")
            .setContentText("${type.displayName} - ${duration / 60} minutes")
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun sendCompletionNotification(type: FocusType) {
        val notificationManager = getApplication<Application>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(getApplication(), "focus_channel")
            .setContentTitle("Focus Session Completed!")
            .setContentText("Great job completing your ${type.displayName} session!")
            .setSmallIcon(R.drawable.onbord)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(2, notification)
    }

    private fun sendPauseNotification() {
        // Implementation for pause notification
    }

    private fun sendResumeNotification() {
        // Implementation for resume notification
    }

    private fun sendProgressNotification(type: FocusType, elapsed: Long, total: Long) {
        // Implementation for progress notification
    }

    private fun cancelNotification() {
        val notificationManager = getApplication<Application>()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }
}

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val type: FocusType, val duration: Long, val elapsed: Long) : TimerState()
    data class Completed(val type: FocusType, val score: Int) : TimerState()
}