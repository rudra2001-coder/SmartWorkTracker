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
Bottom nav has 5 tabs: **Dashboard**, **Calendar**, **Analytics**, **All Features**, **Settings**. Drawer provides full app navigation. Routes are defined via `sealed class NavigationItem` (~35 routes).

#### Navigation Drawer — Upgraded June 2026
- **Scrollable**: Wrapped in `Column(verticalScroll(rememberScrollState()))` — all 33 items accessible on any screen size.
- **Grouped sections**: 5 groups with headers (Overview, Work, Finance, Personal, Tools) for easy scanning.
- **Visual refresh**:
  - Gradient header (blue `#1A73E8`→`#0D47A1`) with app name and tagline.
  - Each item has a rounded icon box (36dp, 10dp radius) with blue tint when selected.
  - Item labels show description text (11sp, gray) beneath the title.
  - Selected item has blue background tint + semi-bold title.
  - Fixed width 300dp for consistent layout.
- **Data structure**: Items are organized via `data class NavGroup(title, items)` inside `MainApp()` for easy reordering.

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

### Monthly Report (ui/screens/report/) — Upgraded June 2026
- **Old**: Only showed work type distribution (pie chart + counts) for a selected month
- **New**: Full monthly insights dashboard with financial data, date range filtering, period comparison, and full data report
- **ViewModel** (`MonthlyReportViewModel.kt`): Combines 37 data sources via `combine` + `stateIn`. Computes work stats, income/expense/net, meal expense, savings deposits/withdrawals, expense/income by category, previous-period comparison, and `FullReportData` (40+ metrics from all entity types).
- **UiState** (`MonthlyReportUiState`): Added `fullData: FullReportData` with 40+ fields spanning work, productivity, loans, credit cards, EMIs, journal, check-ins, decisions, reality, debt, recurring, financial transactions, meals, and other entities.
- **Screen** (`MonthlyReportScreen.kt`): Added `FullDataReportSection` with 10 collapsible subsections (Work, Productivity, Loans & Credit, EMIs, Journal & Check-in, Reality & Debt, Recurring, Financial Transactions, Meals, Other).
- **Factory** (`MonthlyReportViewModelFactory.kt`): Passes `ExpenseRepository`, `IncomeRepository`, and `AppDatabase` in addition to `WorkLogRepository`.

### Reports Screen (ui/screens/reports/) — Upgraded June 2026
- **Modules**: `ReportsScreen.kt`, `ReportsViewModel.kt`, `ReportsViewModelFactory.kt`, `ReportUiState.kt`
- **ViewModel** (`ReportsViewModel.kt`): Combines 38 data sources via `combine` + `stateIn`. Has `AppDatabase` parameter for full DB access. Computes filtered item lists (work/incomes/expenses), summary stats, and `ReportsFullData` (40+ metrics).
- **UiState** (`ReportUiState.kt`): Contains `fullReport: ReportsFullData` with 40+ fields — `workDayCount`, `workSessionCount`, `workSessionHours`, `habitCount`, `focusSessionCount`, `focusSessionMinutes`, `healthMetricCount`, `achievementCount`, `achievementsUnlocked`, `journalCount`, `checkInCount`, `decisionCount`, `positiveDecisions`, `negativeDecisions`, `realityPlanned`, `realityCompleted`, `activeLoanCount`, `totalLoanAmount`, `totalRemainingLoan`, `borrowedLoanCount`, `lentLoanCount`, `totalBorrowedRemaining`, `totalLentRemaining`, `activeEmiCount`, `pendingEmiCount`, `overdueEmiCount`, `totalPendingEmiAmount`, `creditCardCount`, `totalCreditCardDebt`, `totalCreditCardLimit`, `financialTxCount`, `financialTxIncome`, `financialTxExpense`, `activeRecurringRules`, `recurringTxCount`, `pendingRecurringTxCount`, `mealCount`, `mealTotalCost`, `specialMealDateCount`, `colleagueCount`, `scheduleCount`, `travelExpenseAmount`, `totalDebtAmount`, `weeklyReportCount`, `monthlyInputCount`.
- **Screen** (`ReportsScreen.kt`): Added `ReportsFullDataCard` composable with 10 collapsible subsections matching the MonthlyReport structure. Uses `ReportsFullData` from `ReportUiState` (duplicated from `FullReportData` to avoid cross-package coupling).
- **Factory** (`ReportsViewModelFactory.kt`): Passes `AppDatabase` to `ReportsViewModel`.
- **Helper composables**: `ReportsFullDataCard`, `ReportSection` (collapsible subsection with icon/color header), `ReportRow` (label/value pair row).

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

