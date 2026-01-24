package com.rudra.smartworktracker.model

enum class WisdomCategory {
    PRODUCTIVITY,
    MINDFULNESS,
    HABITS
}

data class Wisdom(

    // UUID field for future primary key transition - Rule 1.1
    val uuid: String? = null,
    val text: String,
    val author: String? = null,
    val category: WisdomCategory
)
