package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "emis",
    foreignKeys = [ForeignKey(
        entity = Loan::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Emi(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val loanId: Int,
    val amount: Double,
    val dueDateOfMonth: Int, // Day of the month (e.g., 5)
    var nextDueDate: Long, // Timestamp of the next payment
    var isActive: Boolean = true
)
