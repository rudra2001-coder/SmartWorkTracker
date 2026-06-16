package com.rudra.smartworktracker.data.importexport

import com.rudra.smartworktracker.model.ExpenseCategory
import com.rudra.smartworktracker.model.WorkType
import com.rudra.smartworktracker.data.entity.LoanCategory
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider

enum class ImportEntityType(
    val displayName: String,
    val description: String,
    val iconLabel: String
) {
    EXPENSE("Expenses", "Import expense records with category, amount, date", "expense"),
    INCOME("Income", "Import income records with source, amount, date", "income"),
    WORK_LOG("Work Logs", "Import daily work entries (office, home, etc.)", "work"),
    ACCOUNT("Accounts", "Import financial accounts (wallet, bank, mobile)", "account"),
    LOAN("Loans", "Import loan records (borrowed/lent)", "loan"),
    SAVINGS("Savings", "Import savings deposit/withdrawal history", "savings"),
    HABIT("Habits", "Import habit tracking data", "habit"),
    HEALTH_METRIC("Health Metrics", "Import health tracking records", "health"),
    DAILY_JOURNAL("Daily Journals", "Import journal entries", "journal"),
    CREDIT_CARD("Credit Cards", "Import credit card records", "credit"),
    RECURRING_RULE("Recurring Rules", "Import recurring transaction rules", "recurring"),
    COLLEAGUE("Colleagues", "Import team/colleague profiles", "team"),
    FINANCIAL_TRANSACTION("Transactions", "Import financial transaction records", "transaction"),
}

data class ImportTemplate(
    val type: ImportEntityType,
    val headers: List<String>,
    val requiredFields: Set<String>,
    val fieldDescriptions: Map<String, String>,
    val exampleRow: Map<String, String>
) {
    fun generateCsvHeader(): String = headers.joinToString(",")

    fun generateExampleCsv(): String = buildString {
        appendLine(generateCsvHeader())
        appendLine(headers.joinToString(",") { exampleRow[it] ?: "" })
    }
}

object ImportTemplates {

