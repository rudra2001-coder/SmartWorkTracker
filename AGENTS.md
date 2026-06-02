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
      AppDatabase.kt         # Room DB (v12, 59 entities)
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
- Backup system uses WorkManager (daily at 12:05 AM). Backup version: 33.
- Theme supports light/dark mode via `SmartWorkTrackerTheme`
- Sample data is seeded on first launch from `SampleData.kt`
- Settings managed via `DataStore Preferences` (new) and `SharedPreferences` (legacy)

### Backup System (data/backup/) — Updated May 2026
- **AppBackup** (`AppBackup.kt`): Data class mirroring all DB tables for JSON export/import.
  - Backs up 39+ entity lists including the 5 new calculation tables: `mealTypes`, `weeklyMealRates`, `dailyMealRates`, `mealSettings`, `specialMealDates`
- **BackupManager** (`BackupManager.kt`): Gson-based export (to JSON) / import (from JSON) with Room transaction wrapping.
  - Export: reads all tables via DAO suspend `getAll*()` / `first()` methods
  - Import: clears existing data, inserts all records in a single Room transaction
  - Version: 33 (incremented on schema changes for forward-compatibility)
- **AutoBackupWorker** (`AutoBackupWorker.kt`): WorkManager `CoroutineWorker` triggered daily at 12:05 AM. Saves JSON to Downloads (MediaStore on API 30+). Tracks `last_auto_backup_time` in SharedPreferences.
- **Backup ViewModel/Screen** (`ui/screens/backup/`): UI for manual export/import via SAF, toggle auto-backup, view last/next backup time.

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

### Calculation System (ui/screens/calculation/) — Redesigned May 2026
- **Old**: Multi-meal rate system with MealType/WeeklyMealRate/DailyMealRate tables.
- **New**: Simple meal calculator with normal/special two-rate system + calendar-based + manual modes.
- **Entities**: `MealSettings` (singleton row with normalMealRate, specialMealRate, mealDays as Set<Int>), `SpecialMealDate` (unique date index, auto-gen ID), `TravelAndExpense` (daily travel cost + other expenses). Old tables (`MealType`, `WeeklyMealRate`, `DailyMealRate`, `Calculation`) kept in DB but unused.
- **DAOs**: `MealSettingsDao` (Flow-based singleton), `SpecialMealDateDao` (Flow-based, insert/toggle/delete by date), `TravelExpenseDao`
- **DB version**: 10→12 (destructive migration, tables recreated)
- **UI pattern**: Dashboard design tokens (`CardShape=20.dp`, `EmeraldGreen`, `CoralRed`, `SapphireBlue`, `GoldenAmber`, `VioletPurple`), animated cost counter via `Animatable+tween(800)`

#### Auto-Calculated Section (Top)
- **Month Navigator**: Prev/Next arrows, month label (MMMM yyyy)
- **Summary Header**: Office Days, Meal Cost, Total Cost in primary container card
- **Meal Settings Card**: Normal Rate + Special Rate inputs, Meal Weekdays (Sun–Sat chip toggles with Wkdays/Clear presets), Save button with "Saved!" feedback
- **Special Dates Calendar**: Full calendar grid with office-day dots (from work logs), toggleable special dates (red border/background), legend, special date list
- **Meal Summary Card**: Total/Normal/Special chip stats, cost breakdown table (type/days/rate/cost), Quarterly/Yearly projections, expandable daily breakdown (date/day/icon/rate/cost per row)
- **Monthly Breakdown Chart**: Bar chart all 12 months
- **Pie Chart**: Office day count (donut)
- **Travel & Other Expenses**: Daily travel cost + monthly other expenses
- **Total Cost Summary**: Meal + Travel + Other with Monthly/Quarterly/Yearly projections

#### Manual Calendar Calculator (Bottom)
- Independent month navigator (separate from top calendar)
- Tap dates to mark as Normal (green) or Special (red) via mode toggle chips
- Rate inputs (Normal Rate / Special Rate)
- Calculate button with "Saved!" feedback
- Result: total meals count, normal/special breakdown, animated monthly cost
- Expandable day list showing each selected date with its rate
- Clear button to reset all selections
- Legend showing Normal/Special color coding

