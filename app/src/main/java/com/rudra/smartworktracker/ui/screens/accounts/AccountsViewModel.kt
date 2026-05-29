package com.rudra.smartworktracker.ui.screens.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider
import com.rudra.smartworktracker.data.repository.AccountRepository
import com.rudra.smartworktracker.engine.FusionEngine
import com.rudra.smartworktracker.engine.SmartAlert
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AccountsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val accountRepository = AccountRepository(db.accountDao())
    private val fusionEngine = FusionEngine(db.accountDao(), db.financialTransactionDao())

    private val _uiState = MutableStateFlow(AccountsUiState())
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    private val _selectedAccount = MutableStateFlow<Account?>(null)
    val selectedAccount: StateFlow<Account?> = _selectedAccount.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            accountRepository.initializeDefaultAccounts()

            accountRepository.getAllAccounts().collect { accounts ->
                val wallets = accounts.filter { it.type == AccountCategory.WALLET }
                val banks = accounts.filter { it.type == AccountCategory.BANK }
                val mobileBanking = accounts.filter { it.type == AccountCategory.MOBILE_BANKING }

                val netWorth = accounts.sumOf { it.balance }
                val walletTotal = wallets.sumOf { it.balance }
                val bankTotal = banks.sumOf { it.balance }
                val mobileTotal = mobileBanking.sumOf { it.balance }

                val alerts = try {
                    fusionEngine.getSmartAlerts()
                } catch (e: Exception) {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        accounts = accounts,
                        wallets = wallets,
                        bankAccounts = banks,
                        mobileBankingAccounts = mobileBanking,
                        totalNetWorth = netWorth,
                        walletTotal = walletTotal,
                        bankTotal = bankTotal,
                        mobileBankingTotal = mobileTotal,
                        smartAlerts = alerts,
                        error = null
                    )
                }
            }
        }
    }

    fun selectAccount(account: Account) {
        _selectedAccount.value = account
    }

    fun clearSelectedAccount() {
        _selectedAccount.value = null
    }

    fun deleteAccountWithTransfer(
        accountId: Long,
        targetAccountId: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val account = accountRepository.getAccountById(accountId)
                if (account == null) {
                    onResult(false, "Account not found")
                    return@launch
                }

                if (account.balance > 0 && targetAccountId > 0) {
                    val result = fusionEngine.processTransfer(
                        fromAccountId = accountId,
                        toAccountId = targetAccountId,
                        amount = account.balance,
                        note = "Balance transfer before deleting account: ${account.name}"
                    )
                    when (result) {
                        is com.rudra.smartworktracker.engine.FusionResult.Error -> {
                            onResult(false, "Transfer failed: ${result.message}")
                            return@launch
                        }
                        is com.rudra.smartworktracker.engine.FusionResult.Success -> { }
                    }
                }

                accountRepository.deleteAccountById(accountId)
                onResult(true, "Account deleted successfully. Balance transferred.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete account")
            }
        }
    }

    fun deleteAccountDirectly(accountId: Long) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccountById(accountId)
            } catch (_: Exception) { }
        }
    }

    fun canDeleteAccount(accountId: Long): Boolean {
        val account = _uiState.value.accounts.find { it.id == accountId }
        return account?.balance == 0.0
    }

    fun createAccount(
        name: String,
        type: AccountCategory,
        provider: AccountProvider,
        accountNumber: String,
        nickname: String?,
        balance: Double,
        maxBalance: Double? = null,
        hasLimit: Boolean = false,
        dailyLimit: Double? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                accountRepository.createAccount(name, type, provider, accountNumber, nickname, balance, maxBalance, hasLimit, dailyLimit)
                onResult(true, "Account created successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to create account")
            }
        }
    }

    fun updateAccount(account: Account, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                accountRepository.updateAccountDetails(account)
                onResult(true, "Account updated successfully")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update account")
            }
        }
    }

    fun refreshData() {
        loadAccounts()
    }
}

class AccountDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val accountRepository = AccountRepository(db.accountDao())
    private val financialTransactionDao = db.financialTransactionDao()

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    fun loadAccountDetails(accountId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                accountRepository.getAccountByIdFlow(accountId),
                financialTransactionDao.getTransactionsForAccount(accountId)
            ) { account, transactions ->
                if (account != null) {
                    val history = buildBalanceHistory(account, transactions)
                    val totalInflow = transactions.filter { it.destinationAccountId == accountId }.sumOf { it.amount }
                    val totalOutflow = transactions.filter { it.sourceAccountId == accountId }.sumOf { it.amount }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            account = account,
                            transactions = transactions,
                            balanceHistory = history,
                            error = null,
                            totalInflow = totalInflow,
                            totalOutflow = totalOutflow
                        )
                    }
                }
            }.collect()
        }
    }

    private fun buildBalanceHistory(
        account: Account,
        transactions: List<com.rudra.smartworktracker.data.entity.FinancialTransaction>
    ): List<BalanceHistoryItem> {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val now = calendar.timeInMillis

        return (6 downTo 0).map { daysAgo ->
            val date = calendar.apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = date + 86400000L
            val dayTransactions = transactions.filter { it.date in date until dayEnd }
            val dayFlow = dayTransactions.sumOf { 
                when {
                    it.destinationAccountId == account.id -> it.amount
                    it.sourceAccountId == account.id -> -it.amount
                    else -> 0.0
                }
            }

            BalanceHistoryItem(
                date = date,
                balance = dayFlow,
                dayLabel = dayFormat.format(Date(date))
            )
        }
    }
}