package com.rudra.smartworktracker.model

import java.util.Date

data class Target(
    val id: String = "",
    val goalId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val completedAt: Date? = null,
    val order: Int
)