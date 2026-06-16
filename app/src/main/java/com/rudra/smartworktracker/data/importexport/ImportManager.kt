package com.rudra.smartworktracker.data.importexport

import android.content.Context
import android.net.Uri
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanCategory
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.Savings
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.model.Colleague
import com.rudra.smartworktracker.model.DailyJournal
import com.rudra.smartworktracker.model.Expense
import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.Habit
import com.rudra.smartworktracker.model.HabitDifficulty
import com.rudra.smartworktracker.model.HealthMetric
import com.rudra.smartworktracker.model.HealthMetricType
import com.rudra.smartworktracker.data.entity.Income
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.model.WorkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

data class ImportResult(
    val type: ImportEntityType,
    val totalRows: Int,
    val successCount: Int,
    val errorCount: Int,
    val errors: List<ImportError>
)

data class ImportError(
    val row: Int,
    val message: String
)

class ImportManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun parseCsv(uri: Uri): List<Map<String, String>> = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            CsvParser.parse(stream)
        } ?: emptyList()
    }

    suspend fun parseExcel(uri: Uri): List<Map<String, String>> = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ExcelParser.parse(stream)
        } ?: emptyList()
    }

    suspend fun importData(
        type: ImportEntityType,
        rows: List<Map<String, String>>
    ): ImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<ImportError>()
        var successCount = 0
        val totalRows = rows.size

        // Get existing accounts for lookup
        val accounts = db.accountDao().getAllAccountsList()
        val accountMap = accounts.associateBy { it.name.lowercase().trim() }

        for ((index, row) in rows.withIndex()) {
            try {
                val rowNum = index + 2 // 1-indexed + header row
                when (type) {
                    ImportEntityType.EXPENSE -> insertExpense(row, accountMap, rowNum)
                    ImportEntityType.INCOME -> insertIncome(row, accountMap, rowNum)
                    ImportEntityType.WORK_LOG -> insertWorkLog(row, rowNum)
                    ImportEntityType.ACCOUNT -> insertAccount(row, rowNum)
                    ImportEntityType.LOAN -> insertLoan(row, accountMap, rowNum)
                    ImportEntityType.SAVINGS -> insertSavings(row, accountMap, rowNum)
                    ImportEntityType.HABIT -> insertHabit(row, rowNum)
                    ImportEntityType.HEALTH_METRIC -> insertHealthMetric(row, rowNum)
                    ImportEntityType.DAILY_JOURNAL -> insertDailyJournal(row, rowNum)
                    ImportEntityType.CREDIT_CARD -> insertCreditCard(row, accountMap, rowNum)
                    ImportEntityType.RECURRING_RULE -> insertRecurringRule(row, accountMap, rowNum)
                    ImportEntityType.COLLEAGUE -> insertColleague(row, rowNum)
                    ImportEntityType.FINANCIAL_TRANSACTION -> insertFinancialTransaction(row, accountMap, rowNum)
                }
                successCount++
            } catch (e: Exception) {
                errors.add(ImportError(row = index + 2, message = e.localizedMessage ?: "Unknown error"))
            }
        }

        ImportResult(
            type = type,
            totalRows = totalRows,
            successCount = successCount,
            errorCount = errors.size,
            errors = errors
        )
    }

    private suspend fun insertExpense(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val categoryStr = requireField(row, "category", rowNum)
        val accountName = requireField(row, "accountName", rowNum)

        val date = parseDate(dateStr)
        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val category = tryParseCategory(categoryStr)
        val account = resolveAccount(accountName, accounts, rowNum)
        val merchant = row["merchant"]?.takeIf { it.isNotBlank() }
        val notes = row["notes"]?.takeIf { it.isNotBlank() }

        // Update account balance
        val newBalance = account.balance - amount
        if (newBalance < 0) throw IllegalArgumentException("Row $rowNum: Insufficient balance in '${account.name}' (${account.balance}) for expense $amount")
        db.accountDao().updateBalance(account.id, newBalance)

        val expense = Expense(
            amount = amount,
            category = category,
            merchant = merchant,
            notes = notes,
            timestamp = date.time,
            accountId = account.id,
            currency = "BDT"
        )
        db.expenseDao().insertExpense(expense)
    }

    private suspend fun insertIncome(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val accountName = requireField(row, "accountName", rowNum)

        val date = parseDate(dateStr)
        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val account = resolveAccount(accountName, accounts, rowNum)
        val source = row["source"]?.takeIf { it.isNotBlank() } ?: "Import"
        val description = row["description"]?.takeIf { it.isNotBlank() } ?: source
        val category = row["category"]?.takeIf { it.isNotBlank() } ?: "General"

        // Update account balance
        val newBalance = account.balance + amount
        db.accountDao().updateBalance(account.id, newBalance)

        val income = Income(
            amount = amount,
            description = description,
            category = category,
            timestamp = date.time,
            source = source,
            accountId = account.id
        )
        db.incomeDao().insertIncome(income)
    }

    private suspend fun insertWorkLog(row: Map<String, String>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val workTypeStr = requireField(row, "workType", rowNum)

        val date = parseDate(dateStr)
        val workType = parseEnum<WorkType>(workTypeStr)
            ?: throw IllegalArgumentException("Row $rowNum: Invalid workType '$workTypeStr'. Valid: ${enumNames<WorkType>()}")
        val startTime = row["startTime"]?.takeIf { it.isNotBlank() }
        val endTime = row["endTime"]?.takeIf { it.isNotBlank() }
        val isOvertime = row["isOvertime"]?.lowercase()?.let { it == "true" || it == "yes" || it == "1" } ?: false
        val overtimeRate = row["overtimeRate"]?.toDoubleOrNull()

        val workLog = WorkLog(
            date = date,
            workType = workType,
            startTime = startTime,
            endTime = endTime,
            isOvertime = isOvertime,
            overtimeRate = overtimeRate
        )
        db.workLogDao().insertWorkLog(workLog)
    }

    private suspend fun insertAccount(row: Map<String, String>, rowNum: Int) {
        val name = requireField(row, "name", rowNum)
        val typeStr = requireField(row, "type", rowNum)

        val type = parseEnum<AccountCategory>(typeStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid type '$typeStr'. Valid: ${enumNames<AccountCategory>()}")
        val provider = row["provider"]?.takeIf { it.isNotBlank() }?.let {
            parseEnum<AccountProvider>(it) ?: AccountProvider.OTHER
        } ?: AccountProvider.OTHER
        val balance = row["balance"]?.toDoubleOrNull() ?: 0.0
        val accountNumber = row["accountNumber"]?.takeIf { it.isNotBlank() } ?: ""
        val notes = row["notes"]?.takeIf { it.isNotBlank() }

        val account = Account(
            name = name,
            type = type,
            provider = provider,
            accountNumber = accountNumber,
            balance = balance,
            notes = notes,
            currency = "BDT"
        )
        db.accountDao().insertAccount(account)
    }

    private suspend fun insertLoan(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val personName = requireField(row, "personName", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val loanTypeStr = requireField(row, "loanType", rowNum)
        val accountName = requireField(row, "accountName", rowNum)

        val date = parseDate(dateStr)
        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val loanType = parseEnum<LoanType>(loanTypeStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid loanType '$loanTypeStr'. Valid: ${LoanType.entries.joinToString(", ")}")
        val account = resolveAccount(accountName, accounts, rowNum)
        val loanCategory = row["loanCategory"]?.takeIf { it.isNotBlank() }?.let {
            parseEnum<LoanCategory>(it) ?: LoanCategory.PERSONAL
        } ?: LoanCategory.PERSONAL
        val dueDateStr = row["dueDate"]?.takeIf { it.isNotBlank() }
        val dueDate = dueDateStr?.let { parseDate(it).time }
        val notes = row["notes"]?.takeIf { it.isNotBlank() }

        // Update account balance
        when (loanType) {
            LoanType.BORROWED -> {
                val newBalance = account.balance + amount
                db.accountDao().updateBalance(account.id, newBalance)
            }
            LoanType.LENT -> {
                val newBalance = account.balance - amount
                if (newBalance < 0) throw IllegalArgumentException("Row $rowNum: Insufficient balance in '${account.name}' (${account.balance}) for LENT loan $amount")
                db.accountDao().updateBalance(account.id, newBalance)
            }
        }

        val loan = Loan(
            personName = personName,
            initialAmount = amount,
            remainingAmount = amount,
            loanType = loanType,
            loanCategory = loanCategory,
            date = date.time,
            dueDate = dueDate,
            notes = notes,
            accountId = account.id,
            isActive = true,
            isFullyPaid = false
        )
        db.loanDao().insertLoan(loan)
    }

    private suspend fun insertSavings(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val accountName = requireField(row, "accountName", rowNum)

        val date = parseDate(dateStr)
        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val account = resolveAccount(accountName, accounts, rowNum)
        val category = row["category"]?.takeIf { it.isNotBlank() } ?: "General"
        val note = row["note"]?.takeIf { it.isNotBlank() } ?: ""

        // Update account balance: positive = deposit (deduct), negative = withdrawal (add)
        if (amount > 0) {
            val newBalance = account.balance - amount
            if (newBalance < 0) throw IllegalArgumentException("Row $rowNum: Insufficient balance in '${account.name}' (${account.balance}) for savings deposit $amount")
            db.accountDao().updateBalance(account.id, newBalance)
        } else {
            val newBalance = account.balance + (-amount)
            db.accountDao().updateBalance(account.id, newBalance)
        }

        val savings = Savings(
            amount = amount,
            note = note,
            category = category,
            timestamp = date.time,
            accountId = account.id
        )
        db.savingsDao().insert(savings)
    }

    private suspend fun insertHabit(row: Map<String, String>, rowNum: Int) {
        val name = requireField(row, "name", rowNum)
        val description = row["description"]?.takeIf { it.isNotBlank() } ?: ""
        val difficulty = row["difficulty"]?.takeIf { it.isNotBlank() }?.let {
            parseEnum<HabitDifficulty>(it) ?: HabitDifficulty.MEDIUM
        } ?: HabitDifficulty.MEDIUM

        val habit = Habit(
            name = name,
            description = description,
            difficulty = difficulty
        )
        db.habitDao().insertHabit(habit)
    }

    private suspend fun insertHealthMetric(row: Map<String, String>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val metricTypeStr = requireField(row, "metricType", rowNum)
        val valueStr = requireField(row, "value", rowNum)

        val date = parseDate(dateStr)
        val metricType = parseEnum<HealthMetricType>(metricTypeStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid metricType '$metricTypeStr'")
        val value = valueStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid value '$valueStr'")
        val notes = row["notes"]?.takeIf { it.isNotBlank() }

        val metric = HealthMetric(
            type = metricType,
            value = value,
            timestamp = date.time,
            notes = notes
        )
        db.healthMetricDao().insertHealthMetric(metric)
    }

    private suspend fun insertDailyJournal(row: Map<String, String>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val date = try { LocalDate.parse(dateStr) } catch (e: Exception) {
            throw IllegalArgumentException("Row $rowNum: Invalid date '$dateStr'. Use YYYY-MM-DD format")
        }
        val morningIntention = row["morningIntention"]?.takeIf { it.isNotBlank() } ?: ""
        val gratitude = row["gratitude"]?.takeIf { it.isNotBlank() } ?: ""
        val eveningReflection = row["eveningReflection"]?.takeIf { it.isNotBlank() } ?: ""

        val journal = DailyJournal(
            date = date,
            morningIntention = morningIntention,
            gratitude = gratitude,
            eveningReflection = eveningReflection
        )
        db.dailyJournalDao().insertJournal(journal)
    }

    private suspend fun insertCreditCard(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val cardName = requireField(row, "cardName", rowNum)
        val cardLimitStr = requireField(row, "cardLimit", rowNum)
        val accountName = requireField(row, "accountName", rowNum)

        val cardLimit = cardLimitStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid cardLimit '$cardLimitStr'")
        val account = resolveAccount(accountName, accounts, rowNum)
        val cardNumber = row["cardNumber"]?.takeIf { it.isNotBlank() } ?: ""
        val statementDate = row["statementDate"]?.toIntOrNull() ?: 1
        val dueDate = row["dueDate"]?.toIntOrNull() ?: 15

        val creditCard = CreditCard(
            cardName = cardName,
            cardNumber = cardNumber,
            cardLimit = cardLimit,
            statementDate = statementDate,
            dueDate = dueDate,
            accountId = account.id
        )
        db.creditCardDao().insertCard(creditCard)
    }

    private suspend fun insertRecurringRule(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val name = requireField(row, "name", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val txTypeStr = requireField(row, "transactionType", rowNum)
        val frequencyStr = requireField(row, "frequency", rowNum)
        val startDateStr = requireField(row, "startDate", rowNum)

        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val transactionType = parseEnum<TransactionType>(txTypeStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid transactionType '$txTypeStr'")
        val frequency = parseEnum<RecurringFrequency>(frequencyStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid frequency '$frequencyStr'")
        val startDate = parseDate(startDateStr)

        val sourceAccountName = row["sourceAccountName"]?.takeIf { it.isNotBlank() }
        val destinationAccountName = row["destinationAccountName"]?.takeIf { it.isNotBlank() }
        val sourceId = sourceAccountName?.let { resolveAccount(it, accounts, rowNum).id } ?: 0L
        val destId = destinationAccountName?.let { resolveAccount(it, accounts, rowNum).id }
        val notes = row["notes"]?.takeIf { it.isNotBlank() }

        val rule = RecurringRule(
            name = name,
            transactionType = transactionType,
            amount = amount,
            frequency = frequency,
            startDate = startDate.time,
            nextExecutionDate = startDate.time,
            sourceAccountId = sourceId,
            destinationAccountId = destId,
            notes = notes,
            isActive = true
        )
        db.recurringRuleDao().insertRule(rule)
    }

    private suspend fun insertColleague(row: Map<String, String>, rowNum: Int) {
        val name = requireField(row, "name", rowNum)
        val designation = row["designation"]?.takeIf { it.isNotBlank() } ?: ""
        val department = row["department"]?.takeIf { it.isNotBlank() } ?: ""
        val phone = row["phone"]?.takeIf { it.isNotBlank() } ?: ""
        val email = row["email"]?.takeIf { it.isNotBlank() } ?: ""

        val colleague = Colleague(
            fullName = name,
            designation = designation,
            department = department,
            workEmail = email,
            phoneNumber = phone,
            workLocation = "",
            joiningDate = LocalDate.now(),
            reportingManager = "",
            workingShift = "",
            skillTags = emptyList(),
            strengths = "",
            relationshipType = "Colleague"
        )
        db.colleagueDao().insertColleague(colleague)
    }

    private suspend fun insertFinancialTransaction(row: Map<String, String>, accounts: Map<String, Account>, rowNum: Int) {
        val dateStr = requireField(row, "date", rowNum)
        val amountStr = requireField(row, "amount", rowNum)
        val typeStr = requireField(row, "type", rowNum)

        val date = parseDate(dateStr)
        val amount = amountStr.toDoubleOrNull() ?: throw IllegalArgumentException("Row $rowNum: Invalid amount '$amountStr'")
        val txType = parseEnum<TransactionType>(typeStr) ?: throw IllegalArgumentException("Row $rowNum: Invalid type '$typeStr'")

        val sourceName = row["sourceAccountName"]?.takeIf { it.isNotBlank() }
        val destName = row["destinationAccountName"]?.takeIf { it.isNotBlank() }
        val sourceId = sourceName?.let { resolveAccount(it, accounts, rowNum).id } ?: 0L
        val destId = destName?.let { resolveAccount(it, accounts, rowNum).id }
        val note = row["note"]?.takeIf { it.isNotBlank() } ?: ""

        val tx = FinancialTransaction(
            type = txType,
            amount = amount,
            sourceAccountId = sourceId,
            destinationAccountId = destId,
            note = note,
            date = date.time
        )
        db.financialTransactionDao().insertTransaction(tx)
    }

    private fun requireField(row: Map<String, String>, key: String, rowNum: Int): String {
        val value = row[key] ?: row.entries.firstOrNull { it.key.lowercase().trim() == key.lowercase() }?.value
        return value?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Row $rowNum: Missing required field '$key'")
    }

    private fun parseDate(dateStr: String): Date {
        val trimmed = dateStr.trim()
        return try {
            dateFormat.parse(trimmed) ?: throw IllegalArgumentException("Row date: Invalid date '$trimmed'. Use YYYY-MM-DD")
        } catch (e: Exception) {
            throw IllegalArgumentException("Row date: Invalid date '$trimmed'. Use YYYY-MM-DD format")
        }
    }

    private fun resolveAccount(name: String, accounts: Map<String, Account>, rowNum: Int): Account {
        return accounts[name.lowercase().trim()]
            ?: throw IllegalArgumentException("Row $rowNum: Account '$name' not found. Create it first or check spelling. Available: ${accounts.keys.take(5).joinToString(", ") { it }}")
    }

    private inline fun <reified T : Enum<T>> parseEnum(value: String): T? {
        val normalized = value.uppercase().trim().replace(" ", "_").replace("-", "_").replace(".", "_")
        return try {
            java.lang.Enum.valueOf(T::class.java, normalized)
        } catch (e: Exception) {
            T::class.java.enumConstants?.firstOrNull { it.name.replace("_", "") == normalized.replace("_", "") }
        }
    }

    private inline fun <reified T : Enum<T>> enumNames(): String {
        return T::class.java.enumConstants?.joinToString(", ") { it.name } ?: ""
    }

    private fun tryParseCategory(value: String): ExpenseCategory {
        val normalized = value.uppercase().trim().replace(" ", "_").replace("-", "_").replace(".", "_").replace("&", "AND")
        parseEnum<ExpenseCategory>(normalized)?.let { return it }
        ExpenseCategory.entries.firstOrNull { it.displayName.replace(" ", "_").uppercase() == normalized }?.let { return it }
        ExpenseCategory.entries.firstOrNull { it.name.contains(normalized) || normalized.contains(it.name.replace("_", "")) }?.let { return it }
        return ExpenseCategory.OTHER
    }
}
