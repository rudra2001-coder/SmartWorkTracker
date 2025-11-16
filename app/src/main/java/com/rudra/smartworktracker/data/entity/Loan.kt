package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personName: String,
    val initialAmount: Double,
    var remainingAmount: Double,
    val loanType: LoanType, // BORROWED or LENT
    val date: Long,
    val interestRate: Double? = null,
    val notes: String? = null
)
