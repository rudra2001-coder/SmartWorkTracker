package com.rudra.smartworktracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val mealRateKey = doublePreferencesKey("meal_rate")
    private val darkThemeKey = booleanPreferencesKey(DARK_THEME)
    private val notificationsKey = booleanPreferencesKey(NOTIFICATIONS)
    private val vibrationKey = booleanPreferencesKey(VIBRATION)

    val mealRate: Flow<Double> = context.dataStore.data.map {
        it[mealRateKey] ?: 60.0
    }

    suspend fun setMealRate(rate: Double) {
        context.dataStore.edit {
            it[mealRateKey] = rate
        }
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map {
        it[darkThemeKey] ?: false
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit {
            it[darkThemeKey] = isDark
        }
    }

    val notifications: Flow<Boolean> = context.dataStore.data.map {
        it[notificationsKey] ?: true
    }

    suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit {
            it[notificationsKey] = enabled
        }
    }

    val vibration: Flow<Boolean> = context.dataStore.data.map {
        it[vibrationKey] ?: true
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit {
            it[vibrationKey] = enabled
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit {
            it.clear()
        }
    }

    companion object {
        const val NOTIFICATIONS = "notifications"
        const val DARK_THEME = "dark_theme"
        const val VIBRATION = "vibration"
    }
}
