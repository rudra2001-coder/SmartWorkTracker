package com.rudra.smartworktracker.ui.screens.accounts

import android.app.Application
import android.app.Activity
import android.widget.Toast
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider
import com.rudra.smartworktracker.data.entity.displayName
import com.rudra.smartworktracker.data.entity.icon
import com.rudra.smartworktracker.engine.SmartAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onNavigateToTransfer: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onNavigateToAccountDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: AccountsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Account?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Accounts", fontWeight = FontWeight.Bold)
                        Text("${uiState.accounts.size} accounts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account")
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    NetWorthCard(netWorth = uiState.totalNetWorth)
                }

                if (uiState.smartAlerts.isNotEmpty()) {
                    item {
                        SmartAlertsSection(alerts = uiState.smartAlerts)
                    }
                }

                item {
                    WalletSection(
                        wallets = uiState.wallets,
                        total = uiState.walletTotal,
                        onAccountClick = onNavigateToAccountDetail,
                        onEditClick = { showEditDialog = it },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }

                item {
                    BankAccountsSection(
                        accounts = uiState.bankAccounts,
                        total = uiState.bankTotal,
                        onAccountClick = onNavigateToAccountDetail,
                        onEditClick = { showEditDialog = it },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }

                item {
                    MobileBankingSection(
                        accounts = uiState.mobileBankingAccounts,
                        total = uiState.mobileBankingTotal,
                        onAccountClick = onNavigateToAccountDetail,
                        onEditClick = { showEditDialog = it },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    QuickActionsSection(
                        onTransfer = onNavigateToTransfer,
                        onAddAccount = { showAddDialog = true }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (showAddDialog) {
            AddAccountDialog(
                accounts = uiState.accounts,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, category, provider, number, nickname, balance, maxBalance, hasLimit, dailyLimit ->
                    viewModel.createAccount(name, category, provider, number, nickname, balance, maxBalance, hasLimit, dailyLimit) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) showAddDialog = false
                    }
                }
            )
        }

        showEditDialog?.let { account ->
            EditAccountDialog(
                account = account,
                onDismiss = { showEditDialog = null },
                onConfirm = { updatedAccount ->
                    viewModel.updateAccount(updatedAccount) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) showEditDialog = null
                    }
                }
            )
        }

        showDeleteDialog?.let { account ->
            DeleteAccountDialog(
                account = account,
                accounts = uiState.accounts.filter { it.id != account.id },
                onDismiss = { showDeleteDialog = null },
                onConfirm = { targetAccountId ->
                    viewModel.deleteAccountWithTransfer(account.id, targetAccountId) { success, message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        if (success) showDeleteDialog = null
                    }
                }
            )
        }
    }
}

@Composable
fun NetWorthCard(netWorth: Double) {
    val greeting = getGreeting()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total Net Worth",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "৳ ${formatAmount(netWorth)}",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SmartAlertsSection(alerts: List<SmartAlert>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alerts.take(2).forEach { alert ->
            AlertItem(alert = alert)
        }
    }
}

