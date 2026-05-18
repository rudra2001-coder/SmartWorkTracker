package com.rudra.smartworktracker.ui.screens.income

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.data.repository.AccountRepository
import com.rudra.smartworktracker.data.repository.IncomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val incomeRepository = IncomeRepository(db.incomeDao(), db.accountDao())
    private val accountRepository = AccountRepository(db.accountDao())

    private val _income = MutableStateFlow(0.0)
    val income: StateFlow<Double> = _income.asStateFlow()
    private val _recentIncomes = MutableStateFlow<List<Income>>(emptyList())
    val recentIncomes: StateFlow<List<Income>> = _recentIncomes.asStateFlow()
    
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    init {
        loadTotalIncome()
        loadRecentIncomes()
        loadAccounts()
    }

    private fun loadTotalIncome() {
        viewModelScope.launch {
            db.incomeDao().getTotalIncome().collect { totalIncome ->
                _income.value = totalIncome ?: 0.0
            }
        }
    }
    private fun loadRecentIncomes() {
        viewModelScope.launch {
            db.incomeDao().getLatest5Incomes().collect { incomes ->
                _recentIncomes.value = incomes
            }
        }
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.initializeDefaultAccounts()
            accountRepository.getAllAccounts().collect { accountList ->
                _accounts.value = accountList
            }
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            incomeRepository.deleteIncome(income)
        }
    }

    fun saveIncome(
        amount: Double, 
        description: String, 
        category: String, 
        source: String, 
        timestamp: Long,
        selectedAccountId: Long
    ) {
        viewModelScope.launch {
            val newIncome = Income(
                amount = amount,
                description = description,
                category = category,
                timestamp = timestamp,
                source = source,
                accountId = selectedAccountId
            )
            incomeRepository.insertIncome(newIncome)
        }
    }
}