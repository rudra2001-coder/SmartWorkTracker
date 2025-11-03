package com.rudra.smartworktracker.model

import java.util.Date

data class WorkLog(
    val date: Date,
    val workType: WorkType,
    val gender: Gender
)

enum class WorkType {
    OFFICE,
    HOME,
    OFF,
    EXTRA
}

enum class Gender {
    MALE,
    FEMALE
}
