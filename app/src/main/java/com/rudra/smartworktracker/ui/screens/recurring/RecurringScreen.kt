package com.rudra.smartworktracker.ui.screens.recurring

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.ExpenseCategories
import com.rudra.smartworktracker.data.entity.IncomeCategories
import com.rudra.smartworktracker.data.entity.PreferredTime
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.entity.WeekdayAdjustment
import com.rudra.smartworktracker.engine.PatternSuggestion
import com.rudra.smartworktracker.ui.screens.recurring.RecurringViewModel.RecurringUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val AmberSurface = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: RecurringViewModel = viewModel(
        factory = RecurringViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRuleSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringRule?>(null) }
    var showManualExecutionDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tabs = listOf("Rules", "Transactions", "Calendar", "History")

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showManualExecutionDialog = true },
                    containerColor = SapphireBlue,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Manual Execution",
                        tint = Color.White
                    )
                }
                FloatingActionButton(
                    onClick = { showAddRuleSheet = true },
                    containerColor = EmeraldGreen
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Rule",
                        tint = Color.White
                    )
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
                pausedRulesCount = uiState.pausedRulesCount,
                upcomingTransactionsCount = uiState.upcomingTransactions.size,
                totalIncomeThisMonth = uiState.totalIncomeThisMonth,
                totalExpensesThisMonth = uiState.totalExpensesThisMonth,
                executedThisMonth = uiState.executedThisMonth,
                failedThisMonth = uiState.failedThisMonth,
                monthlyImpact = uiState.monthlyImpact
            )

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> RulesTab(
                    rules = uiState.rules,
                    patternSuggestions = uiState.patternSuggestions,
                    onToggleRule = { viewModel.toggleRuleActive(it) },
                    onTogglePause = { viewModel.toggleRulePaused(it) },
                    onEditRule = { editingRule = it },
                    onDeleteRule = { viewModel.deleteRule(it) },
                    onExecuteNow = { viewModel.executeRuleNow(it) }
                )
                1 -> TransactionsTab(
                    transactions = uiState.allTransactions,
                    onSkipTransaction = { viewModel.skipTransaction(it) }
                )
                2 -> CalendarTab(
                    transactions = uiState.upcomingTransactions,
                    rules = uiState.rules
                )
                3 -> HistoryTab(viewModel = viewModel)
            }
        }

        if (showManualExecutionDialog) {
            ManualExecutionDialog(
                onDismiss = { showManualExecutionDialog = false },
                onExecute = { rulesToExecute ->
                    viewModel.manualExecuteRules(rulesToExecute)
                    showManualExecutionDialog = false
                },
                rules = uiState.rules.filter { it.isActive && !it.isPaused }
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
    }
}

@Composable
fun RecurringHeader(
    activeRulesCount: Int,
    pausedRulesCount: Int,
    upcomingTransactionsCount: Int,
    totalIncomeThisMonth: Double,
    totalExpensesThisMonth: Double,
    executedThisMonth: Int,
    failedThisMonth: Int,
    monthlyImpact: com.rudra.smartworktracker.engine.MonthlyImpact?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, CardShape, clip = false),
            shape = CardShape,
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(VioletPurple, SapphireBlue)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recurring Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = "Active",
                            value = activeRulesCount.toString(),
                            icon = Icons.Default.PlayArrow
                        )
                        StatItem(
                            label = "Paused",
                            value = pausedRulesCount.toString(),
                            icon = Icons.Default.Pause
                        )
                        StatItem(
                            label = "Upcoming",
                            value = upcomingTransactionsCount.toString(),
                            icon = Icons.Default.CalendarMonth
                        )
                    }

                    if (monthlyImpact != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Net Monthly: ৳${"%,.0f".format(monthlyImpact.netCashflow)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (monthlyImpact.netCashflow >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradientStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AttachMoney,
                label = "Monthly Income",
                value = "৳${"%,.0f".format(totalIncomeThisMonth)}",
                gradient = listOf(EmeraldGreen, Color(0xFF34D399)),
                surfaceTint = GreenSurface
            )
            GradientStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Savings,
                label = "Monthly Expenses",
                value = "৳${"%,.0f".format(totalExpensesThisMonth)}",
                gradient = listOf(CoralRed, GoldenAmber),
                surfaceTint = RedSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Succeeded",
                value = executedThisMonth.toString(),
                color = EmeraldGreen
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Failed",
                value = failedThisMonth.toString(),
                color = CoralRed
            )
            MiniStatCard(
                modifier = Modifier.weight(1f),
                label = "Success Rate",
                value = if (executedThisMonth + failedThisMonth > 0)
                    "${(executedThisMonth * 100 / (executedThisMonth + failedThisMonth))}%"
                else "N/A",
                color = SapphireBlue
            )
        }
    }
}

