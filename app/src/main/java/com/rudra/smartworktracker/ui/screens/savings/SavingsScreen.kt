package com.rudra.smartworktracker.ui.screens.savings

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Savings
import java.text.SimpleDateFormat
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val GreenSurface = Color(0xFFE6FBF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen() {
    val context = LocalContext.current
    val viewModel: SavingsViewModel = viewModel(factory = SavingsViewModelFactory(context.applicationContext as Application))
    val uiState by viewModel.uiState.collectAsState()

    var showHistory by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TimeRange.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {}
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Savings Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Track your savings goals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(
                            onClick = { showHistory = !showHistory },
                            shape = PillShape
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (showHistory) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                item { AnimatedSavingsCard(savings = uiState.savings) }

                item { SavingsStatsCards(stats = uiState.stats) }

                item {
                    FilterChips(
                        selectedRange = selectedFilter,
                        onRangeSelected = {
                            selectedFilter = it
                            viewModel.filterByTimeRange(it)
                        }
                    )
                }

                item {
                    SavingsHistoryChart(history = uiState.filteredHistory.ifEmpty { uiState.savingsHistory })
                }

                item {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Transaction", fontWeight = FontWeight.Bold)
                    }
                }

                if (showHistory && uiState.filteredHistory.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(32.dp).background(
                                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                                    shape = RoundedCornerShape(10.dp)
                                ), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.History, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.toggleSortOrder() }) {
                                Icon(
                                    if (uiState.sortOrder == SortOrder.DESCENDING) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = "Sort"
                                )
                            }
                        }
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            LazyColumn(modifier = Modifier.heightIn(max = 350.dp).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(uiState.filteredHistory.reversed()) { savings ->
                                    SavingsHistoryItem(savings = savings, onDelete = { viewModel.deleteTransaction(savings) })
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { amt: Double, nte: String, isWtd: Boolean ->
                if (isWtd) {
                    viewModel.withdrawFromSavings(amt, nte)
                } else {
                    viewModel.addToSavings(amt, nte)
                }
                showAddDialog = false
            }
        )
    }

    uiState.errorMessage?.let { error ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Snackbar(
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") } }
            ) { Text(error) }
        }
    }

    uiState.successMessage?.let { success ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Snackbar(containerColor = MaterialTheme.colorScheme.primaryContainer) { Text(success) }
        }
    }
}

@Composable
fun FilterChips(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.name.replace("_", " ")) }
            )
        }
    }
}

@Composable
fun AnimatedSavingsCard(savings: Double) {
    val animatedSavings by animateFloatAsState(
        targetValue = savings.toFloat(),
        animationSpec = tween(durationMillis = 1500),
        label = "savings_animation"
    )

    Card(
        modifier = Modifier.fillMaxWidth().shadow(12.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(
                        brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Savings, "Savings", tint = Color.White, modifier = Modifier.size(18.dp)) }
                Text("Current Savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = String.format("%.2f BDT", animatedSavings.toDouble()),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreen,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Surface(shape = PillShape, color = GreenSurface) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.TrendingUp, null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                    Text("Keep saving!", style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SavingsHistoryChart(history: List<Savings>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).clip(ChipShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("No savings history yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(32.dp).background(
                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                    shape = RoundedCornerShape(10.dp)
                ), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.TrendingUp, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Savings Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxSavings = history.maxOfOrNull { it.amount } ?: 1.0
                    val minSavings = history.minOfOrNull { it.amount } ?: 0.0
                    val range = maxSavings - minSavings

                    for (i in 0..4) {
                        val y = size.height * (1 - i * 0.25f)
                        drawLine(color = Color.Gray.copy(alpha = 0.3f), start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
                    }

                    val path = Path()
                    val gradientPath = Path()

                    for (index in history.indices) {
                        val savings = history[index]
                        val x = (index.toFloat() / (history.size - 1).coerceAtLeast(1).toFloat()) * size.width
                        val y = if (range > 0) size.height - ((savings.amount - minSavings) / range * size.height).toFloat() else size.height / 2

                        if (index == 0) { path.moveTo(x, y); gradientPath.moveTo(x, y) }
                        else { path.lineTo(x, y); gradientPath.lineTo(x, y) }

                        drawCircle(color = EmeraldGreen, radius = 4f, center = Offset(x, y))
                    }

                    gradientPath.lineTo(size.width, size.height)
                    gradientPath.lineTo(0f, size.height)
                    gradientPath.close()

                    drawPath(path = gradientPath, brush = Brush.verticalGradient(listOf(EmeraldGreen.copy(alpha = 0.3f), Color.Transparent)))
                    drawPath(path = path, color = EmeraldGreen, style = Stroke(width = 3f))
                }
            }
        }
    }
}

@Composable
fun SavingsStatsCards(stats: SavingsStats) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBlock("Deposits", "৳ ${String.format("%.0f", stats.totalDeposits)}", EmeraldGreen)
            Box(Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outlineVariant))
            StatBlock("Withdrawals", "৳ ${String.format("%.0f", stats.totalWithdrawals)}", CoralRed)
            Box(Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outlineVariant))
            StatBlock("Transactions", "${stats.transactionCount}", SapphireBlue)
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun SavingsHistoryItem(savings: Savings, onDelete: (Savings) -> Unit = {}) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ChipShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(36.dp).background(
                        if (savings.amount >= 0) EmeraldGreen.copy(alpha = 0.1f) else CoralRed.copy(alpha = 0.1f),
                        ChipShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (savings.amount >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        null,
                        tint = if (savings.amount >= 0) EmeraldGreen else CoralRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = savings.note.ifEmpty { if (savings.amount >= 0) "Deposit" else "Withdrawal" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${dateFormat.format(Date(savings.timestamp))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = String.format("৳ %.2f", kotlin.math.abs(savings.amount)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (savings.amount >= 0) EmeraldGreen else CoralRed
            )
            IconButton(onClick = { onDelete(savings) }) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (Double, String, Boolean) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isWithdrawal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = { Text(if (isWithdrawal) "Withdraw Money" else "Add Money", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) amount = it },
                    label = { Text("Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = ChipShape
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = ChipShape
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { isWithdrawal = false },
                        modifier = Modifier.weight(1f),
                        shape = ChipShape,
                        color = if (!isWithdrawal) EmeraldGreen else EmeraldGreen.copy(alpha = 0.08f),
                        border = null
                    ) {
                        Text(
                            "Deposit",
                            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (!isWithdrawal) Color.White else EmeraldGreen
                        )
                    }
                    Surface(
                        onClick = { isWithdrawal = true },
                        modifier = Modifier.weight(1f),
                        shape = ChipShape,
                        color = if (isWithdrawal) CoralRed else CoralRed.copy(alpha = 0.08f),
                        border = null
                    ) {
                        Text(
                            "Withdrawal",
                            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = if (isWithdrawal) Color.White else CoralRed
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { amount.toDoubleOrNull()?.let { onAdd(it, note, isWithdrawal) } },
                enabled = amount.toDoubleOrNull() ?: 0.0 > 0
            ) { Text("Add", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
