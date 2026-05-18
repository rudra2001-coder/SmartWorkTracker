package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val uuid: String? = null,
    
    val personName: String,
    val contactNumber: String? = null,
    val initialAmount: Double,
    var remainingAmount: Double,
    val loanType: LoanType,
    val loanCategory: LoanCategory = LoanCategory.PERSONAL,
    val date: Long,
    val dueDate: Long? = null,
    val interestRate: Double? = null,
    val emiAmount: Double? = null,
    val totalEmis: Int? = null,
    val paidEmis: Int = 0,
    val notes: String? = null,
    
    val sourceAccount: AccountType = AccountType.BANK,
    val destinationAccount: AccountType = AccountType.CASH,
    
    val isActive: Boolean = true,
    val isFullyPaid: Boolean = false,

    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity {
    
    val progress: Float
        get() = if (initialAmount > 0) {
            ((initialAmount - remainingAmount) / initialAmount).toFloat()
        } else 0f
    
    val remainingEmis: Int
        get() = if (totalEmis != null) totalEmis - paidEmis else 0
    
    val isOverdue: Boolean
        get() = dueDate?.let { it < System.currentTimeMillis() && !isFullyPaid } ?: false
}

enum class LoanCategory {
    PERSONAL,
    HOME,
    CAR,
    EDUCATION,
    BUSINESS,
    MEDICAL,
    OTHER
}
