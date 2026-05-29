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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.displayName
import com.rudra.smartworktracker.data.entity.icon
import com.rudra.smartworktracker.ui.components.AppColors
import kotlinx.coroutines.flow.collectLatest
import java.util.*

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

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
    var transferFee by remember { mutableStateOf("") }
    var cashOutFee by remember { mutableStateOf("") }
    var showFees by remember { mutableStateOf(false) }

    var showFromSelector by remember { mutableStateOf(false) }
    var showToSelector by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    val validationResult = viewModel.validateTransfer(fromAccount, toAccount, amount, transferFee, cashOutFee)

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                AccountSelectorCard(
                    label = "FROM",
                    selectedAccount = fromAccount,
                    onClick = { showFromSelector = true },
                    isExpanded = showFromSelector,
                    accounts = accounts.filter { it.id != toAccount?.id }
                )
            }

            item {
                AccountSelectorCard(
                    label = "TO",
                    selectedAccount = toAccount,
                    onClick = { showToSelector = true },
                    isExpanded = showToSelector,
                    accounts = accounts.filter { it.id != fromAccount?.id }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = ChipShape
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Transfer Details",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
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
            }

            fromAccount?.let { account ->
                item {
                    Text(
                        text = "Available: ৳ ${String.format(Locale.getDefault(), "%,.0f", account.balance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                val feeAmount = (transferFee.toDoubleOrNull() ?: 0.0) + (cashOutFee.toDoubleOrNull() ?: 0.0)
                val totalAmount = (amount.toDoubleOrNull() ?: 0.0) + feeAmount
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(ChipShape)
                            .clickable { showFees = !showFees }
                            .background(
                                brush = Brush.horizontalGradient(listOf(GoldenAmber, CoralRed)),
                                shape = ChipShape
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (showFees) "▼ Fees & Charges" else "▶ Fees & Charges",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (feeAmount > 0) {
                            Text(
                                text = "৳ ${String.format(Locale.getDefault(), "%,.0f", feeAmount)}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showFees) {
                        OutlinedTextField(
                            value = transferFee,
                            onValueChange = {
                                if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) {
                                    transferFee = it
                                }
                            },
                            label = { Text("Transfer Fee (Optional)") },
                            prefix = { Text("৳ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cashOutFee,
                            onValueChange = {
                                if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) {
                                    cashOutFee = it
                                }
                            },
                            label = { Text("Cash Out Fee (Optional)") },
                            prefix = { Text("৳ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: ৳ ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (error != null) {
                item {
                    Card(
                        modifier = Modifier.shadow(6.dp, CardShape, clip = false),
                        shape = CardShape,
                        elevation = CardDefaults.cardElevation(0.dp),
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
            }

            item {
                Button(
                    onClick = { 
                        if (validationResult is ValidationResult.Valid && transferState !is TransferState.Loading) {
                            viewModel.makeTransfer(
                                fromAccount = fromAccount!!,
                                toAccount = toAccount!!,
                                amount = amount.toDouble(),
                                notes = notes.ifEmpty { null },
                                transferFee = transferFee.toDoubleOrNull() ?: 0.0,
                                cashOutFee = cashOutFee.toDoubleOrNull() ?: 0.0
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fromAccount != null && toAccount != null && amount.isNotBlank() && 
                             fromAccount?.id != toAccount?.id && amount.toDoubleOrNull() != null &&
                             (amount.toDoubleOrNull() ?: 0.0) > 0 &&
                             transferState !is TransferState.Loading
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Transfer")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                totalFee = state.totalFee,
                onDismiss = {
                    showConfirmation = false
                    fromAccount = null
                    toAccount = null
                    amount = ""
                    notes = ""
                    transferFee = ""
                    cashOutFee = ""
                    showFees = false
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
            com.rudra.smartworktracker.data.entity.AccountCategory.WALLET -> AppColors.PurpleSurface
            com.rudra.smartworktracker.data.entity.AccountCategory.BANK -> AppColors.GreenSurface
            com.rudra.smartworktracker.data.entity.AccountCategory.MOBILE_BANKING -> AppColors.AmberSurface
        }
    } ?: AppColors.GraySurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, CardShape, clip = false)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(listOf(EmeraldGreen, SapphireBlue)),
                        shape = ChipShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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
                            .shadow(6.dp, CardShape, clip = false)
                            .clickable { onSelect(account) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = CardShape,
                        elevation = CardDefaults.cardElevation(0.dp)
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
    totalFee: Double = 0.0,
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
                if (totalFee > 0) {
                    Text(
                        text = "Fees charged: ৳ ${String.format(Locale.getDefault(), "%,.0f", totalFee)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralRed
                    )
                }
                HorizontalDivider()
                Text(
                    text = "New balances:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "• ${fromAccount.nickname ?: fromAccount.name}: ৳ ${String.format(Locale.getDefault(), "%,.0f", fromAccount.balance)} (was ${String.format(Locale.getDefault(), "%,.0f", fromAccount.balance + amount + totalFee)})",
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