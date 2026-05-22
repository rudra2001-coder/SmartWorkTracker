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

### Account System (ui/screens/accounts/)
- **Account** entity (`data/entity/Account.kt`): Fields include `id`, `name`, `type` (AccountCategory), `provider` (AccountProvider), `accountNumber`, `balance`, `maxBalance`, `hasLimit`, `dailyTransferLimit`, `isActive`, `nickname`, `linkedGoalId`, `iconColor`, `notes`
- **AccountDao** (`data/dao/AccountDao.kt`): Standard Room DAO with `getAllAccounts()`, `getAccountById()`, `updateBalance()`, `deleteAccount()`, `deactivateAccount()`, etc.
- **AccountRepository** (`data/repository/AccountRepository.kt`): Bridges DAO to ViewModels. Key methods: `createAccount()`, `updateAccountDetails()`, `deleteAccountWithTransfer()` (transfers balance before deleting), `transferBetweenAccounts()`, `addIncomeToAccount()`, `deductExpenseFromAccount()`, `initializeDefaultAccounts()`
- **FusionEngine** (`engine/FusionEngine.kt`): Handles transfers with balance updates **and** creates `FinancialTransaction` records (type TRANSFER). Used by `TransferViewModel` and `AccountsViewModel` for delete-with-transfer flow. Methods: `processTransfer()`, `getSmartAlerts()`, `getNetWorth()`
- **FinancialTransactionDao** (`data/dao/FinancialTransactionDao.kt`): Has `getTransactionsForAccount(accountId)` for account-specific transaction history
- **Swipe gestures on AccountsScreen**: Right swipe (StartToEnd) opens **Edit** dialog, left swipe (EndToStart) opens **Delete** flow. Delete with balance > 0 shows transfer-to-another-account dialog using FusionEngine. Delete with zero balance shows direct confirmation.
- **AccountDetailScreen**: Shows real `FinancialTransaction` data per account, inflow/outflow metrics, balance activity chart (7-day), follows Dashboard design pattern (same color tokens, card shapes, shadows, gradients).
- **Dashboard design tokens** used across accounts: `EmeraldGreen`, `CoralRed`, `SapphireBlue`, `GoldenAmber`, `VioletPurple`, `CardShape = 20.dp`, shadows, gradient icon boxes, animated metrics.
