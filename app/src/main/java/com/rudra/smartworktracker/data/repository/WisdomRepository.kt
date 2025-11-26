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
        Wisdom("The secret to getting ahead is getting started.", "Mark Twain", WisdomCategory.HABITS),

        // Nutrition & Health (NEW)
        Wisdom("Let food be thy medicine and medicine be thy food.", "Hippocrates", WisdomCategory.MINDFULNESS),
        Wisdom("You are what you eat, so don’t be fast, cheap, easy, or fake.", "Unknown", WisdomCategory.HABITS),
        Wisdom("Your diet is a bank account. Good food choices are good investments.", "Bethenny Frankel", WisdomCategory.HABITS),
        Wisdom("Eat food, not too much, mostly plants.", "Michael Pollan", WisdomCategory.HABITS),
        Wisdom("A healthy outside starts from the inside.", "Robert Urich", WisdomCategory.MINDFULNESS),
        Wisdom("Every bite you take is either fighting disease or feeding it.", "Heather Morgan", WisdomCategory.MINDFULNESS),
        Wisdom("The food you eat can be either the safest and most powerful form of medicine, or the slowest form of poison.", "Ann Wigmore", WisdomCategory.MINDFULNESS),

        // Fitness & Energy (NEW)
        Wisdom("The body achieves what the mind believes.", "Napoleon Hill", WisdomCategory.PRODUCTIVITY),
        Wisdom("If it doesn’t challenge you, it won’t change you.", "Fred DeVito", WisdomCategory.HABITS),
        Wisdom("Exercise is a celebration of what your body can do, not a punishment for what you ate.", "Women's Health Movement", WisdomCategory.MINDFULNESS),
        Wisdom("Movement is a medicine for creating change in a person’s physical, emotional, and mental states.", "Carol Welch", WisdomCategory.MINDFULNESS),

        // Science-backed Well-being (NEW)
        Wisdom("What we think determines how we feel. What we feel determines how we act.", "Albert Ellis", WisdomCategory.MINDFULNESS),
        Wisdom("Small daily improvements are the key to staggering long-term results.", "Robin Sharma", WisdomCategory.HABITS),
        Wisdom("The greatest wealth is health.", "Virgil", WisdomCategory.MINDFULNESS)
    )

}
