package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.AccountType
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanCategory
import com.rudra.smartworktracker.data.entity.LoanType
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: LoanViewModel = viewModel(factory = LoanViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Management") },
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
                                contentDescription = "Overdue loans",
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
                onClick = { viewModel.openAddLoanDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
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
                selectedTabIndex = LoanTab.entries.indexOf(uiState.selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                LoanTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.setSelectedTab(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.title)
                                val count = when (tab) {
                                    LoanTab.ALL -> uiState.loans.size
                                    LoanTab.BORROWED -> uiState.statistics.borrowedCount
                                    LoanTab.LENT -> uiState.statistics.lentCount
                                    LoanTab.OVERDUE -> uiState.statistics.overdueCount
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
            } else if (uiState.filteredLoans.isEmpty()) {
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
                            "No loans found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(uiState.filteredLoans, key = { it.id }) { loan ->
                        LoanCard(
                            loan = loan,
                            onClick = { viewModel.openLoanDetailsDialog(loan) },
                            onEditClick = { viewModel.openEditLoanDialog(loan) },
                            onRepayClick = { viewModel.openRepayDialog(loan) },
                            onDeleteClick = { viewModel.openDeleteConfirmationDialog(loan) }
                        )
                    }
                }
            }
        }

        if (uiState.showAddLoanDialog) {
            AddEditLoanBottomSheet(
                loan = null,
                onDismiss = { viewModel.closeAddLoanDialog() },
                onSave = { personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, source, dest ->
                    viewModel.addLoan(personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, source, dest)
                }
            )
        }

        if (uiState.showEditLoanDialog != null) {
            AddEditLoanBottomSheet(
                loan = uiState.showEditLoanDialog,
                onDismiss = { viewModel.closeEditLoanDialog() },
                onSave = { personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, source, dest ->
                    viewModel.updateLoan(
                        uiState.showEditLoanDialog!!.copy(
                            personName = personName,
                            contactNumber = contact,
                            loanType = type,
                            loanCategory = category,
                            dueDate = dueDate,
                            interestRate = interest,
                            emiAmount = emi,
                            totalEmis = totalEmis,
                            notes = notes,
                            sourceAccount = source,
                            destinationAccount = dest
                        )
                    )
                }
            )
        }

        uiState.showRepayDialogForLoan?.let { loan ->
            RepayLoanDialog(
                loan = loan,
                onDismiss = { viewModel.closeRepayDialog() },
                onConfirm = { amount -> viewModel.repayLoan(loan, amount) }
            )
        }

        uiState.showDeleteConfirmationForLoan?.let { loan ->
            DeleteConfirmationDialog(
                loan = loan,
                onDismiss = { viewModel.closeDeleteConfirmationDialog() },
                onConfirm = { viewModel.deleteLoan(loan) }
            )
        }

        uiState.showLoanDetailsDialog?.let { loan ->
            LoanDetailsBottomSheet(
                loan = loan,
                onDismiss = { viewModel.closeLoanDetailsDialog() },
                onEdit = {
                    viewModel.closeLoanDetailsDialog()
                    viewModel.openEditLoanDialog(loan)
                },
                onMarkPaid = { viewModel.markLoanAsPaid(loan) },
                onDelete = {
                    viewModel.closeLoanDetailsDialog()
                    viewModel.openDeleteConfirmationDialog(loan)
                }
            )
        }
    }
}

