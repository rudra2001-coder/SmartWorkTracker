package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FinancialStatementViewModel(application: Application) : AndroidViewModel(application) {

    private val financialTransactionDao = AppDatabase.getDatabase(application).financialTransactionDao()

    val transactions: StateFlow<List<FinancialTransaction>> = financialTransactionDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalIncome: StateFlow<Double> = transactions.map {
        it.filter { t -> t.type == TransactionType.INCOME || t.type == TransactionType.LOAN_RECEIVE }
            .sumOf { t -> t.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = transactions.map {
        it.filter { t -> t.type == TransactionType.EXPENSE || t.type == TransactionType.EMI_PAID }
            .sumOf { t -> t.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netFlow: StateFlow<Double> = transactions.map {
        val income = it.filter { t -> t.type == TransactionType.INCOME || t.type == TransactionType.LOAN_RECEIVE }.sumOf { t -> t.amount }
        val expenses = it.filter { t -> t.type == TransactionType.EXPENSE || t.type == TransactionType.EMI_PAID }.sumOf { t -> t.amount }
        income - expenses
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

}
