package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val monthlySalary: Double,
    val initialSavings: Double,
    val salaryPeriod: SalaryPeriod = SalaryPeriod.MONTHLY,
    val language: Language = Language.ENGLISH
)

enum class SalaryPeriod {
    MONTHLY,
    WEEKLY,
    BI_WEEKLY
}

enum class Language {
    BENGALI,
    ENGLISH
}
