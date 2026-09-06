package com.rudra.smartworktracker.ui.screens.breaks

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers

data class BreathingPattern(
    val name: String,
    val description: String,
    val inhaleSec: Int,
    val holdSec: Int,
    val exhaleSec: Int,
    val restSec: Int
)

object BreathingPatterns {
    val BOX = BreathingPattern("Box Breathing", "Inhale 4s / Hold 4s / Exhale 4s / Rest 4s — Navy SEAL technique for calm & focus", 4, 4, 4, 4)
    val RELAX = BreathingPattern("4-7-8 Relax", "Inhale 4s / Hold 7s / Exhale 8s — Dr. Weil's relaxation breath", 4, 7, 8, 2)
    val CALM = BreathingPattern("Calm Flow", "Inhale 4s / Hold 2s / Exhale 4s — Quick centering exercise", 4, 2, 4, 2)
    val ENERGIZE = BreathingPattern("Energizer", "Inhale 4s / Exhale 2s — Energizing breath for morning", 4, 0, 2, 2)

    val all = listOf(BOX, RELAX, CALM, ENERGIZE)
}

enum class SessionState { IDLE, RUNNING, PAUSED, COMPLETED }

data class MindfulBreakUiState(
    val selectedPattern: BreathingPattern = BreathingPatterns.BOX,
    val sessionState: SessionState = SessionState.IDLE,
    val currentInstruction: String = "Tap Start to begin",
    val cycleCount: Int = 0,
    val breathingProgress: Float = 0.3f,
    val elapsedSeconds: Int = 0,
    val sessionMinutes: Int = 0,
    val sessionsToday: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreak: Int = 0,
    val showSummary: Boolean = false
)

class MindfulBreakViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(MindfulBreakUiState())
    val uiState: StateFlow<MindfulBreakUiState> = _uiState.asStateFlow()

    private var breathingJob: Job? = null
    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    private var lastSessionDate = ""

    init {
        loadStats()
    }

    private fun loadStats() {
        val prefs = context.getSharedPreferences("mindful_break", Context.MODE_PRIVATE)
        val today = java.time.LocalDate.now().toString()
        val date = prefs.getString("last_session_date", "") ?: ""
        val totalMin = prefs.getInt("total_minutes", 0)
        val streak = prefs.getInt("current_streak", 0)

        val sessionsToday = if (date == today) prefs.getInt("sessions_today", 0) else 0
        lastSessionDate = date

        _uiState.value = _uiState.value.copy(
            sessionsToday = sessionsToday,
            totalMinutes = totalMin,
            currentStreak = streak
        )
    }

    fun selectPattern(pattern: BreathingPattern) {
        if (_uiState.value.sessionState == SessionState.IDLE) {
            _uiState.value = _uiState.value.copy(selectedPattern = pattern)
        }
    }

    fun startSession() {
        if (_uiState.value.sessionState != SessionState.IDLE) return
        elapsedSeconds = 0
        _uiState.value = _uiState.value.copy(
            sessionState = SessionState.RUNNING,
            cycleCount = 0,
            currentInstruction = "Breathe In",
            elapsedSeconds = 0,
            showSummary = false
        )
        startTimer()
        startBreathingCycle()
    }

    fun pauseSession() {
        if (_uiState.value.sessionState != SessionState.RUNNING) return
        _uiState.value = _uiState.value.copy(sessionState = SessionState.PAUSED, currentInstruction = "Paused")
        breathingJob?.cancel()
        timerJob?.cancel()
    }

    fun resumeSession() {
        if (_uiState.value.sessionState != SessionState.PAUSED) return
        _uiState.value = _uiState.value.copy(sessionState = SessionState.RUNNING)
        startTimer()
        startBreathingCycle()
    }

    fun stopSession() {
        breathingJob?.cancel()
        timerJob?.cancel()
        val minutes = elapsedSeconds / 60
        val state = _uiState.value
        _uiState.value = state.copy(
            sessionState = SessionState.COMPLETED,
            sessionMinutes = minutes,
            currentInstruction = "Great job!",
            showSummary = true
        )
        saveSession(minutes)
    }

    fun dismissSummary() {
        _uiState.value = _uiState.value.copy(showSummary = false, sessionState = SessionState.IDLE, breathingProgress = 0.3f)
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            try {
                while (true) {
                    delay(1000)
                    elapsedSeconds++
                    _uiState.value = _uiState.value.copy(elapsedSeconds = elapsedSeconds)
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Timer cancelled
            }
        }
    }

    private fun startBreathingCycle() {
        val pattern = _uiState.value.selectedPattern
        breathingJob = viewModelScope.launch {
            try {
                while (true) {
                    _uiState.value = _uiState.value.copy(currentInstruction = "Breathe In")
                    animateBreathing(0.3f, 1f, pattern.inhaleSec * 1000L)

                    if (pattern.holdSec > 0) {
                        _uiState.value = _uiState.value.copy(currentInstruction = "Hold")
                        delay(pattern.holdSec * 1000L)
                    }

                    _uiState.value = _uiState.value.copy(currentInstruction = "Breathe Out")
                    animateBreathing(1f, 0.3f, pattern.exhaleSec * 1000L)

                    if (pattern.restSec > 0) {
                        _uiState.value = _uiState.value.copy(currentInstruction = "Rest")
                        delay(pattern.restSec * 1000L)
                    }

                    val newCycle = _uiState.value.cycleCount + 1
                    _uiState.value = _uiState.value.copy(cycleCount = newCycle)
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Job cancelled normally
            }
        }
    }

    private suspend fun animateBreathing(from: Float, to: Float, durationMs: Long) {
        val steps = (durationMs / 50).toInt().coerceAtLeast(1)
        for (i in 0 until steps) {
            val fraction = i.toFloat() / steps
            val eased = fraction * fraction * (3 - 2 * fraction)
            _uiState.value = _uiState.value.copy(
                breathingProgress = from + (to - from) * eased
            )
            delay(50)
        }
        _uiState.value = _uiState.value.copy(breathingProgress = to)
    }

    private fun saveSession(minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("mindful_break", Context.MODE_PRIVATE)
            val today = java.time.LocalDate.now().toString()
            val lastDate = prefs.getString("last_session_date", "") ?: ""

            val sessionsToday = if (lastDate == today) prefs.getInt("sessions_today", 0) + 1 else 1
            val totalMin = prefs.getInt("total_minutes", 0) + minutes

            // Streak calculation
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val currentStreak = if (lastDate == yesterday || lastDate == today) {
                prefs.getInt("current_streak", 0) + 1
            } else if (lastDate != today) {
                1
            } else {
                prefs.getInt("current_streak", 0)
            }

            prefs.edit().apply {
                putString("last_session_date", today)
                putInt("sessions_today", sessionsToday)
                putInt("total_minutes", totalMin)
                putInt("current_streak", currentStreak)
                apply()
            }

            _uiState.value = _uiState.value.copy(
                sessionsToday = sessionsToday,
                totalMinutes = totalMin,
                currentStreak = currentStreak
            )
        }
    }
}
