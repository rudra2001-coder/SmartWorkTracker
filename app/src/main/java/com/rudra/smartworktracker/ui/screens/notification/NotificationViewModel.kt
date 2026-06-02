package com.rudra.smartworktracker.ui.screens.notification

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.InAppNotification
import com.rudra.smartworktracker.data.entity.NotificationType
import com.rudra.smartworktracker.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
        loadCounts()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications().collect { notifications ->
                _uiState.value = _uiState.value.copy(notifications = notifications)
            }
        }
    }

    private fun loadCounts() {
        viewModelScope.launch {
            repository.getUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
        viewModelScope.launch {
            repository.getTotalCount().collect { count ->
                _uiState.value = _uiState.value.copy(totalCount = count)
            }
        }
    }

    fun setFilter(type: NotificationType?) {
        _uiState.value = _uiState.value.copy(selectedFilter = type)
        viewModelScope.launch {
            val notifications = if (type != null) {
                repository.getNotificationsByType(type).first()
            } else {
                repository.getAllNotifications().first()
            }
            _uiState.value = _uiState.value.copy(notifications = notifications)
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun deleteRead() {
        viewModelScope.launch {
            repository.deleteRead()
        }
    }

    data class NotificationUiState(
        val notifications: List<InAppNotification> = emptyList(),
        val unreadCount: Int = 0,
        val totalCount: Int = 0,
        val selectedFilter: NotificationType? = null,
        val isLoading: Boolean = false
    )
}

class NotificationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val repository = NotificationRepository(database.inAppNotificationDao())
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
