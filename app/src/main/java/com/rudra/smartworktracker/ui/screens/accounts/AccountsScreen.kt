package com.rudra.smartworktracker.ui.screens.accounts

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account

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
