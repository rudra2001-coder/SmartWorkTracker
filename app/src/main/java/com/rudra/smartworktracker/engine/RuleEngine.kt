package com.rudra.smartworktracker.engine

import java.time.LocalDate

// --- Data Models ---
data class Rule(
    val name: String,
    val condition: (RuleContext) -> Boolean,
    val action: RuleAction
)

data class RuleContext(val date: LocalDate)

sealed class RuleAction
object MarkAsOfficeDay : RuleAction()
object MarkAsWeekend : RuleAction()
object MarkAsHoliday : RuleAction()

enum class DayType {
    OFFICE_DAY,
    WEEKEND,
    HOLIDAY
}

// --- Rule Repository (In-Memory for now) ---
class RulesRepository {
    private val rules = listOf(
        Rule("Weekend Rule", { context -> isWeekend(context.date) }, MarkAsWeekend),
        // Holidays can be added here later
        Rule("Default Office Day", { true }, MarkAsOfficeDay) // Default case
    )

    fun getActiveRules(): List<Rule> = rules
}

// --- Helper Functions ---
private fun isWeekend(date: LocalDate): Boolean {
    return date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7
}

// --- The Rule Engine ---
class RuleEngine(private val rulesRepository: RulesRepository) {

    fun determineDayType(date: LocalDate): DayType {
        val context = RuleContext(date)
        val action = rulesRepository.getActiveRules()
            .firstOrNull { it.condition(context) }?.action

        return when (action) {
            is MarkAsOfficeDay -> DayType.OFFICE_DAY
            is MarkAsWeekend -> DayType.WEEKEND
            is MarkAsHoliday -> DayType.HOLIDAY
            else -> DayType.OFFICE_DAY // Default fallback
        }
    }
}
