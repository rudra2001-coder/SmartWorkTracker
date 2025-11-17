package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.*
import com.rudra.smartworktracker.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

class MealOvertimeRepository(
    private val workDayDao: WorkDayDao,
    private val settingsDao: SettingsDao,
    private val summaryDao: SummaryDao,
    private val monthlyInputDao: MonthlyInputDao
) {

    // -----------------------------------------------------------
    // SETTINGS
    // -----------------------------------------------------------
    suspend fun getSettings(): Settings {
        val saved = settingsDao.getSettings()

        if (saved != null) return saved

        val default = Settings(
            mealRate = 60.0,
            overtimeRate = 100.0,
            dailyWorkHours = 9.0,
            workingDaysPerWeek = 5
        )

        settingsDao.saveSettings(default)
        return default
    }

    suspend fun saveSettings(settings: Settings) {
        settingsDao.saveSettings(settings)
    }

    // -----------------------------------------------------------
    // MONTH INITIALIZATION
    // -----------------------------------------------------------
    suspend fun initializeMonth(year: String, month: String) {
        val monthKey = "$year-$month"

        if (monthlyInputDao.getMonthlyInput(monthKey) == null) {
            monthlyInputDao.insertMonthlyInput(
                MonthlyInput(
                    month = monthKey,
                    totalWorkingDays = 0,
                    totalMeals = 0,
                    totalOvertimeHours = 0.0,
                    isAutoCalculated = true
                )
            )
        }
    }

    // -----------------------------------------------------------
    // CALCULATE FROM FIRST WEEK
    // -----------------------------------------------------------
    suspend fun calculateFromFirstWeek(
        year: String,
        month: String,
        firstWeekData: FirstWeekData
    ): MonthlySummary {

        val settings = getSettings()
        val yearMonth = YearMonth.of(year.toInt(), month.toInt())
        val daysInMonth = yearMonth.lengthOfMonth()

        // Average derived from first week
        val avgMeals = firstWeekData.totalMeals.toDouble() /
                firstWeekData.workingDaysInFirstWeek

        val avgOvertime = firstWeekData.totalOvertimeHours.toDouble() /
                firstWeekData.workingDaysInFirstWeek

        // Estimated working days for whole month
        val totalWorkingDays =
            (daysInMonth / 7.0 * settings.workingDaysPerWeek).toInt()

        // Calculated totals
        val totalMeals = (totalWorkingDays * avgMeals).toInt()
        val totalOvertimeHours = totalWorkingDays * avgOvertime

        val monthKey = "$year-$month"

        val updated = MonthlyInput(
            month = monthKey,
            totalWorkingDays = totalWorkingDays,
            totalMeals = totalMeals,
            totalOvertimeHours = totalOvertimeHours,
            isAutoCalculated = true
        )

        return updateMonthlyInput(updated)
    }

    // -----------------------------------------------------------
    // UPDATE INPUT + AUTO GENERATE SUMMARY
    // -----------------------------------------------------------
    suspend fun updateMonthlyInput(monthlyInput: MonthlyInput): MonthlySummary {

        monthlyInputDao.insertMonthlyInput(monthlyInput)

        val settings = getSettings()

        val totalMealCost = monthlyInput.totalMeals * settings.mealRate
        val totalOvertimePay = monthlyInput.totalOvertimeHours * settings.overtimeRate

        val totalExpense = totalMealCost

        val summary = MonthlySummary(
            month = monthlyInput.month,
            totalWorkDays = monthlyInput.totalWorkingDays,
            totalMeals = monthlyInput.totalMeals,
            totalMealCost = totalMealCost,
            totalOvertimeHours = monthlyInput.totalOvertimeHours,
            totalOvertimePay = totalOvertimePay,
            totalExpense = totalExpense
        )

      //  summaryDao.insertOrUpdate(summary)
        return summary
    }

    // -----------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------
    suspend fun getMonthlyInput(monthKey: String): MonthlyInput? {
        return monthlyInputDao.getMonthlyInput(monthKey)
    }

    fun getSummary(monthKey: String): Flow<MonthlySummary?> {
        return summaryDao.getSummary(monthKey)
    }
}

data class FirstWeekData(
    val workingDaysInFirstWeek: Int,
    val totalMeals: Int,
    val totalOvertimeHours: Float
)
