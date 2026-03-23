package com.rudra.smartworktracker.ui.screens.spendadvisor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.repository.SpendAdvisorRepository
import com.rudra.smartworktracker.model.ExpenseAnalysis
import com.rudra.smartworktracker.model.ExpenseAdvice
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.SavingsTip
import com.rudra.smartworktracker.model.SpendAdvisor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
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

    private val _recentAnalyses = MutableStateFlow<List<ExpenseAnalysis>>(emptyList())
    val recentAnalyses: StateFlow<List<ExpenseAnalysis>> = _recentAnalyses.asStateFlow()

    private val _savingsTips = MutableStateFlow<List<SavingsTip>>(emptyList())
    val savingsTips: StateFlow<List<SavingsTip>> = _savingsTips.asStateFlow()

    init {
        loadSpendAdvisor()
        loadSavingsTips()
    }

    private fun loadSpendAdvisor() {
        viewModelScope.launch {
            repository.getSpendAdvisorFlow().collect { advisor ->
                _spendAdvisor.value = advisor
                _isLoading.value = false
            }
        }
    }

    private fun loadSavingsTips() {
        viewModelScope.launch {
            val tips = listOf(
                SavingsTip(
                    id = "1",
                    title = "50/30/20 Rule",
                    description = "Allocate 50% for needs, 30% for wants, and 20% for savings",
                    icon = Icons.Default.Info,
                    color = Color(0xFF43A047)
                ),
                SavingsTip(
                    id = "2",
                    title = "Track Small Expenses",
                    description = "Small daily expenses add up. Track them to identify savings opportunities",
                    icon = Icons.Default.Info,
                    color = Color(0xFF2196F3)
                ),
                SavingsTip(
                    id = "3",
                    title = "Use Cashback Apps",
                    description = "Use apps that offer cashback on regular purchases",
                    icon = Icons.Default.Info,
                    color = Color(0xFFFF9800)
                ),
                SavingsTip(
                    id = "4",
                    title = "Cook at Home",
                    description = "Reduce dining out expenses by cooking meals at home",
                    icon = Icons.Default.Info,
                    color = Color(0xFF9C27B0)
                ),
                SavingsTip(
                    id = "5",
                    title = "Cancel Unused Subscriptions",
                    description = "Review and cancel subscriptions you don't use regularly",
                    icon = Icons.Default.Info,
                    color = Color(0xFFE53935)
                )
            )
            _savingsTips.value = tips
        }
    }

    fun analyzeExpense(plannedAmount: Double, category: ExpenseCategory = ExpenseCategory.OTHER) {
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

        val analysis = ExpenseAnalysis(
            advice = advice,
            remainingAfterExpense = remainingAfterExpense,
            safeLimit = safeLimit,
            warningLimit = warningLimit,
            suggestion = suggestion,
            confidenceScore = confidenceScore,
            category = category,
            timestamp = System.currentTimeMillis()
        )

        _analysis.value = analysis

        val currentHistory = _recentAnalyses.value.toMutableList()
        currentHistory.add(0, analysis)
        if (currentHistory.size > 20) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        _recentAnalyses.value = currentHistory
    }

    fun applySavingsTip(tip: SavingsTip) {
        _analysis.value = _analysis.value?.copy(
            suggestion = "Tip applied: ${tip.title} - ${tip.description}"
        )
    }

    fun applySuggestion(suggestion: String) {
        _analysis.value = _analysis.value?.copy(
            suggestion = suggestion
        )
    }

    fun updateMonthlyGoal(newGoal: Double) {
        repository.updateMonthlyGoal(newGoal)
    }

    fun refreshData() {
        _isLoading.value = true
        loadSpendAdvisor()
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
