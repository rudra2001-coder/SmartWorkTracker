package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val SlateGray = Color(0xFF64748B)

private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val AmberSurface = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

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
    var transactionToDelete by remember { mutableStateOf<UnifiedTransaction?>(null) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = CardShape,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed, modifier = Modifier.size(20.dp))
                    Text("Delete Transaction", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Are you sure you want to delete this transaction? This will remove both the debit and credit entries.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    shape = ChipShape
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) { Text("Cancel") }
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp).background(
                                brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Financial Statement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Double-entry ledger overview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SummaryCard(
                        totalIncome = uiState.totalIncome,
                        totalExpenses = uiState.totalExpenses,
                        netFlow = uiState.netFlow
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
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
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SapphireBlue)
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage!!,
                            color = CoralRed,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else if (uiState.transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(72.dp).background(
                                        brush = Brush.linearGradient(listOf(SlateGray.copy(alpha = 0.3f), SlateGray.copy(alpha = 0.1f))),
                                        shape = CircleShape
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(36.dp), tint = SlateGray.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No transactions found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Add income or expense to see them here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).background(
                                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Text("Double-Entry Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(items = uiState.transactions, key = { it.id }) { transaction ->
                        DoubleEntryTransactionItem(
                            transaction = transaction,
                            onDelete = { transactionToDelete = transaction }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionFilter.values().forEach { filter ->
                val selected = selectedFilter == filter
                Surface(
                    shape = PillShape,
                    color = if (selected) VioletPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.shadow(if (selected) 4.dp else 2.dp, PillShape, clip = false)
                ) {
                    Row(
                        modifier = Modifier.clickable { onFilterSelected(filter) }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = VioletPurple)
                        }
                        Text(
                            filter.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) VioletPurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (startDate != null && endDate != null) {
            Surface(
                color = BlueSurface,
                shape = PillShape,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = SapphireBlue)
                    Text(
                        text = "${startDate} to ${endDate}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireBlue
                    )
                    IconButton(onClick = { onFilterSelected(TransactionFilter.ALL) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp), tint = SapphireBlue)
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
    var animatedIncome by remember { mutableFloatStateOf(0f) }
    var animatedExpense by remember { mutableFloatStateOf(0f) }
    var animatedNet by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(totalIncome, totalExpenses, netFlow) {
        delay(200)
        animatedIncome = totalIncome.toFloat()
        delay(100)
        animatedExpense = totalExpenses.toFloat()
        delay(100)
        animatedNet = netFlow.toFloat()
    }

    val incomeAnim by animateFloatAsState(animatedIncome, tween(1000), label = "fi")
    val expenseAnim by animateFloatAsState(animatedExpense, tween(1000), label = "fe")
    val netAnim by animateFloatAsState(animatedNet, tween(1000), label = "fn")

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).background(
                        brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Financial Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            IncomeExpenseBar(income = totalIncome, expense = totalExpenses, modifier = Modifier.fillMaxWidth().height(10.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(label = "Income", amount = incomeAnim.toDouble(), color = EmeraldGreen)
                SummaryStat(label = "Expense", amount = expenseAnim.toDouble(), color = CoralRed)
                SummaryStat(
                    label = "Net Flow",
                    amount = netAnim.toDouble(),
                    color = if (netFlow >= 0) EmeraldGreen else CoralRed
                )
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun DoubleEntryTransactionItem(
    transaction: UnifiedTransaction,
    onDelete: (UnifiedTransaction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isDebit = transaction.entryType == EntryType.DEBIT
    val isIncomeType = transaction.type == TransactionType.INCOME || transaction.type == TransactionType.LOAN_RECEIVE
    val accentColor = if (isIncomeType) EmeraldGreen else CoralRed
    val entryLabel = if (isDebit) "DR" else "CR"
    val entryLabelColor = if (isDebit) SapphireBlue else EmeraldGreen

    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDebit) BlueSurface else GreenSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = entryLabelColor.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = entryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = entryLabelColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.debitAccount != null) {
                        Text(
                            text = transaction.debitAccount,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (transaction.debitAccount != null && transaction.creditAccount != null) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp).padding(horizontal = 4.dp),
                            tint = SlateGray
                        )
                    }
                    if (transaction.creditAccount != null) {
                        Text(
                            text = transaction.creditAccount,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = PillShape, color = accentColor.copy(alpha = 0.1f)) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = " • ${formatTransactionDate(transaction.date)} • ${transaction.sourceType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            if (!transaction.id.endsWith("_credit")) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onDelete(transaction) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = CoralRed.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseBar(income: Double, expense: Double, modifier: Modifier = Modifier) {
    val total = (income + expense).coerceAtLeast(1.0)
    val incomeRatio = (income / total).toFloat().coerceIn(0f, 1f)

    Box(
        modifier = modifier.clip(RoundedCornerShape(5.dp)).background(CoralRed.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(incomeRatio).background(
                brush = Brush.horizontalGradient(listOf(EmeraldGreen, EmeraldGreen.copy(alpha = 0.8f))),
                shape = RoundedCornerShape(5.dp)
            )
        )
    }
}

fun formatTransactionDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun formatCurrency(amount: Double): String {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("bn", "BD")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }
    return currencyFormat.format(amount)
}

fun formatCurrencyStatic(amount: Double): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("bn", "BD")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }
    return currencyFormat.format(amount)
}
