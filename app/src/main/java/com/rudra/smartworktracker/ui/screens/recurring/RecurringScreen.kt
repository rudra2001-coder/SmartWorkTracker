package com.rudra.smartworktracker.ui.screens.recurring

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.PreferredTime
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.IncomeCategories
import com.rudra.smartworktracker.data.entity.ExpenseCategories
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: RecurringViewModel = viewModel(factory = RecurringViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRuleSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringRule?>(null) }
    var showManualExecutionDialog by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val prefs = remember { context.getSharedPreferences("recurring_prefs", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarding_shown", false)) }

    val tabs = listOf("Rules", "Transactions", "Calendar", "Insights", "History")

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showTemplateSheet = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Repeat, contentDescription = "Templates", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = { showManualExecutionDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Execute", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                FloatingActionButton(
                    onClick = { showAddRuleSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Rule")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RecurringHeader(
                activeRulesCount = uiState.activeRulesCount,
                upcomingTransactionsCount = uiState.upcomingTransactions.size,
                totalIncomeThisMonth = uiState.totalIncomeThisMonth,
                totalExpensesThisMonth = uiState.totalExpensesThisMonth
            )

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> RulesTab(
                    rules = if (searchQuery.isBlank() && selectedFilter == RuleFilter.ALL) uiState.rules else uiState.filteredRules,
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    isRefreshing = uiState.isRefreshing,
                    isMultiSelectMode = uiState.isMultiSelectMode,
                    selectedRuleIds = uiState.selectedRuleIds,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onFilterChange = { viewModel.updateFilter(it) },
                    onToggleRule = { viewModel.toggleRuleActive(it) },
                    onEditRule = { editingRule = it },
                    onDeleteRule = { viewModel.deleteRule(it) },
                    onExecuteNow = { viewModel.executeRuleNow(it) },
                    onRefresh = { viewModel.refreshData() },
                    onToggleMultiSelect = { viewModel.toggleMultiSelect() },
                    onToggleRuleSelection = { viewModel.toggleRuleSelection(it) },
                    onSelectAll = { viewModel.selectAllRules() },
                    onDeselectAll = { viewModel.deselectAllRules() },
                    onDeleteSelected = { viewModel.deleteSelectedRules() },
                    onToggleSelected = { viewModel.toggleSelectedRulesActive() }
                )
                1 -> TransactionsTab(
                    transactions = uiState.allTransactions,
                    isRefreshing = uiState.isRefreshing,
                    pendingConfirmations = uiState.pendingConfirmations,
                    onSkipTransaction = { viewModel.skipTransaction(it) },
                    onConfirmTransaction = { viewModel.confirmTransaction(it) },
                    onConfirmAllPending = { viewModel.confirmAllPending() },
                    onSnoozeTransaction = { viewModel.snoozeTransaction(it) },
                    onSnoozeAllFailed = { viewModel.snoozeAllFailed() },
                    onRefresh = { viewModel.refreshData() }
                )
                2 -> CalendarTab(
                    rules = uiState.rules,
                    transactions = uiState.upcomingTransactions
                )
                3 -> InsightsTab(
                    yearlyProjection = uiState.yearlyProjection,
                    patternSuggestions = uiState.patternSuggestions,
                    spendingAlerts = uiState.spendingAlerts,
                    categoryBreakdown = uiState.categoryBreakdown,
                    onAddFromPattern = { suggestion ->
                        val rule = RecurringRule(
                            name = suggestion.name,
                            transactionType = TransactionType.EXPENSE,
                            amount = suggestion.amount,
                            category = suggestion.category,
                            sourceAccount = AccountType.BALANCE,
                            frequency = suggestion.frequency,
                            startDate = System.currentTimeMillis(),
                            nextExecutionDate = System.currentTimeMillis(),
                            preferredTime = PreferredTime.MORNING,
                            priority = RecurringPriority.MEDIUM,
                            autoExecute = true,
                            isActive = true
                        )
                        viewModel.addRule(rule)
                    }
                )
                4 -> HistoryTab(viewModel = viewModel)
            }
        }

        if (showManualExecutionDialog) {
            ManualExecutionDialog(
                onDismiss = { showManualExecutionDialog = false },
                onExecute = { rulesToExecute ->
                    viewModel.manualExecuteRules(rulesToExecute)
                    showManualExecutionDialog = false
                },
                rules = uiState.rules.filter { it.isActive }
            )
        }

        if (uiState.lastExecutionResult != null) {
            ExecutionResultDialog(
                result = uiState.lastExecutionResult!!,
                onDismiss = { viewModel.clearExecutionResult() }
            )
        }

        if (showAddRuleSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddRuleSheet = false },
                sheetState = sheetState
            ) {
                AddRuleContent(
                    onSave = { rule ->
                        viewModel.addRule(rule)
                        showAddRuleSheet = false
                    },
                    onCancel = { showAddRuleSheet = false }
                )
            }
        }

        if (editingRule != null) {
            ModalBottomSheet(
                onDismissRequest = { editingRule = null },
                sheetState = sheetState
            ) {
                AddRuleContent(
                    existingRule = editingRule,
                    onSave = { rule ->
                        viewModel.updateRule(rule)
                        editingRule = null
                    },
                    onCancel = { editingRule = null }
                )
            }
        }

        if (showTemplateSheet) {
            TemplateSelectionSheet(
                templates = viewModel.getRuleTemplates(),
                onSelectTemplate = { template ->
                    viewModel.addRuleFromTemplate(template)
                    showTemplateSheet = false
                    Toast.makeText(context, "${template.name} added!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { showTemplateSheet = false }
            )
        }
    }

    if (showOnboarding) {
        RecurringOnboardingDialog(
            onDismiss = {
                showOnboarding = false
                prefs.edit().putBoolean("onboarding_shown", true).apply()
            }
        )
    }
}

