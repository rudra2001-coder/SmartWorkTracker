package com.rudra.smartworktracker.model

enum class WisdomCategory {
    PRODUCTIVITY,
    MINDFULNESS,
    HABITS
}

data class Wisdom(
    val text: String,
    val author: String? = null,
    val category: WisdomCategory
)