#### Calculation Logic
```
For each work log where workType==OFFICE AND dayOfWeek in mealDays:
  cost = specialMealRate if date in specialDates else normalMealRate
Total = sum of all costs
Quarterly = Total × 3, Yearly = Total × 12
```
- Manual mode: independent calculation using selected dates + entered rates (does not use work logs or mealDays filter)
- Flow-based reactivity: DB inserts trigger `combine` → auto-recalculation

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

### Transfer System (ui/screens/transfer/) — Updated May 2026
- **Screen** (`TransferScreen.kt`): Card-based layout with FROM/TO account selectors, amount input, expandable **Fees & Charges** section, notes field, and confirm button.
- **ViewModel** (`TransferViewModel.kt`): Uses `FusionEngine.processTransfer()` for execution. Double-click guard via `_transferState.value = TransferState.Loading` set **before** coroutine launch.
- **FusionEngine** (`engine/FusionEngine.kt`): `processTransfer()` now accepts optional `transferFee` and `cashOutFee` parameters (default `0.0`).
  - Deducts only `amount` from fromAccount for the transfer itself
  - Each fee > 0 is deducted separately via `recordFee()` which reads the **post-transfer** balance and calls `accountDao.updateBalance()` — avoids double-counting
  - Creates `FinancialTransaction` (type `TRANSFER`) for the transfer amount
  - Creates `FinancialTransaction` (type `EXPENSE`, category `TRANSFER_FEE`) for each fee
  - Validates `fromAccount.balance >= amount + transferFee + cashOutFee` before processing
- **UI (Fees & Charges)**: Collapsible section toggled via "▶ Fees & Charges" chip. Two optional number inputs: **Transfer Fee** and **Cash Out Fee**. Live total calculation includes fees. Badge shows fee amount on the chip when non-zero.
- **Success Dialog**: Shows transfer details + total fees charged if any. Reset clears all fields including fees.
- **ExpenseCategory** (`model/Expense.kt`): Added `TRANSFER_FEE("Transfer Fee")` with color `#FF6B35` for fee categorization in reports.

### Savings System (ui/screens/savings/)
- **Savings** entity (`data/entity/Savings.kt`, table `savings`): Tracks savings deposits/withdrawals with `amount`, `note`, `category`, `timestamp`, and now `accountId` (linking to Account system). Positive `amount` = deposit, negative `amount` = withdrawal.
- **SavingsRepository** (`data/repository/SavingsRepository.kt`): Bridges DAO to ViewModels. Key methods:
  - `addToSavings(amount, note, category, accountId)` — **accountId is required** (no default). Deducts from account (validates balance), throws if account not found or insufficient funds, creates `FinancialTransaction` (type `SAVINGS_ADD`), inserts savings record
  - `withdrawFromSavings(amount, note, category, accountId)` — **accountId is required** (no default). Adds to account balance, throws if account not found, creates `FinancialTransaction` (type `SAVINGS_WITHDRAW`), inserts savings record
  - `deleteTransaction(savings)` — reverses balance change (validates if reversing a withdrawal), deletes savings record
- **FinancialTransaction** links savings ops via `TransactionType.SAVINGS_ADD` / `SAVINGS_WITHDRAW`.
- **Upgraded (May 2026):** Previously savings had no account link — money could be deposited/withdrawn without any balance change. Now fully account-integrated.
- **Mandatory account selection (May 2026):** `AddTransactionDialog` removed the "No account (savings only)" option. Account dropdown auto-selects the first available account. Confirm button requires both `amount > 0` and `selectedAccountId > 0`. Repository removed all `if (accountId > 0)` guards — account operations always execute. ViewModel validates `accountId > 0` and shows error if not selected. `RecurringEngine` updated to fail early if destination account is missing for withdrawals.
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