@Composable
fun AlertItem(alert: SmartAlert) {
    val (icon, color, message) = when (alert) {
        is SmartAlert.LowBalance -> Triple(
            Icons.Default.Warning,
            Color(0xFFFF9800),
            alert.message
        )
        is SmartAlert.ApproachingLimit -> Triple(
            Icons.Default.TrendingUp,
            Color(0xFFFFC107),
            alert.message
        )
        is SmartAlert.HighSpending -> Triple(
            Icons.Default.TrendingDown,
            Color(0xFFF44336),
            alert.message
        )
        is SmartAlert.TransferHabit -> Triple(
            Icons.Default.SwapHoriz,
            Color(0xFF2196F3),
            alert.message
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

@Composable
fun WalletSection(
    wallets: List<Account>,
    total: Double,
    onAccountClick: (Long) -> Unit,
    onEditClick: (Account) -> Unit,
    onDeleteClick: (Account) -> Unit
) {
    AccountSection(
        title = "💳 WALLETS",
        accounts = wallets,
        total = total,
        onAccountClick = onAccountClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick
    )
}

@Composable
fun BankAccountsSection(
    accounts: List<Account>,
    total: Double,
    onAccountClick: (Long) -> Unit,
    onEditClick: (Account) -> Unit,
    onDeleteClick: (Account) -> Unit
) {
    AccountSection(
        title = "🏦 BANK ACCOUNTS",
        accounts = accounts,
        total = total,
        onAccountClick = onAccountClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick
    )
}

@Composable
fun MobileBankingSection(
    accounts: List<Account>,
    total: Double,
    onAccountClick: (Long) -> Unit,
    onEditClick: (Account) -> Unit,
    onDeleteClick: (Account) -> Unit
) {
    AccountSection(
        title = "📱 MOBILE BANKING",
        accounts = accounts,
        total = total,
        onAccountClick = onAccountClick,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick
    )
}

@Composable
fun AccountSection(
    title: String,
    accounts: List<Account>,
    total: Double,
    onAccountClick: (Long) -> Unit,
    onEditClick: (Account) -> Unit,
    onDeleteClick: (Account) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "৳ ${formatAmount(total)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        accounts.forEach { account ->
            AccountCard(
                account = account,
                onClick = { onAccountClick(account.id) },
                onEditClick = { onEditClick(account) },
                onDeleteClick = { onDeleteClick(account) }
            )
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val cardColor = getAccountCardColor(account)
    val balancePercentage = account.getBalancePercentage()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(16.dp),
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
                verticalAlignment = Alignment.Top
            ) {
Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Column {
                        Text(
                            text = account.nickname ?: account.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = account.provider.displayName(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (account.hasLimit && account.dailyTransferLimit != null) {
                                Text(
                                    text = "• ${account.dailyTransferLimit.toInt()} limit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = maskAccountNumber(account.accountNumber),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳ ${formatAmount(account.balance)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    account.maxBalance?.let { max ->
                        Text(
                            text = "of ৳ ${formatAmount(max)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert, 
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = { 
                                    showMenu = false
                                    onEditClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { 
                                    showMenu = false
                                    onDeleteClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                    }
                }
            }
            
            if (account.maxBalance != null && account.maxBalance > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { balancePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getProgressColor(balancePercentage),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(balancePercentage * 100).toInt()}% used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (account.maxBalance - account.balance > 0) {
                        Text(
                            text = "৳ ${formatAmount(account.maxBalance - account.balance)} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getProgressColor(percentage: Float): Color {
    return when {
        percentage >= 0.9f -> Color(0xFFF44336)
        percentage >= 0.7f -> Color(0xFFFF9800)
        percentage >= 0.5f -> Color(0xFFFFC107)
        else -> Color(0xFF4CAF50)
    }
}

fun getAccountCardColor(account: Account): Color {
    return when (account.type) {
        AccountCategory.WALLET -> Color(0xFFF8F5FF)
        AccountCategory.BANK -> Color(0xFFE8F5E9)
        AccountCategory.MOBILE_BANKING -> Color(0xFFFFF3E0)
    }
}

@Composable
fun QuickActionsSection(
    onTransfer: () -> Unit,
    onAddAccount: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onTransfer,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Transfer")
        }
        
        OutlinedButton(
            onClick = onAddAccount,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Account")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (String, AccountCategory, AccountProvider, String, String?, Double, Double?, Boolean, Double?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AccountCategory.WALLET) }
    var selectedProvider by remember { mutableStateOf(AccountProvider.CASH) }
    var accountNumber by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("0") }
    var maxBalance by remember { mutableStateOf("") }
    var hasLimit by remember { mutableStateOf(false) }
    var dailyLimit by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var providerExpanded by remember { mutableStateOf(false) }
    var showMaxBalanceField by remember { mutableStateOf(false) }

    val providersForCategory = when (selectedCategory) {
        AccountCategory.WALLET -> listOf(AccountProvider.CASH)
        AccountCategory.BANK -> listOf(AccountProvider.BANK, AccountProvider.SAVINGS, AccountProvider.CREDIT_CARD, AccountProvider.LOAN, AccountProvider.DBBL, AccountProvider.CITY_BANK, AccountProvider.BRAC_BANK, AccountProvider.BKB, AccountProvider.SONALI_BANK)
        AccountCategory.MOBILE_BANKING -> listOf(AccountProvider.BKASH, AccountProvider.NAGAD, AccountProvider.ROCKET, AccountProvider.UCASH)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("➕ Add New Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Account Type", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        AccountCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName()) },
                                onClick = {
                                    selectedCategory = category
                                    selectedProvider = when (category) {
                                        AccountCategory.WALLET -> AccountProvider.CASH
                                        AccountCategory.BANK -> AccountProvider.BANK
                                        AccountCategory.MOBILE_BANKING -> AccountProvider.BKASH
                                    }
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Provider", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = providerExpanded,
                    onExpandedChange = { providerExpanded = !providerExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = providerExpanded,
                        onDismissRequest = { providerExpanded = false }
                    ) {
                        providersForCategory.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.displayName()) },
                                onClick = {
                                    selectedProvider = provider
                                    providerExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) initialBalance = it },
                    label = { Text("Initial Balance") },
                    prefix = { Text("৳ ") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Set Max Balance", style = MaterialTheme.typography.labelMedium)
                        Text("Show progress bar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = showMaxBalanceField,
                        onCheckedChange = { showMaxBalanceField = it }
                    )
                }

                if (showMaxBalanceField) {
                    OutlinedTextField(
                        value = maxBalance,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) maxBalance = it },
                        label = { Text("Max Balance (for progress bar)") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 50000") }
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Transfer Limit", style = MaterialTheme.typography.labelMedium)
                        Text("Set a limit for daily transfers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = hasLimit,
                        onCheckedChange = { hasLimit = it }
                    )
                }

                if (hasLimit) {
                    OutlinedTextField(
                        value = dailyLimit,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) dailyLimit = it },
                        label = { Text("Limit Amount") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 25000") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = initialBalance.toDoubleOrNull() ?: 0.0
                    val max = if (showMaxBalanceField && maxBalance.isNotBlank()) maxBalance.toDoubleOrNull() else null
                    val limit = if (hasLimit && dailyLimit.isNotBlank()) dailyLimit.toDoubleOrNull() else null
                    onConfirm(
                        selectedProvider.displayName(),
                        selectedCategory,
                        selectedProvider,
                        accountNumber,
                        nickname.ifEmpty { null },
                        balance,
                        max,
                        hasLimit,
                        limit
                    )
                },
                enabled = accountNumber.isNotBlank() && (!hasLimit || dailyLimit.isNotBlank())
            ) {
                Text("Add Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    var nickname by remember { mutableStateOf(account.nickname ?: "") }
    var accountNumber by remember { mutableStateOf(account.accountNumber) }
    var balance by remember { mutableStateOf(account.balance.toString()) }
    var maxBalance by remember { mutableStateOf(account.maxBalance?.toString() ?: "") }
    var hasLimit by remember { mutableStateOf(account.hasLimit) }
    var dailyLimit by remember { mutableStateOf(account.dailyTransferLimit?.toString() ?: "") }
    var showMaxBalance by remember { mutableStateOf(account.maxBalance != null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("✏️ Edit Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(account.provider.icon(), fontSize = 32.sp)
                        Column {
                            Text(account.name, fontWeight = FontWeight.Medium)
                            Text(account.type.displayName(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) balance = it },
                    label = { Text("Current Balance") },
                    prefix = { Text("৳ ") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Set Max Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (account.maxBalance != null) "Current: ৳ ${account.maxBalance.toInt()}" else "For progress bar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showMaxBalance,
                        onCheckedChange = { showMaxBalance = it }
                    )
                }

                if (showMaxBalance) {
                    OutlinedTextField(
                        value = maxBalance,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) maxBalance = it },
                        label = { Text("Max Balance") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 50000") }
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Transfer Limit", style = MaterialTheme.typography.labelMedium)
                        Text(
                            if (account.getEffectiveLimit() != null) "Current: ৳ ${account.getEffectiveLimit()?.toInt()}" else "No limit set",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = hasLimit,
                        onCheckedChange = { hasLimit = it }
                    )
                }

                if (hasLimit) {
                    OutlinedTextField(
                        value = dailyLimit,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) dailyLimit = it },
                        label = { Text("Limit Amount") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 25000") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newBalance = balance.toDoubleOrNull() ?: account.balance
                    val max = if (showMaxBalance && maxBalance.isNotBlank()) maxBalance.toDoubleOrNull() else null
                    val limit = if (hasLimit && dailyLimit.isNotBlank()) dailyLimit.toDoubleOrNull() else null
                    val updatedAccount = account.copy(
                        nickname = nickname.ifEmpty { null },
                        accountNumber = accountNumber,
                        balance = newBalance,
                        maxBalance = max,
                        hasLimit = hasLimit,
                        dailyTransferLimit = limit,
                        lastUpdated = System.currentTimeMillis()
                    )
                    onConfirm(updatedAccount)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountDialog(
    account: Account,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedTargetAccount by remember { mutableStateOf<Account?>(null) }
    var targetExpanded by remember { mutableStateOf(false) }
    val canDeleteDirectly = account.balance == 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("🗑️ Delete Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(account.provider.icon(), fontSize = 32.sp)
                        Column {
                            Text(account.nickname ?: account.name, fontWeight = FontWeight.Medium)
                            Text("Balance: ৳ ${formatAmount(account.balance)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (!canDeleteDirectly) {
                    Text(
                        text = "⚠️ This account has balance. Transfer to another account first:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    ExposedDropdownMenuBox(
                        expanded = targetExpanded,
                        onExpandedChange = { targetExpanded = !targetExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedTargetAccount?.nickname ?: selectedTargetAccount?.name ?: "Select target account",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false }
                        ) {
                            accounts.forEach { targetAccount ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(targetAccount.nickname ?: targetAccount.name)
                                            Text("Balance: ৳ ${formatAmount(targetAccount.balance)}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedTargetAccount = targetAccount
                                        targetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "✅ This account has zero balance. You can delete it directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canDeleteDirectly) {
                        onConfirm(-1)
                    } else {
                        selectedTargetAccount?.let { onConfirm(it.id) }
                    }
                },
                enabled = canDeleteDirectly || selectedTargetAccount != null,
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

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "👋 Good morning"
        hour < 17 -> "☀️ Good afternoon"
        else -> "🌙 Good evening"
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.0f", amount)
}

private fun maskAccountNumber(number: String): String {
    return if (number.length > 4) {
        "****${number.takeLast(4)}"
    } else {
        number
    }
}