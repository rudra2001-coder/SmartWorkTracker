package com.rudra.smartworktracker.ui.screens.creditcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditCardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val creditCardDao = db.creditCardDao()
    private val creditCardTransactionDao = db.creditCardTransactionDao()
    private val financialTransactionDao = db.financialTransactionDao()

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
                type = TransactionType.EXPENSE, // A purchase with a credit card is an expense
                amount = amount,
                source = AccountType.CREDIT_CARD,
                destination = null,
                note = description,
                date = timestamp
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
                source = AccountType.BALANCE, // Assuming payment from main balance
                destination = AccountType.CREDIT_CARD,
                note = "Paid bill for ${card.cardName}",
                date = timestamp
            )
            financialTransactionDao.insertTransaction(financialTransaction)
        }
    }
}
