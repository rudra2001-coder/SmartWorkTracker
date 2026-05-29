package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.dao.ExpenseDao
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseByCategory
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val accountDao: AccountDao
) {

    fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
    }

    fun getExpenses(page: Int, pageSize: Int): Flow<List<Expense>> {
        val offset = (page - 1) * pageSize
        return expenseDao.getPaginatedExpenses(offset, pageSize)
    }

    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesBetween(startTime, endTime)
    }

    fun getMealExpensesBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return expenseDao.getMealExpensesBetween(startTime, endTime)
    }

    fun getTotalExpensesBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesBetween(startTime, endTime)
    }

    fun getTotalExpensesBefore(endTime: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesBefore(endTime)
    }

    fun getTotalExpensesUpTo(endTime: Long): Flow<Double?> {
        return expenseDao.getTotalExpensesUpTo(endTime)
    }

    fun getTotalExpenses(): Flow<Double?> {
        return expenseDao.getTotalExpenses()
    }

    fun getExpensesByCategoryBetween(startTime: Long, endTime: Long): Flow<List<ExpenseByCategory>> {
        return expenseDao.getExpensesByCategoryBetween(startTime, endTime)
    }

    suspend fun insertExpense(expense: Expense) {
        if (expense.accountId > 0) {
            val account = accountDao.getAccountById(expense.accountId)
                ?: throw IllegalStateException("Account not found")
            if (account.balance < expense.amount) {
                throw IllegalStateException(
                    "Insufficient balance in ${account.name}. " +
                    "Current balance: ৳${"%,.0f".format(account.balance)}, " +
                    "Required: ৳${"%,.0f".format(expense.amount)}"
                )
            }
            accountDao.updateBalance(expense.accountId, account.balance - expense.amount)
        }
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
        
        if (expense.accountId > 0) {
            val account = accountDao.getAccountById(expense.accountId)
            account?.let {
                val newBalance = it.balance + expense.amount
                accountDao.updateBalance(expense.accountId, newBalance)
            }
        }
    }

    suspend fun deleteExpenseById(expenseId: String) {
        val expense = expenseDao.getExpenseById(expenseId)
        expense?.let {
            expenseDao.deleteExpenseById(expenseId)
            
            if (it.accountId > 0) {
                val account = accountDao.getAccountById(it.accountId)
                account?.let { acc ->
                    val newBalance = acc.balance + it.amount
                    accountDao.updateBalance(it.accountId, newBalance)
                }
            }
        }
    }

    suspend fun clearAll() {
        expenseDao.deleteAll()
    }
}
