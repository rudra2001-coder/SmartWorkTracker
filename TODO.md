# TODO & Pending Work

## Recently Completed (Recurring System Upgrade)
- [x] Fixed monthly totals calculation (was summing raw amounts)
- [x] Implemented savings execution (was stub)
- [x] Implemented transfer execution (was stub)
- [x] Added pattern detection algorithm
- [x] Added yearly projection feature
- [x] Added search and filter functionality
- [x] Added rule templates (8 predefined)
- [x] Redesigned UI with 5 tabs
- [x] Added step-by-step rule creation wizard
- [x] Added expandable rule cards
- [x] Added empty states
- [x] Added Insights tab with projections
- [x] Added swipe-to-delete on recurring rule cards
- [x] Added pull-to-refresh on Rules, Transactions, and History tabs
- [x] Added CSV data export to Settings > Data Management
- [x] Added database indices on recurring_rules, recurring_transactions, incomes, expenses
- [x] Added unit tests for RecurringEngine
- [x] Improved accessibility (content descriptions on stats, chips, badges)
- [x] Added spending alerts when approaching budget limits (Insights tab)
- [x] Added bulk edit/delete for rules (multi-select mode in Rules tab)
- [x] Added transaction confirmation flow (confirm button on pending transactions)
- [x] Added snooze/defer for failed transactions (retry tomorrow button)
- [x] Improved calendar tab with proper month grid view
- [x] Added category breakdown visualization in Insights tab
- [x] Added integration tests for RecurringViewModel
- [x] Added ProGuard rules for release builds
- [x] Persisted execution history to database (execution_history table)
- [x] Added loading skeletons for data screens
- [x] Added onboarding for recurring feature (3-step dialog)
- [x] Added tooltips for complex features (rule action buttons)
- [x] Improved WorkManager with constraints and retry logic
- [x] Added calendar export (ICS format)
- [x] Added bill splitting support
- [x] Added biometric authentication (fingerprint/face unlock)

## Recently Completed (Accounting Module Upgrade)
- [x] Migrated all ViewModels to Kotlin Coroutines Flow (WisdomViewModel, CalculationViewModel, FinancialStatementViewModel, BackupViewModel, CalendarViewModel)
- [x] Fixed one-shot events using SharedFlow instead of StateFlow (snackbar, error messages, triggers)
- [x] Fixed ReportsViewModel StateFlow encapsulation leak (.asStateFlow())
- [x] Added SharedFlow buffer for TeamViewModel emit resilience
- [x] Migrated OvertimeViewModel selectedTab from mutableStateOf to MutableStateFlow
- [x] Deleted dead root model/Expense.kt (duplicate entity)
- [x] Fixed ExpenseDao.deleteExpenseById to use String PK (matching UUID-based Expense entity)
- [x] Simplified FinancialStatementViewModel expense deletion (no more minimal object workaround)
- [x] Created Currency utility (utils/Currency.kt) replacing hardcoded BDT
- [x] Replaced all hardcoded "BDT" strings with Currency.format() calls (FusionEngine, AccountRepository, AddEntryViewModel, ExpenseScreen, SavingsScreen, DashboardViewModel)
- [x] Added efficient aggregate queries to FinancialTransactionDao (getTotalIncome, getTotalExpenses)
- [x] Simplified TransactionRepository to use DB-level aggregation instead of in-memory filtering
- [x] Extracted hardcoded spending limit in RecurringViewModel to DEFAULT_SPENDING_LIMIT constant
- [x] Fixed FusionEngine.getAccountTypeName() to resolve actual account names (was hardcoded "BANK")
- [x] Extracted hardcoded low balance threshold in FusionEngine to constant
- [x] Upgraded BillSplit entity from String fields to List<String>/List<Double> with TypeConverters
- [x] Updated BillSplitScreen to display per-participant amounts

## Potential Future Improvements

### General App
- [ ] Add cloud sync
- [ ] Add widgets for home screen
- [ ] Add multi-currency support
- [ ] Add receipt photo capture
- [ ] Add voice input for transactions

### Technical
- [x] Migrate to Kotlin Coroutines Flow consistently

## Known Issues
- JAVA_HOME not configured on dev machine (can't run Gradle)
- Database uses destructive migration (data lost on schema changes)
- Database bumped to v11 (added bill_splits table) — will reset data on next install

## Notes for Next Session
- Read `ARCHITECTURE.md` for project structure
- Read `RECURRING_SYSTEM.md` for recurring feature details
- Read `PATTERNS.md` for code conventions
- Read `SCREENS.md` for feature overview