### Dashboard Hero Section — Upgraded June 2026

**Files**: `DashboardScreen.kt` (`NetBalanceHeroCard`, `AccountBalanceCard`, `HeroColorPickerDialog`, `ColorSlider`, `AccountBalanceRow`)

#### Custom Color Picker (replaced preset dropdown)
- **Old**: Simple `DropdownMenu` with 10 preset hex colors (`#FFFFFF`, `#E6FBF4`, etc.) — no custom color support.
- **New** (`HeroColorPickerDialog`): Full color picker `AlertDialog` with:
  - **16 preset colors** in a 4-column grid with checkmark selection
  - **3 HSV sliders** (Hue 0–360°, Saturation 0–100%, Brightness 0–100%) using Material3 `Slider` with colored thumbs/tracks
  - **Hex input field** (`OutlinedTextField`) with validation — accepts any 6-digit hex code, auto-converts to HSL sliders
  - **Live preview** strip showing the selected color with hex label
  - **Apply/Cancel** buttons — persists via `settingsRepository.setHeroColor(hex)` (DataStore, key `hero_color`)
- **Trigger**: Palette icon button (`Icons.Outlined.ColorLens`) in the hero card settings row (unchanged icon).

#### Account Selector Upgrade
- **Old**: Showed account names only in dropdown — no balance context.
- **New**: Each `DropdownMenuItem` now shows:
  - **Colored dot** (green for positive balance, red for zero)
  - **Account name** (bold when selected)
  - **Balance** (right-aligned, `৳X,XXX` format)
  - "All-Time Net Balance" also shows the computed net value with a chart icon
- Selection persists via `settingsRepository.setHeroAccountId(id)` (DataStore, key `hero_account_id`).

#### Redesigned Account Balance Card
- **Old** (`AccountBalanceCard`): Single big number showing `totalBalance` — no per-account breakdown.
- **New**: Lists each account with `balance > 0` as an individual row with:
  - **Color-coded dot**: Wallet = `EmeraldGreen`, Bank = `SapphireBlue`, Mobile Banking = `VioletPurple`
  - **Animated balance** per account (staggered 500ms delay, `tween(800)`)
  - **Proportional progress bar** showing each account's share of total (animated via `Animatable`)
  - **Divider** + **Total row** at the bottom with the animated aggregate
  - Falls back to single big number when no accounts exist with balance > 0
- **Signature changed**: `AccountBalanceCard(accounts: List<Account>, totalBalance: Double)` — call site updated to pass `uiState.accounts`.

#### Exported/Public Composable API
All existing public signatures preserved:
- `NetBalanceHeroCard(financialSummary, heroColor, heroAccountId, accounts, onColorSelected, onAccountSelected)` — unchanged
- `FinancialSummaryChart(financialSummary)` — unchanged (delegates to `NetBalanceHeroCard`)
- `AccountBalanceCard(accounts, totalBalance)` — new parameter `accounts` added

### Calculation Module Bug Fixes (Applied May 22 2026)
| File | Bug | Fix |
|---|---|---|
| `CalculationScreen.kt:710-714` | `SummaryRow` (a `RowScope` extension) called directly inside `Column` — 5 compilation errors: "Unresolved reference" | Wrapped each `SummaryRow` in `Row(Modifier.fillMaxWidth())` |
| `CalculationViewModel.kt:199,280` | `sdf.parse(wl.date)` — `wl.date` is already a `Date` object, but `SimpleDateFormat.parse()` expects `String` | Replaced with `wl.date.time` (direct millis access) |

### Calendar Module — Data Accuracy Fixes (Applied June 2026)
**Goal**: Ensure calendar always shows accurate work log data regardless of month navigation, filtering, or timezone.

