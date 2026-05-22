package com.rudra.smartworktracker.ui.screens.accounts

import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.engine.SmartAlert

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    val wallets: List<Account> = emptyList(),
    val bankAccounts: List<Account> = emptyList(),
    val mobileBankingAccounts: List<Account> = emptyList(),
    val totalNetWorth: Double = 0.0,
    val walletTotal: Double = 0.0,
    val bankTotal: Double = 0.0,
    val mobileBankingTotal: Double = 0.0,
    val smartAlerts: List<SmartAlert> = emptyList(),
    val error: String? = null
)

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val transactions: List<FinancialTransaction> = emptyList(),
    val balanceHistory: List<BalanceHistoryItem> = emptyList(),
    val error: String? = null,
    val totalInflow: Double = 0.0,
    val totalOutflow: Double = 0.0
)

data class BalanceHistoryItem(
    val date: Long,
    val balance: Double,
    val dayLabel: String
)