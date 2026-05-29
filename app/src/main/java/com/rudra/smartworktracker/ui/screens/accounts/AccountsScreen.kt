package com.rudra.smartworktracker.ui.screens.accounts

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
private val DeleteRed = Color(0xFFE53935)
private val EditBlue = Color(0xFF1E88E5)

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
    var showDeleteDialog by remember { mutableStateOf<Pair<Account, Boolean>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Accounts", fontWeight = FontWeight.Bold)
                        Text(
                            "${uiState.accounts.size} accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                item { NetWorthHeroCard(netWorth = uiState.totalNetWorth) }

                if (uiState.smartAlerts.isNotEmpty()) {
                    item { SmartAlertsSection(alerts = uiState.smartAlerts) }
                }

                if (uiState.wallets.isNotEmpty()) {
                    item {
                        AccountCategorySection(
                            title = "WALLETS",
                            accounts = uiState.wallets,
                            total = uiState.walletTotal,
                            onAccountClick = onNavigateToAccountDetail,
                            onEdit = { showEditDialog = it },
                            onDelete = { account ->
                                showDeleteDialog = account to (account.balance > 0)
                            }
                        )
                    }
                }

                if (uiState.bankAccounts.isNotEmpty()) {
                    item {
                        AccountCategorySection(
                            title = "BANK ACCOUNTS",
                            accounts = uiState.bankAccounts,
                            total = uiState.bankTotal,
                            onAccountClick = onNavigateToAccountDetail,
                            onEdit = { showEditDialog = it },
                            onDelete = { account ->
                                showDeleteDialog = account to (account.balance > 0)
                            }
                        )
                    }
                }

                if (uiState.mobileBankingAccounts.isNotEmpty()) {
                    item {
                        AccountCategorySection(
                            title = "MOBILE BANKING",
                            accounts = uiState.mobileBankingAccounts,
                            total = uiState.mobileBankingTotal,
                            onAccountClick = onNavigateToAccountDetail,
                            onEdit = { showEditDialog = it },
                            onDelete = { account ->
                                showDeleteDialog = account to (account.balance > 0)
                            }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToTransfer,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Transfer")
                        }
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Account")
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
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

    showDeleteDialog?.let { (account, hasBalance) ->
        if (hasBalance) {
            DeleteAccountWithTransferDialog(
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
        } else {
            DeleteAccountConfirmDialog(
                account = account,
                onDismiss = { showDeleteDialog = null },
                onConfirm = {
                    viewModel.deleteAccountDirectly(account.id)
                    showDeleteDialog = null
                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun NetWorthHeroCard(netWorth: Double) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    var animatedNetWorth by remember { mutableDoubleStateOf(0.0) }
    val animatedValue by animateFloatAsState(
        targetValue = animatedNetWorth.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "networth"
    )
    LaunchedEffect(netWorth) {
        animatedNetWorth = netWorth
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = SlateGray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Total Net Worth",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "৳ ${formatAmount(animatedValue.toDouble())}",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (netWorth >= 0) EmeraldGreen else CoralRed
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = PillShape,
                color = if (netWorth >= 0) GreenSurface else RedSurface
            ) {
                Text(
                    if (netWorth >= 0) "Healthy finances" else "Review your accounts",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (netWorth >= 0) EmeraldGreen else CoralRed,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun SmartAlertsSection(alerts: List<com.rudra.smartworktracker.engine.SmartAlert>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        alerts.take(2).forEach { alert ->
            val (icon, color, message) = when (alert) {
                is com.rudra.smartworktracker.engine.SmartAlert.LowBalance -> Triple(
                    Icons.Default.Warning, GoldenAmber, alert.message
                )
                is com.rudra.smartworktracker.engine.SmartAlert.ApproachingLimit -> Triple(
                    Icons.Default.TrendingUp, GoldenAmber, alert.message
                )
                is com.rudra.smartworktracker.engine.SmartAlert.HighSpending -> Triple(
                    Icons.Default.TrendingDown, CoralRed, alert.message
                )
                is com.rudra.smartworktracker.engine.SmartAlert.TransferHabit -> Triple(
                    Icons.Default.SwapHoriz, SapphireBlue, alert.message
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = color)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCategorySection(
    title: String,
    accounts: List<Account>,
    total: Double,
    onAccountClick: (Long) -> Unit,
    onEdit: (Account) -> Unit,
    onDelete: (Account) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Surface(shape = PillShape, color = SapphireBlue.copy(alpha = 0.1f)) {
                Text(
                    "৳ ${formatAmount(total)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = SapphireBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        accounts.forEach { account ->
            SwipeableAccountCard(
                account = account,
                onClick = { onAccountClick(account.id) },
                onEdit = { onEdit(account) },
                onDelete = { onDelete(account) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableAccountCard(
    account: Account,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    val bgColor by animateColorAsState(
        targetValue = when (dismissState.targetValue) {
            SwipeToDismissBoxValue.StartToEnd -> EditBlue.copy(alpha = 0.15f)
            SwipeToDismissBoxValue.EndToStart -> DeleteRed.copy(alpha = 0.15f)
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        },
        label = "bg"
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EditBlue)
                            Text("Edit", color = EditBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DeleteRed)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete", color = DeleteRed, fontWeight = FontWeight.Bold)
                        }
                    }
                    SwipeToDismissBoxValue.Settled -> { }
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
        AccountCard(account = account, onClick = onClick)
    }
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit
) {
    val cardColor = when (account.type) {
        AccountCategory.WALLET -> PurpleSurface
        AccountCategory.BANK -> BlueSurface
        AccountCategory.MOBILE_BANKING -> AmberSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, CardShape, clip = false)
            .clickable(onClick = onClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    when (account.type) {
                                        AccountCategory.WALLET -> listOf(VioletPurple, SapphireBlue)
                                        AccountCategory.BANK -> listOf(SapphireBlue, EmeraldGreen)
                                        AccountCategory.MOBILE_BANKING -> listOf(GoldenAmber, CoralRed)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (account.type) {
                                AccountCategory.WALLET -> Icons.Default.AccountBalance
                                AccountCategory.BANK -> Icons.Default.AccountBalance
                                AccountCategory.MOBILE_BANKING -> Icons.Default.PhoneAndroid
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            account.nickname ?: account.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                account.provider.displayName(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (account.hasLimit && account.dailyTransferLimit != null) {
                                Surface(
                                    shape = ChipShape,
                                    color = SapphireBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        " ${account.dailyTransferLimit.toInt()} limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SapphireBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            maskAccountNumber(account.accountNumber),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "৳ ${formatAmount(account.balance)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else CoralRed
                    )
                    account.maxBalance?.let { max ->
                        Text(
                            "of ৳ ${formatAmount(max)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (account.maxBalance != null && account.maxBalance > 0) {
                Spacer(Modifier.height(12.dp))
                val progress = account.getBalancePercentage()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = when {
                        progress >= 0.9f -> CoralRed
                        progress >= 0.7f -> GoldenAmber
                        progress >= 0.5f -> Color(0xFFFFC107)
                        else -> EmeraldGreen
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${(progress * 100).toInt()}% used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (account.maxBalance - account.balance > 0) {
                        Text(
                            "৳ ${formatAmount(account.maxBalance - account.balance)} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
        AccountCategory.BANK -> listOf(
            AccountProvider.BANK, AccountProvider.SAVINGS, AccountProvider.CREDIT_CARD,
            AccountProvider.LOAN, AccountProvider.DBBL, AccountProvider.CITY_BANK,
            AccountProvider.BRAC_BANK, AccountProvider.BKB, AccountProvider.SONALI_BANK
        )
        AccountCategory.MOBILE_BANKING -> listOf(
            AccountProvider.BKASH, AccountProvider.NAGAD, AccountProvider.ROCKET, AccountProvider.UCASH
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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
                    Switch(checked = showMaxBalanceField, onCheckedChange = { showMaxBalanceField = it })
                }

                if (showMaxBalanceField) {
                    OutlinedTextField(
                        value = maxBalance,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) maxBalance = it },
                        label = { Text("Max Balance") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        Text("Set limit for daily transfers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasLimit, onCheckedChange = { hasLimit = it })
                }

                if (hasLimit) {
                    OutlinedTextField(
                        value = dailyLimit,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) dailyLimit = it },
                        label = { Text("Limit Amount") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    onConfirm(selectedProvider.displayName(), selectedCategory, selectedProvider, accountNumber, nickname.ifEmpty { null }, balance, max, hasLimit, limit)
                },
                enabled = accountNumber.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text("Edit Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(SapphireBlue, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
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
                    label = { Text("Balance") },
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
                        Text("Max Balance", style = MaterialTheme.typography.labelMedium)
                        Text("For progress bar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = showMaxBalance, onCheckedChange = { showMaxBalance = it })
                }

                if (showMaxBalance) {
                    OutlinedTextField(
                        value = maxBalance,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) maxBalance = it },
                        label = { Text("Max Balance") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                        Text("For daily transfers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasLimit, onCheckedChange = { hasLimit = it })
                }

                if (hasLimit) {
                    OutlinedTextField(
                        value = dailyLimit,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) dailyLimit = it },
                        label = { Text("Limit Amount") },
                        prefix = { Text("৳ ") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    onConfirm(account.copy(
                        nickname = nickname.ifEmpty { null },
                        accountNumber = accountNumber,
                        balance = newBalance,
                        maxBalance = max,
                        hasLimit = hasLimit,
                        dailyTransferLimit = limit,
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountConfirmDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CoralRed)
                        Column {
                            Text(account.nickname ?: account.name, fontWeight = FontWeight.Medium)
                            Text("Balance: ৳ ${formatAmount(account.balance)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Text(
                    "This account has zero balance and will be permanently removed.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountWithTransferDialog(
    account: Account,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedTargetAccount by remember { mutableStateOf<Account?>(null) }
    var targetExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete & Transfer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CoralRed)
                        Column {
                            Text(account.nickname ?: account.name, fontWeight = FontWeight.Medium)
                            Text("Balance: ৳ ${formatAmount(account.balance)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Text(
                    "This account has a balance of ৳ ${formatAmount(account.balance)}. Transfer the balance to another account before deleting:",
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
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
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

                selectedTargetAccount?.let { target ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GreenSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Text(
                                "Will transfer ৳ ${formatAmount(account.balance)} → ${target.nickname ?: target.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedTargetAccount?.let { onConfirm(it.id) } },
                enabled = selectedTargetAccount != null,
                colors = ButtonDefaults.buttonColors(containerColor = DeleteRed)
            ) { Text("Delete & Transfer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%,.0f", amount)
}

private fun maskAccountNumber(number: String): String {
    return if (number.length > 4) "****${number.takeLast(4)}" else number
}
