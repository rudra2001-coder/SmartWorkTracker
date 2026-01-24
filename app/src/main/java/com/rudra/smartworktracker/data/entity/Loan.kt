package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val personName: String,
    val initialAmount: Double,
    var remainingAmount: Double,
    val loanType: LoanType, // BORROWED or LENT
    val date: Long,
    val interestRate: Double? = null,
    val notes: String? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
