# Agents

This file defines custom agents for opencode.

## Project Overview: Smart Work Tracker

### What It Is
A comprehensive **personal productivity, finance, and life management** Android app (Kotlin, Jetpack Compose, Room). Single-activity MVVM architecture with ~35 screens covering work tracking, expense/income/financial management, habits, health, focus sessions, journals, achievements, and more.

### Tech Stack
- **Language**: Kotlin 2.3.21
- **UI**: Jetpack Compose (Material3, BOM 2025.01.00)
- **Database**: Room 2.8.3 (KSP) with ~54 entities, ~33 DAOs
- **Architecture**: Single-Activity + MVVM + Repository + Room (no Hilt/Dagger — manual DI via `DatabaseModule.kt`)
- **Build**: Gradle 9.4.1, AGP 9.2.1, Kotlin DSL
- **Min SDK**: 28, **Target/Compile SDK**: 36
- **App ID**: `com.rudra.smartworktracker`
- **Navigation**: `NavHost` + `ModalNavigationDrawer` + `BottomNavigationBar` (5 tabs)

### Key Dependencies
- **Compose**: navigation-compose, material-icons-extended, lottie-compose, konfetti, Vico/ycharts
- **Lifecycle**: lifecycle-runtime-ktx, lifecycle-viewmodel-compose
- **WorkManager**: 2.9.0 (backup, recurring notifications)
- **Data**: Room, DataStore Preferences, Gson, kotlinx-serialization-json
- **DateTime**: kotlinx-datetime 0.8.0
- **ML**: LiteRT support API (on-device AI)
- **Testing**: JUnit 4, Mockito-Kotlin, kotlinx-coroutines-test, Compose UI test

### Project Structure
```
app/
  src/main/java/com/rudra/smartworktracker/
    MainActivity.kt          # Single Activity entry, Compose host
    SmartWorkTrackerApp.kt   # Minimal Application subclass
    SmartWorkTrackerApplication.kt # Actual Application: schedules backup + notifications
    alarm/                   # AlarmActivity, AlarmReceiver, AlarmScheduler, BootReceiver, RecurringNotificationWorker
    data/
      AppDatabase.kt         # Room DB (v10, 54 entities)
      SampleData.kt          # Sample data seeding
      SharedPreferenceManager.kt # Legacy prefs
      backup/                # AppBackup, AutoBackupWorker, BackupManager
      dao/                   # 33+ Room DAO interfaces
      entity/                # ~54 Room @Entity classes (Account, WorkDay, Expense, Meal, Habit, etc.)
      repository/            # 27 repository classes bridging DAOs -> ViewModels
    di/
      DatabaseModule.kt      # Manual DI singleton
    engine/                  # FusionEngine, RuleEngine, RecurringEngine, AchievementManager
    model/                   # 20 domain model classes (non-Room)
    repository/              # ColleagueRepository.kt
    ui/
      components/            # DesignSystem.kt, AnimatedFAB.kt
      navigation/            # MainApp.kt (central NavHost, ~35 routes)
      screens/               # 35+ screen packages each with *Screen.kt + *ViewModel.kt
      theme/                 # Theme.kt, Color.kt, Type.kt
      UiState.kt
    utils/                   # Utility helpers
```

### Navigation (MainApp.kt)
Bottom nav has 5 tabs: **Dashboard**, **Calendar**, **Analytics**, **All Features**, **Settings**. Drawer provides additional navigation. Routes are defined via `sealed class NavigationItem`.

### Architecture Pattern
```
UI (Compose Screens) --> ViewModels (StateFlow) --> Repositories --> Room DAOs --> SQLite
```
- ViewModels use `MutableStateFlow<UiState>` for reactive state
- Many ViewModels have manual `*ViewModelFactory` (no DI framework)
- Repositories wrap DAO `Flow<T>` calls

### Testing
- 3 test files total (2 unit, 1 instrumented)
- Run unit tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- No CI/CD, no linting/formatting tools configured

