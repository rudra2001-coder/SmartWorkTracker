package com.rudra.smartworktracker.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import com.rudra.smartworktracker.data.entity.displayName
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
private val SlateGray = Color(0xFF64748B)

private val GreenSurface = Color(0xFFE6FBF4)
private val RedSurface = Color(0xFFFFEDED)
private val BlueSurface = Color(0xFFEFF6FF)
private val PurpleSurface = Color(0xFFF5F3FF)
private val AmberSurface = Color(0xFFFFFBEB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: AccountDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadAccountDetails(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.account?.nickname ?: uiState.account?.name ?: "Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            ) { CircularProgressIndicator() }
        } else {
            uiState.account?.let { account ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    item { AccountBalanceHeroCard(account = account) }
                    item { QuickMetricsRow(account = account, totalInflow = uiState.totalInflow, totalOutflow = uiState.totalOutflow) }
                    item { BalanceHistorySection(history = uiState.balanceHistory) }

                    if (uiState.transactions.isNotEmpty()) {
                        item { TransactionSectionHeader() }
                        items(uiState.transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                accountId = account.id
                            )
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                                shape = CardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = SlateGray, modifier = Modifier.size(40.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("No transactions yet", style = MaterialTheme.typography.bodyLarge, color = SlateGray)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun AccountBalanceHeroCard(account: Account) {
    val accentColor = when (account.type) {
        AccountCategory.WALLET -> VioletPurple
        AccountCategory.BANK -> SapphireBlue
        AccountCategory.MOBILE_BANKING -> GoldenAmber
    }
    val surfaceColor = when (account.type) {
        AccountCategory.WALLET -> PurpleSurface
        AccountCategory.BANK -> BlueSurface
        AccountCategory.MOBILE_BANKING -> AmberSurface
    }

    var animatedBalance by remember { mutableStateOf(0.0) }
    LaunchedEffect(account.balance) {
        animatedBalance = account.balance
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(accentColor, if (account.type == AccountCategory.WALLET) SapphireBlue else EmeraldGreen)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                account.nickname ?: account.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                account.provider.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "৳ ${"%,.0f".format(animatedBalance)}",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else CoralRed
            )
            Spacer(Modifier.height(12.dp))
            Surface(shape = PillShape, color = surfaceColor) {
                Text(
                    "${account.type.displayName()} • ${maskNumber(account.accountNumber)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun QuickMetricsRow(account: Account, totalInflow: Double, totalOutflow: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricBlock(
            label = "Total In",
            value = "৳${"%,.0f".format(totalInflow)}",
            accentColor = EmeraldGreen,
            bgColor = GreenSurface,
            modifier = Modifier.weight(1f)
        )
        MetricBlock(
            label = "Total Out",
            value = "৳${"%,.0f".format(totalOutflow)}",
            accentColor = CoralRed,
            bgColor = RedSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (accentColor == EmeraldGreen) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = accentColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BalanceHistorySection(history: List<BalanceHistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                Text("Balance Activity", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(Modifier.height(16.dp))

            if (history.isEmpty() || history.all { it.balance == 0.0 }) {
                Text("No activity in last 7 days", style = MaterialTheme.typography.bodyMedium, color = SlateGray)
            } else {
                val maxBalance = history.maxOfOrNull { it.balance }?.coerceAtLeast(1.0) ?: 1.0
                val minBalance = history.minOfOrNull { it.balance }?.coerceAtMost(0.0) ?: 0.0
                val range = (maxBalance - minBalance).coerceAtLeast(1.0)

                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    history.forEach { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            val heightFraction = ((item.balance - minBalance) / range).toFloat().coerceIn(0.05f, 1f)
                            val barColor = if (item.balance >= 0) EmeraldGreen else CoralRed

                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height((80 * heightFraction).dp.coerceAtLeast(8.dp))
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            listOf(barColor, barColor.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                item.dayLabel.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionSectionHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Text("Transactions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun TransactionCard(
    transaction: FinancialTransaction,
    accountId: Long
) {
    val isInflow = transaction.destinationAccountId == accountId
    val accentColor = if (isInflow) EmeraldGreen else CoralRed
    val bgColor = if (isInflow) GreenSurface else RedSurface

    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isInflow) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatTransactionType(transaction.type, isInflow),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    transaction.note.ifEmpty { "No note" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    formatDate(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Text(
                "${if (isInflow) "+" else "-"} ৳${"%,.0f".format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = accentColor
            )
        }
    }
}

private fun formatTransactionType(type: TransactionType, isInflow: Boolean): String {
    return when (type) {
        TransactionType.INCOME -> "Income"
        TransactionType.EXPENSE -> if (isInflow) "Refund" else "Expense"
        TransactionType.TRANSFER -> if (isInflow) "Received" else "Sent"
        TransactionType.SAVINGS_ADD -> if (isInflow) "Savings Added" else "Savings Deposit"
        TransactionType.SAVINGS_WITHDRAW -> if (isInflow) "Savings Return" else "Savings Withdrawal"
        TransactionType.LOAN_BORROW -> if (isInflow) "Loan Received" else "Loan Taken"
        TransactionType.LOAN_LEND -> if (isInflow) "Loan Returned" else "Loan Given"
        TransactionType.LOAN_REPAY -> "Loan Repayment"
        TransactionType.LOAN_RECEIVE -> "Loan Received"
        TransactionType.EMI_PAID -> "EMI Payment"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun maskNumber(number: String): String {
    return if (number.length > 4) "****${number.takeLast(4)}" else number
}
