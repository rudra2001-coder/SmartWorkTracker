package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.PreferredTime
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class RecurringEngineTest {

    private lateinit var engine: RecurringEngine

    @Before
    fun setup() {
        engine = RecurringEngine(
            recurringRepository = null!!,
            incomeRepository = null!!,
            expenseRepository = null!!
        )
    }

    private fun createRule(
        amount: Double = 1000.0,
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        transactionType: TransactionType = TransactionType.EXPENSE,
        interval: Int = 1,
        selectedDaysOfWeek: List<DayOfWeek>? = null,
        category: String? = "Test"
    ) = RecurringRule(
        name = "Test Rule",
        amount = amount,
        frequency = frequency,
        transactionType = transactionType,
        interval = interval,
        selectedDaysOfWeek = selectedDaysOfWeek,
        category = category,
        sourceAccount = AccountType.BALANCE,
        startDate = System.currentTimeMillis(),
        nextExecutionDate = System.currentTimeMillis(),
        preferredTime = PreferredTime.MORNING,
        priority = RecurringPriority.MEDIUM,
        autoExecute = true,
        isActive = true
    )

    // --- calculateNextExecutionDate tests ---

    @Test
    fun `daily frequency adds one day`() {
        val now = System.currentTimeMillis()
        val result = engine.calculateNextExecutionDate(now, RecurringFrequency.DAILY)
        val diffDays = (result - now) / (24 * 60 * 60 * 1000)
        assertEquals(1L, diffDays)
    }

    @Test
    fun `weekly frequency adds 7 days`() {
        val now = System.currentTimeMillis()
        val result = engine.calculateNextExecutionDate(now, RecurringFrequency.WEEKLY)
        val diffDays = (result - now) / (24 * 60 * 60 * 1000)
        assertEquals(7L, diffDays)
    }

    @Test
    fun `monthly frequency adds one month`() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 15, 9, 0, 0)
        val date = cal.timeInMillis

        val result = engine.calculateNextExecutionDate(date, RecurringFrequency.MONTHLY)

        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `yearly frequency adds one year`() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.MARCH, 10, 9, 0, 0)
        val date = cal.timeInMillis

        val result = engine.calculateNextExecutionDate(date, RecurringFrequency.YEARLY)

        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(2027, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH))
    }

    @Test
    fun `biweekly frequency adds 14 days`() {
        val now = System.currentTimeMillis()
        val result = engine.calculateNextExecutionDate(now, RecurringFrequency.BIWEEKLY)
        val diffDays = (result - now) / (24 * 60 * 60 * 1000)
        assertEquals(14L, diffDays)
    }

    @Test
    fun `quarterly frequency adds 3 months`() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 15, 9, 0, 0)
        val date = cal.timeInMillis

        val result = engine.calculateNextExecutionDate(date, RecurringFrequency.QUARTERLY)

        val resultCal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.APRIL, resultCal.get(Calendar.MONTH))
    }

    @Test
    fun `preferred time sets correct hour`() {
        val now = System.currentTimeMillis()

        val morning = engine.calculateNextExecutionDate(now, RecurringFrequency.DAILY, preferredTime = PreferredTime.MORNING)
        assertEquals(9, Calendar.getInstance().apply { timeInMillis = morning }.get(Calendar.HOUR_OF_DAY))

        val afternoon = engine.calculateNextExecutionDate(now, RecurringFrequency.DAILY, preferredTime = PreferredTime.AFTERNOON)
        assertEquals(14, Calendar.getInstance().apply { timeInMillis = afternoon }.get(Calendar.HOUR_OF_DAY))

        val evening = engine.calculateNextExecutionDate(now, RecurringFrequency.DAILY, preferredTime = PreferredTime.EVENING)
        assertEquals(19, Calendar.getInstance().apply { timeInMillis = evening }.get(Calendar.HOUR_OF_DAY))

        val night = engine.calculateNextExecutionDate(now, RecurringFrequency.DAILY, preferredTime = PreferredTime.NIGHT)
        assertEquals(22, Calendar.getInstance().apply { timeInMillis = night }.get(Calendar.HOUR_OF_DAY))
    }

    // --- calculateMonthlyEquivalent tests ---

    @Test
    fun `monthly equivalent for monthly frequency`() {
        val rule = createRule(amount = 5000.0, frequency = RecurringFrequency.MONTHLY)
        assertEquals(5000.0, engine.calculateMonthlyEquivalent(rule), 0.01)
    }

    @Test
    fun `monthly equivalent for daily frequency`() {
        val rule = createRule(amount = 100.0, frequency = RecurringFrequency.DAILY)
        assertEquals(3000.0, engine.calculateMonthlyEquivalent(rule), 0.01)
    }

    @Test
    fun `monthly equivalent for weekly frequency`() {
        val rule = createRule(amount = 500.0, frequency = RecurringFrequency.WEEKLY)
        assertEquals(2165.0, engine.calculateMonthlyEquivalent(rule), 1.0)
    }

    @Test
    fun `monthly equivalent for yearly frequency`() {
        val rule = createRule(amount = 12000.0, frequency = RecurringFrequency.YEARLY)
        assertEquals(1000.0, engine.calculateMonthlyEquivalent(rule), 0.01)
    }

    @Test
    fun `monthly equivalent for quarterly frequency`() {
        val rule = createRule(amount = 3000.0, frequency = RecurringFrequency.QUARTERLY)
        assertEquals(1000.0, engine.calculateMonthlyEquivalent(rule), 0.01)
    }

    @Test
    fun `monthly equivalent for biweekly frequency`() {
        val rule = createRule(amount = 200.0, frequency = RecurringFrequency.BIWEEKLY)
        assertEquals(434.0, engine.calculateMonthlyEquivalent(rule), 1.0)
    }

    // --- calculateYearlyProjection tests ---

    @Test
    fun `yearly projection calculates income and expenses`() {
        val rules = listOf(
            createRule(amount = 50000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.INCOME),
            createRule(amount = 15000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.EXPENSE, category = "Rent"),
            createRule(amount = 2000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.EXPENSE, category = "Bills")
        )

        val projection = engine.calculateYearlyProjection(rules)

        assertEquals(600000.0, projection.totalYearlyIncome, 0.01)
        assertEquals(204000.0, projection.totalYearlyExpenses, 0.01)
        assertEquals(396000.0, projection.netYearly, 0.01)
    }

    @Test
    fun `yearly projection ignores inactive rules`() {
        val activeRule = createRule(amount = 50000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.INCOME)
        val inactiveRule = createRule(amount = 100000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.INCOME).copy(isActive = false)

        val projection = engine.calculateYearlyProjection(listOf(activeRule, inactiveRule))

        assertEquals(600000.0, projection.totalYearlyIncome, 0.01)
    }

    @Test
    fun `yearly projection category breakdown`() {
        val rules = listOf(
            createRule(amount = 15000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.EXPENSE, category = "Rent"),
            createRule(amount = 2000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.EXPENSE, category = "Bills"),
            createRule(amount = 15000.0, frequency = RecurringFrequency.MONTHLY, transactionType = TransactionType.EXPENSE, category = "Rent")
        )

        val projection = engine.calculateYearlyProjection(rules)

        assertEquals(360000.0, projection.categoryBreakdown["Rent"]!!, 0.01)
        assertEquals(24000.0, projection.categoryBreakdown["Bills"]!!, 0.01)
    }

    // --- isExecutionDay tests ---

    @Test
    fun `isExecutionDay returns true for non-specific-day rules`() {
        val rule = createRule(frequency = RecurringFrequency.MONTHLY)
        assertTrue(engine.isExecutionDay(rule))
    }

    @Test
    fun `isExecutionDay checks selected days for specific-day rules`() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayDayOfWeek = when (today) {
            Calendar.MONDAY -> DayOfWeek.MONDAY
            Calendar.TUESDAY -> DayOfWeek.TUESDAY
            Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
            Calendar.THURSDAY -> DayOfWeek.THURSDAY
            Calendar.FRIDAY -> DayOfWeek.FRIDAY
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }

        val rule = createRule(
            frequency = RecurringFrequency.WEEKLY_SPECIFIC_DAYS,
            selectedDaysOfWeek = listOf(todayDayOfWeek)
        )
        assertTrue(engine.isExecutionDay(rule))
    }

}