### Important Notes
- **SQLite is the single source of truth** (per AppDatabase.kt comment)
- App uses manual DI only (`DatabaseModule` provides Room DB instance)
- No Hilt, Dagger, Koin, or other DI framework
- No Multiplatform, no Compose for Desktop/iOS
- Backup system uses WorkManager (daily at 12:05 AM)
- Theme supports light/dark mode via `SmartWorkTrackerTheme`
- Sample data is seeded on first launch from `SampleData.kt`
- Settings managed via `DataStore Preferences` (new) and `SharedPreferences` (legacy)

### Financial System Integrity (Fixes Applied May 2026)
**Goal**: Eliminate all system loss, balance mismatches, and silent data corruption paths.

**Rules enforced:**
- **No silent `coerceAtLeast(0.0)`** — balance changes that would overdraw an account throw `IllegalStateException` with account name + current balance context. Never silently cap.
- **Balance update before record insert** — ExpenseRepository now validates + updates balance before inserting the expense record.
- **Single deduction per transaction** — no double-deduction bugs (LoanRepository repays deduct from `paymentAccountId` only).

**Fixes applied:**

| File | Bug | Fix |
|---|---|---|
| `ExpenseRepository.kt` | `insertExpense` silently capped balance via `.coerceAtLeast(0.0)` — expense recorded at full amount but balance only reduced partially or to 0 → **system loss** | Validate `account.balance >= expense.amount` first, throw on insufficient funds, update balance then insert |
| `IncomeRepository.kt` | `deleteIncome` / `deleteIncomeById` used `.coerceAtLeast(0.0)` when reversing income — if money was spent, reversal was capped → **balance corruption** | Remove `.coerceAtLeast(0.0)`, always subtract exact income amount from account |
| `LoanRepository.kt` | `repayLoan` for BORROWED deducted amount from BOTH `paymentAccountId` AND `loan.accountId` — when same account, **money deducted twice** → **critical system loss** | Removed second deduction from `loan.accountId`; only deduct from `paymentAccountId` |
| `AccountRepository.kt` | `deductExpenseFromAccount` silently did nothing when `newBalance < 0` — caller thinks money was deducted but balance unchanged → **silent failure** | Throw `IllegalStateException` with account name/balance context if insufficient funds |
| `EmiRepository.kt` | `payEmi` updated loan balance + created `FinancialTransaction` but **never deducted from actual account** → **system loss** (debt cleared, money stayed in account) | Added `AccountDao`, deduct money from `emi.paymentAccountId` for BORROWED, add for LENT, with balance validation + insufficient funds throw |
| `CreditCardViewModel.kt` | `payCreditCardBill(card, amount)` reduced card debt but **never deducted from linked account** → **system loss** | Now deducts from `card.accountId` with balance validation |
| `CreditCardViewModel.kt` | `payCreditCardBill(card, amount, accountId)` silently skipped deduction when account not found | Now throws `IllegalStateException` |
| `CreditCardViewModel.kt` | `deleteCreditCard` could silently create negative balances; **no handling for overpaid cards** | Now throws if insufficient funds; refunds credit to linked account when `currentBalance < 0` |
| `CreditCardViewModel.kt` | `addCardTransaction`, `transferFromCreditCard`, initial transfer had **no card limit check** → could exceed `cardLimit` | Added `checkCardLimit()` — throws if `currentBalance + amount > cardLimit` |
| `CreditCardViewModel.kt` | `transferFromCreditCard` / initial transfer silently skipped balance updates when account not found | Now throw `IllegalStateException` |
| `CreditCardViewModel.kt` | `payCreditCardBill(card, amount)` 2-param version silently skipped balance deduction when linked account was null — card balance reduced but no money deducted → **system loss** | Now throws `IllegalStateException` with account context if linked account not found |
| `CreditCardScreen.kt` | Confirm button `enabled` check didn't require `selectedAccount != null` for PAY_BILL — user could submit without selecting account → crash | Added `selectedAccount != null` requirement in `enabled` for PAY_BILL; also added guard + `enabled` to "Pay Full" button |
| `LoanRepository.kt` | `repayLoan` for LENT used `loan.accountId` instead of `paymentAccountId` — when a different account was selected for repayment, money was still credited to original loan account → **wrong account credited** | Now uses `paymentAccountId` for LENT (same as BORROWED) — money goes to the user-selected account |
| `LoanRepository.kt` | `repayLoan` for LENT silently skipped balance update when account was null → **system loss** (loan marked paid, money never added to any account) | Now throws `IllegalStateException` if receiving account not found |
| `LoanRepository.kt` | `markLoanAsPaid` for both BORROWED and LENT silently skipped balance updates when linked account was null → **system loss** | Both cases now throw `IllegalStateException` if account not found |
| `LoansScreen.kt` | `RepayLoanDialog` only showed account dropdown for BORROWED — LENT users had no way to select which account receives repayment; if `loan.accountId` was 0, confirm button was permanently disabled | Removed `if (isBorrowed)` guard — dropdown now shown for both BORROWED and LENT, allowing account selection for all repayment flows |

