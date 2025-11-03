package com.rudra.smartworktracker.data

import com.rudra.smartworktracker.model.Gender
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import java.util.Calendar
import java.util.Date

object SampleData {
    fun getSampleWorkLogs(): List<WorkLog> {
        val calendar = Calendar.getInstance()
        return listOf(
            WorkLog(calendar.time, WorkType.OFFICE, Gender.MALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -1) }.time, WorkType.HOME, Gender.FEMALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -2) }.time, WorkType.OFF, Gender.MALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -3) }.time, WorkType.EXTRA, Gender.FEMALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -4) }.time, WorkType.OFFICE, Gender.MALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -5) }.time, WorkType.OFFICE, Gender.MALE),
            WorkLog(calendar.apply { add(Calendar.DATE, -6) }.time, WorkType.HOME, Gender.FEMALE),
        )
    }
}
