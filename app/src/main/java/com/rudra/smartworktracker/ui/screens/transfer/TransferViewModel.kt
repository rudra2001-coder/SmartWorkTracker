package com.rudra.smartworktracker.ui.screens.transfer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.launch

class TransferViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val financialTransactionDao = db.financialTransactionDao()

    fun makeTransfer(amount: Double, from: AccountType, to: AccountType, notes: String?) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()

            val transaction = FinancialTransaction(
                type = TransactionType.TRANSFER,
                amount = amount,
                source = from,
                destination = to,
                note = notes ?: "",
                date = timestamp
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }
}
