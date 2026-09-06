package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardName: String,

    val uuid: String? = null,

    val cardNumber: String,
    val cardLimit: Double,
    var currentBalance: Double = 0.0,
    val statementDate: Int,
    val dueDate: Int,

    val accountId: Long = 0,

    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
