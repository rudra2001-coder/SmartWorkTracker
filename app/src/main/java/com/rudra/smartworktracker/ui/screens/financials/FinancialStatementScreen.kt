package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FinancialStatementScreen(
    onEditTransaction: (FinancialTransaction) -> Unit = {},
    onAddTransaction: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: FinancialStatementViewModel = viewModel(
        factory = FinancialStatementViewModelFactory(context.applicationContext as Application)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePickerRange by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<FinancialTransaction?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transactionToDelete!!)
                    transactionToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePickerRange) {
        val datePickerState = rememberDateRangePickerState()
        DateRangePickerDialog(
            state = datePickerState,
            onDismiss = { showDatePickerRange = false },
            onConfirm = {
                val start = datePickerState.selectedStartDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                val end = datePickerState.selectedEndDateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                if (start != null && end != null) {
                    viewModel.setDateRange(start, end)
                }
                showDatePickerRange = false
            }
        )
    }

    Scaffold(

        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            SummaryCard(
                totalIncome = uiState.totalIncome,
                totalExpenses = uiState.totalExpenses,
                netFlow = uiState.netFlow
            )

            FilterSection(
                selectedFilter = uiState.filter,
                onFilterSelected = { filter ->
                    if (filter == TransactionFilter.DATE_RANGE) {
                        showDatePickerRange = true
                    } else {
                        viewModel.setFilter(filter)
                    }
                },
                startDate = uiState.startDate,
                endDate = uiState.endDate
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    item {
                        Text(
                            text = if (uiState.filter == TransactionFilter.ALL) "Showing Default (50 Income + 50 Expense)" else "Showing Filtered Results",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(items = uiState.transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onEdit = onEditTransaction,
                            onDelete = { transactionToDelete = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.displayName) },
                    leadingIcon = if (selectedFilter == filter) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        if (selectedFilter == TransactionFilter.DATE_RANGE && startDate != null && endDate != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${startDate} to ${endDate}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onFilterSelected(TransactionFilter.ALL) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = state.selectedEndDateMillis != null) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = state,
            title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
            headline = { 
                Text(
                    text = "Select your start and end dates",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(totalIncome: Double, totalExpenses: Double, netFlow: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Financial Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            IncomeExpenseBar(income = totalIncome, expense = totalExpenses, modifier = Modifier.fillMaxWidth().height(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat(label = "Income", amount = totalIncome, color = MaterialTheme.colorScheme.primary)
                SummaryStat(label = "Expense", amount = totalExpenses, color = MaterialTheme.colorScheme.error)
                SummaryStat(
                    label = "Net Flow", 
                    amount = netFlow, 
                    color = if (netFlow >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, amount: Double, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun TransactionItem(
    transaction: FinancialTransaction,
    onEdit: (FinancialTransaction) -> Unit,
    onDelete: (FinancialTransaction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Card(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(
                        if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.LOAN_RECEIVE)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.LOAN_RECEIVE)
                            Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.LOAN_RECEIVE)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.note.ifEmpty { transaction.type.name.replace("_", " ") },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = formatTransactionDate(transaction.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.LOAN_RECEIVE)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { onEdit(transaction); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(transaction); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.rotate(rotation))
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("Source", transaction.source.name)
                    if (transaction.destination != null) DetailRow("Destination", transaction.destination.name)
                    DetailRow("Transaction Type", transaction.type.name.replace("_", " "))
                    if (transaction.note.isNotEmpty()) DetailRow("Note", transaction.note)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IncomeExpenseBar(income: Double, expense: Double, modifier: Modifier = Modifier) {
    val total = (income + expense).coerceAtLeast(1.0)
    val incomeRatio = (income / total).toFloat()

    Row(modifier = modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(incomeRatio).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth().background(MaterialTheme.colorScheme.error))
    }
}

fun formatTransactionDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun formatCurrency(amount: Double): String {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }
    return currencyFormat.format(amount).replace("$", "৳")
}
