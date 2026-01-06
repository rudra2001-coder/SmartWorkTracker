package com.rudra.smartworktracker.ui.screens.overtime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.launch
import java.util.Date

class OvertimeViewModel(application: Application) : AndroidViewModel(application) {

    private val workLogDao = AppDatabase.getDatabase(application).workLogDao()

    fun saveOvertime(date: Date, startTime: String, endTime: String, overtimeRate: Double) {
        viewModelScope.launch {
            val workLog = WorkLog(
                date = date,
                workType = WorkType.OVERTIME,
                startTime = startTime,
                endTime = endTime,
                isOvertime = true,
                overtimeRate = overtimeRate
            )
            workLogDao.insertWorkLog(workLog)
        }
    }
}
