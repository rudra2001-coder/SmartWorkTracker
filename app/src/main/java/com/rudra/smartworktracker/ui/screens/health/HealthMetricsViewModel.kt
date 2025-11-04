package com.rudra.smartworktracker.ui.screens.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import kotlinx.coroutines.launch

class HealthMetricsViewModel(application: Application) : AndroidViewModel(application) {

    private val healthMetricDao = AppDatabase.getDatabase(application).healthMetricDao()

    fun saveHealthMetric(type: HealthMetricType, value: Double) {
        viewModelScope.launch {
            val metric = HealthMetric(
                type = type,
                value = value,
                timestamp = System.currentTimeMillis()
            )
            healthMetricDao.insertHealthMetric(metric)
        }
    }
}
