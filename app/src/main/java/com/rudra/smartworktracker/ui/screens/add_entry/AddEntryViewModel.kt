package com.rudra.smartworktracker.ui.screens.add_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.repository.ExpenseRepository
import com.rudra.smartworktracker.data.repository.WorkLogRepository
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.ui.AddEntryUiState
import com.rudra.smartworktracker.ui.EntryType
import com.rudra.smartworktracker.utils.CurrencyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class AddEntryViewModel(
    private val expenseRepository: ExpenseRepository,
    private val workLogRepository: WorkLogRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState = _uiState.asStateFlow()

    private val workLogId: Long? = savedStateHandle["workLogId"]

    init {
        if (workLogId != null && workLogId != -1L) {
            viewModelScope.launch {
                workLogRepository.getWorkLogById(workLogId).collectLatest { workLog ->
                    workLog?.let {
                        _uiState.update {
                            it.copy(
                                workType = workLog.workType,
                                workStartTime = workLog.startTime ?: "",
                                workEndTime = workLog.endTime ?: ""
                            )
                        }
                    }
                }
            }
        }
    }

    fun onExpenseAmountChange(amount: String) {
        _uiState.update { it.copy(expenseAmount = amount) }
    }

    fun onExpenseCategoryChange(category: ExpenseCategory) {
        _uiState.update { it.copy(expenseCategory = category) }
    }

    fun onExpenseNotesChange(notes: String) {
        _uiState.update { it.copy(expenseNotes = notes) }
    }

    fun saveExpense() {
        viewModelScope.launch {
            val amount = _uiState.value.expenseAmount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid amount") }
                return@launch
            }
            _uiState.update { it.copy(errorMessage = null) }
            val expense = Expense(
                amount = amount,
                currency = CurrencyManager.getCurrencyCode(),
                category = _uiState.value.expenseCategory,
                merchant = null,
                notes = _uiState.value.expenseNotes,
                timestamp = System.currentTimeMillis(),
                imageUri = null
            )
            expenseRepository.insertExpense(expense)
            _uiState.update { it.copy(isEntrySaved = true) }
        }
    }

    fun onWorkTypeChange(workType: com.rudra.smartworktracker.model.WorkType) {
        _uiState.update { it.copy(workType = workType) }
    }

    fun onWorkStartTimeChange(time: String) {
        _uiState.update { it.copy(workStartTime = time) }
    }

    fun onWorkEndTimeChange(time: String) {
        _uiState.update { it.copy(workEndTime = time) }
    }

    fun saveWorkLog() {
        viewModelScope.launch {
            if (_uiState.value.workStartTime.isBlank() || _uiState.value.workEndTime.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please enter start and end times") }
                return@launch
            }
            _uiState.update { it.copy(errorMessage = null) }
            val workLog = WorkLog(
                id = workLogId ?: 0,
                date = Date(),
                workType = _uiState.value.workType,
                startTime = _uiState.value.workStartTime,
                endTime = _uiState.value.workEndTime
            )
            workLogRepository.insertWorkLog(workLog)
            _uiState.update { it.copy(isEntrySaved = true) }
        }
    }

    fun onMealAmountChange(amount: String) {
        _uiState.update { it.copy(mealAmount = amount) }
    }

    fun onMealNotesChange(notes: String) {
        _uiState.update { it.copy(mealNotes = notes) }
    }

    fun saveMeal() {
        viewModelScope.launch {
            val amount = _uiState.value.mealAmount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid meal amount") }
                return@launch
            }
            _uiState.update { it.copy(errorMessage = null) }
            val mealExpense = Expense(
                amount = amount,
                currency = CurrencyManager.getCurrencyCode(),
                category = ExpenseCategory.MEAL,
                merchant = null,
                notes = _uiState.value.mealNotes,
                timestamp = System.currentTimeMillis(),
                imageUri = null
            )
            expenseRepository.insertExpense(mealExpense)
            _uiState.update { it.copy(isEntrySaved = true) }
        }
    }

    fun onEntryTypeChange(entryType: EntryType) {
        _uiState.update { it.copy(selectedEntryType = entryType, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val savedStateHandle = extras.createSavedStateHandle()
                val database = AppDatabase.getDatabase(application)
                val expenseRepository = ExpenseRepository(database.expenseDao())
                val workLogRepository = WorkLogRepository(database.workLogDao())
                return AddEntryViewModel(expenseRepository, workLogRepository, savedStateHandle) as T
            }
        }
    }
}