### Monthly Report (ui/screens/report/) — Upgraded May 2026
- **Old**: Only showed work type distribution (pie chart + counts) for a selected month
- **New**: Full monthly insights dashboard with financial data, date range filtering, and period comparison
- **ViewModel** (`MonthlyReportViewModel.kt`): Now combines 5 data sources (work logs, expenses, incomes, savings, accounts) via `combine` + `stateIn`. Computes work stats, income/expense/net, meal expense, savings deposits/withdrawals, expense/income by category, and previous-period comparison.
- **UiState** (`MonthlyReportUiState`): Now includes `totalIncome`, `totalExpense`, `netAmount`, `mealExpense`, `totalSavingsDeposited`, `totalSavingsWithdrawn`, `netSavings`, `expenseByCategory`, `incomeByCategory`, `previousPeriod`, `useCustomRange`, `customStartDate`, `customEndDate`, `compareWithPrevious`.
- **Screen** (`MonthlyReportScreen.kt`): New card-based layout with:
  - Month selector + Year navigation (left/right arrows)
  - Custom date range toggle with Material3 DatePickerDialog for start/end dates
  - "Compare with previous period" toggle (Switch)
  - 4 overview StatCards (Work Days, Income, Expense, Net) with gradient icon boxes
  - Work Distribution pie chart (existing, using ycharts)
  - Expense by Category pie chart (new) using `ExpenseCategory.color` for slice colors
  - Income by Category pie chart (new) with predefined color palette
  - Savings Activity summary card (deposited/withdrawn/net)
  - Detailed Summary card with work breakdown + financial breakdown
  - Comparison card (if toggled) showing side-by-side current vs previous metrics with percentage change
- **Factory** (`MonthlyReportViewModelFactory.kt`): Now passes `ExpenseRepository`, `IncomeRepository`, and `AppDatabase` in addition to `WorkLogRepository`.

### Analytics Screen (ui/screens/analytics/) — Upgraded May 2026
- **Goal**: Align Analytics screen UI with Dashboard design language for visual consistency
- **Design tokens replaced**: Old 6-color palette (`AccentBlue`, `AccentGreen`, `AccentAmber`, `AccentRed`, `AccentPurple`, `AccentCyan`) → Dashboard's 5-color system (`EmeraldGreen`, `CoralRed`, `SapphireBlue`, `GoldenAmber`, `VioletPurple`) + surface tints (`GreenSurface`, `RedSurface`, `BlueSurface`, `AmberSurface`, `PurpleSurface`)
- **Card styling**: All 11 cards updated to use `CardShape = RoundedCornerShape(20.dp)`, `Modifier.shadow(8.dp, CardShape)` elevation, `CardDefaults.cardElevation(0.dp)`, and gradient icon boxes (`Brush.linearGradient`) as card headers
- **Components upgraded**:
  - **DashboardHeader**: Added shadow to back button, pill-style score badge with `GoldenAmber` tint
  - **KPI Strip**: Redesigned with `GlassMetricCard` style — colored surface backgrounds, shadow, rounded icon containers
  - **BalanceRingCard**: Added gradient icon box header (`SapphireBlue→VioletPurple`)
  - **FinancialOverviewCard**: Added gradient icon box header (`EmeraldGreen→SapphireBlue`), updated divider/surface styling
  - **MonthlyBarChartCard**: Added gradient icon box header (`SapphireBlue→EmeraldGreen`), **new income/expense value rows** below the bar chart — first row shows green income values for all 6 months, second row shows red expense values, giving an at-a-glance trend overview without tapping individual bars
  - **ProductivityRingCard / HabitStreakCard**: Updated shadow and color tokens
  - **WellnessGrid**: Updated cards with shadow and new colors
  - **FocusTimelineCard**: Added gradient icon box header (`SapphireBlue→VioletPurple`), updated split bar colors
  - **WeeklyPulseChart**: Added gradient icon box header (`SapphireBlue→VioletPurple`), updated bar colors
  - **AchievementsRow**: Replaced custom Box borders with `Card` + shadow
- **Cleanup**: Removed duplicate `SummaryItem` data class, removed leftover old FocusTimelineCard code block, removed `BorderStroke` import
- **Files**: `AnalyticsScreen.kt` (all UI changes), `AnalyticsViewModel.kt` (unchanged), `AnalyticsViewModelFactory.kt` (unchanged)

### Calculation Module Bug Fixes (Applied May 22 2026)
| File | Bug | Fix |
|---|---|---|
| `CalculationScreen.kt:710-714` | `SummaryRow` (a `RowScope` extension) called directly inside `Column` — 5 compilation errors: "Unresolved reference" | Wrapped each `SummaryRow` in `Row(Modifier.fillMaxWidth())` |
| `CalculationViewModel.kt:199,280` | `sdf.parse(wl.date)` — `wl.date` is already a `Date` object, but `SimpleDateFormat.parse()` expects `String` | Replaced with `wl.date.time` (direct millis access) |
