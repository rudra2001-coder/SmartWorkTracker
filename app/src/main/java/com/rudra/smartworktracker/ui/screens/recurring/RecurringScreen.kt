package com.rudra.smartworktracker.ui.screens.recurring

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.PreferredTime
import com.rudra.smartworktracker.data.entity.RecurringFrequency
import com.rudra.smartworktracker.data.entity.RecurringPriority
import com.rudra.smartworktracker.data.entity.RecurringRule
import com.rudra.smartworktracker.data.entity.RecurringTransaction
import com.rudra.smartworktracker.data.entity.RecurringTransactionStatus
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.repository.RecurringRepository
import com.rudra.smartworktracker.engine.RecurringEngine
import com.rudra.smartworktracker.ui.screens.recurring.RecurringViewModel.RecurringUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val viewModel: RecurringViewModel = viewModel(
        factory = RecurringViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddRuleSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringRule?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val tabs = listOf("Rules", "Transactions", "Calendar")
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRuleSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            RecurringHeader(
                activeRulesCount = uiState.activeRulesCount,
                upcomingTransactionsCount = uiState.upcomingTransactions.size,
                totalIncomeThisMonth = uiState.totalIncomeThisMonth,
                totalExpensesThisMonth = uiState.totalExpensesThisMonth
            )
            
            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> RulesTab(
                    rules = uiState.rules,
                    onToggleRule = { viewModel.toggleRuleActive(it) },
                    onEditRule = { editingRule = it },
                    onDeleteRule = { viewModel.deleteRule(it) },
                    onExecuteNow = { viewModel.executeRuleNow(it) }
                )
                1 -> TransactionsTab(
                    transactions = uiState.allTransactions,
                    onSkipTransaction = { viewModel.skipTransaction(it) }
                )
                2 -> CalendarTab(
                    rules = uiState.rules,
                    transactions = uiState.upcomingTransactions
                )
            }
        }
        
        // Add Rule Bottom Sheet
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
        
        // Edit Rule Bottom Sheet
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
    upcomingTransactionsCount: Int,
    totalIncomeThisMonth: Double,
    totalExpensesThisMonth: Double
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recurring Transactions",
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
                        label = "Active Rules",
                        value = activeRulesCount.toString(),
                        icon = Icons.Default.Schedule
                    )
                    StatItem(
                        label = "Upcoming",
                        value = upcomingTransactionsCount.toString(),
                        icon = Icons.Default.CalendarMonth
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Income/Month",
                        value = "$${String.format("%.2f", totalIncomeThisMonth)}",
                        icon = Icons.Default.AttachMoney,
                        valueColor = Color(0xFF4CAF50)
                    )
                    StatItem(
                        label = "Expenses/Month",
                        value = "$${String.format("%.2f", totalExpensesThisMonth)}",
                        icon = Icons.Default.Savings,
                        valueColor = Color(0xFFFF5252)
                    )
                }
            }
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
    onToggleRule: (RecurringRule) -> Unit,
    onEditRule: (RecurringRule) -> Unit,
    onDeleteRule: (RecurringRule) -> Unit,
    onExecuteNow: (RecurringRule) -> Unit
) {
    if (rules.isEmpty()) {
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
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

@Composable
fun RuleCard(
    rule: RecurringRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExecuteNow: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                        text = "$${String.format("%.2f", rule.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (rule.transactionType == TransactionType.INCOME) 
                            Color(0xFF4CAF50) else Color(0xFFFF5252),
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Priority Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = rule.priority)
                
                Row {
                    IconButton(onClick = onExecuteNow) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Execute Now",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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
            .clip(RoundedCornerShape(4.dp))
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
    if (transactions.isEmpty()) {
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onSkip = { onSkipTransaction(transaction) }
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: RecurringTransaction,
    onSkip: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.transactionType == TransactionType.INCOME) 
                    Color(0xFF4CAF50) else Color(0xFFFF5252)
            )
            
            if (transaction.status == RecurringTransactionStatus.PENDING || 
                transaction.status == RecurringTransactionStatus.CONFIRMED) {
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Default.ToggleOff,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.error
                    )
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
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun CalendarTab(
    rules: List<RecurringRule>,
    transactions: List<RecurringTransaction>
) {
    // Simple calendar view - could be enhanced with a proper calendar component
    val dateFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = dateFormat.format(calendar.time),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Upcoming transactions list grouped by date
        val groupedTransactions = transactions.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.scheduledDate }
                .get(Calendar.DAY_OF_YEAR)
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(transactions.take(30)) { transaction ->
                CalendarTransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun CalendarTransactionItem(transaction: RecurringTransaction) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
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
                            Color(0xFF4CAF50) else Color(0xFFFF5252),
                        RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (transaction.transactionType == TransactionType.INCOME)
                        Color(0xFF4CAF50) else Color(0xFFFF5252)
                )
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
    
    var transactionType by remember { 
        mutableStateOf(existingRule?.transactionType ?: TransactionType.EXPENSE) 
    }
    var sourceAccount by remember { 
        mutableStateOf(existingRule?.sourceAccount ?: AccountType.BALANCE) 
    }
    var destinationAccount by remember { 
        mutableStateOf(existingRule?.destinationAccount) 
    }
    var frequency by remember { 
        mutableStateOf(existingRule?.frequency ?: RecurringFrequency.MONTHLY) 
    }
    var priority by remember { 
        mutableStateOf(existingRule?.priority ?: RecurringPriority.MEDIUM) 
    }
    var preferredTime by remember { 
        mutableStateOf(existingRule?.preferredTime ?: PreferredTime.MORNING) 
    }
    var startDate by remember { mutableStateOf(existingRule?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(existingRule?.endDate) }
    var autoExecute by remember { mutableStateOf(existingRule?.autoExecute ?: true) }
    var minimumBalance by remember { mutableStateOf(existingRule?.minimumBalanceRequired?.toString() ?: "") }
    
    var typeExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var sourceExpanded by remember { mutableStateOf(false) }
    var destinationExpanded by remember { mutableStateOf(false) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val context = LocalContext.current
    
    val transactionTypes = TransactionType.values().filter { 
        it != TransactionType.LOAN_BORROW && it != TransactionType.LOAN_LEND && 
        it != TransactionType.LOAN_REPAY && it != TransactionType.LOAN_RECEIVE && it != TransactionType.EMI_PAID
    }
    val frequencies = RecurringFrequency.values()
    val priorities = RecurringPriority.values()
    val times = PreferredTime.values()
    val accounts = AccountType.values()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = if (existingRule != null) "Edit Rule" else "Add Recurring Rule",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Transaction Type
        Text(
            text = "Transaction Type",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = transactionType.name.replace("_", " "),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                transactionTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.replace("_", " ")) },
                        onClick = {
                            transactionType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name *") },
            placeholder = { Text("e.g., Monthly Rent") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Amount
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Amount *") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Text("$") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Category
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            placeholder = { Text("e.g., Bills, Salary") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Frequency
        Text(
            text = "Frequency",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = frequencyExpanded,
            onExpandedChange = { frequencyExpanded = it }
        ) {
            OutlinedTextField(
                value = frequency.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = frequencyExpanded,
                onDismissRequest = { frequencyExpanded = false }
            ) {
                frequencies.forEach { freq ->
                    DropdownMenuItem(
                        text = { Text(freq.name) },
                        onClick = {
                            frequency = freq
                            frequencyExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Priority
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = priorityExpanded,
            onExpandedChange = { priorityExpanded = it }
        ) {
            OutlinedTextField(
                value = priority.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = priorityExpanded,
                onDismissRequest = { priorityExpanded = false }
            ) {
                priorities.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.name) },
                        onClick = {
                            priority = p
                            priorityExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Source Account
        Text(
            text = "Source Account",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = sourceExpanded,
            onExpandedChange = { sourceExpanded = it }
        ) {
            OutlinedTextField(
                value = sourceAccount.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = sourceExpanded,
                onDismissRequest = { sourceExpanded = false }
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = { Text(acc.name) },
                        onClick = {
                            sourceAccount = acc
                            sourceExpanded = false
                        }
                    )
                }
            }
        }
        
        // Destination Account (for transfers)
        if (transactionType == TransactionType.TRANSFER) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Destination Account",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ExposedDropdownMenuBox(
                expanded = destinationExpanded,
                onExpandedChange = { destinationExpanded = it }
            ) {
                OutlinedTextField(
                    value = destinationAccount?.name ?: "Select",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = destinationExpanded,
                    onDismissRequest = { destinationExpanded = false }
                ) {
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            onClick = {
                                destinationAccount = acc
                                destinationExpanded = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Start Date
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
        
        // Preferred Time
        Text(
            text = "Preferred Time",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = timeExpanded,
            onExpandedChange = { timeExpanded = it }
        ) {
            OutlinedTextField(
                value = preferredTime.name,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = timeExpanded,
                onDismissRequest = { timeExpanded = false }
            ) {
                times.forEach { t ->
                    DropdownMenuItem(
                        text = { Text(t.name) },
                        onClick = {
                            preferredTime = t
                            timeExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Minimum Balance (for expenses)
        if (transactionType == TransactionType.EXPENSE) {
            OutlinedTextField(
                value = minimumBalance,
                onValueChange = { minimumBalance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Minimum Balance Required") },
                placeholder = { Text("Leave empty for no minimum") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Auto Execute Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auto Execute",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = autoExecute,
                onCheckedChange = { autoExecute = it }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
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
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull() ?: 0.0
                    val minBalance = minimumBalance.toDoubleOrNull()
                    
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
                        priority = priority,
                        preferredTime = preferredTime,
                        startDate = startDate,
                        endDate = endDate,
                        nextExecutionDate = existingRule?.nextExecutionDate ?: startDate,
                        minimumBalanceRequired = minBalance,
                        autoExecute = autoExecute,
                        isActive = existingRule?.isActive ?: true
                    )
                    onSave(rule)
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && amount.isNotBlank()
            ) {
                Text(if (existingRule != null) "Update" else "Save")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Helper functions
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
    }
}
