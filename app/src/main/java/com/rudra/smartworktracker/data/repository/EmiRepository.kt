package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.dao.EmiDao
import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.dao.LoanDao
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.EmiStatus
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class EmiRepository(
    private val emiDao: EmiDao,
    private val loanDao: LoanDao,
    private val transactionDao: FinancialTransactionDao,
    private val accountDao: AccountDao
) {
    fun getActiveEmis(): Flow<List<Emi>> = emiDao.getActiveEmis()

    fun getAllActiveEmis(): Flow<List<Emi>> = emiDao.getAllActiveEmis()

    fun getEmisForLoan(loanId: Int): Flow<List<Emi>> = emiDao.getEmisForLoan(loanId)

    fun getAllEmis(): Flow<List<Emi>> = emiDao.getAllEmis()

    fun getEmiById(emiId: Int): Flow<Emi?> = emiDao.getEmiById(emiId)

    fun getOverdueEmis(): Flow<List<Emi>> = emiDao.getOverdueEmis(System.currentTimeMillis())

    fun getEmisDueThisMonth(): Flow<List<Emi>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis
        
        return emiDao.getEmisDueInRange(startOfMonth, endOfMonth)
    }

    fun getPaidEmis(): Flow<List<Emi>> = emiDao.getPaidEmis()

    fun getTotalPendingAmount(): Flow<Double?> = emiDao.getTotalPendingEmiAmount()

    fun getPendingEmiCount(): Flow<Int> = emiDao.getPendingEmiCount()

    fun getOverdueEmiCount(): Flow<Int> = emiDao.getOverdueEmiCount(System.currentTimeMillis())

    fun getTotalPenaltyCollected(): Flow<Double?> = emiDao.getTotalPenaltyCollected()

    suspend fun insertEmi(emi: Emi): Long {
        return emiDao.insertEmi(emi)
    }

    suspend fun updateEmi(emi: Emi) {
        val updatedEmi = emi.copy(updatedAt = System.currentTimeMillis())
        emiDao.updateEmi(updatedEmi)
    }

    suspend fun deleteEmi(emi: Emi) {
        val deletedEmi = emi.copy(isDeleted = true, updatedAt = System.currentTimeMillis())
        emiDao.updateEmi(deletedEmi)
    }

    suspend fun payEmi(emi: Emi) {
        val loan = loanDao.getLoanById(emi.loanId).first()
        if (loan == null) return

        val paymentDate = System.currentTimeMillis()

        if (emi.paymentAccountId > 0) {
            val paymentAccount = accountDao.getAccountById(emi.paymentAccountId)
                ?: throw IllegalStateException("Payment account not found")
            if (loan.loanType == LoanType.BORROWED) {
                if (paymentAccount.balance < emi.totalPayable) {
                    throw IllegalStateException(
                        "Insufficient balance in ${paymentAccount.name}. " +
                        "Current balance: ৳${"%,.0f".format(paymentAccount.balance)}, " +
                        "Required: ৳${"%,.0f".format(emi.totalPayable)}"
                    )
                }
                accountDao.updateBalance(emi.paymentAccountId, paymentAccount.balance - emi.totalPayable)
            } else {
                accountDao.updateBalance(emi.paymentAccountId, paymentAccount.balance + emi.totalPayable)
            }
        }

        val newRemainingAmount = (loan.remainingAmount - emi.principalAmount).coerceAtLeast(0.0)
        val isLoanFullyPaid = newRemainingAmount <= 0.0

        loanDao.updateLoan(
            loan.copy(
                remainingAmount = newRemainingAmount,
                isFullyPaid = isLoanFullyPaid,
                isActive = !isLoanFullyPaid,
                paidEmis = loan.paidEmis + 1,
                updatedAt = paymentDate
            )
        )

        emiDao.markEmiAsPaid(emi.id, paymentDate)

        if (!isLoanFullyPaid) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = emi.nextDueDate
            calendar.add(Calendar.MONTH, 1)
            emiDao.updateEmiDueDate(emi.id, calendar.timeInMillis, paymentDate)
        }

        val transactionType = if (loan.loanType == LoanType.BORROWED) {
            TransactionType.LOAN_REPAY
        } else {
            TransactionType.LOAN_RECEIVE
        }

        val transaction = FinancialTransaction(
            type = transactionType,
            amount = emi.amount,
            sourceAccountId = if (loan.loanType == LoanType.BORROWED) emi.paymentAccountId else 0,
            destinationAccountId = if (loan.loanType == LoanType.BORROWED) 0 else emi.paymentAccountId,
            note = buildString {
                append("EMI payment for ${loan.personName}")
                if (emi.interestAmount > 0) append(" (Principal: ${emi.principalAmount}, Interest: ${emi.interestAmount})")
                if (emi.penaltyAmount > 0) append(" (Includes penalty: ${emi.penaltyAmount})")
            },
            date = paymentDate,
            relatedLoanId = loan.id
        )
        transactionDao.insertTransaction(transaction)
    }

    suspend fun skipEmi(emi: Emi) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = emi.nextDueDate
        calendar.add(Calendar.MONTH, 1)
        emiDao.updateEmiDueDate(emi.id, calendar.timeInMillis)
        emiDao.skipEmi(emi.id)
    }

    suspend fun getEmiWithLoan(emiId: Int): Pair<Emi?, Loan?> {
        val emi = emiDao.getEmiById(emiId).first()
        val loan = emi?.let { loanDao.getLoanById(it.loanId).first() }
        return Pair(emi, loan)
    }

    suspend fun getUpcomingEmisWithLoans(): List<Pair<Emi, Loan>> {
        val emis = emiDao.getActiveEmis().first()
        return emis.mapNotNull { emi ->
            val loan = loanDao.getLoanById(emi.loanId).first()
            if (loan != null) Pair(emi, loan) else null
        }
    }
}