    fun getTemplate(type: ImportEntityType): ImportTemplate = when (type) {
        ImportEntityType.EXPENSE -> ImportTemplate(
            type = ImportEntityType.EXPENSE,
            headers = listOf("date", "amount", "category", "accountName", "merchant", "notes"),
            requiredFields = setOf("date", "amount", "category", "accountName"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format (e.g. 2026-01-15)",
                "amount" to "Numeric amount (e.g. 500.00)",
                "category" to ExpenseCategory.entries.joinToString(", ") { it.name },
                "accountName" to "Name of the account to deduct from (must exist)",
                "merchant" to "Optional merchant/store name",
                "notes" to "Optional description"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "amount" to "500.00",
                "category" to "FOOD_AND_DINING",
                "accountName" to "Main Wallet",
                "merchant" to "Local Restaurant",
                "notes" to "Lunch meeting"
            )
        )

        ImportEntityType.INCOME -> ImportTemplate(
            type = ImportEntityType.INCOME,
            headers = listOf("date", "amount", "source", "accountName", "category", "description"),
            requiredFields = setOf("date", "amount", "accountName"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format (e.g. 2026-01-15)",
                "amount" to "Numeric amount (e.g. 50000.00)",
                "source" to "Income source name (e.g. Salary, Freelance)",
                "accountName" to "Name of the account to credit (must exist)",
                "category" to "Optional category",
                "description" to "Optional description"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "amount" to "50000.00",
                "source" to "Salary",
                "accountName" to "Main Bank",
                "category" to "Salary",
                "description" to "January salary"
            )
        )

        ImportEntityType.WORK_LOG -> ImportTemplate(
            type = ImportEntityType.WORK_LOG,
            headers = listOf("date", "workType", "startTime", "endTime", "isOvertime", "overtimeRate"),
            requiredFields = setOf("date", "workType"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format (e.g. 2026-01-15)",
                "workType" to WorkType.entries.joinToString(", ") { it.name },
                "startTime" to "HH:MM format (e.g. 09:00)",
                "endTime" to "HH:MM format (e.g. 18:00)",
                "isOvertime" to "true or false",
                "overtimeRate" to "Optional overtime rate multiplier"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "workType" to "OFFICE",
                "startTime" to "09:00",
                "endTime" to "18:00",
                "isOvertime" to "false",
                "overtimeRate" to ""
            )
        )

        ImportEntityType.ACCOUNT -> ImportTemplate(
            type = ImportEntityType.ACCOUNT,
            headers = listOf("name", "type", "provider", "balance", "accountNumber", "notes"),
            requiredFields = setOf("name", "type"),
            fieldDescriptions = mapOf(
                "name" to "Account display name",
                "type" to AccountCategory.entries.joinToString(", ") { it.name },
                "provider" to AccountProvider.entries.joinToString(", ") { it.name },
                "balance" to "Initial balance (default 0.0)",
                "accountNumber" to "Optional account number",
                "notes" to "Optional notes"
            ),
            exampleRow = mapOf(
                "name" to "Main Wallet",
                "type" to "WALLET",
                "provider" to "CASH",
                "balance" to "10000.00",
                "accountNumber" to "",
                "notes" to "Primary wallet"
            )
        )

        ImportEntityType.LOAN -> ImportTemplate(
            type = ImportEntityType.LOAN,
            headers = listOf("date", "personName", "amount", "loanType", "loanCategory", "accountName", "dueDate", "notes"),
            requiredFields = setOf("date", "personName", "amount", "loanType", "accountName"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format",
                "personName" to "Name of the person",
                "amount" to "Loan amount",
                "loanType" to LoanType.entries.joinToString(", ") { it.name },
                "loanCategory" to LoanCategory.entries.joinToString(", ") { it.name },
                "accountName" to "Account to use (deduct for LENT, add for BORROWED)",
                "dueDate" to "Optional due date YYYY-MM-DD",
                "notes" to "Optional notes"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "personName" to "John Doe",
                "amount" to "50000.00",
                "loanType" to "LENT",
                "loanCategory" to "PERSONAL",
                "accountName" to "Main Wallet",
                "dueDate" to "2026-06-15",
                "notes" to "Personal loan to friend"
            )
        )

        ImportEntityType.SAVINGS -> ImportTemplate(
            type = ImportEntityType.SAVINGS,
            headers = listOf("date", "amount", "accountName", "category", "note"),
            requiredFields = setOf("date", "amount", "accountName"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format",
                "amount" to "Positive for deposit, negative for withdrawal",
                "accountName" to "Account to use (deposit deducts, withdrawal adds)",
                "category" to "Category: DEPOSIT, WITHDRAWAL, INTEREST, TRANSFER, OTHER",
                "note" to "Optional note"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "amount" to "5000.00",
                "accountName" to "Main Bank",
                "category" to "DEPOSIT",
                "note" to "Monthly savings"
            )
        )

        ImportEntityType.HABIT -> ImportTemplate(
            type = ImportEntityType.HABIT,
            headers = listOf("name", "description", "difficulty"),
            requiredFields = setOf("name"),
            fieldDescriptions = mapOf(
                "name" to "Habit name",
                "description" to "Optional description",
                "difficulty" to "EASY, MEDIUM, or HARD (default MEDIUM)"
            ),
            exampleRow = mapOf(
                "name" to "Morning Exercise",
                "description" to "30 min exercise every morning",
                "difficulty" to "MEDIUM"
            )
        )

        ImportEntityType.HEALTH_METRIC -> ImportTemplate(
            type = ImportEntityType.HEALTH_METRIC,
            headers = listOf("date", "metricType", "value", "unit", "notes"),
            requiredFields = setOf("date", "metricType", "value"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format",
                "metricType" to "e.g. WEIGHT, SLEEP, WATER, STEPS, HEART_RATE, BLOOD_PRESSURE",
                "value" to "Numeric value",
                "unit" to "Optional unit (kg, hrs, ml, etc.)",
                "notes" to "Optional notes"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "metricType" to "WEIGHT",
                "value" to "72.5",
                "unit" to "kg",
                "notes" to ""
            )
        )

        ImportEntityType.DAILY_JOURNAL -> ImportTemplate(
            type = ImportEntityType.DAILY_JOURNAL,
            headers = listOf("date", "morningIntention", "gratitude", "eveningReflection"),
            requiredFields = setOf("date"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format (one entry per date)",
                "morningIntention" to "Optional morning intention",
                "gratitude" to "Optional gratitude entry",
                "eveningReflection" to "Optional evening reflection"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "morningIntention" to "Complete the project report",
                "gratitude" to "Grateful for good health",
                "eveningReflection" to "Productive day overall"
            )
        )

        ImportEntityType.CREDIT_CARD -> ImportTemplate(
            type = ImportEntityType.CREDIT_CARD,
            headers = listOf("cardName", "cardNumber", "cardLimit", "accountName", "statementDate", "dueDate"),
            requiredFields = setOf("cardName", "cardLimit", "accountName"),
            fieldDescriptions = mapOf(
                "cardName" to "Card display name",
                "cardNumber" to "Optional card number",
                "cardLimit" to "Card credit limit amount",
                "accountName" to "Linked account name (must exist)",
                "statementDate" to "Day of month (1-31)",
                "dueDate" to "Day of month (1-31)"
            ),
            exampleRow = mapOf(
                "cardName" to "My Credit Card",
                "cardNumber" to "****1234",
                "cardLimit" to "100000.00",
                "accountName" to "Main Bank",
                "statementDate" to "5",
                "dueDate" to "25"
            )
        )

        ImportEntityType.RECURRING_RULE -> ImportTemplate(
            type = ImportEntityType.RECURRING_RULE,
            headers = listOf("name", "amount", "transactionType", "frequency", "startDate", "sourceAccountName", "destinationAccountName", "notes"),
            requiredFields = setOf("name", "amount", "transactionType", "frequency", "startDate"),
            fieldDescriptions = mapOf(
                "name" to "Rule name",
                "amount" to "Transaction amount",
                "transactionType" to "INCOME, EXPENSE, SAVINGS_ADD, SAVINGS_WITHDRAW, TRANSFER",
                "frequency" to "DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY",
                "startDate" to "YYYY-MM-DD start date",
                "sourceAccountName" to "Source account name (optional)",
                "destinationAccountName" to "Destination account name (optional)",
                "notes" to "Optional notes"
            ),
            exampleRow = mapOf(
                "name" to "Monthly Savings",
                "amount" to "10000.00",
                "transactionType" to "SAVINGS_ADD",
                "frequency" to "MONTHLY",
                "startDate" to "2026-01-01",
                "sourceAccountName" to "Main Bank",
                "destinationAccountName" to "",
                "notes" to "Auto savings"
            )
        )

        ImportEntityType.COLLEAGUE -> ImportTemplate(
            type = ImportEntityType.COLLEAGUE,
            headers = listOf("name", "designation", "department", "phone", "email"),
            requiredFields = setOf("name"),
            fieldDescriptions = mapOf(
                "name" to "Colleague name",
                "designation" to "Optional job title",
                "department" to "Optional department",
                "phone" to "Optional phone number",
                "email" to "Optional email address"
            ),
            exampleRow = mapOf(
                "name" to "Jane Smith",
                "designation" to "Software Engineer",
                "department" to "Engineering",
                "phone" to "+8801XXXXXXXXX",
                "email" to "jane@example.com"
            )
        )

        ImportEntityType.FINANCIAL_TRANSACTION -> ImportTemplate(
            type = ImportEntityType.FINANCIAL_TRANSACTION,
            headers = listOf("date", "amount", "type", "sourceAccountName", "destinationAccountName", "note"),
            requiredFields = setOf("date", "amount", "type"),
            fieldDescriptions = mapOf(
                "date" to "YYYY-MM-DD format",
                "amount" to "Transaction amount",
                "type" to "INCOME, EXPENSE, SAVINGS_ADD, SAVINGS_WITHDRAW, LOAN_BORROW, LOAN_LEND, LOAN_REPAY, LOAN_RECEIVE, EMI_PAID, TRANSFER",
                "sourceAccountName" to "Source account name (if applicable)",
                "destinationAccountName" to "Destination account name (if applicable)",
                "note" to "Optional note"
            ),
            exampleRow = mapOf(
                "date" to "2026-01-15",
                "amount" to "5000.00",
                "type" to "TRANSFER",
                "sourceAccountName" to "Main Wallet",
                "destinationAccountName" to "Main Bank",
                "note" to "Transfer to savings"
            )
        )
    }

