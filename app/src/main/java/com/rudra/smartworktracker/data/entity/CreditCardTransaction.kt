package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_card_transactions",
    foreignKeys = [ForeignKey(
        entity = CreditCard::class,
        parentColumns = ["id"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["cardId"])]
)
data class CreditCardTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,
    
    val cardId: Int,
    val amount: Double,
    val description: String,
    val date: Long,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
