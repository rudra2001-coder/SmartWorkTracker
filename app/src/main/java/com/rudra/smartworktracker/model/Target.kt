package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "life_plan_targets",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class Target(
    @PrimaryKey val id: String,
    val goalId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val order: Int
)