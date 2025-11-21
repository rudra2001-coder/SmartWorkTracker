package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.dao.LoanDao
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class LoanRepository(private val loanDao: LoanDao, private val transactionDao: FinancialTransactionDao) {

    fun getAllLoans(): Flow<List<Loan>> {
        return loanDao.getAllLoans()
    }

    suspend fun insertLoan(loan: Loan) {
        val loanId = loanDao.insertLoan(loan)
        val transaction = FinancialTransaction(
            type = if (loan.loanType == com.rudra.smartworktracker.data.entity.LoanType.BORROWED) TransactionType.LOAN_RECEIVE else TransactionType.LOAN_LEND,
            amount = loan.initialAmount,
            source = if (loan.loanType == com.rudra.smartworktracker.data.entity.LoanType.BORROWED) AccountType.CASH else AccountType.BANK,
            destination = if (loan.loanType == com.rudra.smartworktracker.data.entity.LoanType.BORROWED) AccountType.BANK else AccountType.CASH,
            note = "Loan: ${loan.personName}",
            date = loan.date,
            relatedLoanId = loanId.toInt()
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteLoan(loan: Loan) {
        loanDao.deleteLoan(loan)
        transactionDao.deleteTransactionsByLoanId(loan.id)
    }

    suspend fun repayLoan(loan: Loan, amount: Double) {
        val updatedLoan = loan.copy(remainingAmount = loan.remainingAmount - amount)
        loanDao.updateLoan(updatedLoan)
        val transaction = FinancialTransaction(
            type = TransactionType.EMI_PAID,
            amount = amount,
            source = AccountType.BANK,
            destination = AccountType.CASH,
            note = "Repayment for loan to ${loan.personName}",
            date = System.currentTimeMillis(),
            relatedLoanId = loan.id
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun receiveLoanRepayment(loan: Loan, amount: Double) {
        val updatedLoan = loan.copy(remainingAmount = loan.remainingAmount - amount)
        loanDao.updateLoan(updatedLoan)
        val transaction = FinancialTransaction(
            type = TransactionType.INCOME,
            amount = amount,
            source = AccountType.CASH,
            destination = AccountType.BANK,
            note = "Repayment received for loan from ${loan.personName}",
            date = System.currentTimeMillis(),
            relatedLoanId = loan.id
        )
        transactionDao.insertTransaction(transaction)
    }
}
