package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val uuid: String? = null,

    val type: TransactionType,
    val amount: Double,
    val sourceAccountId: Long = 0,
    val destinationAccountId: Long? = null,
    val note: String,
    val category: String? = null,
    val date: Long,
    val relatedLoanId: Int? = null,
    val relatedCreditCardId: Int? = null,

    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
