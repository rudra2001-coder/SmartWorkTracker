package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.model.Wisdom
import com.rudra.smartworktracker.model.WisdomCategory

class WisdomRepository {

    fun getWisdom(): List<Wisdom> {
        return allWisdom
    }

    private val allWisdom = listOf(
        // Productivity
        Wisdom("The key is not to prioritize what's on your schedule, but to schedule your priorities.", "Stephen Covey", WisdomCategory.PRODUCTIVITY),
        Wisdom("Work on your most important task for 90 minutes, without interruption. You'll be amazed at what you can accomplish.", "The 90/90/1 Rule", WisdomCategory.PRODUCTIVITY),
        Wisdom("Don't wait for the perfect moment. Take the moment and make it perfect.", null, WisdomCategory.PRODUCTIVITY),

        // Mindfulness
        Wisdom("The present moment is filled with joy and happiness. If you are attentive, you will see it.", "Thich Nhat Hanh", WisdomCategory.MINDFULNESS),
        Wisdom("Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.", "Thich Nhat Hanh", WisdomCategory.MINDFULNESS),
        Wisdom("You can't stop the waves, but you can learn to surf.", "Jon Kabat-Zinn", WisdomCategory.MINDFULNESS),

        // Habits
        Wisdom("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear", WisdomCategory.HABITS),
        Wisdom("Every action you take is a vote for the type of person you wish to become.", "James Clear", WisdomCategory.HABITS),
        Wisdom("The secret to getting ahead is getting started.", "Mark Twain", WisdomCategory.HABITS)
    )
}