@Composable
fun GradientStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    gradient: List<Color>,
    surfaceTint: Color
) {
    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradient))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier.shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = Color.White
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RulesTab(
    rules: List<RecurringRule>,
    patternSuggestions: List<PatternSuggestion>,
    onToggleRule: (RecurringRule) -> Unit,
    onTogglePause: (RecurringRule) -> Unit,
    onEditRule: (RecurringRule) -> Unit,
    onDeleteRule: (RecurringRule) -> Unit,
    onExecuteNow: (RecurringRule) -> Unit
) {
    if (rules.isEmpty() && patternSuggestions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No recurring rules yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Tap + to add your first rule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (patternSuggestions.isNotEmpty()) {
                item {
                    PatternSuggestionCard(suggestions = patternSuggestions)
                }
            }

            val sortedRules = rules.sortedByDescending { it.isActive }
            items(sortedRules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { onToggleRule(rule) },
                    onTogglePause = { onTogglePause(rule) },
                    onEdit = { onEditRule(rule) },
                    onDelete = { onDeleteRule(rule) },
                    onExecuteNow = { onExecuteNow(rule) }
                )
            }
        }
    }
}

@Composable
fun PatternSuggestionCard(suggestions: List<PatternSuggestion>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = AmberSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = GoldenAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pattern Detected",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            suggestions.take(3).forEach { suggestion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = suggestion.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "৳${"%,.0f".format(suggestion.amount)} - ${suggestion.frequency.name.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                if (suggestion.confidence > 0.7f) EmeraldGreen.copy(alpha = 0.1f)
                                else GoldenAmber.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(suggestion.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (suggestion.confidence > 0.7f) EmeraldGreen else GoldenAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

@Composable
fun RuleCard(
    rule: RecurringRule,
    onToggle: () -> Unit,
    onTogglePause: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExecuteNow: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardBg by animateColorAsState(
        targetValue = if (rule.isActive) Color.White else Color(0xFFF5F5F5),
        animationSpec = tween(300),
        label = "cardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getTransactionTypeColor(rule.transactionType)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTransactionTypeIcon(rule.transactionType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (rule.isPaused) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(ChipShape)
                                        .background(GoldenAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PAUSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldenAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = getFrequencyText(rule.frequency),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = rule.isActive && !rule.isPaused,
                    onCheckedChange = {
                        if (rule.isActive && rule.isPaused) onTogglePause()
                        else if (rule.isActive) onTogglePause()
                        else onToggle()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "৳${"%,.0f".format(rule.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rule.transactionType == TransactionType.INCOME)
                            EmeraldGreen else CoralRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Next Execution",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dateFormat.format(Date(rule.nextExecutionDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (rule.maxExecutions != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Progress: ${rule.executedCount}/${rule.maxExecutions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = (rule.executedCount.toFloat() / rule.maxExecutions).coerceIn(0f, 1f),
                        modifier = Modifier.weight(1f).height(6.dp).clip(ChipShape),
                        color = if (rule.executedCount >= rule.maxExecutions) EmeraldGreen else SapphireBlue,
                        trackColor = Color(0xFFE5E7EB)
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
                    if (rule.isActive) {
                        IconButton(onClick = onTogglePause) {
                            Icon(
                                imageVector = if (rule.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (rule.isPaused) "Resume" else "Pause",
                                tint = if (rule.isPaused) EmeraldGreen else GoldenAmber
                            )
                        }
                    }
                    if (rule.isActive && !rule.isPaused) {
                        IconButton(onClick = onExecuteNow) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Execute Now",
                                tint = SapphireBlue
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CoralRed
                        )
                    }
                }
            }

            if (rule.executedCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Executed ${rule.executedCount} times | Total: ৳${"%,.0f".format(rule.totalExecutedAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Rule") },
            text = { Text("Delete '${rule.name}'? This rule has executed ${rule.executedCount} times (total ৳${"%,.0f".format(rule.totalExecutedAmount)}).") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = CoralRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
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
            .clip(ChipShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionsTab(
    transactions: List<RecurringTransaction>,
    onSkipTransaction: (RecurringTransaction) -> Unit
) {
    var statusFilter by remember { mutableStateOf<RecurringTransactionStatus?>(null) }

    val filteredTransactions = if (statusFilter != null) {
        transactions.filter { it.status == statusFilter }
    } else transactions

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    label = "All",
                    selected = statusFilter == null,
                    onClick = { statusFilter = null }
                )
            }
            RecurringTransactionStatus.entries.forEach { status ->
                item {
                    FilterChip(
                        label = status.name,
                        selected = statusFilter == status,
                        onClick = { statusFilter = status }
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onSkip = { onSkipTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = PillShape,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SapphireBlue else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (selected) 4.dp else 2.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TransactionItem(
    transaction: RecurringTransaction,
    onSkip: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(getTransactionTypeColor(transaction.transactionType)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTransactionTypeIcon(transaction.transactionType),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = transaction.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = dateFormat.format(Date(transaction.scheduledDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                StatusBadge(status = transaction.status)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳${"%,.0f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.transactionType == TransactionType.INCOME)
                        EmeraldGreen else CoralRed
                )

                if (transaction.status == RecurringTransactionStatus.PENDING ||
                    transaction.status == RecurringTransactionStatus.CONFIRMED) {
                    TextButton(onClick = onSkip) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Skip",
                            modifier = Modifier.size(16.dp),
                            tint = CoralRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip", color = CoralRed, style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (transaction.failureReason != null) {
                    Text(
                        text = transaction.failureReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralRed.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RecurringTransactionStatus) {
    val (color, text) = when (status) {
        RecurringTransactionStatus.PENDING -> Pair(SapphireBlue, "Pending")
        RecurringTransactionStatus.CONFIRMED -> Pair(EmeraldGreen, "Confirmed")
        RecurringTransactionStatus.EXECUTING -> Pair(GoldenAmber, "Executing")
        RecurringTransactionStatus.EXECUTED -> Pair(Color(0xFF4CAF50), "Executed")
        RecurringTransactionStatus.FAILED -> Pair(CoralRed, "Failed")
        RecurringTransactionStatus.SKIPPED -> Pair(Color(0xFF757575), "Skipped")
        RecurringTransactionStatus.CANCELLED -> Pair(Color(0xFF616161), "Cancelled")
    }

    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(ChipShape)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CalendarTab(
    transactions: List<RecurringTransaction>,
    rules: List<RecurringRule>
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = monthFormat.format(calendar.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No upcoming transactions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions.take(50)) { transaction ->
                    CalendarTransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun CalendarTransactionItem(transaction: RecurringTransaction) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dateFormat.format(Date(transaction.scheduledDate)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        if (transaction.transactionType == TransactionType.INCOME)
                            EmeraldGreen else CoralRed,
                        RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "৳${"%,.0f".format(transaction.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (transaction.transactionType == TransactionType.INCOME)
                            EmeraldGreen else CoralRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = transaction.status)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleContent(
    existingRule: RecurringRule? = null,
    onSave: (RecurringRule) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(existingRule?.name ?: "") }
    var description by remember { mutableStateOf(existingRule?.description ?: "") }
    var amount by remember { mutableStateOf(existingRule?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existingRule?.category ?: "") }
    var tags by remember { mutableStateOf(existingRule?.tags ?: "") }
    var notes by remember { mutableStateOf(existingRule?.notes ?: "") }
    var maxExecutions by remember { mutableStateOf(existingRule?.maxExecutions?.toString() ?: "") }

    var transactionType by remember {
        mutableStateOf(existingRule?.transactionType ?: TransactionType.EXPENSE)
    }
    var sourceAccountId by remember {
        mutableStateOf(existingRule?.sourceAccountId ?: 0L)
    }
    var destinationAccountId by remember {
        mutableStateOf(existingRule?.destinationAccountId)
    }
    var frequency by remember {
        mutableStateOf(existingRule?.frequency ?: RecurringFrequency.MONTHLY)
    }
    var selectedDaysOfWeek by remember {
        mutableStateOf(existingRule?.selectedDaysOfWeek ?: emptyList())
    }
    var priority by remember {
        mutableStateOf(existingRule?.priority ?: RecurringPriority.MEDIUM)
    }
    var preferredTime by remember {
        mutableStateOf(existingRule?.preferredTime ?: PreferredTime.MORNING)
    }
    var weekdayAdjustment by remember {
        mutableStateOf(existingRule?.weekdayAdjustment ?: WeekdayAdjustment.SKIP)
    }
    var startDate by remember { mutableStateOf(existingRule?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(existingRule?.endDate) }
    var autoExecute by remember { mutableStateOf(existingRule?.autoExecute ?: true) }
    var skipIfHoliday by remember { mutableStateOf(existingRule?.skipIfHoliday ?: false) }
    var minimumBalance by remember { mutableStateOf(existingRule?.minimumBalanceRequired?.toString() ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var weekdayAdjustmentExpanded by remember { mutableStateOf(false) }
    var sourceExpanded by remember { mutableStateOf(false) }
    var destinationExpanded by remember { mutableStateOf(false) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    val transactionTypes = TransactionType.entries.filter {
        it != TransactionType.LOAN_BORROW && it != TransactionType.LOAN_LEND &&
        it != TransactionType.LOAN_REPAY && it != TransactionType.LOAN_RECEIVE && it != TransactionType.EMI_PAID
    }
    val frequencies = RecurringFrequency.entries
    val priorities = RecurringPriority.entries
    val times = PreferredTime.entries
    val adjustments = WeekdayAdjustment.entries
    val db = AppDatabase.getDatabase(context)
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }

    LaunchedEffect(Unit) {
        accounts = db.accountDao().getAllAccountsList()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = if (existingRule != null) "Edit Recurring Rule" else "Add Recurring Rule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("Transaction Details")

        DropdownField(
            label = "Transaction Type",
            value = transactionType.name.replace("_", " "),
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it },
            options = transactionTypes,
            optionLabel = { it.name.replace("_", " ") },
            onSelect = {
                transactionType = it
                typeExpanded = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            placeholder = { Text("e.g., Monthly Rent") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount *") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("৳") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        val categoriesForType = when (transactionType) {
            TransactionType.INCOME -> IncomeCategories.categories
            TransactionType.EXPENSE -> ExpenseCategories.categories
            else -> listOf("Other")
        }

        var categoryExpanded by remember { mutableStateOf(false) }
        val categoryValue = if (category.isEmpty()) "Select Category" else category

        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = categoryValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoriesForType.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Schedule")

        DropdownField(
            label = "Frequency",
            value = getFrequencyDisplayName(frequency),
            expanded = frequencyExpanded,
            onExpandedChange = { frequencyExpanded = it },
            options = frequencies,
            optionLabel = { getFrequencyDisplayName(it) },
            onSelect = {
                frequency = it
                if (it != RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
                    selectedDaysOfWeek = emptyList()
                }
                frequencyExpanded = false
            }
        )

        if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Select Days of Week",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val daysOfWeek = DayOfWeek.entries

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                daysOfWeek.forEach { day ->
                    val isSelected = selectedDaysOfWeek.contains(day)
                    Card(
                        onClick = {
                            selectedDaysOfWeek = if (isSelected) {
                                selectedDaysOfWeek - day
                            } else {
                                selectedDaysOfWeek + day
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SapphireBlue else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = ChipShape
                    ) {
                        Text(
                            text = day.shortName,
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { selectedDaysOfWeek = daysOfWeek.toList() },
                    enabled = selectedDaysOfWeek.size != daysOfWeek.size,
                    modifier = Modifier.weight(1f)
                ) { Text("Select All") }
                TextButton(
                    onClick = { selectedDaysOfWeek = emptyList() },
                    enabled = selectedDaysOfWeek.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) { Text("Clear All") }
            }

            if (selectedDaysOfWeek.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlueSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Next Executions:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val nextDates = calculateNextExecutionDates(
                            selectedDays = selectedDaysOfWeek,
                            startFrom = System.currentTimeMillis(),
                            count = 3
                        )
                        nextDates.forEachIndexed { index, date ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Execution ${index + 1}:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(date)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Timing & Priority")

        DropdownField(
            label = "Priority",
            value = priority.name,
            expanded = priorityExpanded,
            onExpandedChange = { priorityExpanded = it },
            options = priorities,
            optionLabel = { it.name },
            onSelect = {
                priority = it
                priorityExpanded = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownField(
            label = "Preferred Time",
            value = preferredTime.name,
            expanded = timeExpanded,
            onExpandedChange = { timeExpanded = it },
            options = times,
            optionLabel = { it.name },
            onSelect = {
                preferredTime = it
                timeExpanded = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DropdownField(
            label = "Weekend Adjustment",
            value = weekdayAdjustment.name.replace("_", " "),
            expanded = weekdayAdjustmentExpanded,
            onExpandedChange = { weekdayAdjustmentExpanded = it },
            options = adjustments,
            optionLabel = { it.name.replace("_", " ") },
            onSelect = {
                weekdayAdjustment = it
                weekdayAdjustmentExpanded = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Accounts")

        DropdownField(
            label = "Source Account",
            value = accounts.find { it.id == sourceAccountId }?.let { "${it.name} (৳${it.balance.toInt()})" } ?: "Select Account",
            expanded = sourceExpanded,
            onExpandedChange = { sourceExpanded = it },
            options = accounts,
            optionLabel = { "${it.name} (৳${it.balance.toInt()})" },
            onSelect = {
                sourceAccountId = it.id
                sourceExpanded = false
            }
        )

        if (transactionType == TransactionType.TRANSFER) {
            Spacer(modifier = Modifier.height(12.dp))

            DropdownField(
                label = "Destination Account",
                value = accounts.find { it.id == destinationAccountId }?.let { "${it.name} (৳${it.balance.toInt()})" } ?: "Select Account",
                expanded = destinationExpanded,
                onExpandedChange = { destinationExpanded = it },
                options = accounts.filter { it.id != sourceAccountId },
                optionLabel = { "${it.name} (৳${it.balance.toInt()})" },
                onSelect = {
                    destinationAccountId = it.id
                    destinationExpanded = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Dates")

        OutlinedTextField(
            value = dateFormat.format(Date(startDate)),
            onValueChange = {},
            label = { Text("Start Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showStartDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartDatePicker = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = if (endDate != null) dateFormat.format(Date(endDate!!)) else "No end date (ongoing)",
            onValueChange = {},
            label = { Text("End Date (optional)") },
            readOnly = true,
            trailingIcon = {
                Row {
                    if (endDate != null) {
                        IconButton(onClick = { endDate = null }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Clear end date", tint = CoralRed)
                        }
                    }
                    IconButton(onClick = { showEndDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStartDatePicker = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Limits")

        OutlinedTextField(
            value = maxExecutions,
            onValueChange = { maxExecutions = it.filter { c -> c.isDigit() } },
            label = { Text("Max Executions (optional)") },
            placeholder = { Text("Leave empty for unlimited") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (transactionType == TransactionType.EXPENSE) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = minimumBalance,
                onValueChange = { minimumBalance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Minimum Balance Required") },
                placeholder = { Text("Leave empty for no minimum") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("৳") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Labels")

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma-separated)") },
            placeholder = { Text("e.g., bills, essential, monthly") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Options")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Auto Execute", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = autoExecute, onCheckedChange = { autoExecute = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Skip on Weekend", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = skipIfHoliday, onCheckedChange = { skipIfHoliday = it })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) { Text("Cancel") }

            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val minBalance = minimumBalance.toDoubleOrNull()
                    val maxExec = maxExecutions.toIntOrNull()

                    if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS && selectedDaysOfWeek.isEmpty()) {
                        Toast.makeText(context, "Please select at least one day", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val initialNextDate = if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS && selectedDaysOfWeek.isNotEmpty()) {
                        calculateInitialNextDate(selectedDaysOfWeek, startDate)
                    } else startDate

                    val rule = RecurringRule(
                        id = existingRule?.id ?: 0,
                        uuid = existingRule?.uuid,
                        name = name,
                        description = description.ifBlank { null },
                        transactionType = transactionType,
                        amount = amountDouble,
                        category = category.ifBlank { null },
                        sourceAccountId = sourceAccountId,
                        destinationAccountId = if (transactionType == TransactionType.TRANSFER) destinationAccountId else null,
                        frequency = frequency,
                        selectedDaysOfWeek = if (frequency == RecurringFrequency.WEEKLY_SPECIFIC_DAYS) selectedDaysOfWeek else null,
                        priority = priority,
                        preferredTime = preferredTime,
                        weekdayAdjustment = weekdayAdjustment,
                        startDate = startDate,
                        endDate = endDate,
                        nextExecutionDate = existingRule?.nextExecutionDate ?: initialNextDate,
                        minimumBalanceRequired = minBalance,
                        autoExecute = autoExecute,
                        isActive = existingRule?.isActive ?: true,
                        isPaused = existingRule?.isPaused ?: false,
                        maxExecutions = maxExec,
                        executedCount = existingRule?.executedCount ?: 0,
                        totalExecutedAmount = existingRule?.totalExecutedAmount ?: 0.0,
                        lastExecutedDate = existingRule?.lastExecutedDate,
                        skipIfHoliday = skipIfHoliday,
                        tags = tags.ifBlank { null },
                        notes = notes.ifBlank { null }
                    )
                    onSave(rule)
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && amount.isNotBlank()
            ) { Text(if (existingRule != null) "Update" else "Save") }
        }

        Spacer(modifier = Modifier.height(32.dp))
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

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
fun ManualExecutionDialog(
    onDismiss: () -> Unit,
    onExecute: (List<RecurringRule>) -> Unit,
    rules: List<RecurringRule>
) {
    var selectedRules by remember { mutableStateOf(setOf<RecurringRule>()) }
    var selectAll by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manual Execution")
                TextButton(onClick = {
                    selectAll = !selectAll
                    selectedRules = if (selectAll) rules.toSet() else emptySet()
                }) { Text(if (selectAll) "Deselect All" else "Select All") }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (rules.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active rules to execute",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn {
                        items(rules) { rule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRules = if (selectedRules.contains(rule)) {
                                            selectedRules - rule
                                        } else {
                                            selectedRules + rule
                                        }
                                    }
                                    .padding(vertical = 8.dp),
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = rule.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "৳${"%,.0f".format(rule.amount)} - ${rule.frequency.name.lowercase()}",
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
            Button(
                onClick = { onExecute(selectedRules.toList()) },
                enabled = selectedRules.isNotEmpty()
            ) { Text("Execute Selected (${selectedRules.size})") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ExecutionResultDialog(
    result: RecurringViewModel.ExecutionResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.success) EmeraldGreen else CoralRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (result.success) "Execution Successful" else "Execution Completed with Errors")
            }
        },
        text = {
            Column {
                Text(
                    text = "Summary:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GreenSurface),
                    shape = ChipShape
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DetailRow("Successful", "${result.successCount}", EmeraldGreen)
                        DetailRow("Failed", "${result.failureCount}", if (result.failureCount > 0) CoralRed else EmeraldGreen)
                        DetailRow("Total Amount", "৳${"%,.0f".format(result.totalAmount)}", MaterialTheme.colorScheme.onSurface)
                        if (result.totalIncome > 0) DetailRow("Income", "৳${"%,.0f".format(result.totalIncome)}", EmeraldGreen)
                        if (result.totalExpenses > 0) DetailRow("Expenses", "৳${"%,.0f".format(result.totalExpenses)}", CoralRed)
                    }
                }

                if (result.failedRules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Failed Rules:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    result.failedRules.forEach { (ruleName, reason) ->
                        Text(
                            text = "• $ruleName: $reason",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun HistoryTab(viewModel: RecurringViewModel) {
    val executionHistory by viewModel.executionHistory.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (executionHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "No execution history yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Rules can be executed manually or automatically",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        } else {
            items(executionHistory) { execution ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, CardShape, clip = false),
                    shape = CardShape,
                    elevation = CardDefaults.cardElevation(0.dp)
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
                                    tint = if (execution.success) EmeraldGreen else CoralRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateFormat.format(Date(execution.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${execution.successCount} / ${execution.totalCount}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (execution.successCount == execution.totalCount) EmeraldGreen else CoralRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progress by animateFloatAsState(
                            targetValue = execution.successCount.toFloat() / execution.totalCount.coerceAtLeast(1),
                            animationSpec = tween(800), label = "progress"
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(ChipShape),
                            color = if (execution.successCount == execution.totalCount) EmeraldGreen
                            else if (execution.successCount > 0) GoldenAmber
                            else CoralRed,
                            trackColor = Color(0xFFE5E7EB)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Total: ৳${"%,.0f".format(execution.totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (execution.failedRules.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            execution.failedRules.forEach { (name, reason) ->
                                Text(
                                    text = "✗ $name: $reason",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralRed.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getTransactionTypeColor(type: TransactionType): Color {
    return when (type) {
        TransactionType.INCOME -> EmeraldGreen
        TransactionType.EXPENSE -> CoralRed
        TransactionType.SAVINGS_ADD -> SapphireBlue
        TransactionType.SAVINGS_WITHDRAW -> GoldenAmber
        TransactionType.TRANSFER -> VioletPurple
        else -> Color(0xFF607D8B)
    }
}

fun getTransactionTypeIcon(type: TransactionType): ImageVector {
    return when (type) {
        TransactionType.INCOME -> Icons.Default.AttachMoney
        TransactionType.EXPENSE -> Icons.Default.Savings
        TransactionType.SAVINGS_ADD -> Icons.Default.TrendingUp
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
        RecurringFrequency.WEEKLY_SPECIFIC_DAYS -> "Weekly (Specific Days)"
    }
}

fun getFrequencyDisplayName(frequency: RecurringFrequency): String = getFrequencyText(frequency)

fun calculateNextExecutionDates(
    selectedDays: List<DayOfWeek>,
    startFrom: Long,
    count: Int
): List<Long> {
    val dates = mutableListOf<Long>()
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = startFrom

    val selectedCalendarDays = selectedDays.map { DayOfWeek.toCalendarDay(it) }.sorted()

    repeat(count) {
        val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

        var nextDay: Int? = null
        for (day in selectedCalendarDays) {
            if (day > currentDayOfWeek) {
                nextDay = day
                break
            }
        }

        if (nextDay != null) {
            val daysToAdd = nextDay - currentDayOfWeek
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
        } else {
            val daysToAdd = (7 - currentDayOfWeek) + selectedCalendarDays.first()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
        }

        dates.add(calendar.timeInMillis)
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }

    return dates
}

fun calculateInitialNextDate(
    selectedDays: List<DayOfWeek>,
    startDate: Long
): Long {
    if (selectedDays.isEmpty()) return startDate

    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = startDate
    val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

    val selectedCalendarDays = selectedDays.map { DayOfWeek.toCalendarDay(it) }.sorted()

    for (day in selectedCalendarDays) {
        if (day >= currentDayOfWeek) {
            val daysToAdd = day - currentDayOfWeek
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
            return calendar.timeInMillis
        }
    }

    val daysToAdd = (7 - currentDayOfWeek) + selectedCalendarDays.first()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, daysToAdd)
    return calendar.timeInMillis
}
