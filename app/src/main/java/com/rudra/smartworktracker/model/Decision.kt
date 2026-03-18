package com.rudra.smartworktracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "decisions")
data class Decision(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val decisionType: DecisionType,
    val category: DecisionCategory = decisionType.category,
    val customTitle: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPositive: Boolean = decisionType.defaultImpact > 0
)

enum class DecisionType(
    val displayName: String,
    val defaultImpact: Float,
    val shortTerm7Day: String,
    val longTerm30Day: String,
    val category: DecisionCategory,
    val recoveryAction: String,
    val recoveryAction2: String,
    val immediateConsequence: String,
    val estimatedWeightChange: Float, // kg per week
    val energyImpact: Int // percentage
) {
    // Health decisions
    SKIP_GYM(
        displayName = "Skip Gym",
        defaultImpact = -5f,
        shortTerm7Day = "Muscle loss, low energy, poor mood",
        longTerm30Day = "Weight gain, decreased fitness, health risks",
        category = DecisionCategory.HEALTH,
        recoveryAction = "Do 10 pushups + 10 squats now",
        recoveryAction2 = "Take a 15-min walk",
        immediateConsequence = "Energy -12% | Focus -8% | Mood ↓",
        estimatedWeightChange = 0.4f,
        energyImpact = -12
    ),
    GO_TO_GYM(
        displayName = "Go to Gym",
        defaultImpact = 8f,
        shortTerm7Day = "Better mood, increased energy, better sleep",
        longTerm30Day = "Improved fitness, weight management, confidence",
        category = DecisionCategory.HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Energy +15% | Dopamine ↑ | Confidence +",
        estimatedWeightChange = -0.5f,
        energyImpact = 15
    ),
    EAT_JUNK_FOOD(
        displayName = "Eat Junk Food",
        defaultImpact = -4f,
        shortTerm7Day = "Low energy, acne, temporary satisfaction",
        longTerm30Day = "Weight gain, health issues, poor digestion",
        category = DecisionCategory.HEALTH,
        recoveryAction = "Drink 2 glasses of water now",
        recoveryAction2 = "Add protein to next meal",
        immediateConsequence = "Energy -10% | Sugar crash in 2hrs",
        estimatedWeightChange = 0.3f,
        energyImpact = -10
    ),
    EAT_HEALTHY(
        displayName = "Eat Healthy",
        defaultImpact = 6f,
        shortTerm7Day = "More energy, better mood, clear skin",
        longTerm30Day = "Better health, weight control, longevity",
        category = DecisionCategory.HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Energy +8% | Stable blood sugar",
        estimatedWeightChange = -0.3f,
        energyImpact = 8
    ),
    SLEEP_LATE(
        displayName = "Sleep Late",
        defaultImpact = -6f,
        shortTerm7Day = "Fatigue, poor focus, irritability",
        longTerm30Day = "Chronic fatigue, memory issues, health decline",
        category = DecisionCategory.HEALTH,
        recoveryAction = "Take a 20-min power nap today",
        recoveryAction2 = "Go to bed 30 mins earlier tomorrow",
        immediateConsequence = "Focus -20% | Reaction time slower",
        estimatedWeightChange = 0.2f,
        energyImpact = -20
    ),
    SLEEP_EARLY(
        displayName = "Sleep Early",
        defaultImpact = 7f,
        shortTerm7Day = "Better focus, stable mood, more energy",
        longTerm30Day = "Improved memory, better health, productivity",
        category = DecisionCategory.HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Focus +15% | Memory consolidation ↑",
        estimatedWeightChange = -0.2f,
        energyImpact = 15
    ),
    NO_EXERCISE(
        displayName = "Skip Exercise",
        defaultImpact = -4f,
        shortTerm7Day = "Low stamina, weight gain tendency",
        longTerm30Day = "Declining fitness, health problems",
        category = DecisionCategory.HEALTH,
        recoveryAction = "Do 50 jumping jacks now",
        recoveryAction2 = "Take the stairs for rest of day",
        immediateConsequence = "Metabolism slows | Circulation ↓",
        estimatedWeightChange = 0.3f,
        energyImpact = -8
    ),
    EXERCISE(
        displayName = "Exercise",
        defaultImpact = 7f,
        shortTerm7Day = "Endorphins, better sleep, more energy",
        longTerm30Day = "Fitness, mental health, longevity",
        category = DecisionCategory.HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Endorphins ↑ | Stress -30%",
        estimatedWeightChange = -0.4f,
        energyImpact = 12
    ),

    // Finance decisions
    SPEND_MONEY(
        displayName = "Unnecessary Purchase",
        defaultImpact = -5f,
        shortTerm7Day = "Reduced savings, instant gratification",
        longTerm30Day = "Financial stress, missed investments",
        category = DecisionCategory.FINANCE,
        recoveryAction = "Transfer equivalent to savings",
        recoveryAction2 = "Skip one non-essential purchase this week",
        immediateConsequence = "Savings account - amount | Satisfaction fades",
        estimatedWeightChange = 0f,
        energyImpact = 0
    ),
    SAVE_MONEY(
        displayName = "Save Money",
        defaultImpact = 6f,
        shortTerm7Day = "Peace of mind, financial security",
        longTerm30Day = "Emergency fund, investment potential",
        category = DecisionCategory.FINANCE,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Peace of mind + | Security ↑",
        estimatedWeightChange = 0f,
        energyImpact = 5
    ),
    INVEST(
        displayName = "Invest Money",
        defaultImpact = 8f,
        shortTerm7Day = "Start of wealth building",
        longTerm30Day = "Compound growth, financial independence",
        category = DecisionCategory.FINANCE,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Future wealth + | Control ↑",
        estimatedWeightChange = 0f,
        energyImpact = 5
    ),
    DEBT(
        displayName = "Add Debt",
        defaultImpact = -7f,
        shortTerm7Day = "Financial burden, stress",
        longTerm30Day = "Debt accumulation, limited options",
        category = DecisionCategory.FINANCE,
        recoveryAction = "Create debt repayment plan",
        recoveryAction2 = "Cut one expense immediately",
        immediateConsequence = "Stress + | Freedom -",
        estimatedWeightChange = 0f,
        energyImpact = -15
    ),

    // Productivity decisions
    PROCRASTINATE(
        displayName = "Procrastinate",
        defaultImpact = -6f,
        shortTerm7Day = "Rushed work, stress, missed opportunities",
        longTerm30Day = "Failed projects, lost credibility, regret",
        category = DecisionCategory.PRODUCTIVITY,
        recoveryAction = "Do 1 task for just 5 minutes",
        recoveryAction2 = "Break task into smallest step",
        immediateConsequence = "Anxiety + | Guilt starts building",
        estimatedWeightChange = 0f,
        energyImpact = -10
    ),
    WORK_FOCUS(
        displayName = "Focus Work",
        defaultImpact = 8f,
        shortTerm7Day = "Completed tasks, less stress, free time",
        longTerm30Day = "Career growth, achievements, reputation",
        category = DecisionCategory.PRODUCTIVITY,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Progress + | Dopamine ↑",
        estimatedWeightChange = 0f,
        energyImpact = 10
    ),
    LEARN_SKILL(
        displayName = "Learn New Skill",
        defaultImpact = 9f,
        shortTerm7Day = "Mental stimulation, new connections",
        longTerm30Day = "Career opportunities, higher income",
        category = DecisionCategory.PRODUCTIVITY,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Brain growth | New neural paths",
        estimatedWeightChange = 0f,
        energyImpact = 8
    ),
    WASTE_TIME(
        displayName = "Waste Time",
        defaultImpact = -5f,
        shortTerm7Day = "Regret, unfinished tasks, guilt",
        longTerm30Day = "Missed opportunities, stagnation",
        category = DecisionCategory.PRODUCTIVITY,
        recoveryAction = "Set a 25-min timer and start",
        recoveryAction2 = "List 3 things you should do instead",
        immediateConsequence = "Regret + | Time lost forever",
        estimatedWeightChange = 0f,
        energyImpact = -8
    ),

    // Social decisions
    SOCIAL_MEDIA(
        displayName = "Excessive Social Media",
        defaultImpact = -4f,
        shortTerm7Day = "Reduced productivity, comparison anxiety",
        longTerm30Day = "Mental health issues, isolation, time loss",
        category = DecisionCategory.SOCIAL,
        recoveryAction = "Put phone in another room for 1hr",
        recoveryAction2 = "Call one friend instead of texting",
        immediateConsequence = "Attention fragmented | FOMO ↑",
        estimatedWeightChange = 0f,
        energyImpact = -10
    ),
    MEET_FRIENDS(
        displayName = "Meet Friends",
        defaultImpact = 5f,
        shortTerm7Day = "Joy, connection, support network",
        longTerm30Day = "Strong relationships, happiness, wellbeing",
        category = DecisionCategory.SOCIAL,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Happiness + | Oxytocin ↑",
        estimatedWeightChange = 0f,
        energyImpact = 12
    ),
    ISOLATE(
        displayName = "Isolate Self",
        defaultImpact = -5f,
        shortTerm7Day = "Loneliness, low mood, cabin fever",
        longTerm30Day = "Depression, weak network, missed support",
        category = DecisionCategory.SOCIAL,
        recoveryAction = "Text one person right now",
        recoveryAction2 = "Join an online community",
        immediateConsequence = "Loneliness + | Support network weakens",
        estimatedWeightChange = 0f,
        energyImpact = -15
    ),
    HELP_OTHERS(
        displayName = "Help Others",
        defaultImpact = 6f,
        shortTerm7Day = "Purpose, satisfaction, connections",
        longTerm30Day = "Stronger relationships, reputation, fulfillment",
        category = DecisionCategory.SOCIAL,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Purpose + | Serotonin ↑",
        estimatedWeightChange = 0f,
        energyImpact = 10
    ),

    // Mental Health
    MEDITATE(
        displayName = "Meditate",
        defaultImpact = 7f,
        shortTerm7Day = "Calm, clarity, reduced stress",
        longTerm30Day = "Emotional regulation, mental resilience",
        category = DecisionCategory.MENTAL_HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Stress -25% | Clarity ↑",
        estimatedWeightChange = 0f,
        energyImpact = 10
    ),
    STRESS(
        displayName = "High Stress Day",
        defaultImpact = -7f,
        shortTerm7Day = "Anxiety, poor sleep, irritability",
        longTerm30Day = "Burnout, health issues, relationship strain",
        category = DecisionCategory.MENTAL_HEALTH,
        recoveryAction = "Take 5 deep breaths now",
        recoveryAction2 = "List 3 things you're grateful for",
        immediateConsequence = "Cortisol ↑ | Decision making impaired",
        estimatedWeightChange = 0f,
        energyImpact = -25
    ),
    JOURNAL(
        displayName = "Journal/Reflect",
        defaultImpact = 5f,
        shortTerm7Day = "Clarity, self-awareness, goal focus",
        longTerm30Day = "Personal growth, emotional intelligence",
        category = DecisionCategory.MENTAL_HEALTH,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Clarity + | Processing emotions",
        estimatedWeightChange = 0f,
        energyImpact = 5
    ),
    SCROLL_PHONE(
        displayName = "Excessive Phone Use",
        defaultImpact = -5f,
        shortTerm7Day = "Eye strain, wasted time, sleep issues",
        longTerm30Day = "Reduced attention span, productivity loss",
        category = DecisionCategory.MENTAL_HEALTH,
        recoveryAction = "Screen-free 30 minutes",
        recoveryAction2 = "Do one real-world activity",
        immediateConsequence = "Dopamine overstimulated | Focus ↓",
        estimatedWeightChange = 0f,
        energyImpact = -12
    ),

    // Personal
    READ_BOOK(
        displayName = "Read Book",
        defaultImpact = 6f,
        shortTerm7Day = "Knowledge, mental stimulation, calm",
        longTerm30Day = "Wisdom, better vocabulary, perspective",
        category = DecisionCategory.PERSONAL,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Knowledge + | Calm ↑",
        estimatedWeightChange = 0f,
        energyImpact = 5
    ),
    CREATE(
        displayName = "Create Something",
        defaultImpact = 7f,
        shortTerm7Day = "Accomplishment, flow state, pride",
        longTerm30Day = "Portfolio, skills, creative expression",
        category = DecisionCategory.PERSONAL,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Flow state | Purpose ↑",
        estimatedWeightChange = 0f,
        energyImpact = 8
    ),
    WASTE_EVENING(
        displayName = "Waste Evening",
        defaultImpact = -4f,
        shortTerm7Day = "Regret, no progress, poor sleep",
        longTerm30Day = "Stagnation, missed potential, emptiness",
        category = DecisionCategory.PERSONAL,
        recoveryAction = "Plan tomorrow's priorities now",
        recoveryAction2 = "Read 10 pages before bed",
        immediateConsequence = "Potential wasted | Next day impact",
        estimatedWeightChange = 0f,
        energyImpact = -5
    ),
    SET_GOALS(
        displayName = "Set Goals",
        defaultImpact = 6f,
        shortTerm7Day = "Direction, motivation, clarity",
        longTerm30Day = "Achievements, progress, purpose",
        category = DecisionCategory.PERSONAL,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Direction + | Motivation ↑",
        estimatedWeightChange = 0f,
        energyImpact = 10
    ),

    // Custom
    CUSTOM(
        displayName = "Custom Decision",
        defaultImpact = 0f,
        shortTerm7Day = "Custom short-term impact",
        longTerm30Day = "Custom long-term impact",
        category = DecisionCategory.OTHER,
        recoveryAction = "",
        recoveryAction2 = "",
        immediateConsequence = "Custom impact",
        estimatedWeightChange = 0f,
        energyImpact = 0
    )
}

