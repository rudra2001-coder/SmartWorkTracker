package com.rudra.smartworktracker.ui.screens.creditcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditCardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val creditCardDao = db.creditCardDao()
    private val creditCardTransactionDao = db.creditCardTransactionDao()
    private val financialTransactionDao = db.financialTransactionDao()
    private val accountDao = db.accountDao()

    val creditCards: StateFlow<List<CreditCard>> = creditCardDao.getAllCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private suspend fun updateAccountBalance(accountId: Long, amountChange: Double) {
        val account = accountDao.getAccountById(accountId)
        account?.let {
            val newBalance = (it.balance + amountChange).coerceAtLeast(0.0)
            accountDao.updateBalance(accountId, newBalance)
        }
    }

    fun addCreditCard(card: CreditCard, transferAmount: Double, transferToAccountId: Long?) {
        viewModelScope.launch {
            creditCardDao.insertCard(card)

            if (transferAmount > 0 && transferToAccountId != null) {
                val targetAccount = accountDao.getAccountById(transferToAccountId)
                targetAccount?.let {
                    accountDao.updateBalance(transferToAccountId, it.balance + transferAmount)
                }

                val timestamp = System.currentTimeMillis()
                val cardTransaction = CreditCardTransaction(
                    cardId = card.id,
                    amount = transferAmount,
                    description = "Credit limit transferred to ${targetAccount?.name ?: "account"}",
                    date = timestamp
                )
                creditCardTransactionDao.insertTransaction(cardTransaction)

                val financialTransaction = FinancialTransaction(
                    type = TransactionType.TRANSFER,
                    amount = transferAmount,
                    sourceAccountId = card.accountId,
                    destinationAccountId = transferToAccountId,
                    note = "Credit transfer from ${card.cardName} to ${targetAccount?.name}",
                    date = timestamp,
                    relatedLoanId = null,
                    relatedCreditCardId = card.id
                )
                financialTransactionDao.insertTransaction(financialTransaction)
            }
        }
    }

    fun addCardTransaction(card: CreditCard, amount: Double, description: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updatedCard = card.copy(
                currentBalance = card.currentBalance + amount,
                updatedAt = timestamp
            )
            creditCardDao.updateCard(updatedCard)

            val cardTransaction = CreditCardTransaction(
                cardId = card.id,
                amount = amount,
                description = description,
                date = timestamp
            )
            creditCardTransactionDao.insertTransaction(cardTransaction)

            val financialTransaction = FinancialTransaction(
                type = TransactionType.EXPENSE,
                amount = amount,
                sourceAccountId = card.accountId,
                destinationAccountId = null,
                note = description,
                date = timestamp,
                relatedLoanId = null,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun transferFromCreditCard(card: CreditCard, amount: Double, targetAccountId: Long) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            val availableCredit = card.cardLimit - card.currentBalance
            if (amount > availableCredit) return@launch

            val timestamp = System.currentTimeMillis()

            val updatedCard = card.copy(
                currentBalance = card.currentBalance + amount,
                updatedAt = timestamp
            )
            creditCardDao.updateCard(updatedCard)

            updateAccountBalance(targetAccountId, amount)

            val targetAccount = accountDao.getAccountById(targetAccountId)
            val cardTransaction = CreditCardTransaction(
                cardId = card.id,
                amount = amount,
                description = "Transfer to ${targetAccount?.name ?: "account"}",
                date = timestamp
            )
            creditCardTransactionDao.insertTransaction(cardTransaction)

            val financialTransaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                sourceAccountId = card.accountId,
                destinationAccountId = targetAccountId,
                note = "Credit transfer from ${card.cardName} to ${targetAccount?.name}",
                date = timestamp,
                relatedLoanId = null,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun payCreditCardBill(card: CreditCard, amount: Double, sourceAccountId: Long?) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val paymentAmount = amount.coerceAtMost(card.currentBalance)

            val updatedCard = card.copy(
                currentBalance = (card.currentBalance - paymentAmount).coerceAtLeast(0.0),
                updatedAt = timestamp
            )
            creditCardDao.updateCard(updatedCard)

            val actualSourceAccountId = sourceAccountId ?: card.accountId
            if (actualSourceAccountId > 0) {
                updateAccountBalance(actualSourceAccountId, -paymentAmount)
            }

            val sourceAccount = accountDao.getAccountById(actualSourceAccountId)
            val financialTransaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = paymentAmount,
                sourceAccountId = actualSourceAccountId,
                destinationAccountId = card.accountId,
                note = "Paid bill for ${card.cardName}",
                date = timestamp,
                relatedLoanId = null,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            if (card.currentBalance > 0 && card.accountId > 0) {
                updateAccountBalance(card.accountId, -card.currentBalance)
            }

            creditCardTransactionDao.deleteTransactionsByCardId(card.id)
            creditCardDao.softDeleteCard(card.id, timestamp)
        }
    }

    suspend fun getAllAccounts(): List<Account> {
        return accountDao.getAllAccountsList()
    }

    suspend fun getTransactionsForCard(cardId: Int): List<CreditCardTransaction> {
        return creditCardTransactionDao.getTransactionsForCard(cardId).first()
    }
}
