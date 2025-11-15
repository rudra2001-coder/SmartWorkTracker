package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.MonthlyInputDao
import com.rudra.smartworktracker.data.dao.SettingsDao
import com.rudra.smartworktracker.data.dao.SummaryDao
import com.rudra.smartworktracker.data.dao.WorkDayDao
import com.rudra.smartworktracker.data.entity.MonthlyInput
import com.rudra.smartworktracker.data.entity.MonthlySummary
import com.rudra.smartworktracker.data.entity.Settings
import java.time.LocalDate
import java.time.YearMonth

class MealOvertimeRepository(
    private val workDayDao: WorkDayDao,
    private val settingsDao: SettingsDao,
    private val summaryDao: SummaryDao,
    private val monthlyInputDao: MonthlyInputDao
) {

    suspend fun getSettings(): Settings {
        return settingsDao.getSettings() ?: Settings(
            mealRate = 60.0,
            overtimeRate = 100.0,
            dailyWorkHours = 9.0,
            workingDaysPerWeek = 5
        ).also {
            settingsDao.saveSettings(it)
        }
    }

    suspend fun saveSettings(settings: Settings) {
        settingsDao.saveSettings(settings)
    }

    suspend fun initializeMonth(year: String, month: String) {
        val monthKey = "$year-$month"
        if (monthlyInputDao.getMonthlyInput(monthKey) == null) {
            val defaultInput = MonthlyInput(
                month = monthKey,
                totalWorkingDays = 0,
                totalMeals = 0,
                totalOvertimeHours = 0.0,
                isAutoCalculated = true
            )
            monthlyInputDao.insertMonthlyInput(defaultInput)
        }
    }

    suspend fun calculateFromFirstWeek(year: String, month: String, firstWeekData: FirstWeekData): MonthlySummary {
        val settings = getSettings()
        val daysInMonth = YearMonth.of(year.toInt(), month.toInt()).lengthOfMonth()

        val weeklyMeals = firstWeekData.totalMeals.toDouble() / firstWeekData.workingDaysInFirstWeek
        val weeklyOvertime = firstWeekData.totalOvertimeHours.toDouble() / firstWeekData.workingDaysInFirstWeek

        val totalWorkingDays = (daysInMonth / 7.0 * settings.workingDaysPerWeek).toInt()
        val totalMeals = (totalWorkingDays * weeklyMeals).toInt()
        val totalOvertimeHours = totalWorkingDays * weeklyOvertime

        val updatedInput = MonthlyInput(
            month = "$year-$month",
            totalWorkingDays = totalWorkingDays,
            totalMeals = totalMeals,
            totalOvertimeHours = totalOvertimeHours,
            isAutoCalculated = true
        )

        return updateMonthlyInput(updatedInput)
    }

    suspend fun updateMonthlyInput(monthlyInput: MonthlyInput): MonthlySummary {
        monthlyInputDao.insertMonthlyInput(monthlyInput)

        val settings = getSettings()
        val totalMealCost = monthlyInput.totalMeals * settings.mealRate
        val totalOvertimePay = monthlyInput.totalOvertimeHours * settings.overtimeRate
        val totalExpense = totalMealCost - totalOvertimePay

        val summary = MonthlySummary(
            month = monthlyInput.month,
            totalWorkDays = monthlyInput.totalWorkingDays,
            totalMeals = monthlyInput.totalMeals,
            totalMealCost = totalMealCost,
            totalOvertimeHours = monthlyInput.totalOvertimeHours,
            totalOvertimePay = totalOvertimePay,
            totalExpense = totalExpense
        )
        summaryDao.insertSummary(summary)
        return summary
    }

    suspend fun getMonthlyInput(monthKey: String): MonthlyInput? {
        return monthlyInputDao.getMonthlyInput(monthKey)
    }

    suspend fun getSummary(monthKey: String): MonthlySummary? {
        return summaryDao.getSummary(monthKey)
    }
}

data class FirstWeekData(
    val workingDaysInFirstWeek: Int,
    val totalMeals: Int,
    val totalOvertimeHours: Float
)
