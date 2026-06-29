package com.rudra.smartworktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_rules")
data class RecurringRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val uuid: String? = null,

    val name: String,
    val description: String? = null,

    val transactionType: TransactionType,

    val amount: Double,

    val category: String? = null,

    val sourceAccountId: Long = 0,
    val destinationAccountId: Long? = null,

    val frequency: RecurringFrequency,

    val interval: Int = 1,

    val selectedDaysOfWeek: List<DayOfWeek>? = null,

    val selectedDaysOfMonth: List<Int>? = null,

    val monthlyDayOption: MonthlyDayOption = MonthlyDayOption.DAY_OF_MONTH,

    val weeklyInterval: Int = 1,

    val cronExpression: String? = null,

    val startDate: Long,

    val endDate: Long? = null,

    val nextExecutionDate: Long,

    val preferredTime: PreferredTime = PreferredTime.MORNING,

    val priority: RecurringPriority = RecurringPriority.MEDIUM,

    val minimumBalanceRequired: Double? = null,

    val autoExecute: Boolean = true,

    val gracePeriodMinutes: Int = 60,

    val isActive: Boolean = true,

    val isPaused: Boolean = false,

    val maxExecutions: Int? = null,

    val executedCount: Int = 0,

    val totalExecutedAmount: Double = 0.0,

    val lastExecutedDate: Long? = null,

    val skipIfHoliday: Boolean = false,

    val weekdayAdjustment: WeekdayAdjustment = WeekdayAdjustment.SKIP,

    val strictMode: Boolean = false,

    val maxCatchUpDays: Int = 0,

    val lastCheckedTimestamp: Long? = null,

    val pendingRetry: Boolean = false,

    val retryCount: Int = 0,

    val maxRetries: Int = 10,

    val tags: String? = null,

    val notes: String? = null,

    val notifyBeforeExecution: Boolean = true,
    val notifyOnExecution: Boolean = false,
    val notifyOnFailure: Boolean = true,

    val notifyDaysBefore: Int = 1,

    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    CUSTOM,
    WEEKLY_SPECIFIC_DAYS,
    MONTHLY_SPECIFIC_DAYS
}

enum class DayOfWeek(val displayName: String, val shortName: String) {
    MONDAY("Monday", "Mon"),
    TUESDAY("Tuesday", "Tue"),
    WEDNESDAY("Wednesday", "Wed"),
    THURSDAY("Thursday", "Thu"),
    FRIDAY("Friday", "Fri"),
    SATURDAY("Saturday", "Sat"),
    SUNDAY("Sunday", "Sun");

    companion object {
        fun fromCalendarDay(calendarDay: Int): DayOfWeek {
            return when (calendarDay) {
                java.util.Calendar.MONDAY -> MONDAY
                java.util.Calendar.TUESDAY -> TUESDAY
                java.util.Calendar.WEDNESDAY -> WEDNESDAY
                java.util.Calendar.THURSDAY -> THURSDAY
                java.util.Calendar.FRIDAY -> FRIDAY
                java.util.Calendar.SATURDAY -> SATURDAY
                java.util.Calendar.SUNDAY -> SUNDAY
                else -> MONDAY
            }
        }

        fun toCalendarDay(dayOfWeek: DayOfWeek): Int {
            return when (dayOfWeek) {
                MONDAY -> java.util.Calendar.MONDAY
                TUESDAY -> java.util.Calendar.TUESDAY
                WEDNESDAY -> java.util.Calendar.WEDNESDAY
                THURSDAY -> java.util.Calendar.THURSDAY
                FRIDAY -> java.util.Calendar.FRIDAY
                SATURDAY -> java.util.Calendar.SATURDAY
                SUNDAY -> java.util.Calendar.SUNDAY
            }
        }

        fun isWeekend(day: DayOfWeek): Boolean {
            return day == FRIDAY || day == SATURDAY
        }
    }
}

enum class PreferredTime {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

enum class RecurringPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    OPTIONAL
}

enum class WeekdayAdjustment {
    SKIP,
    PREVIOUS_WORKDAY,
    NEXT_WORKDAY
}

enum class MonthlyDayOption {
    DAY_OF_MONTH,
    FIRST_DAY,
    LAST_DAY,
    FIRST_WEEKDAY,
    LAST_WEEKDAY
}
