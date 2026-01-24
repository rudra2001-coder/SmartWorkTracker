package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emis",
    foreignKeys = [ForeignKey(
        entity = Loan::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["loanId"])]
)
data class Emi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val loanId: Int,
    val amount: Double,

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,

    val dueDateOfMonth: Int, // Day of the month (e.g., 5)
    var nextDueDate: Long, // Timestamp of the next payment
    var isActive: Boolean = true,

    // Audit fields - Rule 1.2
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