**Still known gaps (lower priority):**
- `AccountRepository.addIncomeToAccount` / `deductExpenseFromAccount` update balance only, no `FinancialTransaction` record
- `ExpenseRepository.insertExpense` validates balance against current `account.balance` but doesn't account for pending transactions (acceptable for single-user app)

**Caller patterns:**
- `FusionEngine.processTransfer()` is the **single correct path** for transfers — updates both balances + creates `FinancialTransaction` record
- Delete-with-transfer flow: calls `FusionEngine.processTransfer()` first, then `accountDao.deleteAccountById()` — never adds `FinancialTransactionDao` directly to `AccountRepository`
- `RecurringEngine` delegates to `incomeRepository.insertIncome()` / `expenseRepository.insertExpense()` — inherits all their logic (now fixed)
- `RecurringEngine` delegates to `savingsRepository.addToSavings()` / `withdrawFromSavings()` for recurring savings — inherits account integration + `FinancialTransaction` creation
- `RecurringEngine.executeTransfer()` uses `FusionEngine.processTransfer()` for real money movement

### Account System (ui/screens/accounts/)
- **Account** entity (`data/entity/Account.kt`): Fields include `id`, `name`, `type` (AccountCategory), `provider` (AccountProvider), `accountNumber`, `balance`, `maxBalance`, `hasLimit`, `dailyTransferLimit`, `isActive`, `nickname`, `linkedGoalId`, `iconColor`, `notes`
- **AccountDao** (`data/dao/AccountDao.kt`): Standard Room DAO with `getAllAccounts()`, `getAccountById()`, `updateBalance()`, `deleteAccount()`, `deactivateAccount()`, etc.
- **AccountRepository** (`data/repository/AccountRepository.kt`): Bridges DAO to ViewModels. Key methods: `createAccount()`, `updateAccountDetails()`, `deleteAccountWithTransfer()` (transfers balance before deleting), `transferBetweenAccounts()`, `addIncomeToAccount()`, `deductExpenseFromAccount()`, `initializeDefaultAccounts()`
- **FusionEngine** (`engine/FusionEngine.kt`): Handles transfers with balance updates **and** creates `FinancialTransaction` records (type TRANSFER). Used by `TransferViewModel` and `AccountsViewModel` for delete-with-transfer flow. Methods: `processTransfer()`, `getSmartAlerts()`, `getNetWorth()`
- **FinancialTransactionDao** (`data/dao/FinancialTransactionDao.kt`): Has `getTransactionsForAccount(accountId)` for account-specific transaction history
- **Swipe gestures on AccountsScreen**: Right swipe (StartToEnd) opens **Edit** dialog, left swipe (EndToStart) opens **Delete** flow. Delete with balance > 0 shows transfer-to-another-account dialog using FusionEngine. Delete with zero balance shows direct confirmation.
- **AccountDetailScreen**: Shows real `FinancialTransaction` data per account, inflow/outflow metrics, balance activity chart (7-day), follows Dashboard design pattern (same color tokens, card shapes, shadows, gradients).
 - **Dashboard design tokens** used across accounts: `EmeraldGreen`, `CoralRed`, `SapphireBlue`, `GoldenAmber`, `VioletPurple`, `CardShape = 20.dp`, shadows, gradient icon boxes, animated metrics.

