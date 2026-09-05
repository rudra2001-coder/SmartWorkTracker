# Code Patterns & Conventions

## File Naming
- Entities: PascalCase (e.g., `RecurringRule.kt`)
- DAOs: EntityName + Dao (e.g., `RecurringRuleDao.kt`)
- Repositories: EntityName + Repository (e.g., `RecurringRepository.kt`)
- ViewModels: FeatureName + ViewModel (e.g., `RecurringViewModel.kt`)
- Screens: FeatureName + Screen (e.g., `RecurringScreen.kt`)

## Compose Patterns

### State Management
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Screen
val uiState by viewModel.uiState.collectAsState()
```

### Bottom Sheet Form
```kotlin
var showSheet by remember { mutableStateOf(false) }
val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

if (showSheet) {
    ModalBottomSheet(onDismissRequest = { showSheet = false }, sheetState = sheetState) {
        FormContent(onSave = { ... }, onCancel = { showSheet = false })
    }
}
```

### Expandable Card
```kotlin
var expanded by remember { mutableStateOf(false) }
Card(modifier = Modifier.animateContentSize()) {
    Column {
        Row { /* header */ }
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn()) {
            Column { /* details */ }
        }
    }
}
```

### Filter Chips
```kotlin
LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    items(filters) { filter ->
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) primary else surfaceVariant)
                .clickable { onSelect(filter) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) { Text(label) }
    }
}
```

### Step Wizard
```kotlin
var currentStep by remember { mutableIntStateOf(0) }
StepIndicator(currentStep = currentStep, totalSteps = 3)
when (currentStep) {
    0 -> StepBasicInfo(...)
    1 -> StepSchedule(...)
    2 -> StepAdvanced(...)
}
// Back/Next buttons at bottom
```

## Database Patterns

### Entity with BaseEntity
```kotlin
@Entity(tableName = "table_name")
data class MyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String? = null,
    // ... fields
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val isDeleted: Boolean = false,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) : BaseEntity
```

### DAO with Flow
```kotlin
@Dao
interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MyEntity): Long
    
    @Query("SELECT * FROM table WHERE isDeleted = 0 ORDER BY field ASC")
    fun getAll(): Flow<List<MyEntity>>
    
    @Query("UPDATE table SET field = :value WHERE id = :id")
    suspend fun updateField(id: Long, value: Type)
}
```

### Repository
```kotlin
class MyRepository(private val dao: MyDao) {
    fun getAll(): Flow<List<MyEntity>> = dao.getAll()
    suspend fun insert(entity: MyEntity): Long = dao.insert(entity)
    suspend fun update(entity: MyEntity) = dao.update(entity)
    suspend fun delete(entity: MyEntity) = dao.delete(entity)
}
```

## Color Usage
- **Green (#4CAF50)**: Income, success, positive
- **Red (#FF5252)**: Expense, error, negative
- **Blue (#2196F3)**: Savings, info, pending
- **Purple (#9C27B0)**: Transfers
- **Orange (#FF9800)**: Warnings, withdrawals

## Icon Usage
- `Icons.Default.AttachMoney` - Income
- `Icons.Default.Savings` - Expenses/Savings
- `Icons.Default.SwapHoriz` - Transfers
- `Icons.Default.Repeat` - Recurring
- `Icons.Default.Schedule` - Time/Schedule
- `Icons.Default.CheckCircle` - Success
- `Icons.Default.Error` - Failure

## Common Imports
```kotlin
// Compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Room
import androidx.room.*
```

## ViewModelFactory Pattern
```kotlin
class MyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = MyRepository(db.myDao())
            @Suppress("UNCHECKED_CAST")
            return MyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```
