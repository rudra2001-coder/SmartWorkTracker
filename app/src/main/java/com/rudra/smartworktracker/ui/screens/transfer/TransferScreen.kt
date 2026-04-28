package com.rudra.smartworktracker.ui.screens.transfer

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.displayName
import com.rudra.smartworktracker.data.entity.icon
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: TransferViewModel = viewModel(factory = TransferViewModelFactory(application))
    val accounts by viewModel.accounts.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val error by viewModel.error.collectAsState()

    var fromAccount by remember(accounts) { 
        mutableStateOf(accounts.find { it.name == "Cash" } ?: accounts.firstOrNull()) 
    }
    var toAccount by remember(accounts) { 
        mutableStateOf(accounts.getOrNull(1) ?: accounts.firstOrNull()) 
    }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showFromSelector by remember { mutableStateOf(false) }
    var showToSelector by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    val validationResult = viewModel.validateTransfer(fromAccount, toAccount, amount)

    LaunchedEffect(transferState) {
        if (transferState is TransferState.Success) {
            showConfirmation = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🔁 Transfer Money") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AccountSelectorCard(
                label = "FROM",
                selectedAccount = fromAccount,
                onClick = { showFromSelector = true },
                isExpanded = showFromSelector,
                accounts = accounts.filter { it.id != toAccount?.id }
            )

            AccountSelectorCard(
                label = "TO",
                selectedAccount = toAccount,
                onClick = { showToSelector = true },
                isExpanded = showToSelector,
                accounts = accounts.filter { it.id != fromAccount?.id }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) {
                        amount = it
                    }
                },
                label = { Text("Amount") },
                prefix = { Text("৳ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error != null && amount.isNotEmpty()
            )

            fromAccount?.let { account ->
                Text(
                    text = "Available: ৳ ${String.format(Locale.getDefault(), "%,.0f", account.balance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Fee: ৳ 0 (within app)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Total: ৳ ${amount.ifEmpty { "0" }}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    if (validationResult is ValidationResult.Valid) {
                        viewModel.makeTransfer(fromAccount!!, toAccount!!, amount.toDouble(), notes.ifEmpty { null })
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fromAccount != null && toAccount != null && amount.isNotBlank() && 
                         fromAccount?.id != toAccount?.id && amount.toDoubleOrNull() != null &&
                         (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Transfer")
            }
        }

        if (showFromSelector) {
            AccountSelectionSheet(
                title = "Select Source Account",
                accounts = accounts.filter { it.id != toAccount?.id },
                onSelect = { 
                    fromAccount = it
                    showFromSelector = false
                },
                onDismiss = { showFromSelector = false }
            )
        }

        if (showToSelector) {
            AccountSelectionSheet(
                title = "Select Destination Account",
                accounts = accounts.filter { it.id != fromAccount?.id },
                onSelect = { 
                    toAccount = it
                    showToSelector = false
                },
                onDismiss = { showToSelector = false }
            )
        }

        if (showConfirmation && transferState is TransferState.Success) {
            val state = transferState as TransferState.Success
            TransferSuccessDialog(
                amount = state.amount,
                fromAccount = state.fromAccount,
                toAccount = state.toAccount,
                onDismiss = {
                    showConfirmation = false
                    fromAccount = null
                    toAccount = null
                    amount = ""
                    notes = ""
                    viewModel.resetState()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectorCard(
    label: String,
    selectedAccount: Account?,
    onClick: () -> Unit,
    isExpanded: Boolean,
    accounts: List<Account>
) {
    val cardColor = selectedAccount?.let { 
        when (it.type) {
            com.rudra.smartworktracker.data.entity.AccountCategory.WALLET -> Color(0xFFF8F5FF)
            com.rudra.smartworktracker.data.entity.AccountCategory.BANK -> Color(0xFFE8F5E9)
            com.rudra.smartworktracker.data.entity.AccountCategory.MOBILE_BANKING -> Color(0xFFFFF3E0)
        }
    } ?: MaterialTheme.colorScheme.surfaceVariant

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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedAccount != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedAccount.nickname ?: selectedAccount.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Balance: ৳ ${String.format(Locale.getDefault(), "%,.0f", selectedAccount.balance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Select account",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionSheet(
    title: String,
    accounts: List<Account>,
    onSelect: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(account) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = account.nickname ?: account.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
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
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "৳ ${String.format(Locale.getDefault(), "%,.0f", account.balance)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TransferSuccessDialog(
    amount: Double,
    fromAccount: Account,
    toAccount: Account,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("✅", style = MaterialTheme.typography.headlineLarge) },
        title = { Text("Transfer Successful!") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "৳ ${String.format(Locale.getDefault(), "%,.0f", amount)} moved from ${fromAccount.nickname ?: fromAccount.name} → ${toAccount.nickname ?: toAccount.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider()
                Text(
                    text = "New balances:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• ${fromAccount.nickname ?: fromAccount.name}: ৳ ${String.format(Locale.getDefault(), "%,.0f", fromAccount.balance)} (was ${String.format(Locale.getDefault(), "%,.0f", fromAccount.balance + amount)})",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• ${toAccount.nickname ?: toAccount.name}: ৳ ${String.format(Locale.getDefault(), "%,.0f", toAccount.balance)} (was ${String.format(Locale.getDefault(), "%,.0f", toAccount.balance - amount)})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}