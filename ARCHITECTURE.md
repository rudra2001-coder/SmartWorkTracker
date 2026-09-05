# SmartWorkTracker Architecture

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Database**: Room (SQLite)
- **Architecture**: MVVM (ViewModel + Repository + DAO)
- **DI**: Manual (ViewModelFactory pattern)

## Package Structure
```
com.rudra.smartworktracker/
├── data/
│   ├── entity/        # Room entities (data models)
│   ├── dao/           # Room DAOs (database queries)
│   ├── repository/    # Data repositories (abstraction layer)
│   ├── AppDatabase.kt # Room database (version 8)
│   └── backup/        # Backup/restore functionality
├── engine/
│   ├── RecurringEngine.kt    # Recurring transaction logic
│   ├── RuleEngine.kt         # Business rules
│   ├── FusionEngine.kt       # Combined analytics
│   └── AchievementManager.kt # Gamification
├── ui/
│   ├── screens/       # Feature screens (Compose)
│   ├── theme/         # Colors, Typography, Theme
│   └── UiState.kt     # Common UI state
├── viewmodel/         # ViewModels
├── alarm/             # Alarms, notifications, workers
├── di/                # Dependency injection modules
└── utils/             # Utilities, extensions
```

## Key Entities
| Entity | Table | Purpose |
|--------|-------|---------|
| RecurringRule | recurring_rules | Defines recurring transaction rules |
| RecurringTransaction | recurring_transactions | Instances generated from rules |
| Income | income | Income records |
| Expense | expenses | Expense records |
| Savings | savings | Savings deposits/withdrawals |
| FinancialTransaction | financial_transactions | Transfers, loans, EMI |
| Schedule | schedules | Alarm/schedule management |

## Database Info
- **Name**: `smart_work_tracker_v2`
- **Version**: 8
- **Migration**: `fallbackToDestructiveMigration(dropAllTables = true)`

## Common Patterns

### ViewModel Creation
```kotlin
class MyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(context)
        val repository = MyRepository(db.myDao())
        return MyViewModel(repository) as T
    }
}
```

### Repository Pattern
```kotlin
class MyRepository(private val dao: MyDao) {
    fun getAll(): Flow<List<Entity>> = dao.getAll()
    suspend fun insert(entity: Entity): Long = dao.insert(entity)
    suspend fun update(entity: Entity) = dao.update(entity)
    suspend fun delete(entity: Entity) = dao.delete(entity)
}
```

## Theme Colors
- **Primary**: Indigo (#6366F1 light / #818CF8 dark)
- **Secondary**: Emerald (#10B981 light / #34D399 dark)
- **Tertiary**: Amber (#F59E0B light / #FBBF24 dark)
- **Background**: Light Gray / Dark Slate Blue

## Files Modified in Recurring Upgrade
1. `engine/RecurringEngine.kt` - Core logic, pattern detection, yearly projection
2. `ui/screens/recurring/RecurringViewModel.kt` - Search, filters, templates, insights
3. `ui/screens/recurring/RecurringScreen.kt` - Full UI with 5 tabs, wizard, animations
4. `data/repository/TransactionRepository.kt` - Added insertTransaction()
