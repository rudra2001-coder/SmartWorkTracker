package com.rudra.smartworktracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.rudra.smartworktracker.data.dao.WorkLogDao
import com.rudra.smartworktracker.ui.screens.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val workLogDao: WorkLogDao
) {

    companion object {
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val VIBRATION = booleanPreferencesKey("vibration")
    }

    fun getSettings(): Flow<SettingsUiState> = dataStore.data.map {
        SettingsUiState(
            notificationsEnabled = it[NOTIFICATIONS] ?: true,
            darkThemeEnabled = it[DARK_THEME] ?: false,
            vibrationEnabled = it[VIBRATION] ?: true
        )
    }

    suspend fun saveSettings(
        notificationsEnabled: Boolean,
        darkThemeEnabled: Boolean,
        vibrationEnabled: Boolean
    ) {
        dataStore.edit {
            it[NOTIFICATIONS] = notificationsEnabled
            it[DARK_THEME] = darkThemeEnabled
            it[VIBRATION] = vibrationEnabled
        }
    }

    suspend fun backupData() {
        // Implement backup logic (e.g., export to file or cloud)
    }

    suspend fun restoreData() {
        // Implement restore logic
    }

    suspend fun clearAllWorkLogs() {
        workLogDao.clearAll()
    }
}
