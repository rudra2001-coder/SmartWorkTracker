package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.dao.FinancialTransactionDao
import com.rudra.smartworktracker.data.dao.SavingsDao
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class SavingsRepository(
    private val savingsDao: SavingsDao,
    private val accountDao: AccountDao,
    private val financialTransactionDao: FinancialTransactionDao
) {

    fun getSavings(): Flow<Double> = savingsDao.getTotalSavings()

    fun getSavingsHistory(): Flow<List<Savings>> = savingsDao.getSavingsHistory()

    suspend fun getAllAccounts(): List<Account> = accountDao.getAllAccountsList()

    suspend fun addToSavings(amount: Double, note: String = "", category: String = "Deposit", accountId: Long = 0) {
        if (accountId > 0) {
            val account = accountDao.getAccountById(accountId)
                ?: throw IllegalStateException("Account not found")
            if (account.balance < amount) {
                throw IllegalStateException(
                    "Insufficient balance in ${account.name}. " +
                    "Current balance: ৳${"%,.0f".format(account.balance)}, " +
                    "Required: ৳${"%,.0f".format(amount)}"
                )
            }
            accountDao.updateBalance(accountId, account.balance - amount)

            financialTransactionDao.insertTransaction(
                FinancialTransaction(
                    type = TransactionType.SAVINGS_ADD,
                    amount = amount,
                    sourceAccountId = accountId,
                    destinationAccountId = 0,
                    note = note.ifBlank { "Savings deposit" },
                    date = System.currentTimeMillis()
                )
            )
        }

        val savings = Savings(
            amount = amount,
            note = note,
            category = category,
            timestamp = System.currentTimeMillis(),
            accountId = accountId
        )
        savingsDao.insert(savings)
    }

    suspend fun withdrawFromSavings(amount: Double, note: String = "", category: String = "Withdrawal", accountId: Long = 0) {
        if (accountId > 0) {
            val account = accountDao.getAccountById(accountId)
                ?: throw IllegalStateException("Account not found")
            accountDao.updateBalance(accountId, account.balance + amount)

            financialTransactionDao.insertTransaction(
                FinancialTransaction(
                    type = TransactionType.SAVINGS_WITHDRAW,
                    amount = amount,
                    sourceAccountId = 0,
                    destinationAccountId = accountId,
                    note = note.ifBlank { "Savings withdrawal" },
                    date = System.currentTimeMillis()
                )
            )
        }

        val savings = Savings(
            amount = -amount,
            note = note,
            category = category,
            timestamp = System.currentTimeMillis(),
            accountId = accountId
        )
        savingsDao.insert(savings)
    }

    suspend fun deleteTransaction(savings: Savings) {
        if (savings.accountId > 0) {
            val account = accountDao.getAccountById(savings.accountId)
            if (account != null) {
                val reverseBalance = account.balance + savings.amount
                if (savings.amount < 0 && account.balance < -savings.amount) {
                    throw IllegalStateException(
                        "Insufficient balance in ${account.name} to reverse withdrawal. " +
                        "Current balance: ৳${"%,.0f".format(account.balance)}"
                    )
                }
                accountDao.updateBalance(savings.accountId, reverseBalance)
            }
        }
        savingsDao.delete(savings)
    }

    fun getSavingsBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return savingsDao.getSavingsBetween(startTime, endTime)
    }

    fun getSavingsByCategory(category: String): Flow<List<Savings>> {
        return savingsDao.getSavingsByCategory(category)
    }

    fun searchSavings(query: String): Flow<List<Savings>> {
        return savingsDao.searchSavings(query)
    }

    fun getSavingsSince(startTime: Long): Flow<List<Savings>> {
        return savingsDao.getSavingsSince(startTime)
    }

    suspend fun clearAll() {
        savingsDao.deleteAll()
    }
}
