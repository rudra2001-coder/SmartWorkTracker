package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EmiStatus {
    UPCOMING,
    DUE,
    OVERDUE,
    PAID,
    SKIPPED
}

@Entity(
    tableName = "emis",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class Emi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uuid: String? = null,
    val loanId: Int,
    
    val amount: Double,
    val principalAmount: Double,
    val interestAmount: Double = 0.0,
    val penaltyAmount: Double = 0.0,
    
    val dueDateOfMonth: Int,
    var nextDueDate: Long,
    var lastPaymentDate: Long? = null,
    
    var isActive: Boolean = true,
    var isPaid: Boolean = false,
    var isSkipped: Boolean = false,
    
    val notes: String? = null,
    val paymentAccount: AccountType = AccountType.BANK,

    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity {
    
    val status: EmiStatus
        get() = when {
            isPaid -> EmiStatus.PAID
            isSkipped -> EmiStatus.SKIPPED
            nextDueDate < System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000 -> EmiStatus.OVERDUE
            nextDueDate < System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000 -> EmiStatus.DUE
            else -> EmiStatus.UPCOMING
        }
    
    val totalPayable: Double
        get() = amount + penaltyAmount
}
