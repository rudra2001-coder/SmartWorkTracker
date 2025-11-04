package com.rudra.smartworktracker.data

import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import java.util.Calendar
import java.util.Date

object SampleData {
    fun getSampleWorkLogs(): List<WorkLog> {
        val calendar = Calendar.getInstance()
        return listOf(
            WorkLog(date = calendar.time, workType = WorkType.OFFICE, startTime = "09:00", endTime = "17:00"),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -1) }.time, workType = WorkType.HOME_OFFICE, startTime = "09:00", endTime = "17:00"),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -2) }.time, workType = WorkType.OFF_DAY, startTime = null, endTime = null),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -3) }.time, workType = WorkType.EXTRA_WORK, startTime = "18:00", endTime = "20:00"),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -4) }.time, workType = WorkType.OFFICE, startTime = "09:00", endTime = "17:00"),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -5) }.time, workType = WorkType.OFFICE, startTime = "09:00", endTime = "17:00"),
            WorkLog(date = calendar.apply { add(Calendar.DATE, -6) }.time, workType = WorkType.HOME_OFFICE, startTime = "09:00", endTime = "17:00"),
        )
    }
}
