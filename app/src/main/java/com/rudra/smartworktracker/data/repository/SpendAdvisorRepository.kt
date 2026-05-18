package com.rudra.smartworktracker.data.repository

import android.content.Context
import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.model.SpendAdvisor
import com.rudra.smartworktracker.model.SpendingTrend
import com.rudra.smartworktracker.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SpendAdvisorRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao,
    private val context: Context? = null
) {
    private val prefs = context?.getSharedPreferences("spend_advisor_prefs", Context.MODE_PRIVATE)
    private val defaultMonthlyGoal = 30000.0

    fun getSpendAdvisorFlow(): Flow<SpendAdvisor> {
        val allTimeIncomeFlow: Flow<Double?> = incomeDao.getTotalIncome()
        val allTimeExpensesFlow: Flow<Double?> = expenseDao.getTotalExpenses()

        val monthlyGoal = prefs?.getFloat("monthly_goal", defaultMonthlyGoal.toFloat())?.toDouble() ?: defaultMonthlyGoal

        return combine(allTimeIncomeFlow, allTimeExpensesFlow) { income: Double?, expenses: Double? ->
            val incomeVal = income ?: 0.0
            val expenseVal = expenses ?: 0.0
            val currentBalance = incomeVal - expenseVal

            val dailyAverage = if (expenseVal > 0) expenseVal / 30.0 else 0.0

            SpendAdvisor(
                totalIncome = incomeVal,
                totalExpenses = expenseVal,
                currentBalance = currentBalance,
                dailyAverageExpense = dailyAverage,
                monthlyGoal = monthlyGoal,
                spendingTrend = SpendingTrend.STABLE,
                trendData = emptyList(),
                categoryBreakdown = emptyMap()
            )
        }
    }

    fun updateMonthlyGoal(newGoal: Double) {
        prefs?.edit()?.putFloat("monthly_goal", newGoal.toFloat())?.apply()
    }
}
