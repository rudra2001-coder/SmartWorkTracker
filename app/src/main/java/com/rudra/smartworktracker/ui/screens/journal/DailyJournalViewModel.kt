package com.rudra.smartworktracker.ui.screens.journal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.DailyJournal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyJournalViewModel(application: Application) : AndroidViewModel(application) {

    private val journalDao = AppDatabase.getDatabase(application).dailyJournalDao()

    val todayJournal: StateFlow<DailyJournal?> = journalDao.getJournalForDate(LocalDate.now())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveJournal(journal: DailyJournal) {
        viewModelScope.launch {
            journalDao.upsertJournal(journal)
        }
    }
}