    fun autoDetectType(headers: Set<String>): ImportEntityType? {
        val normalized = headers.map { it.trim().lowercase() }.toSet()
        return when {
            normalized.containsAll(setOf("date", "amount", "category", "accountname")) ||
                normalized.containsAll(setOf("date", "amount", "category", "accountname", "merchant")) -> ImportEntityType.EXPENSE

            normalized.containsAll(setOf("date", "amount", "accountname")) &&
                normalized.any { it == "source" } -> ImportEntityType.INCOME

            normalized.containsAll(setOf("date", "worktype")) ||
                normalized.containsAll(setOf("date", "worktype", "starttime")) -> ImportEntityType.WORK_LOG

            normalized.containsAll(setOf("name", "type")) &&
                normalized.any { it == "provider" || it == "balance" } -> ImportEntityType.ACCOUNT

            normalized.containsAll(setOf("date", "personname", "amount", "loantype")) ||
                normalized.containsAll(setOf("date", "personname", "amount", "loantype", "loanategory")) -> ImportEntityType.LOAN

            normalized.containsAll(setOf("date", "amount")) &&
                normalized.any { it == "accountname" || it == "note" } &&
                !normalized.contains("worktype") &&
                !normalized.contains("category") -> ImportEntityType.SAVINGS

            normalized.containsAll(setOf("name", "difficulty")) ||
                normalized.containsAll(setOf("name", "description")) -> ImportEntityType.HABIT

            normalized.containsAll(setOf("date", "metrictype", "value")) -> ImportEntityType.HEALTH_METRIC

            normalized.containsAll(setOf("date", "morningintention")) ||
                normalized.containsAll(setOf("date", "gratitude")) ||
                normalized.containsAll(setOf("date", "eveningreflection")) -> ImportEntityType.DAILY_JOURNAL

            normalized.containsAll(setOf("cardname", "cardlimit", "accountname")) ||
                normalized.containsAll(setOf("cardname", "cardnumber", "cardlimit")) -> ImportEntityType.CREDIT_CARD

            normalized.containsAll(setOf("name", "amount", "transactiontype", "frequency")) -> ImportEntityType.RECURRING_RULE

            normalized.containsAll(setOf("name", "designation")) ||
                normalized.containsAll(setOf("name", "department")) -> ImportEntityType.COLLEAGUE

            normalized.containsAll(setOf("date", "amount", "type")) &&
                (normalized.contains("sourceaccountname") || normalized.contains("destinationaccountname")) -> ImportEntityType.FINANCIAL_TRANSACTION

            else -> null
        }
    }
}
