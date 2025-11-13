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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

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
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                amount = _uiState.value.expenseAmount.toDoubleOrNull() ?: 0.0,
                currency = "BDT",
                category = _uiState.value.expenseCategory,
                merchant = null,
                notes = _uiState.value.expenseNotes,
                timestamp = System.currentTimeMillis()
            )
            expenseRepository.insertExpense(expense)
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
            val workLog = WorkLog(
                id = workLogId ?: 0,
                date = Date(),
                workType = _uiState.value.workType,
                startTime = _uiState.value.workStartTime,
                endTime = _uiState.value.workEndTime
            )
            workLogRepository.insertWorkLog(workLog)
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
            val mealExpense = Expense(
                id = UUID.randomUUID().toString(),
                amount = _uiState.value.mealAmount.toDoubleOrNull() ?: 0.0,
                currency = "BDT",
                category = ExpenseCategory.MEAL,
                merchant = null,
                notes = _uiState.value.mealNotes,
                timestamp = System.currentTimeMillis()
            )
            expenseRepository.insertExpense(mealExpense)
        }
    }

    fun onEntryTypeChange(entryType: EntryType) {
        _uiState.update { it.copy(selectedEntryType = entryType) }
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
