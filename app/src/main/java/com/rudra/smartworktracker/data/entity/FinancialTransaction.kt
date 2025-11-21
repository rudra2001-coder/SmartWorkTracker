package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: TransactionType,
    val amount: Double,
    val source: AccountType,
    val destination: AccountType?,
    val note: String,
    val date: Long,
    val relatedLoanId: Int? = null
)
