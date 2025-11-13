package com.rudra.smartworktracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val mealRateKey = doublePreferencesKey("meal_rate")

    val mealRate: Flow<Double> = context.dataStore.data.map {
        it[mealRateKey] ?: 65.0
    }

    suspend fun setMealRate(rate: Double) {
        context.dataStore.edit {
            it[mealRateKey] = rate
        }
    }

    companion object {
        const val NOTIFICATIONS = "notifications"
        const val DARK_THEME = "dark_theme"
        const val VIBRATION = "vibration"
    }
}
