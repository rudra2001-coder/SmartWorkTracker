package com.rudra.smartworktracker.ui.screens.spendadvisor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.SpendAdvisorRepository
import com.rudra.smartworktracker.model.ExpenseAnalysis
import com.rudra.smartworktracker.model.ExpenseAdvice
import com.rudra.smartworktracker.model.SpendAdvisor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpendAdvisorViewModel(
    application: Application,
    private val repository: SpendAdvisorRepository
) : AndroidViewModel(application) {

    private val _spendAdvisor = MutableStateFlow(SpendAdvisor())
    val spendAdvisor: StateFlow<SpendAdvisor> = _spendAdvisor.asStateFlow()

    private val _analysis = MutableStateFlow<ExpenseAnalysis?>(null)
    val analysis: StateFlow<ExpenseAnalysis?> = _analysis.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSpendAdvisor()
    }

    private fun loadSpendAdvisor() {
        viewModelScope.launch {
            repository.getSpendAdvisorFlow().collect { advisor ->
                _spendAdvisor.value = advisor
                _isLoading.value = false
            }
        }
    }

    fun analyzeExpense(plannedAmount: Double) {
        val advisor = _spendAdvisor.value
        val currentBalance = advisor.currentBalance
        val monthlyGoal = advisor.monthlyGoal

        val safeLimit = monthlyGoal * 0.3
        val warningLimit = monthlyGoal * 0.5

        val remainingAfterExpense = currentBalance - plannedAmount

        val advice = when {
            remainingAfterExpense >= safeLimit -> ExpenseAdvice.SAFE
            remainingAfterExpense >= warningLimit -> ExpenseAdvice.RISKY
            else -> ExpenseAdvice.NOT_ACCEPTABLE
        }

        val confidenceScore = calculateConfidenceScore(
            currentBalance = currentBalance,
            plannedAmount = plannedAmount,
            monthlyIncome = advisor.totalIncome,
            dailyAverage = advisor.dailyAverageExpense
        )

        val suggestion = when (advice) {
            ExpenseAdvice.SAFE -> null
            ExpenseAdvice.RISKY -> "Consider reducing to ৳${(currentBalance * 0.3).toLong()} for better savings."
            ExpenseAdvice.NOT_ACCEPTABLE -> "Suggested: ৳${(currentBalance * 0.2).toLong()} instead of ৳${plannedAmount.toLong()}"
        }

        _analysis.value = ExpenseAnalysis(
            advice = advice,
            remainingAfterExpense = remainingAfterExpense,
            safeLimit = safeLimit,
            warningLimit = warningLimit,
            suggestion = suggestion,
            confidenceScore = confidenceScore
        )
    }

    private fun calculateConfidenceScore(
        currentBalance: Double,
        plannedAmount: Double,
        monthlyIncome: Double,
        dailyAverage: Double
    ): Int {
        if (monthlyIncome <= 0) return 50

        val balanceRatio = currentBalance / monthlyIncome
        val expenseRatio = plannedAmount / monthlyIncome

        var score = when {
            balanceRatio > 0.5 -> 80
            balanceRatio > 0.3 -> 65
            balanceRatio > 0.1 -> 45
            else -> 25
        }

        if (expenseRatio > 0.5) score -= 20
        else if (expenseRatio > 0.3) score -= 10

        if (plannedAmount > dailyAverage * 3) score -= 15

        return score.coerceIn(0, 100)
    }

    fun clearAnalysis() {
        _analysis.value = null
    }
}