@Composable
fun StatisticsCard(stats: LoanStatistics) {
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
                    "Loan Overview",
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
                EnhancedStatItem(
                    label = "Borrowed",
                    value = currencyFormat.format(stats.totalBorrowed),
                    color = Color(0xFFF44336),
                    icon = Icons.Default.ArrowBack,
                    modifier = Modifier.weight(1f)
                )
                EnhancedStatItem(
                    label = "Lent",
                    value = currencyFormat.format(stats.totalLent),
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.Payment,
                    modifier = Modifier.weight(1f)
                )
                EnhancedStatItem(
                    label = "Net",
                    value = currencyFormat.format(stats.netPosition),
                    color = if (stats.netPosition >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (stats.totalCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Total: ${stats.totalCount} loans",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedStatItem(
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
                        "Search by name, notes, or contact...",
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
fun LoanCard(
    loan: Loan,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onRepayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }
    
    val cardColor by animateColorAsState(
        targetValue = when {
            loan.isFullyPaid -> MaterialTheme.colorScheme.surfaceVariant
            loan.isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            loan.loanType == LoanType.BORROWED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        },
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
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
                                if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            loan.personName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                loan.loanType.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                " - ${loan.loanCategory.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row {
                    if (loan.isFullyPaid) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Paid",
                            tint = Color(0xFF4CAF50)
                        )
                    } else if (loan.isOverdue) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Overdue",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormat.format(loan.remainingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormat.format(loan.initialAmount),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { loan.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                        dateFormat.format(Date(loan.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${(loan.progress * 100).toInt()}% paid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!loan.isFullyPaid) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRepayClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loan.loanType == LoanType.BORROWED) MaterialTheme.colorScheme.error
                                         else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (loan.loanType == LoanType.BORROWED) "Make Payment" else "Receive Payment")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLoanBottomSheet(
    loan: Loan?,
    onDismiss: () -> Unit,
    onSave: (String, String?, Double, LoanType, LoanCategory, Long?, Double?, Double?, Int?, String?, AccountType, AccountType) -> Unit
) {
    val isEditing = loan != null
    var personName by remember { mutableStateOf(loan?.personName ?: "") }
    var contactNumber by remember { mutableStateOf(loan?.contactNumber ?: "") }
    var amount by remember { mutableStateOf(loan?.initialAmount?.toString() ?: "") }
    var loanType by remember { mutableStateOf(loan?.loanType ?: LoanType.BORROWED) }
    var loanCategory by remember { mutableStateOf(loan?.loanCategory ?: LoanCategory.PERSONAL) }
    var notes by remember { mutableStateOf(loan?.notes ?: "") }
    var interestRate by remember { mutableStateOf(loan?.interestRate?.toString() ?: "") }
    var emiAmount by remember { mutableStateOf(loan?.emiAmount?.toString() ?: "") }
    var totalEmis by remember { mutableStateOf(loan?.totalEmis?.toString() ?: "") }
    var sourceAccount by remember { mutableStateOf(loan?.sourceAccount ?: AccountType.BANK) }
    var destinationAccount by remember { mutableStateOf(loan?.destinationAccount ?: AccountType.CASH) }
    var selectedDueDate by remember { mutableStateOf(loan?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    var loanTypeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var sourceExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

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
                if (isEditing) "Edit Loan" else "Add New Loan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = loanType == LoanType.BORROWED,
                    onClick = { loanType = LoanType.BORROWED },
                    label = { Text("I Borrowed") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = loanType == LoanType.LENT,
                    onClick = { loanType = LoanType.LENT },
                    label = { Text("I Lent") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = loanCategory.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    LoanCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                loanCategory = category
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = selectedDueDate?.let {
                    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
                } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                    }
                },
                enabled = false
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest %") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = emiAmount,
                    onValueChange = { emiAmount = it },
                    label = { Text("EMI Amount") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = totalEmis,
                onValueChange = { totalEmis = it },
                label = { Text("Total EMIs") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = sourceExpanded,
                onExpandedChange = { sourceExpanded = !sourceExpanded }
            ) {
                OutlinedTextField(
                    value = sourceAccount.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Source Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = sourceExpanded,
                    onDismissRequest = { sourceExpanded = false }
                ) {
                    AccountType.entries.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                sourceAccount = account
                                sourceExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = destExpanded,
                onExpandedChange = { destExpanded = !destExpanded }
            ) {
                OutlinedTextField(
                    value = destinationAccount.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Destination Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = destExpanded,
                    onDismissRequest = { destExpanded = false }
                ) {
                    AccountType.entries.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                destinationAccount = account
                                destExpanded = false
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
                maxLines = 4
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
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amountValue > 0) {
                            onSave(
                                personName,
                                contactNumber.takeIf { it.isNotBlank() },
                                amountValue,
                                loanType,
                                loanCategory,
                                selectedDueDate,
                                interestRate.toDoubleOrNull(),
                                emiAmount.toDoubleOrNull(),
                                totalEmis.toIntOrNull(),
                                notes.takeIf { it.isNotBlank() },
                                sourceAccount,
                                destinationAccount
                            )
                        }
                    },
                    enabled = personName.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text(if (isEditing) "Update" else "Add Loan")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsBottomSheet(
    loan: Loan,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                Text(
                    "Loan Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                loan.personName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${loan.loanType.name} - ${loan.loanCategory.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailColumn("Initial Amount", currencyFormat.format(loan.initialAmount))
                DetailColumn("Remaining", currencyFormat.format(loan.remainingAmount))
                DetailColumn("Progress", "${(loan.progress * 100).toInt()}%")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            loan.contactNumber?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Start: ${dateFormat.format(Date(loan.date))}", style = MaterialTheme.typography.bodyMedium)
                    loan.dueDate?.let { Text("Due: ${dateFormat.format(Date(it))}", style = MaterialTheme.typography.bodyMedium) }
                }
            }
            
            loan.interestRate?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Interest Rate: $it%", style = MaterialTheme.typography.bodyMedium)
            }
            
            loan.notes?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: $it", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (!loan.isFullyPaid) {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Paid")
                    }
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RepayLoanDialog(loan: Loan, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var showFullAmount by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column {
                Text("For loan with ${loan.personName}")
                Text("Remaining: ${loan.remainingAmount}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = showFullAmount,
                    onClick = {
                        showFullAmount = !showFullAmount
                        if (showFullAmount) amount = loan.remainingAmount.toString()
                    },
                    label = { Text("Pay Full Amount") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirm")
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
fun DeleteConfirmationDialog(loan: Loan, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Loan") },
        text = { Text("Are you sure you want to delete the loan with ${loan.personName}? This will also delete all associated transactions.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
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
