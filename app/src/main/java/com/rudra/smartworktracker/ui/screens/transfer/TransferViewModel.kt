package com.rudra.smartworktracker.ui.screens.transfer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.repository.AccountRepository
import com.rudra.smartworktracker.engine.FusionEngine
import com.rudra.smartworktracker.engine.FusionResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransferViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val accountRepository = AccountRepository(db.accountDao())
    private val fusionEngine = FusionEngine(db.accountDao(), db.financialTransactionDao(), db.expenseDao())

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.initializeDefaultAccounts()
            accountRepository.getAllAccounts().collect { accountList ->
                _accounts.value = accountList
            }
        }
    }

    fun makeTransfer(
        fromAccount: Account,
        toAccount: Account,
        amount: Double,
        notes: String?,
        transferFee: Double = 0.0,
        cashOutFee: Double = 0.0
    ) {
        if (_transferState.value is TransferState.Loading) return
        _transferState.value = TransferState.Loading

        viewModelScope.launch {
            val result = fusionEngine.processTransfer(
                fromAccountId = fromAccount.id,
                toAccountId = toAccount.id,
                amount = amount,
                note = notes,
                transferFee = transferFee,
                cashOutFee = cashOutFee
            )

            when (result) {
                is FusionResult.Success -> {
                    _transferState.value = TransferState.Success(
                        fromAccount = result.fromAccount,
                        toAccount = result.toAccount,
                        amount = amount,
                        totalFee = result.totalFee
                    )
                    _error.value = null
                }
                is FusionResult.Error -> {
                    _transferState.value = TransferState.Error(result.message)
                    _error.value = result.message
                }
            }
        }
    }

    fun resetState() {
        _transferState.value = TransferState.Idle
        _error.value = null
    }

    fun validateTransfer(
        fromAccount: Account?,
        toAccount: Account?,
        amountStr: String,
        transferFeeStr: String = "",
        cashOutFeeStr: String = ""
    ): ValidationResult {
        if (fromAccount == null) {
            return ValidationResult.Error("Please select source account")
        }
        if (toAccount == null) {
            return ValidationResult.Error("Please select destination account")
        }
        if (fromAccount.id == toAccount.id) {
            return ValidationResult.Error("Cannot transfer to the same account")
        }
        
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            return ValidationResult.Error("Please enter a valid amount")
        }

        val transferFee = transferFeeStr.toDoubleOrNull() ?: 0.0
        val cashOutFee = cashOutFeeStr.toDoubleOrNull() ?: 0.0
        val totalDeduction = amount + transferFee + cashOutFee

        if (totalDeduction > fromAccount.balance) {
            val needed = if (transferFee > 0 || cashOutFee > 0) {
                " (Amount: ৳${amount.toInt()} + Fees: ৳${(transferFee + cashOutFee).toInt()})"
            } else ""
            return ValidationResult.Error("Insufficient balance (Available: ৳ ${fromAccount.balance.toInt()})$needed")
        }

        return ValidationResult.Valid
    }
}

sealed class TransferState {
    object Idle : TransferState()
    object Loading : TransferState()
    data class Success(
        val fromAccount: Account,
        val toAccount: Account,
        val amount: Double,
        val totalFee: Double = 0.0
    ) : TransferState()
    data class Error(val message: String) : TransferState()
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}