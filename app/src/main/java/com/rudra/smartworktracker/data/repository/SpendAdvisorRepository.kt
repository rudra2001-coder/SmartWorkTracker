package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.model.SpendAdvisor
import com.rudra.smartworktracker.model.SpendingTrend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SpendAdvisorRepository(
    private val incomeDao: IncomeDao,
    private val expenseDao: ExpenseDao
) {
    fun getSpendAdvisorFlow(): Flow<SpendAdvisor> {
        val allTimeIncomeFlow: Flow<Double?> = incomeDao.getTotalIncome()
        val allTimeExpensesFlow: Flow<Double?> = expenseDao.getTotalExpenses()

        return combine(allTimeIncomeFlow, allTimeExpensesFlow) { income: Double?, expenses: Double? ->
            val incomeVal = income ?: 0.0
            val expenseVal = expenses ?: 0.0
            val currentBalance = incomeVal - expenseVal

            val dailyAverage = if (expenseVal > 0) expenseVal / 30.0 else 0.0
            val monthlyGoal = incomeVal * 0.7

            SpendAdvisor(
                totalIncome = incomeVal,
                totalExpenses = expenseVal,
                currentBalance = currentBalance,
                dailyAverageExpense = dailyAverage,
                monthlyGoal = monthlyGoal,
                spendingTrend = SpendingTrend.STABLE,
                recentExpenses = emptyList(),
                recentIncomes = emptyList()
            )
        }
    }
}