enum class DecisionCategory(val displayName: String) {
    HEALTH("Health"),
    FINANCE("Finance"),
    PRODUCTIVITY("Productivity"),
    SOCIAL("Social"),
    MENTAL_HEALTH("Mental Health"),
    PERSONAL("Personal"),
    OTHER("Other")
}

enum class FutureIdentity(
    val displayName: String,
    val description: String,
    val targetCategories: List<DecisionCategory>,
    val positivePhrase: String,
    val negativePhrase: String
) {
    FIT_SELF(
        displayName = "Fit Version of Me",
        description = "Strong, healthy, energetic",
        targetCategories = listOf(DecisionCategory.HEALTH),
        positivePhrase = "This is what a fit person would do!",
        negativePhrase = "Your current decisions don't match your Fit Self"
    ),
    RICH_SELF(
        displayName = "Rich Version of Me",
        description = "Financially free, abundant",
        targetCategories = listOf(DecisionCategory.FINANCE),
        positivePhrase = "This builds wealth like a rich person!",
        negativePhrase = "Your current decisions don't match your Rich Self"
    ),
    DISCIPLINED_SELF(
        displayName = "Disciplined Version of Me",
        description = "Focused, productive, consistent",
        targetCategories = listOf(DecisionCategory.PRODUCTIVITY, DecisionCategory.MENTAL_HEALTH),
        positivePhrase = "This is what a disciplined person does!",
        negativePhrase = "Your current decisions don't match your Disciplined Self"
    ),
    HAPPY_SELF(
        displayName = "Happy Version of Me",
        description = "Joyful, connected, fulfilled",
        targetCategories = listOf(DecisionCategory.SOCIAL, DecisionCategory.MENTAL_HEALTH),
        positivePhrase = "This brings joy like your Happy Self!",
        negativePhrase = "Your current decisions don't match your Happy Self"
    ),
    GROWING_SELF(
        displayName = "Growing Version of Me",
        description = "Learning, evolving, improving",
        targetCategories = listOf(DecisionCategory.PERSONAL, DecisionCategory.PRODUCTIVITY),
        positivePhrase = "This is growth-oriented!",
        negativePhrase = "Your current decisions don't match your Growing Self"
    ),
    NO_IDENTITY(
        displayName = "No Identity Set",
        description = "Set your future identity",
        targetCategories = emptyList(),
        positivePhrase = "",
        negativePhrase = ""
    )
}
