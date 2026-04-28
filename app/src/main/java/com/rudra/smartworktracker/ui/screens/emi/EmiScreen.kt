package com.rudra.smartworktracker.ui.screens.emi

import android.app.Application
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Emi
import com.rudra.smartworktracker.data.entity.EmiStatus
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiScreen(
    viewModel: EmiViewModel = viewModel(factory = EmiViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (uiState.statistics.overdueCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge { Text(uiState.statistics.overdueCount.toString()) }
                            }
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Overdue EMIs",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddEmiDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add EMI")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StatisticsCard(uiState.statistics)
            
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) }
            )
            
            TabRow(
                selectedTabIndex = EmiTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                EmiTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.title)
                                val count = when (tab) {
                                    EmiTab.ALL -> uiState.emis.size
                                    EmiTab.UPCOMING -> uiState.emis.count { it.emi.status == EmiStatus.UPCOMING }
                                    EmiTab.DUE -> uiState.emis.count { it.emi.status == EmiStatus.DUE }
                                    EmiTab.OVERDUE -> uiState.statistics.overdueCount
                                    EmiTab.PAID -> uiState.emis.count { it.emi.status == EmiStatus.PAID }
                                }
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredEmis.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No EMIs found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(uiState.filteredEmis, key = { it.emi.id }) { emiWithLoan ->
                        EmiCard(
                            emiWithLoan = emiWithLoan,
                            onPayClick = { viewModel.openPayEmiDialog(emiWithLoan) },
                            onSkipClick = { viewModel.skipEmi(emiWithLoan.emi) },
                            onDeleteClick = { viewModel.openDeleteConfirmation(emiWithLoan) }
                        )
                    }
                }
            }
        }

        if (uiState.showAddEmiDialog) {
            AddEmiBottomSheet(
                loans = uiState.availableLoans,
                onDismiss = { viewModel.closeAddEmiDialog() },
                onSave = { loanId, amount, principal, interest, dueDay, notes, account ->
                    viewModel.addEmi(loanId, amount, principal, interest, dueDay, notes, account)
                }
            )
        }

        uiState.showPayEmiDialog?.let { emiWithLoan ->
            PayEmiDialog(
                emiWithLoan = emiWithLoan,
                onDismiss = { viewModel.closePayEmiDialog() },
                onConfirm = { viewModel.payEmi(emiWithLoan.emi) }
            )
        }

        uiState.showDeleteConfirmation?.let { emiWithLoan ->
            DeleteConfirmationDialog(
                emiWithLoan = emiWithLoan,
                onDismiss = { viewModel.closeDeleteConfirmation() },
                onConfirm = { viewModel.deleteEmi(emiWithLoan.emi) }
            )
        }
    }
}

