package com.rudra.smartworktracker.ui.screens.loans

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.Loan
import com.rudra.smartworktracker.data.entity.LoanCategory
import com.rudra.smartworktracker.data.entity.LoanType
import com.rudra.smartworktracker.data.entity.TransactionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
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
private val SlateGray = Color(0xFF64748B)

private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val AmberSurface = Color(0xFFFFFBEB)
private val PurpleSurface = Color(0xFFF5F3FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: LoanViewModel = viewModel(factory = LoanViewModelFactory(LocalContext.current.applicationContext as Application))
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddLoanDialog() },
                containerColor = SapphireBlue,
                contentColor = Color.White,
                shape = CircleShape
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
                                brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Loan Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (uiState.statistics.overdueCount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(shape = PillShape, color = CoralRed.copy(alpha = 0.12f)) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(14.dp))
                                            Text("${uiState.statistics.overdueCount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CoralRed)
                                        }
                                    }
                                }
                            }
                            Text("Track borrowed & lent money", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { StatisticsCard(stats = uiState.statistics) }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                item {
                    TabRow(
                        selectedTabIndex = LoanTab.entries.indexOf(uiState.selectedTab),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = {},
                        divider = {}
                    ) {
                        LoanTab.entries.forEach { tab ->
                            val selected = uiState.selectedTab == tab
                            val count = when (tab) {
                                LoanTab.ALL -> uiState.loans.size
                                LoanTab.BORROWED -> uiState.statistics.borrowedCount
                                LoanTab.LENT -> uiState.statistics.lentCount
                                LoanTab.OVERDUE -> uiState.statistics.overdueCount
                            }
                            Tab(
                                selected = selected,
                                onClick = { viewModel.setSelectedTab(tab) },
                                text = {
                                    Surface(
                                        shape = PillShape,
                                        color = if (selected) VioletPurple.copy(alpha = 0.12f) else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                tab.title,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) VioletPurple else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (count > 0) {
                                                Surface(
                                                    shape = PillShape,
                                                    color = if (selected) VioletPurple else MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        "$count",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxSize().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SapphireBlue)
                        }
                    }
                } else if (uiState.filteredLoans.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(72.dp).background(
                                        brush = Brush.linearGradient(listOf(SlateGray.copy(alpha = 0.3f), SlateGray.copy(alpha = 0.1f))),
                                        shape = CircleShape
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(36.dp), tint = SlateGray.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No loans found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tap + to add a new loan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(uiState.filteredLoans, key = { it.id }) { loan ->
                        Spacer(modifier = Modifier.height(8.dp))
                        LoanCard(
                            loan = loan,
                            onClick = { viewModel.openLoanDetailsDialog(loan) },
                            onEditClick = { viewModel.openEditLoanDialog(loan) },
                            onRepayClick = { viewModel.openRepayDialog(loan) },
                            onDeleteClick = { viewModel.openDeleteConfirmationDialog(loan) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (uiState.showAddLoanDialog) {
            AddEditLoanBottomSheet(
                loan = null,
                onDismiss = { viewModel.closeAddLoanDialog() },
                onSave = { personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, accountId ->
                    viewModel.addLoan(personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, accountId)
                }
            )
        }

        if (uiState.showEditLoanDialog != null) {
            AddEditLoanBottomSheet(
                loan = uiState.showEditLoanDialog,
                onDismiss = { viewModel.closeEditLoanDialog() },
                onSave = { personName, contact, amount, type, category, dueDate, interest, emi, totalEmis, notes, accountId ->
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
                            accountId = accountId
                        )
                    )
                }
            )
        }

        uiState.showRepayDialogForLoan?.let { loan ->
            RepayLoanDialog(
                loan = loan,
                accounts = uiState.accounts,
                onDismiss = { viewModel.closeRepayDialog() },
                onConfirm = { amount, accountId -> viewModel.repayLoan(loan, amount, accountId) }
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

    var animatedBorrowed by remember { mutableFloatStateOf(0f) }
    var animatedLent by remember { mutableFloatStateOf(0f) }
    var animatedNet by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(stats) {
        delay(200)
        animatedBorrowed = stats.totalBorrowed.toFloat()
        animatedLent = stats.totalLent.toFloat()
        animatedNet = stats.netPosition.toFloat()
    }

    val borrowedAnim by animateFloatAsState(animatedBorrowed, tween(1000), label = "b")
    val lentAnim by animateFloatAsState(animatedLent, tween(1000), label = "l")
    val netAnim by animateFloatAsState(animatedNet, tween(1000), label = "n")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(
                        brush = Brush.linearGradient(listOf(EmeraldGreen, VioletPurple)),
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Loan Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (stats.overdueCount > 0) {
                    Surface(shape = PillShape, color = CoralRed.copy(alpha = 0.12f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed, modifier = Modifier.size(14.dp))
                            Text("${stats.overdueCount} overdue", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = CoralRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Borrowed",
                    value = "৳${"%,.0f".format(borrowedAnim)}",
                    accentColor = CoralRed,
                    bgColor = RedSurface,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Lent",
                    value = "৳${"%,.0f".format(lentAnim)}",
                    accentColor = EmeraldGreen,
                    bgColor = GreenSurface,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Net",
                    value = "৳${"%,.0f".format(netAnim)}",
                    accentColor = if (stats.netPosition >= 0) EmeraldGreen else CoralRed,
                    bgColor = if (stats.netPosition >= 0) GreenSurface else RedSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (stats.totalCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(shape = PillShape, color = SlateGray.copy(alpha = 0.08f)) {
                        Text(
                            "Total: ${stats.totalCount} loans",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(14.dp), clip = false),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (label) {
                        "Borrowed" -> Icons.Default.ArrowBack
                        "Lent" -> Icons.Default.Payment
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, ChipShape, clip = false),
        shape = ChipShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
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

    val isBorrowed = loan.loanType == LoanType.BORROWED
    val accentColor = if (isBorrowed) CoralRed else EmeraldGreen
    val bgColor = if (isBorrowed) RedSurface else GreenSurface
    val cardBg = when {
        loan.isFullyPaid -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        loan.isOverdue -> CoralRed.copy(alpha = 0.06f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(
                        brush = Brush.linearGradient(if (isBorrowed) listOf(CoralRed, CoralRed.copy(alpha = 0.7f)) else listOf(EmeraldGreen, EmeraldGreen.copy(alpha = 0.7f))),
                        shape = RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        loan.personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = PillShape, color = accentColor.copy(alpha = 0.12f)) {
                            Text(
                                if (isBorrowed) "Borrowed" else "Lent",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            "• ${loan.loanCategory.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        if (loan.isFullyPaid) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        } else if (loan.isOverdue) {
                            Icon(Icons.Default.Warning, contentDescription = "Overdue", tint = CoralRed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = SlateGray)
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = CoralRed.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "৳${"%,.0f".format(loan.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "৳${"%,.0f".format(loan.initialAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(accentColor.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(loan.progress).height(8.dp).background(
                        brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor)),
                        shape = RoundedCornerShape(4.dp)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).background(SlateGray.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray)
                    }
                    Text(dateFormat.format(Date(loan.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = PillShape, color = SlateGray.copy(alpha = 0.08f)) {
                    Text(
                        "${(loan.progress * 100).toInt()}% paid",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (!loan.isFullyPaid) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (!loan.isFullyPaid) onRepayClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBorrowed) CoralRed else EmeraldGreen
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isBorrowed) "Make Payment" else "Receive Payment", fontWeight = FontWeight.SemiBold)
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
    onSave: (String, String?, Double, LoanType, LoanCategory, Long?, Double?, Double?, Int?, String?, Long) -> Unit
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
    var selectedAccountId by remember { mutableStateOf(loan?.accountId ?: 0L) }
    var selectedDueDate by remember { mutableStateOf(loan?.dueDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val accountDao = db.accountDao()
    var accounts by remember { mutableStateOf<List<com.rudra.smartworktracker.data.entity.Account>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        accounts = accountDao.getAllAccountsList()
    }

    LaunchedEffect(accounts) {
        if (selectedAccountId == 0L && accounts.isNotEmpty()) {
            val cashAccount = accounts.find { it.name == "Cash" } ?: accounts.first()
            selectedAccountId = cashAccount.id
        }
    }

    var loanTypeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(
                        brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    if (isEditing) "Edit Loan" else "Add New Loan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("Person Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = loanType == LoanType.BORROWED,
                    onClick = { loanType = LoanType.BORROWED },
                    label = { Text("I Borrowed") },
                    modifier = Modifier.weight(1f),
                    shape = PillShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralRed.copy(alpha = 0.15f)
                    )
                )
                FilterChip(
                    selected = loanType == LoanType.LENT,
                    onClick = { loanType = LoanType.LENT },
                    label = { Text("I Lent") },
                    modifier = Modifier.weight(1f),
                    shape = PillShape,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen.copy(alpha = 0.15f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = ChipShape
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

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = selectedDueDate?.let {
                    SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(it))
                } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date (Optional)") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                    }
                },
                enabled = false,
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = interestRate,
                    onValueChange = { interestRate = it },
                    label = { Text("Interest %") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = ChipShape
                )
                OutlinedTextField(
                    value = emiAmount,
                    onValueChange = { emiAmount = it },
                    label = { Text("EMI Amount") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = ChipShape
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = totalEmis,
                onValueChange = { totalEmis = it },
                label = { Text("Total EMIs") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account"
                OutlinedTextField(
                    value = selectedAccountName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = ChipShape
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text("${account.name} (${account.balance.toInt()} BDT)") },
                            onClick = {
                                selectedAccountId = account.id
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = ChipShape
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amountValue > 0 && selectedAccountId > 0) {
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
                                selectedAccountId
                            )
                        }
                    },
                    enabled = personName.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0 && selectedAccountId > 0,
                    shape = ChipShape
                ) {
                    Text(if (isEditing) "Update" else "Add Loan", fontWeight = FontWeight.SemiBold)
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
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
    val dateTimeFormat = remember { SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()) }
    val isBorrowed = loan.loanType == LoanType.BORROWED
    val accentColor = if (isBorrowed) CoralRed else EmeraldGreen

    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val accountDao = db.accountDao()
    val scope = rememberCoroutineScope()
    var accountName by remember { mutableStateOf("") }
    var transactions by remember { mutableStateOf<List<FinancialTransaction>>(emptyList()) }
    var showDateFilter by remember { mutableStateOf(false) }
    var filterStart by remember { mutableStateOf<Long?>(null) }
    var filterEnd by remember { mutableStateOf<Long?>(null) }
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }

    LaunchedEffect(loan) {
        accountName = if (loan.accountId > 0) {
            accountDao.getAccountById(loan.accountId)?.name ?: "Unknown"
        } else "Not set"
        scope.launch {
            val all = db.financialTransactionDao().getAllTransactions().first()
            transactions = all.filter { it.relatedLoanId == loan.id }.sortedByDescending { it.date }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            brush = Brush.linearGradient(if (isBorrowed) listOf(CoralRed, GoldenAmber) else listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("Loan Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SlateGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text(loan.personName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = PillShape, color = accentColor.copy(alpha = 0.12f)) {
                                Text(
                                    loan.loanType.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text("• ${loan.loanCategory.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailColumn("Initial Amount", "৳${"%,.0f".format(loan.initialAmount)}")
                DetailColumn("Remaining", "৳${"%,.0f".format(loan.remainingAmount)}")
                DetailColumn("Progress", "${(loan.progress * 100).toInt()}%")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).background(SlateGray.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray)
                }
                Text("Account: $accountName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(10.dp))

            loan.contactNumber?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(28.dp).background(SlateGray.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray)
                    }
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).background(SlateGray.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = SlateGray)
                }
                Column {
                    Text("Start: ${dateFormat.format(Date(loan.date))}", style = MaterialTheme.typography.bodyMedium)
                    loan.dueDate?.let { Text("Due: ${dateFormat.format(Date(it))}", style = MaterialTheme.typography.bodyMedium) }
                }
            }

            loan.interestRate?.let {
                Spacer(modifier = Modifier.height(10.dp))
                DetailInfoRow("Interest Rate", "$it%")
            }

            loan.notes?.let {
                Spacer(modifier = Modifier.height(10.dp))
                DetailInfoRow("Notes", it)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!loan.isFullyPaid) {
                    Button(
                        onClick = onMarkPaid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = ChipShape
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Paid", fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    shape = ChipShape
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaction History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showDateFilter = !showDateFilter }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showDateFilter) "Hide Filter" else "Filter by Date")
                }
            }

            if (showDateFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text("From (dd/MM/yyyy)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = ChipShape
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        label = { Text("To (dd/MM/yyyy)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = ChipShape
                    )
                    IconButton(onClick = {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        try {
                            filterStart = sdf.parse(startDateText)?.time
                            filterEnd = sdf.parse(endDateText)?.time?.plus(86400000L)
                            scope.launch {
                                val all = db.financialTransactionDao().getAllTransactions().first()
                                var filtered = all.filter { it.relatedLoanId == loan.id }
                                if (filterStart != null && filterEnd != null) {
                                    filtered = filtered.filter { it.date in filterStart!!..filterEnd!! }
                                }
                                transactions = filtered.sortedByDescending { it.date }
                            }
                        } catch (_: Exception) {}
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Filter", tint = SapphireBlue)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                Text("No transactions yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            } else {
                transactions.forEach { txn ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = ChipShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.note, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(dateTimeFormat.format(Date(txn.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "৳${"%,.0f".format(txn.amount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (txn.type.name.contains("REPAY") || txn.type == TransactionType.LOAN_RECEIVE) EmeraldGreen else CoralRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HorizontalDivider(color: Color, thickness: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.fillMaxWidth().height(thickness).background(color))
}

@Composable
fun DetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepayLoanDialog(
    loan: Loan,
    accounts: List<com.rudra.smartworktracker.data.entity.Account>,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long) -> Unit
) {
    val isBorrowed = loan.loanType == LoanType.BORROWED
    var selectedAccountId by remember { mutableStateOf(loan.accountId) }
    var accountExpanded by remember { mutableStateOf(false) }
    val fullAmount = loan.remainingAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = SapphireBlue, modifier = Modifier.size(20.dp))
                Text(if (isBorrowed) "Repay Loan" else "Receive Payment", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("For loan with ${loan.personName}", style = MaterialTheme.typography.bodyMedium)

                Surface(shape = PillShape, color = if (isBorrowed) CoralRed.copy(alpha = 0.12f) else EmeraldGreen.copy(alpha = 0.12f)) {
                    Text(
                        "Remaining: ৳${"%,.0f".format(fullAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBorrowed) CoralRed else EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    if (isBorrowed) "Full amount of ৳${"%,.0f".format(fullAmount)} will be paid from the selected account."
                    else "Full amount of ৳${"%,.0f".format(fullAmount)} will be added to the loan account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account"
                    OutlinedTextField(
                        value = selectedAccountName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isBorrowed) "Pay From Account *" else "Receive To Account *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = ChipShape
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} (৳${"%,.0f".format(account.balance)})") },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fullAmount, selectedAccountId) },
                enabled = selectedAccountId > 0,
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBorrowed) CoralRed else EmeraldGreen
                )
            ) {
                Text(
                    if (isBorrowed) "Pay ৳${"%,.0f".format(fullAmount)}" else "Receive ৳${"%,.0f".format(fullAmount)}",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(loan: Loan, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed, modifier = Modifier.size(20.dp))
                Text("Delete Loan", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text("Are you sure you want to delete the loan with ${loan.personName}? This will also delete all associated transactions.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                shape = ChipShape
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
