package com.rudra.smartworktracker.engine

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class FusionEngine(
    private val accountDao: AccountDao,
    private val financialTransactionDao: FinancialTransactionDao
) {
    suspend fun processTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String?
    ): FusionResult {
        val fromAccount = accountDao.getAccountById(fromAccountId)
            ?: return FusionResult.Error("Source account not found")

        val toAccount = accountDao.getAccountById(toAccountId)
            ?: return FusionResult.Error("Destination account not found")

        if (fromAccount.balance < amount) {
            return FusionResult.Error("Insufficient balance in ${fromAccount.name}")
        }

        val effectiveLimit = fromAccount.getEffectiveLimit()
        if (effectiveLimit != null) {
            val todayTotal = getTodayTransferTotal(fromAccountId)
            if (todayTotal + amount > effectiveLimit) {
                return FusionResult.Error("Daily limit exceeded (${effectiveLimit.toInt()} BDT)")
            }
        }

        val timestamp = System.currentTimeMillis()
        accountDao.updateBalance(fromAccountId, fromAccount.balance - amount, timestamp)
        accountDao.updateBalance(toAccountId, toAccount.balance + amount, timestamp)

        val transaction = FinancialTransaction(
            type = TransactionType.TRANSFER,
            amount = amount,
            sourceAccountId = fromAccountId,
            destinationAccountId = toAccountId,
            note = note ?: "Transfer from ${fromAccount.name} to ${toAccount.name}",
            date = timestamp
        )
        financialTransactionDao.insertTransaction(transaction)

        updateInsights(fromAccount, toAccount, amount)
        checkGoalProgress(toAccount, amount)

        return FusionResult.Success(
            fromAccount = fromAccount.copy(balance = fromAccount.balance - amount),
            toAccount = toAccount.copy(balance = toAccount.balance + amount),
            transaction = transaction
        )
    }

    suspend fun getTodayTransferTotal(accountId: Long): Double {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val transactions = financialTransactionDao.getAllTransactions().first()
        return transactions
            .filter { it.date >= today && it.sourceAccountId == accountId }
            .sumOf { it.amount }
    }

    private suspend fun updateInsights(fromAccount: Account, toAccount: Account, amount: Double) {
        val insights = mutableListOf<String>()
        
        if (amount > 10000) {
            insights.add("Large transfer detected: $amount BDT")
        }

        val totalTransfersToday = getTodayTransferTotal(fromAccount.id)
        if (totalTransfersToday > 5) {
            insights.add("High transfer frequency: ${totalTransfersToday.toInt()} transfers today")
        }

        val accountUsage = getAccountUsageRanking()
        if (accountUsage.firstOrNull()?.id == fromAccount.id) {
            insights.add("Most used account: ${fromAccount.name}")
        }
    }

    private suspend fun getAccountUsageRanking(): List<Account> {
        return accountDao.getAllAccountsList()
            .sortedByDescending { it.lastUpdated }
    }

    private suspend fun checkGoalProgress(account: Account, amount: Double) {
        account.linkedGoalId?.let { goalId ->
            val linkedAccounts = accountDao.getAccountsLinkedToGoal(goalId).first()
            val totalInGoal = linkedAccounts.sumOf { it.balance }
        }
    }

    fun getNetWorth(): Flow<Double?> {
        return accountDao.getTotalBalance()
    }

    fun getNetWorthByCategory(): Flow<Map<AccountCategory, Double>> {
        return accountDao.getTotalBalance().let { balanceFlow ->
            kotlinx.coroutines.flow.flow {
                val allAccounts = accountDao.getAllAccountsList()
                val categoryTotals = AccountCategory.entries.associateWith { category ->
                    allAccounts.filter { it.type == category }.sumOf { it.balance }
                }
                emit(categoryTotals)
            }
        }
    }

    suspend fun getSmartAlerts(): List<SmartAlert> {
        val alerts = mutableListOf<SmartAlert>()
        val accounts = accountDao.getAllAccountsList()
        
        accounts.forEach { account ->
            if (account.balance < 500 && account.type == AccountCategory.MOBILE_BANKING) {
                alerts.add(
                    SmartAlert.LowBalance(
                        accountName = account.name,
                        balance = account.balance,
                        message = "Your ${account.name} balance is low (${account.balance.toInt()} BDT)"
                    )
                )
            }

            if (account.hasLimit && account.dailyTransferLimit != null) {
                val todayTransferred = getTodayTransferTotal(account.id)
                if (todayTransferred > account.dailyTransferLimit * 0.8) {
                    alerts.add(
                        SmartAlert.ApproachingLimit(
                            accountName = account.name,
                            used = todayTransferred,
                            limit = account.dailyTransferLimit,
                            message = "Approaching daily limit: ${todayTransferred.toInt()}/${account.dailyTransferLimit.toInt()} BDT"
                        )
                    )
                }
            }
        }

        return alerts
    }

    suspend fun getTransferInsights(): TransferInsights {
        val transactions = financialTransactionDao.getAllTransactions().first()
        val accounts = accountDao.getAllAccountsList()

        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val monthlyTransfers = transactions.filter { it.date >= thisMonth && it.type == TransactionType.TRANSFER }
        val totalTransferred = monthlyTransfers.sumOf { it.amount }
        val transferCount = monthlyTransfers.size
        val avgTransferAmount = if (transferCount > 0) totalTransferred / transferCount else 0.0

        return TransferInsights(
            totalTransferredThisMonth = totalTransferred,
            transferCountThisMonth = transferCount,
            averageTransferAmount = avgTransferAmount,
            mostUsedAccount = accounts.maxByOrNull { it.lastUpdated }?.name ?: "None"
        )
    }

}

sealed class FusionResult {
    data class Success(
        val fromAccount: Account,
        val toAccount: Account,
        val transaction: FinancialTransaction
    ) : FusionResult()
    
    data class Error(val message: String) : FusionResult()
}

sealed class SmartAlert {
    data class LowBalance(
        val accountName: String,
        val balance: Double,
        val message: String
    ) : SmartAlert()

    data class ApproachingLimit(
        val accountName: String,
        val used: Double,
        val limit: Double,
        val message: String
    ) : SmartAlert()

    data class HighSpending(
        val accountName: String,
        val message: String
    ) : SmartAlert()

    data class TransferHabit(
        val message: String
    ) : SmartAlert()
}

data class TransferInsights(
    val totalTransferredThisMonth: Double,
    val transferCountThisMonth: Int,
    val averageTransferAmount: Double,
    val mostUsedAccount: String
)