@Composable
fun StatisticsCard(stats: EmiStatistics) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EMI Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (stats.overdueCount > 0) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${stats.overdueCount} overdue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedEmiStatItem(
                    label = "Pending",
                    value = currencyFormat.format(stats.totalPending),
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Payment,
                    modifier = Modifier.weight(1f)
                )
                EnhancedEmiStatItem(
                    label = "This Month",
                    value = currencyFormat.format(stats.thisMonthTotal),
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                EnhancedEmiStatItem(
                    label = "Overdue",
                    value = stats.overdueCount.toString(),
                    color = if (stats.overdueCount > 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (stats.totalPenaltyCollected > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF4CAF50).copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Penalty Collected: ${currencyFormat.format(stats.totalPenaltyCollected)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedEmiStatItem(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search by loan holder name...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun EmiCard(
    emiWithLoan: EmiWithLoan,
    onPayClick: () -> Unit,
    onSkipClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val emi = emiWithLoan.emi
    val loan = emiWithLoan.loan
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }
    
    val cardColor by animateColorAsState(
        targetValue = when (emi.status) {
            EmiStatus.PAID -> MaterialTheme.colorScheme.surfaceVariant
            EmiStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            EmiStatus.DUE -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        },
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(
                                when (emi.status) {
                                    EmiStatus.PAID -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    EmiStatus.OVERDUE -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = when (emi.status) {
                                EmiStatus.PAID -> Color(0xFF4CAF50)
                                EmiStatus.OVERDUE -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            loan?.personName ?: "Unknown Loan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        loan?.let {
                            Text(
                                it.loanType.name + " - " + it.loanCategory.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                StatusChip(status = emi.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "EMI Amount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormat.format(emi.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Principal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormat.format(emi.principalAmount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Interest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormat.format(emi.interestAmount),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (emi.penaltyAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Penalty: ${currencyFormat.format(emi.penaltyAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Due: ${dateFormat.format(Date(emi.nextDueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Day: ${emi.dueDateOfMonth}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            emi.notes?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Notes: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!emi.isPaid) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onPayClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onSkipClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                emi.lastPaymentDate?.let {
                    Text(
                        "Paid on: ${dateFormat.format(Date(it))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: EmiStatus) {
    val (color, text) = when (status) {
        EmiStatus.PAID -> Pair(Color(0xFF4CAF50), "PAID")
        EmiStatus.OVERDUE -> Pair(MaterialTheme.colorScheme.error, "OVERDUE")
        EmiStatus.DUE -> Pair(MaterialTheme.colorScheme.tertiary, "DUE")
        EmiStatus.SKIPPED -> Pair(MaterialTheme.colorScheme.outline, "SKIPPED")
        EmiStatus.UPCOMING -> Pair(MaterialTheme.colorScheme.primary, "UPCOMING")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmiBottomSheet(
    loans: List<Loan>,
    onDismiss: () -> Unit,
    onSave: (Int, Double, Double, Double, Int, String?, AccountType) -> Unit
) {
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }
    var amount by remember { mutableStateOf("") }
    var principalAmount by remember { mutableStateOf("") }
    var interestAmount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }
    var paymentAccount by remember { mutableStateOf(AccountType.BANK) }
    
    var isLoansExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Add New EMI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (loans.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "No active loans found. Please add a loan first.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = isLoansExpanded,
                    onExpandedChange = { isLoansExpanded = !isLoansExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLoan?.personName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Loan *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLoansExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isLoansExpanded,
                        onDismissRequest = { isLoansExpanded = false }
                    ) {
                        loans.forEach { loan ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(loan.personName)
                                        Text(
                                            "Remaining: ${loan.remainingAmount}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedLoan = loan
                                    amount = loan.emiAmount?.toString() ?: ""
                                    principalAmount = loan.emiAmount?.toString() ?: ""
                                    isLoansExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = it
                    val total = it.toDoubleOrNull() ?: 0.0
                    val interest = interestAmount.toDoubleOrNull() ?: 0.0
                    principalAmount = (total - interest).coerceAtLeast(0.0).toString()
                },
                label = { Text("Total EMI Amount *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = principalAmount,
                    onValueChange = { principalAmount = it },
                    label = { Text("Principal *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = interestAmount,
                    onValueChange = { 
                        interestAmount = it
                        val total = amount.toDoubleOrNull() ?: 0.0
                        val interest = it.toDoubleOrNull() ?: 0.0
                        principalAmount = (total - interest).coerceAtLeast(0.0).toString()
                    },
                    label = { Text("Interest") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = dueDay,
                onValueChange = { 
                    val day = it.toIntOrNull()
                    if (day == null || (day in 1..31)) dueDay = it
                },
                label = { Text("Due Day of Month (1-31) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = paymentAccount.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    AccountType.entries.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                paymentAccount = account
                                accountExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val loanId = selectedLoan?.id ?: return@Button
                        val emiAmountVal = amount.toDoubleOrNull() ?: 0.0
                        val principal = principalAmount.toDoubleOrNull() ?: emiAmountVal
                        val interest = interestAmount.toDoubleOrNull() ?: 0.0
                        val day = dueDay.toIntOrNull() ?: 5
                        
                        if (loanId > 0 && emiAmountVal > 0 && day in 1..31) {
                            onSave(
                                loanId,
                                emiAmountVal,
                                principal,
                                interest,
                                day,
                                notes.takeIf { it.isNotBlank() },
                                paymentAccount
                            )
                        }
                    },
                    enabled = selectedLoan != null && 
                              (amount.toDoubleOrNull() ?: 0.0) > 0 && 
                              (dueDay.toIntOrNull() ?: 0) in 1..31
                ) {
                    Text("Add EMI")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PayEmiDialog(emiWithLoan: EmiWithLoan, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val emi = emiWithLoan.emi
    val loan = emiWithLoan.loan
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Payment") },
        text = {
            Column {
                loan?.let {
                    Text("Loan: ${it.personName}", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("EMI Amount: ${currencyFormat.format(emi.amount)}")
                Text("Principal: ${currencyFormat.format(emi.principalAmount)}")
                if (emi.interestAmount > 0) {
                    Text("Interest: ${currencyFormat.format(emi.interestAmount)}")
                }
                if (emi.penaltyAmount > 0) {
                    Text("Penalty: ${currencyFormat.format(emi.penaltyAmount)}", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Total Payable: ${currencyFormat.format(emi.totalPayable)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(emiWithLoan: EmiWithLoan, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val emi = emiWithLoan.emi
    val loan = emiWithLoan.loan

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete EMI") },
        text = {
            Text("Are you sure you want to delete this EMI? ${loan?.let { "Loan: ${it.personName}" } ?: ""}")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
}
