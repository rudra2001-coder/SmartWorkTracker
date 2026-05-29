package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.AccountDao
import com.rudra.smartworktracker.data.dao.IncomeDao
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.IncomeByCategory
import kotlinx.coroutines.flow.Flow

class IncomeRepository(
    private val incomeDao: IncomeDao,
    private val accountDao: AccountDao
) {

    fun getIncomes(page: Int, pageSize: Int): Flow<List<Income>> {
        val offset = (page - 1) * pageSize
        return incomeDao.getPaginatedIncomes(offset, pageSize)
    }

    suspend fun insertIncome(income: Income) {
        incomeDao.insertIncome(income)
        
        if (income.accountId > 0) {
            val account = accountDao.getAccountById(income.accountId)
            account?.let {
                val newBalance = it.balance + income.amount
                accountDao.updateBalance(income.accountId, newBalance)
            }
        }
    }

    suspend fun deleteIncome(income: Income) {
        if (income.accountId > 0) {
            val account = accountDao.getAccountById(income.accountId)
            if (account != null) {
                accountDao.updateBalance(income.accountId, account.balance - income.amount)
            }
        }
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(incomeId: Long) {
        val income = incomeDao.getIncomeById(incomeId)
        income?.let {
            if (it.accountId > 0) {
                val account = accountDao.getAccountById(it.accountId)
                account?.let { acc ->
                    accountDao.updateBalance(it.accountId, acc.balance - it.amount)
                }
            }
            incomeDao.deleteIncomeById(incomeId)
        }
    }

    fun getIncomesBetween(startTime: Long, endTime: Long): Flow<List<Income>> {
        return incomeDao.getIncomesBetween(startTime, endTime)
    }

    fun getTotalIncomeBetween(startTime: Long, endTime: Long): Flow<Double?> {
        return incomeDao.getTotalIncomeBetween(startTime, endTime)
    }

    fun getTotalIncomeBefore(endTime: Long): Flow<Double?> {
        return incomeDao.getTotalIncomeBefore(endTime)
    }

    fun getTotalIncomeUpTo(endTime: Long): Flow<Double?> {
        return incomeDao.getTotalIncomeUpTo(endTime)
    }

    fun getTotalIncome(): Flow<Double?> {
        return incomeDao.getTotalIncome()
    }

    fun getIncomesByCategoryBetween(startTime: Long, endTime: Long): Flow<List<IncomeByCategory>> {
        return incomeDao.getIncomesByCategoryBetween(startTime, endTime)
    }
    
    fun getAllIncomes(): Flow<List<Income>> {
        return incomeDao.getAllIncomes()
    }

    suspend fun clearAll() {
        incomeDao.deleteAll()
    }
}
