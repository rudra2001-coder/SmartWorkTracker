package com.rudra.smartworktracker.ui.screens.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.DailyJournal
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyJournalViewModel(application: Application) : AndroidViewModel(application) {

    private val journalDao = AppDatabase.getDatabase(application).dailyJournalDao()

    // Current date state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState

    // Current journal entry for the selected date
    val todayJournal: StateFlow<DailyJournal?> = _selectedDate
        .flatMapLatest { date ->
            journalDao.getJournalForDate(date)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    // Journal entries for the current month
    val monthlyJournals: StateFlow<List<DailyJournal>> = _selectedDate
        .flatMapLatest { date ->
            val firstDayOfMonth = date.withDayOfMonth(1)
            val lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth())
            journalDao.getJournalsBetweenDates(firstDayOfMonth, lastDayOfMonth)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Statistics
    val journalStats: StateFlow<JournalStats> = monthlyJournals
        .map { journals ->
            calculateJournalStats(journals)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            JournalStats()
        )

    init {
        viewModelScope.launch {
            // Load any unsaved changes if app was killed
            loadDraftIfExists()
        }
    }

    fun saveJournal(journal: DailyJournal) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Validate journal entry
                if (journal.morningIntention.isBlank() &&
                    journal.eveningReflection.isBlank() &&
                    journal.gratitude.isBlank()) {
                    _uiState.update { it.copy(error = "Journal entry cannot be completely empty") }
                    return@launch
                }

                journalDao.upsertJournal(journal)
                clearDraft()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        lastSavedDate = LocalDate.now()
                    )
                }

                // Reset success state after 2 seconds
                launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(saveSuccess = false) }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save journal: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(error = null, saveSuccess = false) }
    }

    fun navigateToPreviousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun navigateToNextDay() {
        _selectedDate.update { it.plusDays(1) }
    }

    fun navigateToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun saveDraft(journal: DailyJournal) {
        viewModelScope.launch {
            try {
                // In a real app, you'd save to SharedPreferences or a draft table
                // For now, we'll just update the state
                _uiState.update { it.copy(hasUnsavedChanges = true) }
            } catch (e: Exception) {
                // Silent fail for drafts
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private suspend fun loadDraftIfExists() {
        // Implementation for loading drafts from persistent storage
        // This could use SharedPreferences, Room, or DataStore
        // For now, it's a placeholder
    }

    private suspend fun clearDraft() {
        // Clear any saved drafts for the current date
        _uiState.update { it.copy(hasUnsavedChanges = false) }
    }

    private fun calculateJournalStats(journals: List<DailyJournal>): JournalStats {
        val totalEntries = journals.size
        val completedEntries = journals.count { journal ->
            journal.morningIntention.isNotBlank() ||
                    journal.eveningReflection.isNotBlank() ||
                    journal.gratitude.isNotBlank()
        }

        val completionRate = if (totalEntries > 0) {
            (completedEntries.toDouble() / totalEntries.toDouble() * 100).toInt()
        } else 0

        val averageIntentionLength = journals
            .map { it.morningIntention.length }
            .average()
            .toInt()

        val averageReflectionLength = journals
            .map { it.eveningReflection.length }
            .average()
            .toInt()

        val averageGratitudeLength = journals
            .map { it.gratitude.length }
            .average()
            .toInt()

        val mostFrequentWords = journals
            .flatMap { journal ->
                listOf(
                    journal.morningIntention,
                    journal.eveningReflection,
                    journal.gratitude
                )
            }
            .joinToString(" ")
            .split("\\s+".toRegex())
            .filter { it.length > 3 }
            .groupingBy { it.lowercase() }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        return JournalStats(
            totalEntries = totalEntries,
            completionRate = completionRate,
            streakDays = calculateStreak(journals),
            averageIntentionLength = averageIntentionLength,
            averageReflectionLength = averageReflectionLength,
            averageGratitudeLength = averageGratitudeLength,
            mostFrequentWords = mostFrequentWords
        )
    }

    private fun calculateStreak(journals: List<DailyJournal>): Int {
        val journalDates = journals
            .filter { journal ->
                journal.morningIntention.isNotBlank() ||
                        journal.eveningReflection.isNotBlank() ||
                        journal.gratitude.isNotBlank()
            }
            .map { it.date }
            .toSet()
            .sortedDescending()

        var streak = 0
        var currentDate = LocalDate.now()

        while (journalDates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    fun exportJournalData(): Flow<String> = flow {
        val allJournals = journalDao.getAllJournals()
        val csvData = buildString {
            appendLine("Date,Morning Intention,Evening Reflection,Gratitude")
            allJournals.forEach { journal ->
                appendLine(
                    "\"${journal.date}\"," +
                            "\"${journal.morningIntention.replace("\"", "\"\"")}\"," +
                            "\"${journal.eveningReflection.replace("\"", "\"\"")}\"," +
                            "\"${journal.gratitude.replace("\"", "\"\"")}\""
                )
            }
        }
        emit(csvData)
    }

    fun searchJournals(query: String): Flow<List<DailyJournal>> {
        return if (query.isBlank()) {
            monthlyJournals
        } else {
            journalDao.searchJournals("%$query%")
        }
    }
}

data class JournalUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val lastSavedDate: LocalDate? = null
)

data class JournalStats(
    val totalEntries: Int = 0,
    val completionRate: Int = 0,
    val streakDays: Int = 0,
    val averageIntentionLength: Int = 0,
    val averageReflectionLength: Int = 0,
    val averageGratitudeLength: Int = 0,
    val mostFrequentWords: List<String> = emptyList()
)

// Extension functions for better date handling
fun LocalDate.toDisplayFormat(): String {
    return format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
}

fun LocalDate.isToday(): Boolean {
    return this == LocalDate.now()
}

fun LocalDate.isInPast(): Boolean {
    return this.isBefore(LocalDate.now())
}

fun LocalDate.isInFuture(): Boolean {
    return this.isAfter(LocalDate.now())
}
