package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val loanDao = db.loanDao()
    private val financialTransactionDao = db.financialTransactionDao()

    val loans: StateFlow<List<Loan>> = loanDao.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addLoan(personName: String, amount: Double, loanType: LoanType, notes: String?) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val loan = Loan(
                personName = personName,
                initialAmount = amount,
                remainingAmount = amount,
                loanType = loanType,
                date = timestamp,
                notes = notes
            )
            val loanId = loanDao.insertLoan(loan).toInt()

            val transactionType = if (loanType == LoanType.BORROWED) TransactionType.LOAN_BORROW else TransactionType.LOAN_LEND
            val source = if (loanType == LoanType.BORROWED) AccountType.LOAN else AccountType.BALANCE
            val destination = if (loanType == LoanType.BORROWED) AccountType.BALANCE else AccountType.LOAN

            val transaction = FinancialTransaction(
                type = transactionType,
                amount = amount,
                source = source,
                destination = destination,
                note = notes ?: "",
                date = timestamp,
                relatedLoanId = loanId
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }

    fun repayLoan(loan: Loan, amount: Double) {
        viewModelScope.launch {
            val updatedLoan = loan.copy(remainingAmount = loan.remainingAmount - amount)
            loanDao.updateLoan(updatedLoan)

            val transaction = FinancialTransaction(
                type = TransactionType.LOAN_REPAY,
                amount = amount,
                source = AccountType.BALANCE,
                destination = AccountType.LOAN,
                note = "Repayment to ${loan.personName}",
                date = System.currentTimeMillis(),
                relatedLoanId = loan.id
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }

    fun receiveLoanRepayment(loan: Loan, amount: Double) {
        viewModelScope.launch {
            val updatedLoan = loan.copy(remainingAmount = loan.remainingAmount - amount)
            loanDao.updateLoan(updatedLoan)

            val transaction = FinancialTransaction(
                type = TransactionType.LOAN_RECEIVE,
                amount = amount,
                source = AccountType.LOAN,
                destination = AccountType.BALANCE,
                note = "Received from ${loan.personName}",
                date = System.currentTimeMillis(),
                relatedLoanId = loan.id
            )
            financialTransactionDao.insertTransaction(transaction)
        }
    }
}