### Savings System (ui/screens/savings/)
- **Savings** entity (`data/entity/Savings.kt`, table `savings`): Tracks savings deposits/withdrawals with `amount`, `note`, `category`, `timestamp`, and now `accountId` (linking to Account system). Positive `amount` = deposit, negative `amount` = withdrawal.
- **SavingsRepository** (`data/repository/SavingsRepository.kt`): Bridges DAO to ViewModels. Key methods:
  - `addToSavings(amount, note, category, accountId)` — deducts from account (validates balance), creates `FinancialTransaction` (type `SAVINGS_ADD`), inserts savings record
  - `withdrawFromSavings(amount, note, category, accountId)` — adds to account balance, creates `FinancialTransaction` (type `SAVINGS_WITHDRAW`), inserts savings record
  - `deleteTransaction(savings)` — reverses balance change (validates if reversing a withdrawal), deletes savings record
- **FinancialTransaction** links savings ops via `TransactionType.SAVINGS_ADD` / `SAVINGS_WITHDRAW`.
- **Upgraded (May 2026):** Previously savings had no account link — money could be deposited/withdrawn without any balance change. Now fully account-integrated.
- **RecurringEngine.executeSavings**: Now real implementation — creates savings + account + FinancialTransaction for both `SAVINGS_ADD` and `SAVINGS_WITHDRAW` via `SavingsRepository`.
- **RecurringEngine.executeTransfer**: Now real implementation — uses `FusionEngine.processTransfer()` to move money between accounts with balance updates + `FinancialTransaction`.

### Credit Card System (ui/screens/creditcard/)
- **Dual representation**: `CreditCard` entity (`data/entity/CreditCard.kt`, table `credit_cards`) with card-specific fields (`cardLimit`, `currentBalance`, `statementDate`, `dueDate`) linked to an `Account` via `accountId`. Account entity uses `AccountProvider.CREDIT_CARD` / `AccountCategory.BANK`.
- **CreditCardTransaction** (`data/entity/CreditCardTransaction.kt`, table `credit_card_transactions`): FK to `CreditCard.id` with CASCADE delete, stores individual card charges/payments.
- **FinancialTransaction** links credit card ops via `relatedCreditCardId` field. Charges use `TransactionType.EXPENSE`, payments/transfers use `TransactionType.TRANSFER`.
- **No repository layer** — `CreditCardViewModel` talks directly to DAOs (architectural inconsistency).
- **Fixes applied (May 2026):**
  - `payCreditCardBill(card, amount)` was reducing debt without deducting from any account → **system loss**. Now deducts from `card.accountId` with balance validation.
  - `payCreditCardBill(card, amount, accountId)` was silently skipping deduction when account not found. Now throws if account missing or insufficient funds.
  - `deleteCreditCard` had no balance validation — could silently create negative account balances. Now throws if insufficient funds to settle debt. Also handles overpaid cards (`currentBalance < 0`) by refunding the credit to the linked account.
  - `addCardTransaction`, `transferFromCreditCard`, and initial transfer now enforce `cardLimit` via `checkCardLimit()` — throws if `currentBalance + amount > cardLimit`.
  - Initial transfer creation: was silently skipping balance update when linked account not found. Now throws `IllegalStateException`.
  - `transferFromCreditCard` was silently skipping balance update when destination account not found. Now throws `IllegalStateException`.
