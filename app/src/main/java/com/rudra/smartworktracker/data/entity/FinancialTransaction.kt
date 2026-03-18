package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val type: TransactionType,
    val amount: Double,
    val source: AccountType,
    val destination: AccountType?,
    val note: String,
    val category: String? = null,
    val date: Long,
    val relatedLoanId: Int? = null,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
