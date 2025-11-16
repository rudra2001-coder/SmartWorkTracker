package com.rudra.smartworktracker.ui.screens.emi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class EmiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val emiDao = db.emiDao()
    private val loanDao = db.loanDao()
    private val financialTransactionDao = db.financialTransactionDao()

    val emis: StateFlow<List<Emi>> = emiDao.getActiveEmis()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val loans: StateFlow<List<Loan>> = loanDao.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEmi(loanId: Int, amount: Double, dueDateOfMonth: Int) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, dueDateOfMonth)
            // If due date is in the past for the current month, set it for the next month
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.MONTH, 1)
            }
            val nextDueDate = calendar.timeInMillis

            val emi = Emi(
                loanId = loanId,
                amount = amount,
                dueDateOfMonth = dueDateOfMonth,
                nextDueDate = nextDueDate
            )
            emiDao.insertEmi(emi)
        }
    }

    fun payEmi(emi: Emi, principalAmount: Double) {
        viewModelScope.launch {
            // Update loan
            val loan = loanDao.getLoanById(emi.loanId).first()
            if (loan != null) {
                val updatedLoan = loan.copy(remainingAmount = loan.remainingAmount - principalAmount)
                loanDao.updateLoan(updatedLoan)

                // Deactivate EMI if loan is fully paid
                val updatedEmi = if (updatedLoan.remainingAmount <= 0) {
                    emi.copy(isActive = false)
                } else {
                    // Update EMI to next due date
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = emi.nextDueDate
                    calendar.add(Calendar.MONTH, 1)
                    emi.copy(nextDueDate = calendar.timeInMillis)
                }
                emiDao.updateEmi(updatedEmi)

                // Create financial transaction
                val transaction = FinancialTransaction(
                    type = TransactionType.EMI_PAID,
                    amount = emi.amount,
                    source = AccountType.BALANCE, // Or Savings
                    destination = AccountType.LOAN,
                    note = "EMI payment for loan to ${loan.personName}",
                    date = System.currentTimeMillis(),
                    relatedLoanId = emi.loanId
                )
                financialTransactionDao.insertTransaction(transaction)
            }
        }
    }
}