| File | Bug | Fix |
|---|---|---|
| `CalendarViewModel.kt` | Monthly stats never updated when navigating months via prev/next arrows — `currentMonth` was local `remember` state in the composable, ViewModel had no visibility | Added `_currentMonth` StateFlow + `onMonthChanged()` + `navigateMonth()` in ViewModel; `updateMonthlyStats()` now reads `_currentMonth.value`; `currentMonth` exposed via `CalendarUiState` |
| `CalendarScreen.kt` | `MonthSummaryCard` used `filteredWorkLogs` — with active filters, summary showed partial/inaccurate counts | Changed to `uiState.workLogs` (unfiltered) |
| `CalendarScreen.kt` | `currentMonth` managed as local `remember` state — out of sync with ViewModel | Removed local state, all components read `uiState.currentMonth` |
| `WorkLogDao.kt:46-57` | `getTotalExtraHours` SQL used `strftime('%s', endTime)` on `"HH:MM"` strings — SQLite can't parse time-only as datetime, always returned NULL/0 | Prepend dummy date `'2000-01-01 '` + append `':00'` for valid datetime parsing; added `IS NOT NULL` guards |
| `WorkLogDao.kt:87-88` | `getTodayWorkLog` compared UTC dates via `date(date/1000,'unixepoch') = date('now')` — timezone mismatch near midnight could miss entries | Changed to `WHERE date >= :startOfDay AND date < :endOfDay` accepting local-date millis boundaries |
| `WorkLogRepository.kt:19-24` | Repository passed no timezone context to `getTodayWorkLog` query | Computes local `startOfDay`/`endOfDay` via `java.time.LocalDate` + `ZoneId.systemDefault()` |
| `data/entity/WorkLog.kt` | Empty (0 bytes) duplicate file — actual entity is `model/WorkLog.kt` | Deleted |
| `data/local/DateConverter.kt` | Empty (0 bytes), unused — `TypeConverters.kt` + `Converters.kt` handle all type conversions | Deleted |

### Dashboard Monthly Stats — Timezone Fix (Applied June 2026)
**Goal**: Fix Dashboard monthly work summary showing wrong counts due to UTC-based SQL date filtering.

**Root cause**: Work log dates are stored as UTC epoch millis (`LocalDate.atStartOfDay(zone).toInstant()`). For Bangladesh (UTC+6), **June 1** local = **May 31** 18:00 UTC. DAO queries used `strftime('%Y-%m', date/1000, 'unixepoch')` which operates in **UTC**, shifting ALL dates one month back in the Dashboard. The Calendar avoided this by filtering `LocalDate` in-app.

**Fix**: Replaced all `strftime`-based month/year queries in `WorkLogDao.kt` with millisecond range queries (`WHERE date >= :start AND date <= :end`) computed in local timezone.

| File | Bug | Fix |
|---|---|---|
| `WorkLogDao.kt:40-41` | `countByTypeFlow` used UTC `strftime('%Y-%m', date/1000, 'unixepoch')` — mismatched local dates by timezone offset | Replaced with `countByTypeInRange(startOfMonth, endOfMonth, workType)` using local-time millis bounds |
| `WorkLogDao.kt:62-73` | `getTotalExtraHoursFlow` same UTC strftime issue | Replaced with `getTotalExtraHoursInRange(startOfMonth, endOfMonth, workType)` using local-time millis bounds |
| `WorkLogDao.kt:75-76` | `getOvertimeLogsByMonth` same UTC strftime issue | Added `getOvertimeLogsInRange(startOfMonth, endOfMonth)` half-open range variant |
| `WorkLogDao.kt:84-88` | `getOvertimeLogsByYear` same UTC strftime issue for year queries | Added `getOvertimeLogsInYearRange(startOfYear, endOfYear)` half-open range variant |
| `WorkLogDao.kt:43-44` | `getWorkLogsByMonth` same UTC strftime issue | Added `getWorkLogsInRange(startOfMonth, endOfMonth)` half-open range variant |
| `WorkLogRepository.kt:23-53` | `getMonthlyStats()` captured `monthYear` from UTC `Calendar.getInstance()` — dates shifted to wrong month | Now computes `startOfMonth`/`endOfMonth` via `LocalDate.now().withDayOfMonth(1).atStartOfDay(zone)` and passes to range-based DAO methods |
| `DashboardViewModel.kt:66,95` | `loadDashboardData()` computed `monthYear` for `getOvertimeLogsByMonth` using UTC-based `SimpleDateFormat` | Removed `monthYear`, passes existing local-time `startTime`/`endTime` to `getOvertimeLogsInRange` |
| `OvertimeViewModel.kt:40-51` | `loadOvertimeData()` used UTC-based `monthYear`/`year` from `SimpleDateFormat` | Now computes `startOfMonth`/`endOfMonth` and `startOfYear`/`endOfYear` via `LocalDate` local-timezone; uses `getOvertimeLogsInRange`/`getOvertimeLogsInYearRange` |
| `CalculationViewModel.kt:114-115,227-228` | `runFullCalculation`/`fetchMonthlyBreakdown` used UTC `monthYearFormat` with `getWorkLogsByMonth` | Replaced with `getWorkLogsInRange` using local-time millis bounds from Calendar instance |

### Recurring System — Upgraded June 2026
**Goal**: Upgrade the recurring transaction system with accurate account-based balance checking, richer features, and Dashboard-themed UI.

