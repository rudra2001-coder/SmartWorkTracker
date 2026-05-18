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

    fun addCreditCard(card: CreditCard) {
        viewModelScope.launch {
            creditCardDao.insertCard(card)
        }
    }

    fun addCreditCard(card: CreditCard, transferAmount: Double, accountId: Long?) {
        viewModelScope.launch {
            val cardId = creditCardDao.insertCard(card)
            if (transferAmount > 0 && accountId != null && accountId > 0) {
                val updatedCard = card.copy(
                    id = cardId.toInt(),
                    currentBalance = card.currentBalance + transferAmount
                )
                creditCardDao.updateCard(updatedCard)

                val account = accountDao.getAccountById(accountId)
                if (account != null) {
                    accountDao.updateBalance(accountId, account.balance + transferAmount)
                }

                val financialTransaction = FinancialTransaction(
                    type = TransactionType.TRANSFER,
                    amount = transferAmount,
                    sourceAccountId = 0,
                    destinationAccountId = accountId,
                    note = "Initial transfer from card ${card.cardName} to ${account?.name ?: "account"}",
                    date = System.currentTimeMillis(),
                    relatedCreditCardId = cardId.toInt()
                )
                financialTransactionDao.insertTransaction(financialTransaction)
            }
        }
    }

    fun addCardTransaction(card: CreditCard, amount: Double, description: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updatedCard = card.copy(currentBalance = card.currentBalance + amount)
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
                destinationAccountId = 0,
                note = description,
                date = timestamp,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun payCreditCardBill(card: CreditCard, amount: Double) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updatedCard = card.copy(currentBalance = card.currentBalance - amount)
            creditCardDao.updateCard(updatedCard)

            val financialTransaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                sourceAccountId = card.accountId,
                destinationAccountId = 0,
                note = "Paid bill for ${card.cardName}",
                date = timestamp,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun payCreditCardBill(card: CreditCard, amount: Double, accountId: Long?) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updatedCard = card.copy(currentBalance = card.currentBalance - amount)
            creditCardDao.updateCard(updatedCard)

            val sourceId = accountId ?: card.accountId

            val account = accountDao.getAccountById(sourceId)
            if (account != null) {
                accountDao.updateBalance(sourceId, account.balance - amount)
            }

            val financialTransaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                sourceAccountId = sourceId,
                destinationAccountId = 0,
                note = "Paid bill for ${card.cardName}",
                date = timestamp,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun transferFromCreditCard(card: CreditCard, amount: Double, accountId: Long) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updatedCard = card.copy(currentBalance = card.currentBalance + amount)
            creditCardDao.updateCard(updatedCard)

            val cardTransaction = CreditCardTransaction(
                cardId = card.id,
                amount = amount,
                description = "Transfer to account",
                date = timestamp
            )
            creditCardTransactionDao.insertTransaction(cardTransaction)

            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                accountDao.updateBalance(accountId, account.balance + amount)
            }

            val financialTransaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                sourceAccountId = 0,
                destinationAccountId = accountId,
                note = "Transfer from ${card.cardName} to ${account?.name ?: "account"}",
                date = timestamp,
                relatedCreditCardId = card.id
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        viewModelScope.launch {
            if (card.currentBalance > 0) {
                val account = accountDao.getAccountById(card.accountId)
                if (account != null) {
                    accountDao.updateBalance(card.accountId, account.balance - card.currentBalance)
                }
            }
            creditCardDao.deleteCard(card.id)
        }
    }

    suspend fun getAllAccounts(): List<Account> {
        return accountDao.getAllAccountsList()
    }

    suspend fun getTransactionsForCard(cardId: Int): List<CreditCardTransaction> {
        return creditCardTransactionDao.getTransactionsForCard(cardId).first()
    }
}
