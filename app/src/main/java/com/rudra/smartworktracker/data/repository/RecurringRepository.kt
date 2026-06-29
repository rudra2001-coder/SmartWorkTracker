package com.rudra.smartworktracker.data.repository

import com.rudra.smartworktracker.data.dao.RecurringRuleDao
import com.rudra.smartworktracker.data.dao.RecurringTransactionDao
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

class RecurringRepository(
    private val recurringRuleDao: RecurringRuleDao,
    private val recurringTransactionDao: RecurringTransactionDao
) {
    suspend fun insertRule(rule: RecurringRule): Long = recurringRuleDao.insertRule(rule)

    suspend fun updateRule(rule: RecurringRule) = recurringRuleDao.updateRule(rule)

    suspend fun deleteRule(rule: RecurringRule) = recurringRuleDao.deleteRule(rule)

    suspend fun deleteRuleById(ruleId: Long) = recurringRuleDao.deleteRuleById(ruleId)

    suspend fun getRuleById(ruleId: Long): RecurringRule? = recurringRuleDao.getRuleById(ruleId)

    fun getRuleByIdFlow(ruleId: Long): Flow<RecurringRule?> = recurringRuleDao.getRuleByIdFlow(ruleId)

    fun getAllRules(): Flow<List<RecurringRule>> = recurringRuleDao.getAllRules()

    fun getActiveRules(): Flow<List<RecurringRule>> = recurringRuleDao.getActiveRules()

    fun getPausedRules(): Flow<List<RecurringRule>> = recurringRuleDao.getPausedRules()

    suspend fun getRulesDueForExecution(timestamp: Long): List<RecurringRule> =
        recurringRuleDao.getRulesDueForExecution(timestamp)

    fun getRulesByType(type: TransactionType): Flow<List<RecurringRule>> =
        recurringRuleDao.getRulesByType(type)

    fun getRulesBetweenDates(startDate: Long, endDate: Long): Flow<List<RecurringRule>> =
        recurringRuleDao.getRulesBetweenDates(startDate, endDate)

    suspend fun updateRuleActiveStatus(ruleId: Long, isActive: Boolean) =
        recurringRuleDao.updateRuleActiveStatus(ruleId, isActive)

    suspend fun updateRulePausedStatus(ruleId: Long, isPaused: Boolean) =
        recurringRuleDao.updateRulePausedStatus(ruleId, isPaused)

    suspend fun updateNextExecutionDate(ruleId: Long, nextDate: Long) =
        recurringRuleDao.updateNextExecutionDate(ruleId, nextDate)

    suspend fun updateExecutionStats(ruleId: Long, count: Int, totalAmount: Double, lastDate: Long, nextDate: Long) =
        recurringRuleDao.updateExecutionStats(ruleId, count, totalAmount, lastDate, nextDate)

    fun getActiveRulesCount(): Flow<Int> = recurringRuleDao.getActiveRulesCount()

    fun getPausedRulesCount(): Flow<Int> = recurringRuleDao.getPausedRulesCount()

    fun getTotalAmountByType(type: TransactionType): Flow<Double?> =
        recurringRuleDao.getTotalAmountByType(type)

    fun getTotalExecutedAmount(): Flow<Double?> = recurringRuleDao.getTotalExecutedAmount()

    fun searchRules(query: String): Flow<List<RecurringRule>> = recurringRuleDao.searchRules(query)

    fun getRulesByTag(tag: String): Flow<List<RecurringRule>> = recurringRuleDao.getRulesByTag(tag)

    suspend fun getRulesThatReachedMaxExecutions(): List<RecurringRule> =
        recurringRuleDao.getRulesThatReachedMaxExecutions()

    suspend fun getRulesPastEndDate(timestamp: Long): List<RecurringRule> =
        recurringRuleDao.getRulesPastEndDate(timestamp)

    suspend fun insertTransaction(transaction: RecurringTransaction): Long =
        recurringTransactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: RecurringTransaction) =
        recurringTransactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: RecurringTransaction) =
        recurringTransactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(transactionId: Long) =
        recurringTransactionDao.deleteTransactionById(transactionId)

    suspend fun getTransactionById(transactionId: Long): RecurringTransaction? =
        recurringTransactionDao.getTransactionById(transactionId)

    fun getTransactionByIdFlow(transactionId: Long): Flow<RecurringTransaction?> =
        recurringTransactionDao.getTransactionByIdFlow(transactionId)

    fun getTransactionsByRuleId(ruleId: Long): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getTransactionsByRuleId(ruleId)

    fun getAllTransactions(): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getAllTransactions()

    fun getTransactionsByStatus(status: RecurringTransactionStatus): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getTransactionsByStatus(status)

    suspend fun getTransactionsDueForExecution(status: RecurringTransactionStatus, timestamp: Long): List<RecurringTransaction> =
        recurringTransactionDao.getTransactionsDueForExecution(status, timestamp)

    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.getTransactionsBetweenDates(startDate, endDate)

    suspend fun getPendingTransactionsDue(timestamp: Long): List<RecurringTransaction> =
        recurringTransactionDao.getPendingTransactionsDue(timestamp)

    suspend fun updateTransactionStatus(transactionId: Long, status: RecurringTransactionStatus) =
        recurringTransactionDao.updateTransactionStatus(transactionId, status)

    suspend fun markTransactionExecuted(transactionId: Long, status: RecurringTransactionStatus = RecurringTransactionStatus.EXECUTED) =
        recurringTransactionDao.markTransactionExecuted(transactionId, status)

    suspend fun markTransactionFailed(transactionId: Long, reason: String?) =
        recurringTransactionDao.markTransactionFailed(transactionId, reason = reason)

    suspend fun skipTransaction(transactionId: Long, reason: String?) =
        recurringTransactionDao.skipTransaction(transactionId, reason = reason)

    fun getTransactionCountByStatus(status: RecurringTransactionStatus): Flow<Int> =
        recurringTransactionDao.getTransactionCountByStatus(status)

    fun getTotalExecutedAmountByType(type: TransactionType, startDate: Long, endDate: Long): Flow<Double?> =
        recurringTransactionDao.getTotalExecutedAmountByType(type.name, startDate, endDate)

    fun getExecutedTransactionCount(startDate: Long, endDate: Long): Flow<Int> =
        recurringTransactionDao.getExecutedTransactionCount(startDate, endDate)

    fun getFailedTransactionCount(startDate: Long, endDate: Long): Flow<Int> =
        recurringTransactionDao.getFailedTransactionCount(startDate, endDate)

    fun searchTransactions(query: String): Flow<List<RecurringTransaction>> =
        recurringTransactionDao.searchTransactions(query)

    suspend fun deleteTransactionsByRuleId(ruleId: Long) =
        recurringTransactionDao.deleteTransactionsByRuleId(ruleId)

    suspend fun getTransactionByRelatedIds(incomeId: Long?, expenseId: Long?, transactionId: Int?): RecurringTransaction? =
        recurringTransactionDao.getTransactionByRelatedIds(incomeId, expenseId, transactionId)

    suspend fun updateLastCheckedTimestamp(ruleId: Long, timestamp: Long) =
        recurringRuleDao.updateLastCheckedTimestamp(ruleId, timestamp)

    suspend fun updateNextExecutionAndChecked(ruleId: Long, nextDate: Long, checkedTimestamp: Long) =
        recurringRuleDao.updateNextExecutionAndChecked(ruleId, nextDate, checkedTimestamp)

    suspend fun getActiveRulesByFrequency(frequency: RecurringFrequency): List<RecurringRule> =
        recurringRuleDao.getActiveRulesByFrequency(frequency)

    fun getMonthlySpecificDayRules(frequency: RecurringFrequency = RecurringFrequency.MONTHLY_SPECIFIC_DAYS): Flow<List<RecurringRule>> =
        recurringRuleDao.getMonthlySpecificDayRules(frequency)

    suspend fun getPendingRetryRules(): List<RecurringRule> =
        recurringRuleDao.getPendingRetryRules()

    suspend fun updateRetryState(ruleId: Long, pendingRetry: Boolean, retryCount: Int, nextDate: Long) =
        recurringRuleDao.updateRetryState(ruleId, pendingRetry, retryCount, nextDate)

    suspend fun clearRetryState(ruleId: Long) =
        recurringRuleDao.clearRetryState(ruleId)
}
