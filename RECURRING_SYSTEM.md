# Recurring System - Quick Reference

## Overview
Manages recurring income, expenses, savings, and transfers with smart scheduling.

## Key Files
| File | Purpose |
|------|---------|
| `engine/RecurringEngine.kt` | Core logic: scheduling, execution, pattern detection |
| `ui/screens/recurring/RecurringViewModel.kt` | UI state, search, filters, templates |
| `ui/screens/recurring/RecurringScreen.kt` | Full Compose UI with 5 tabs |
| `data/repository/RecurringRepository.kt` | Data access layer |
| `data/entity/RecurringRule.kt` | Rule entity + enums |
| `data/entity/RecurringTransaction.kt` | Transaction instance entity |
| `alarm/RecurringNotificationWorker.kt` | Background worker for notifications |

## Data Model

### RecurringRule (Table: recurring_rules)
```
id: Long (PK, auto)
name: String
description: String?
transactionType: TransactionType (INCOME/EXPENSE/SAVINGS_ADD/SAVINGS_WITHDRAW/TRANSFER)
amount: Double
category: String?
sourceAccount: AccountType
destinationAccount: AccountType?
frequency: RecurringFrequency
interval: Int (for CUSTOM)
selectedDaysOfWeek: List<DayOfWeek>? (for WEEKLY_SPECIFIC_DAYS)
startDate: Long
endDate: Long?
nextExecutionDate: Long
preferredTime: PreferredTime (MORNING/AFTERNOON/EVENING/NIGHT)
priority: RecurringPriority (CRITICAL/HIGH/MEDIUM/LOW/OPTIONAL)
minimumBalanceRequired: Double?
autoExecute: Boolean
isActive: Boolean
```

### RecurringTransaction (Table: recurring_transactions)
```
id: Long (PK, auto)
ruleId: Long (FK -> RecurringRule)
name, description, transactionType, amount, category
sourceAccount, destinationAccount
scheduledDate: Long
executedDate: Long?
status: RecurringTransactionStatus
failureReason: String?
retryCount: Int
isConfirmed, isSkipped: Boolean
skipReason, userNote: String?
relatedIncomeId, relatedExpenseId, relatedFinancialTransactionId
```

## Enums
- **RecurringFrequency**: DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM, WEEKLY_SPECIFIC_DAYS
- **RecurringTransactionStatus**: PENDING, CONFIRMED, EXECUTING, EXECUTED, FAILED, SKIPPED, CANCELLED
- **RecurringPriority**: CRITICAL (always executes), HIGH, MEDIUM, LOW, OPTIONAL (skippable)
- **PreferredTime**: MORNING(9am), AFTERNOON(2pm), EVENING(7pm), NIGHT(10pm)

## Key Methods

### RecurringEngine
- `calculateNextExecutionDate()` - Next date based on frequency
- `executeRule()` - Execute a rule, create transaction, update balance
- `processDueRules()` - Batch execute all due rules
- `calculateMonthlyEquivalent()` - Convert any frequency to monthly amount
- `calculateYearlyProjection()` - Full year income/expenses projection
- `detectPatterns()` - Analyze transactions for recurring patterns
- `smartReschedule()` - Reschedule failed transactions with grace period

### RecurringViewModel
- `addRule(rule)` / `updateRule(rule)` / `deleteRule(rule)`
- `toggleRuleActive(rule)` - Enable/disable
- `executeRuleNow(rule)` - Manual execution
- `manualExecuteRules(rules)` - Batch execution
- `updateSearchQuery(query)` / `updateFilter(filter)`
- `getRuleTemplates()` - 8 predefined templates
- `addRuleFromTemplate(template)` - Quick creation

### RuleFilter Enum
ALL, ACTIVE, INACTIVE, INCOME, EXPENSE, SAVINGS, TRANSFER

## UI Structure (5 Tabs)
1. **Rules** - Search + filter chips + expandable rule cards
2. **Transactions** - All transaction instances with status
3. **Calendar** - Monthly view of upcoming transactions
4. **Insights** - Yearly projection, category breakdown, pattern suggestions
5. **History** - Execution history with progress bars

## Rule Templates
| Name | Category | Amount | Type | Frequency |
|------|----------|--------|------|-----------|
| Monthly Rent | Housing | 15000 | EXPENSE | MONTHLY |
| Salary Credit | Salary | 50000 | INCOME | MONTHLY |
| Electricity Bill | Bills & Utilities | 2000 | EXPENSE | MONTHLY |
| Internet Bill | Subscriptions | 1000 | EXPENSE | MONTHLY |
| Savings Transfer | Savings | 5000 | SAVINGS_ADD | MONTHLY |
| EMI Payment | Other | 8000 | EXPENSE | MONTHLY |
| Gym Membership | Personal Care | 2000 | EXPENSE | MONTHLY |
| Freelance Income | Freelance | 10000 | INCOME | MONTHLY |

## Monthly Calculation Logic
```kotlin
fun calculateMonthlyEquivalent(rule): Double {
    DAILY -> amount * 30
    WEEKLY -> amount * 4.33
    BIWEEKLY -> amount * 2.17
    MONTHLY -> amount
    QUARTERLY -> amount / 3
    YEARLY -> amount / 12
    CUSTOM -> amount * (30 / interval)
    WEEKLY_SPECIFIC_DAYS -> amount * daysPerWeek * 4.33
}
```

## Balance Protection
- **CRITICAL**: Always executes regardless of balance
- **HIGH**: Executes if balance >= minimum required
- **MEDIUM/LOW**: Executes if balance available
- **OPTIONAL**: Skipped if insufficient balance

## Worker Schedule
- `RecurringNotificationWorker` runs every hour
- Checks for due rules and sends notifications
- Handles success/failure notifications