#### Entity Changes (`RecurringRule.kt`)
- Added fields: `maxExecutions`, `executedCount`, `totalExecutedAmount`, `lastExecutedDate`, `isPaused`, `skipIfHoliday`, `weekdayAdjustment`, `tags`, `notes`
- Added `WeekdayAdjustment` enum (NONE, PREVIOUS_WEEKDAY, NEXT_WEEKDAY, SKIP)

#### DAO Changes
- `RecurringRuleDao`: New queries for paused rules (`getPausedRules`), execution stats, max-reached rules (`getMaxExecutionReachedRules`), past-end-date rules (`getPastEndDateRules`), filtered counts, search by tags
- `RecurringTransactionDao`: New queries for skip/failure status filtering, aggregated stats

#### Engine Rewrite (`RecurringEngine.kt`)
- Now takes `AccountDao` directly — balance lookups are always real-time from the database
- Validates balances before expense/transfer/savings executions
- Enforces `maxExecutions` and `endDate` auto-deactivation
- Handles weekend/weekday adjustment
- Full `detectPatterns()` and `calculateTotalMonthlyImpact()` implementations

#### Three-State Rule Lifecycle
- `isActive=true + isPaused=false` (running)
- `isActive=true + isPaused=true` (paused)
- `isActive=false` (deactivated)

#### Screen Rewrite (`RecurringScreen.kt`)
- Dashboard design tokens: gradient header, stat cards, filter chips
- Enhanced rule cards with progress bars (`executedCount/maxExecutions`) and pause badges
- Form sections: tags, notes, maxExecutions, weekdayAdjustment, endDate picker
- Manual execution dialog with result feedback
- History tab with animated progress bars

#### Worker Upgrade (`RecurringNotificationWorker.kt`)
- Big-text notifications with failure reasons inline
- Amounts in ৳ format

### In-App Notification Module — Created June 2026
**Goal**: Centralized in-app notification system capturing events from recurring, backup, alarm, focus, and team systems.

#### New Entity (`InAppNotification.kt`)
- `id` (Long, auto-generate), `title`, `message`, `type` (NotificationType enum), `source`, `referenceId`, `actionRoute`, `isRead`, `timestamp`
- `NotificationType` enum: RECURRING, BACKUP, ALARM, FOCUS, TEAM, EXPENSE, INCOME, TRANSFER, SYSTEM
- Indexed columns on `isRead` and `type` for fast queries

#### New DAO (`InAppNotificationDao.kt`)
- Standard CRUD: `insert()`, `getNotifications()`, `getUnreadCount()`, `markAsRead()`, `markAllAsRead()`, `deleteById()`, `deleteRead()`, `deleteAll()`
- Optional filters by type via nullable parameter

#### New Repository (`NotificationRepository.kt`)
- Bridges DAO to ViewModels with Flow-based reactivity

#### Singleton Manager (`InAppNotificationManager.kt`)
- Thread-safe double-check locking singleton, accessible from any `Context` without DI
- Methods: `logNotification(title, message, type, source, referenceId, actionRoute)`
- Uses `applicationContext` for safety across component types

#### Integration Points
- `RecurringNotificationWorker` — logs on successful/batched execution, failure, and upcoming transactions
- `AutoBackupWorker` — logs on success, failure, and error
- `AlarmReceiver` — logs when alarm triggers
- `FocusViewModel` — logs on session start and completion
- `DutyNotificationManager` — logs on swap request and approval

#### UI (`NotificationScreen.kt`)
- Gradient header (VioletPurple) with unread count badge
- TopAppBar actions: Mark All Read, Delete All (with confirmation dialog)
- Type filter chips (All + per-type) with pill-style cards
- Notification cards with:
  - Colored type icon in a circular background (`animateColorAsState` for read/unread states)
  - Title, message (2-line max), relative time, source tag
  - Blue dot indicator for unread
  - Delete button per card
  - Tap to mark as read
- Empty state with icon when no notifications match
- Uses Dashboard design tokens: EmeraldGreen, CoralRed, SapphireBlue, GoldenAmber, VioletPurple

#### ViewModel (`NotificationViewModel.kt`)
- State: notifications list, selectedFilter, unreadCount, totalCount
- Actions: markAsRead, markAllAsRead, deleteById, deleteAll, setFilter
- Factory pattern with manual DI via NotificationViewModelFactory

#### Navigation
- `NavigationItem.Notifications` in the sealed class with `Icons.Default.Notifications`
- Entry in drawer navigation list
- Composable route "notifications" in NavHost graph

#### Database
- DB version bumped to 14 with destructive migration
- `AppDatabase` registers `InAppNotification` entity and `InAppNotificationDao`