@Composable
fun RecurringHeader(
    activeRulesCount: Int,
    upcomingTransactionsCount: Int,
    totalIncomeThisMonth: Double,
    totalExpensesThisMonth: Double
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HeaderStatItem(label = "Active", value = activeRulesCount.toString())
                    HeaderStatItem(label = "Upcoming", value = upcomingTransactionsCount.toString())
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HeaderStatItem(
                        label = "Income/Month",
                        value = "$${String.format("%.0f", totalIncomeThisMonth)}",
                        valueColor = Color(0xFFBBF7D0)
                    )
                    HeaderStatItem(
                        label = "Expenses/Month",
                        value = "$${String.format("%.0f", totalExpensesThisMonth)}",
                        valueColor = Color(0xFFFECACA)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val net = totalIncomeThisMonth - totalExpensesThisMonth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Monthly: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$${String.format("%.0f", net)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (net >= 0) Color(0xFFBBF7D0) else Color(0xFFFECACA),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderStatItem(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = "$label: $value" }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesTab(
    rules: List<RecurringRule>,
    searchQuery: String,
    selectedFilter: RuleFilter,
    isRefreshing: Boolean,
    isMultiSelectMode: Boolean,
    selectedRuleIds: Set<Long>,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (RuleFilter) -> Unit,
    onToggleRule: (RecurringRule) -> Unit,
    onEditRule: (RecurringRule) -> Unit,
    onDeleteRule: (RecurringRule) -> Unit,
    onExecuteNow: (RecurringRule) -> Unit,
    onRefresh: () -> Unit,
    onToggleMultiSelect: () -> Unit,
    onToggleRuleSelection: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelected: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search rules...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(onClick = onToggleMultiSelect) {
                Icon(
                    if (isMultiSelectMode) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = if (isMultiSelectMode) "Exit multi-select" else "Multi-select"
                )
            }
        }

        AnimatedVisibility(visible = isMultiSelectMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${selectedRuleIds.size} selected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onSelectAll) { Text("All") }
                    TextButton(onClick = onDeselectAll) { Text("None") }
                    IconButton(onClick = onToggleSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Toggle active", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeleteSelected) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(RuleFilter.values()) { filter ->
                FilterChip(
                    label = when (filter) {
                        RuleFilter.ALL -> "All"
                        RuleFilter.ACTIVE -> "Active"
                        RuleFilter.INACTIVE -> "Inactive"
                        RuleFilter.INCOME -> "Income"
                        RuleFilter.EXPENSE -> "Expense"
                        RuleFilter.SAVINGS -> "Savings"
                        RuleFilter.TRANSFER -> "Transfer"
                    },
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (rules.isEmpty() && isRefreshing) {
            LoadingSkeleton()
        } else if (rules.isEmpty() && !isRefreshing) {
            EmptyState(
                icon = Icons.Default.Repeat,
                title = "No rules found",
                subtitle = "Tap + to add your first recurring rule"
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        if (isMultiSelectMode) {
                            RuleCard(
                                rule = rule,
                                onToggle = { onToggleRule(rule) },
                                onEdit = { onEditRule(rule) },
                                onDelete = { onDeleteRule(rule) },
                                onExecuteNow = { onExecuteNow(rule) },
                                isSelected = selectedRuleIds.contains(rule.id),
                                isMultiSelectMode = true,
                                onToggleSelection = { onToggleRuleSelection(rule.id) }
                            )
                        } else {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        onDeleteRule(rule)
                                        true
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                RuleCard(
                                    rule = rule,
                                    onToggle = { onToggleRule(rule) },
                                    onEdit = { onEditRule(rule) },
                                    onDelete = { onDeleteRule(rule) },
                                    onExecuteNow = { onExecuteNow(rule) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .semantics {
                contentDescription = "$label filter, ${if (isSelected) "selected" else "not selected"}"
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun RuleCard(
    rule: RecurringRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExecuteNow: () -> Unit,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onToggleSelection: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val expandState = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection() },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(getTransactionTypeColor(rule.transactionType)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTransactionTypeIcon(rule.transactionType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = getFrequencyText(rule.frequency),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = rule.isActive,
                    onCheckedChange = { onToggle() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$${String.format("%.2f", rule.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rule.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Next: ${dateFormat.format(Date(rule.nextExecutionDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = rule.priority)

                Row {
                    TooltipWrapper(tooltipText = "Expand details") {
                        IconButton(onClick = { expandState.value = !expandState.value }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (expandState.value) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = "Details",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    TooltipWrapper(tooltipText = "Execute now") {
                        IconButton(onClick = onExecuteNow, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Send, contentDescription = "Execute", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TooltipWrapper(tooltipText = "Edit rule") {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    TooltipWrapper(tooltipText = "Delete rule") {
                        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = expandState.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider()
                    DetailRow("Category", rule.category ?: "Not set")
                    DetailRow("Source", rule.sourceAccount.name)
                    DetailRow("Auto Execute", if (rule.autoExecute) "Yes" else "No")
                    if (rule.minimumBalanceRequired != null) {
                        DetailRow("Min Balance", "$${String.format("%.2f", rule.minimumBalanceRequired)}")
                    }
                    DetailRow("Created", dateFormat.format(Date(rule.createdAt)))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Rule") },
            text = { Text("Are you sure you want to delete '${rule.name}'?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PriorityBadge(priority: RecurringPriority) {
    val (color, text) = when (priority) {
        RecurringPriority.CRITICAL -> Pair(Color(0xFFD32F2F), "CRITICAL")
        RecurringPriority.HIGH -> Pair(Color(0xFFF57C00), "HIGH")
        RecurringPriority.MEDIUM -> Pair(Color(0xFF1976D2), "MEDIUM")
        RecurringPriority.LOW -> Pair(Color(0xFF388E3C), "LOW")
        RecurringPriority.OPTIONAL -> Pair(Color(0xFF757575), "OPTIONAL")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .semantics { contentDescription = "Priority: $text" }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
fun TransactionsTab(
    transactions: List<RecurringTransaction>,
    isRefreshing: Boolean,
    pendingConfirmations: List<RecurringTransaction>,
    onSkipTransaction: (RecurringTransaction) -> Unit,
    onConfirmTransaction: (RecurringTransaction) -> Unit,
    onConfirmAllPending: () -> Unit,
    onSnoozeTransaction: (RecurringTransaction) -> Unit,
    onSnoozeAllFailed: () -> Unit,
    onRefresh: () -> Unit
) {
    val failedCount = transactions.count { it.status == RecurringTransactionStatus.FAILED }

    Column(modifier = Modifier.fillMaxSize()) {
        if (pendingConfirmations.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${pendingConfirmations.size} pending confirmation",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Button(onClick = onConfirmAllPending, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Confirm All", fontSize = 12.sp)
                    }
                }
            }
        }

        if (failedCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$failedCount failed transaction(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(onClick = onSnoozeAllFailed, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Retry Tomorrow", fontSize = 12.sp)
                    }
                }
            }
        }

        if (transactions.isEmpty() && !isRefreshing) {
            EmptyState(
                icon = Icons.Default.Schedule,
                title = "No transactions yet",
                subtitle = "Transactions will appear here when rules execute"
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onSkip = { onSkipTransaction(transaction) },
                            onConfirm = { onConfirmTransaction(transaction) },
                            onSnooze = { onSnoozeTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: RecurringTransaction,
    onSkip: () -> Unit,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormat.format(Date(transaction.scheduledDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                StatusBadge(status = transaction.status)
            }

            Text(
                text = "$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (transaction.status == RecurringTransactionStatus.PENDING && !transaction.isConfirmed) {
                    IconButton(onClick = onConfirm, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirm", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                    }
                }
                if (transaction.status == RecurringTransactionStatus.FAILED) {
                    IconButton(onClick = onSnooze, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = "Snooze 1 day", tint = Color(0xFFF57C00), modifier = Modifier.size(16.dp))
                    }
                }
                if (transaction.status == RecurringTransactionStatus.PENDING || transaction.status == RecurringTransactionStatus.CONFIRMED) {
                    IconButton(onClick = onSkip, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Skip", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RecurringTransactionStatus) {
    val (color, text) = when (status) {
        RecurringTransactionStatus.PENDING -> Pair(Color(0xFF1976D2), "Pending")
        RecurringTransactionStatus.CONFIRMED -> Pair(Color(0xFF388E3C), "Confirmed")
        RecurringTransactionStatus.EXECUTING -> Pair(Color(0xFFF57C00), "Executing")
        RecurringTransactionStatus.EXECUTED -> Pair(Color(0xFF4CAF50), "Executed")
        RecurringTransactionStatus.FAILED -> Pair(Color(0xFFD32F2F), "Failed")
        RecurringTransactionStatus.SKIPPED -> Pair(Color(0xFF757575), "Skipped")
        RecurringTransactionStatus.CANCELLED -> Pair(Color(0xFF616161), "Cancelled")
    }

    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp
        )
    }
}

@Composable
fun CalendarTab(
    rules: List<RecurringRule>,
    transactions: List<RecurringTransaction>
) {
    val calendar = remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("dd", Locale.getDefault()) }

    val year = calendar.value.get(Calendar.YEAR)
    val month = calendar.value.get(Calendar.MONTH)
    val daysInMonth = calendar.value.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendar.value.apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK)

    val transactionsByDay = remember(transactions, year, month) {
        transactions.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.scheduledDate }
            txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month
        }.groupBy { tx ->
            Calendar.getInstance().apply { timeInMillis = tx.scheduledDate }.get(Calendar.DAY_OF_MONTH)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                calendar.value = (calendar.value.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) {
                Text("<", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = dateFormat.format(calendar.value.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                calendar.value = (calendar.value.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            }) {
                Text(">", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val totalCells = firstDayOfWeek - 1 + daysInMonth
        val rows = (totalCells + 6) / 7

        Column {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col
                        val day = dayIndex - (firstDayOfWeek - 1) + 1
                        if (day in 1..daysInMonth) {
                            val dayTransactions = transactionsByDay[day] ?: emptyList()
                            val hasTransactions = dayTransactions.isNotEmpty()
                            val isToday = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == day &&
                                    Calendar.getInstance().get(Calendar.MONTH) == month &&
                                    Calendar.getInstance().get(Calendar.YEAR) == year

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            hasTransactions -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday || hasTransactions) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasTransactions) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                            dayTransactions.take(3).forEach { tx ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (tx.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val selectedDayTransactions = transactionsByDay[Calendar.getInstance().get(Calendar.DAY_OF_MONTH)]?.takeIf {
            Calendar.getInstance().get(Calendar.MONTH) == month && Calendar.getInstance().get(Calendar.YEAR) == year
        } ?: emptyList()

        if (selectedDayTransactions.isNotEmpty()) {
            Text("Today's Transactions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            selectedDayTransactions.forEach { tx ->
                CalendarTransactionItem(transaction = tx)
            }
        } else if (transactions.isNotEmpty()) {
            Text("Upcoming", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            transactions.take(5).forEach { tx ->
                CalendarTransactionItem(transaction = tx)
            }
        }
    }
}

@Composable
fun CalendarTransactionItem(transaction: RecurringTransaction) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(
                    text = dateFormat.format(Date(transaction.scheduledDate)),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(
                        if (transaction.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252),
                        RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (transaction.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    fontSize = 11.sp
                )
            }

            StatusBadge(status = transaction.status)
        }
    }
}

@Composable
fun InsightsTab(
    yearlyProjection: com.rudra.smartworktracker.engine.YearlyProjection?,
    patternSuggestions: List<com.rudra.smartworktracker.engine.PatternSuggestion>,
    spendingAlerts: List<SpendingAlert>,
    categoryBreakdown: Map<String, Double>,
    onAddFromPattern: (com.rudra.smartworktracker.engine.PatternSuggestion) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (spendingAlerts.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Spending Alerts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        spendingAlerts.forEach { alert ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(alert.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(
                                    "${String.format("%.0f", alert.percentage)}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (alert.percentage > 90f) MaterialTheme.colorScheme.error else Color(0xFFF57C00)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { alert.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (alert.percentage > 90f) MaterialTheme.colorScheme.error else Color(0xFFF57C00)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        if (categoryBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        val total = categoryBreakdown.values.sum()
                        categoryBreakdown.entries.sortedByDescending { it.value }.forEach { (category, amount) ->
                            val percentage = if (total > 0) (amount / total * 100).toFloat() else 0f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format("%.0f", amount)} (${String.format("%.0f", percentage)}%)", style = MaterialTheme.typography.bodySmall)
                            }
                            LinearProgressIndicator(
                                progress = { percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yearly Projection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (yearlyProjection != null) {
                        InsightRow("Yearly Income", "$${String.format("%.0f", yearlyProjection.totalYearlyIncome)}", Color(0xFF4CAF50))
                        InsightRow("Yearly Expenses", "$${String.format("%.0f", yearlyProjection.totalYearlyExpenses)}", Color(0xFFFF5252))
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        InsightRow(
                            "Net Yearly",
                            "$${String.format("%.0f", yearlyProjection.netYearly)}",
                            if (yearlyProjection.netYearly >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )

                        if (yearlyProjection.categoryBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Expense Breakdown:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            yearlyProjection.categoryBreakdown.forEach { (category, amount) ->
                                InsightRow(category, "$${String.format("%.0f", amount)}", MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        if (patternSuggestions.isNotEmpty()) {
            item {
                Text(
                    text = "Suggested Rules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(patternSuggestions) { suggestion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = suggestion.name, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${suggestion.frequency.name} - $${String.format("%.2f", suggestion.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Confidence: ${String.format("%.0f", suggestion.confidence * 100)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(onClick = { onAddFromPattern(suggestion) }) {
                            Text("Add", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun HistoryTab(viewModel: RecurringViewModel) {
    val executionHistory by viewModel.executionHistory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    if (executionHistory.isEmpty() && !uiState.isRefreshing) {
        EmptyState(
            icon = Icons.Default.History,
            title = "No execution history",
            subtitle = "Tap play button to test execution"
        )
    } else {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshData() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(executionHistory) { execution ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (execution.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (execution.success) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = dateFormat.format(Date(execution.timestamp)), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "${execution.successCount}/${execution.totalCount}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { execution.successCount.toFloat() / execution.totalCount.coerceAtLeast(1) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (execution.successCount == execution.totalCount) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Total: $${String.format("%.2f", execution.totalAmount)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun TemplateSelectionSheet(
    templates: List<RuleTemplate>,
    onSelectTemplate: (RuleTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Quick Templates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap a template to add it instantly",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        templates.forEach { template ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectTemplate(template) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getTransactionTypeColor(template.transactionType)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getTransactionTypeIcon(template.transactionType),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = template.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(
                                text = template.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Text(
                        text = "$${String.format("%.0f", template.amount)}",
                        fontWeight = FontWeight.Bold,
                        color = if (template.transactionType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleContent(
    existingRule: RecurringRule? = null,
    onSave: (RecurringRule) -> Unit,
    onCancel: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(existingRule?.name ?: "") }
    var description by remember { mutableStateOf(existingRule?.description ?: "") }
    var amount by remember { mutableStateOf(existingRule?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existingRule?.category ?: "") }
    var transactionType by remember { mutableStateOf(existingRule?.transactionType ?: TransactionType.EXPENSE) }
    var sourceAccount by remember { mutableStateOf(existingRule?.sourceAccount ?: AccountType.BALANCE) }
    var destinationAccount by remember { mutableStateOf(existingRule?.destinationAccount) }
    var frequency by remember { mutableStateOf(existingRule?.frequency ?: RecurringFrequency.MONTHLY) }
    var selectedDaysOfWeek by remember { mutableStateOf(existingRule?.selectedDaysOfWeek ?: emptyList()) }
    var priority by remember { mutableStateOf(existingRule?.priority ?: RecurringPriority.MEDIUM) }
    var preferredTime by remember { mutableStateOf(existingRule?.preferredTime ?: PreferredTime.MORNING) }
    var startDate by remember { mutableStateOf(existingRule?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(existingRule?.endDate) }
    var autoExecute by remember { mutableStateOf(existingRule?.autoExecute ?: true) }
    var minimumBalance by remember { mutableStateOf(existingRule?.minimumBalanceRequired?.toString() ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var sourceExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    val transactionTypes = TransactionType.values().filter {
        it != TransactionType.LOAN_BORROW && it != TransactionType.LOAN_LEND &&
        it != TransactionType.LOAN_REPAY && it != TransactionType.LOAN_RECEIVE && it != TransactionType.EMI_PAID
    }
    val categoriesForType = when (transactionType) {
        TransactionType.INCOME -> IncomeCategories.categories
        TransactionType.EXPENSE -> ExpenseCategories.categories
        else -> listOf("Other")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = if (existingRule != null) "Edit Rule" else "Add Rule",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        StepIndicator(currentStep = currentStep, totalSteps = 3)

        when (currentStep) {
            0 -> StepBasicInfo(
                name = name, onNameChange = { name = it },
                description = description, onDescriptionChange = { description = it },
                amount = amount, onAmountChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                transactionType = transactionType, onTypeChange = { transactionType = it },
                transactionTypes = transactionTypes,
                typeExpanded = typeExpanded, onTypeExpandedChange = { typeExpanded = it },
                category = category, onCategoryChange = { category = it },
                categoriesForType = categoriesForType,
                categoryExpanded = categoryExpanded, onCategoryExpandedChange = { categoryExpanded = it }
            )
            1 -> StepSchedule(
                frequency = frequency, onFrequencyChange = { frequency = it },
                frequencyExpanded = frequencyExpanded, onFrequencyExpandedChange = { frequencyExpanded = it },
                selectedDaysOfWeek = selectedDaysOfWeek, onDaysChange = { selectedDaysOfWeek = it },
                preferredTime = preferredTime, onTimeChange = { preferredTime = it },
                timeExpanded = timeExpanded, onTimeExpandedChange = { timeExpanded = it },
                startDate = startDate, onStartDateChange = { startDate = it },
                endDate = endDate, onEndDateChange = { endDate = it },
                context = context, dateFormat = dateFormat
            )
            2 -> StepAdvanced(
                priority = priority, onPriorityChange = { priority = it },
                priorityExpanded = priorityExpanded, onPriorityExpandedChange = { priorityExpanded = it },
                sourceAccount = sourceAccount, onSourceChange = { sourceAccount = it },
                sourceExpanded = sourceExpanded, onSourceExpandedChange = { sourceExpanded = it },
                destinationAccount = destinationAccount, onDestinationChange = { destinationAccount = it },
                transactionType = transactionType,
                minimumBalance = minimumBalance, onMinimumBalanceChange = { minimumBalance = it },
                autoExecute = autoExecute, onAutoExecuteChange = { autoExecute = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentStep > 0) {
                Button(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) { Text("Back") }
            }

            if (currentStep < 2) {
                Button(
                    onClick = { currentStep++ },
                    modifier = Modifier.weight(1f),
                    enabled = if (currentStep == 0) name.isNotBlank() && amount.isNotBlank() else true
                ) { Text("Next") }
            } else {
                Button(
                    onClick = {
                        val amountDouble = amount.toDoubleOrNull() ?: 0.0
                        val minBalance = minimumBalance.toDoubleOrNull()
                        val initialNextDate = if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS && selectedDaysOfWeek.isNotEmpty()) {
                            calculateInitialNextDate(selectedDaysOfWeek, startDate)
                        } else { startDate }

                        val rule = RecurringRule(
                            id = existingRule?.id ?: 0,
                            uuid = existingRule?.uuid,
                            name = name,
                            description = description.ifBlank { null },
                            transactionType = transactionType,
                            amount = amountDouble,
                            category = category.ifBlank { null },
                            sourceAccount = sourceAccount,
                            destinationAccount = destinationAccount,
                            frequency = frequency,
                            selectedDaysOfWeek = if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) selectedDaysOfWeek else null,
                            priority = priority,
                            preferredTime = preferredTime,
                            startDate = startDate,
                            endDate = endDate,
                            nextExecutionDate = existingRule?.nextExecutionDate ?: initialNextDate,
                            minimumBalanceRequired = minBalance,
                            autoExecute = autoExecute,
                            isActive = existingRule?.isActive ?: true
                        )
                        onSave(rule)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank() && amount.isNotBlank()
                ) { Text(if (existingRule != null) "Update" else "Save") }
            }
        }

        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }

        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { startDate = it }
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index <= currentStep) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(
                            if (index < currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepBasicInfo(
    name: String, onNameChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    amount: String, onAmountChange: (String) -> Unit,
    transactionType: TransactionType, onTypeChange: (TransactionType) -> Unit,
    transactionTypes: List<TransactionType>,
    typeExpanded: Boolean, onTypeExpandedChange: (Boolean) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    categoriesForType: List<String>,
    categoryExpanded: Boolean, onCategoryExpandedChange: (Boolean) -> Unit
) {
    Column {
        Text("Basic Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Transaction Type", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = onTypeExpandedChange) {
            OutlinedTextField(
                value = transactionType.name.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { onTypeExpandedChange(false) }) {
                transactionTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.replace("_", " ")) },
                        onClick = { onTypeChange(type); onTypeExpandedChange(false) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name, onValueChange = onNameChange,
            label = { Text("Name *") },
            placeholder = { Text("e.g., Monthly Rent") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount, onValueChange = onAmountChange,
            label = { Text("Amount *") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("$") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = onCategoryExpandedChange) {
            OutlinedTextField(
                value = category.ifBlank { "Select Category" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { onCategoryExpandedChange(false) }) {
                categoriesForType.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = { onCategoryChange(cat); onCategoryExpandedChange(false) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepSchedule(
    frequency: RecurringFrequency, onFrequencyChange: (RecurringFrequency) -> Unit,
    frequencyExpanded: Boolean, onFrequencyExpandedChange: (Boolean) -> Unit,
    selectedDaysOfWeek: List<DayOfWeek>, onDaysChange: (List<DayOfWeek>) -> Unit,
    preferredTime: PreferredTime, onTimeChange: (PreferredTime) -> Unit,
    timeExpanded: Boolean, onTimeExpandedChange: (Boolean) -> Unit,
    startDate: Long, onStartDateChange: (Long) -> Unit,
    endDate: Long?, onEndDateChange: (Long?) -> Unit,
    context: android.content.Context,
    dateFormat: SimpleDateFormat
) {
    Column {
        Text("Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Frequency", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = frequencyExpanded, onExpandedChange = onFrequencyExpandedChange) {
            OutlinedTextField(
                value = getFrequencyDisplayName(frequency),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = frequencyExpanded, onDismissRequest = { onFrequencyExpandedChange(false) }) {
                RecurringFrequency.values().forEach { freq ->
                    DropdownMenuItem(
                        text = { Text(getFrequencyDisplayName(freq)) },
                        onClick = { onFrequencyChange(freq); onFrequencyExpandedChange(false) }
                    )
                }
            }
        }

        if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Select Days", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DayOfWeek.values().forEach { day ->
                    val isSelected = selectedDaysOfWeek.contains(day)
                    Card(
                        onClick = {
                            onDaysChange(if (isSelected) selectedDaysOfWeek - day else selectedDaysOfWeek + day)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = day.shortName,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Preferred Time", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = timeExpanded, onExpandedChange = onTimeExpandedChange) {
            OutlinedTextField(
                value = preferredTime.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = timeExpanded, onDismissRequest = { onTimeExpandedChange(false) }) {
                PreferredTime.values().forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.name) },
                        onClick = { onTimeChange(t); onTimeExpandedChange(false) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dateFormat.format(Date(startDate)),
            onValueChange = {},
            label = { Text("Start Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = startDate }
                    DatePickerDialog(context, { _, year, month, day ->
                        val newCal = Calendar.getInstance()
                        newCal.set(year, month, day)
                        onStartDateChange(newCal.timeInMillis)
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepAdvanced(
    priority: RecurringPriority, onPriorityChange: (RecurringPriority) -> Unit,
    priorityExpanded: Boolean, onPriorityExpandedChange: (Boolean) -> Unit,
    sourceAccount: AccountType, onSourceChange: (AccountType) -> Unit,
    sourceExpanded: Boolean, onSourceExpandedChange: (Boolean) -> Unit,
    destinationAccount: AccountType?, onDestinationChange: (AccountType?) -> Unit,
    transactionType: TransactionType,
    minimumBalance: String, onMinimumBalanceChange: (String) -> Unit,
    autoExecute: Boolean, onAutoExecuteChange: (Boolean) -> Unit
) {
    Column {
        Text("Advanced Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Priority", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = priorityExpanded, onExpandedChange = onPriorityExpandedChange) {
            OutlinedTextField(
                value = priority.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { onPriorityExpandedChange(false) }) {
                RecurringPriority.values().forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.name) },
                        onClick = { onPriorityChange(p); onPriorityExpandedChange(false) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Source Account", style = MaterialTheme.typography.labelMedium)
        ExposedDropdownMenuBox(expanded = sourceExpanded, onExpandedChange = onSourceExpandedChange) {
            OutlinedTextField(
                value = sourceAccount.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = sourceExpanded, onDismissRequest = { onSourceExpandedChange(false) }) {
                AccountType.values().forEach { acc ->
                    DropdownMenuItem(
                        text = { Text(acc.name) },
                        onClick = { onSourceChange(acc); onSourceExpandedChange(false) }
                    )
                }
            }
        }

        if (transactionType == TransactionType.EXPENSE) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = minimumBalance,
                onValueChange = { onMinimumBalanceChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = { Text("Minimum Balance Required") },
                placeholder = { Text("Leave empty for no minimum") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Auto Execute", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = autoExecute, onCheckedChange = onAutoExecuteChange)
        }
    }
}

fun getTransactionTypeColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.INCOME -> Color(0xFF4CAF50)
        TransactionType.EXPENSE -> Color(0xFFFF5252)
        TransactionType.SAVINGS_ADD -> Color(0xFF2196F3)
        TransactionType.SAVINGS_WITHDRAW -> Color(0xFFFF9800)
        TransactionType.TRANSFER -> Color(0xFF9C27B0)
        else -> Color(0xFF607D8B)
    }
}

fun getTransactionTypeIcon(type: TransactionType): ImageVector {
    return when (type) {
        TransactionType.INCOME -> Icons.Default.AttachMoney
        TransactionType.EXPENSE -> Icons.Default.Savings
        TransactionType.SAVINGS_ADD -> Icons.Default.Savings
        TransactionType.SAVINGS_WITHDRAW -> Icons.Default.SwapHoriz
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
        else -> Icons.Default.Repeat
    }
}

fun getFrequencyText(frequency: RecurringFrequency): String {
    return when (frequency) {
        RecurringFrequency.DAILY -> "Daily"
        RecurringFrequency.WEEKLY -> "Weekly"
        RecurringFrequency.BIWEEKLY -> "Every 2 Weeks"
        RecurringFrequency.MONTHLY -> "Monthly"
        RecurringFrequency.QUARTERLY -> "Every 3 Months"
        RecurringFrequency.YEARLY -> "Yearly"
        RecurringFrequency.CUSTOM -> "Custom"
        RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> "Specific Days"
    }
}

fun getFrequencyDisplayName(frequency: RecurringFrequency): String = getFrequencyText(frequency)

@Composable
fun ManualExecutionDialog(
    onDismiss: () -> Unit,
    onExecute: (List<RecurringRule>) -> Unit,
    rules: List<RecurringRule>
) {
    var selectedRules by remember { mutableStateOf(setOf<RecurringRule>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Execution") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (rules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No active rules", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn {
                        items(rules) { rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedRules = if (selectedRules.contains(rule)) selectedRules - rule else selectedRules + rule
                                }.padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedRules.contains(rule),
                                        onCheckedChange = {
                                            selectedRules = if (it) selectedRules + rule else selectedRules - rule
                                        }
                                    )
                                    Column {
                                        Text(rule.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        Text(
                                            "$${String.format("%.2f", rule.amount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onExecute(selectedRules.toList()) }, enabled = selectedRules.isNotEmpty()) {
                Text("Execute (${selectedRules.size})")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ExecutionResultDialog(
    result: RecurringViewModel.ManualExecutionResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFFF5252)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (result.success) "Success" else "Partial Failure")
            }
        },
        text = {
            Column {
                Text("Successful: ${result.successCount}")
                Text("Failed: ${result.failureCount}")
                Text("Total: $${String.format("%.2f", result.totalAmount)}")
                if (result.failedRules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    result.failedRules.forEach { (name, reason) ->
                        Text("• $name: $reason", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
    )
}

fun calculateNextExecutionDates(
    selectedDays: List<DayOfWeek>,
    startFrom: Long,
    count: Int
): List<Long> {
    val dates = mutableListOf<Long>()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = startFrom
    val selectedCalendarDays = selectedDays.map { DayOfWeek.toCalendarDay(it) }.sorted()

    repeat(count) {
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var nextDay: Int? = null
        for (day in selectedCalendarDays) {
            if (day > currentDayOfWeek) { nextDay = day; break }
        }
        if (nextDay != null) {
            calendar.add(Calendar.DAY_OF_YEAR, nextDay - currentDayOfWeek)
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, (7 - currentDayOfWeek) + selectedCalendarDays.first())
        }
        dates.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return dates
}

fun calculateInitialNextDate(selectedDays: List<DayOfWeek>, startDate: Long): Long {
    if (selectedDays.isEmpty()) return startDate
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = startDate
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val selectedCalendarDays = selectedDays.map { DayOfWeek.toCalendarDay(it) }.sorted()

    for (day in selectedCalendarDays) {
        if (day >= currentDayOfWeek) {
            calendar.add(Calendar.DAY_OF_YEAR, day - currentDayOfWeek)
            return calendar.timeInMillis
        }
    }
    calendar.add(Calendar.DAY_OF_YEAR, (7 - currentDayOfWeek) + selectedCalendarDays.first())
    return calendar.timeInMillis
}

// ========== Loading Skeleton ==========
@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Column(
        modifier = modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.value))
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.value))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.value))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.value))
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha.value))
                        )
                    }
                }
            }
        }
    }
}

// ========== Onboarding Dialog ==========
@Composable
fun RecurringOnboardingDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    val steps = listOf(
        Triple("Welcome to Recurring Transactions", "Automate your regular income and expenses so you never miss a payment.", Icons.Default.Repeat),
        Triple("Smart Execution", "Rules execute automatically based on your schedule. You can also run them manually anytime.", Icons.Default.PlayArrow),
        Triple("Insights & Alerts", "Track spending patterns, get alerts when approaching limits, and see yearly projections.", Icons.Default.TrendingUp)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(steps[step].third, contentDescription = null, modifier = Modifier.size(48.dp)) },
        title = { Text(steps[step].first, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(steps[step].second, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("Back") }
                }
                Button(onClick = {
                    if (step < steps.lastIndex) step++ else onDismiss()
                }) {
                    Text(if (step < steps.lastIndex) "Next" else "Get Started")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}

// ========== Tooltip ==========
@Composable
fun TooltipWrapper(
    tooltipText: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = if (enabled) Modifier.clickable { showTooltip = !showTooltip } else Modifier
        ) {
            content()
        }

        AnimatedVisibility(
            visible = showTooltip && enabled,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .widthIn(max = 200.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
            ) {
                Text(
                    text = tooltipText,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        }
    }
}
