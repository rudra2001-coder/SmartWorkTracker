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
    val language: Language = Language.ENGLISH,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,
    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class SalaryPeriod {
    MONTHLY,
    WEEKLY,
    BI_WEEKLY
}

enum class Language {
    BENGALI,
    ENGLISH
}
