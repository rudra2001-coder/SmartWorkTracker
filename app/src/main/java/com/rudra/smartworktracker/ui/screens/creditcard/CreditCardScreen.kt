package com.rudra.smartworktracker.ui.screens.creditcard

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.Account
import com.rudra.smartworktracker.data.entity.AccountCategory
import com.rudra.smartworktracker.data.entity.AccountProvider
import com.rudra.smartworktracker.data.entity.CreditCard
import com.rudra.smartworktracker.data.entity.CreditCardTransaction
import kotlinx.coroutines.runBlocking
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
fun CreditCardScreen() {
    val context = LocalContext.current
    val viewModel: CreditCardViewModel = viewModel(factory = CreditCardViewModelFactory(context.applicationContext as Application))
    val creditCards by viewModel.creditCards.collectAsState()
    var showAddCardSheet by remember { mutableStateOf(false) }
    var selectedCardForActions by remember { mutableStateOf<CreditCard?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<CreditCard?>(null) }

    val totalLimit = creditCards.sumOf { it.cardLimit }
    val totalBalance = creditCards.sumOf { it.currentBalance }
    val totalAvailable = (totalLimit - totalBalance).coerceAtLeast(0.0)

    Scaffold(
        topBar = {},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardSheet = true },
                containerColor = SapphireBlue,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Credit Card", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                CreditCardHeader()
            }

            item {
                CreditCardSummaryCard(
                    totalLimit = totalLimit,
                    totalBalance = totalBalance,
                    totalAvailable = totalAvailable,
                    cardCount = creditCards.size
                )
            }

            if (creditCards.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            items(creditCards, key = { it.id }) { card ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CreditCardItem(
                        card = card,
                        onAddTransactionClick = { selectedCardForActions = it },
                        onPayBillClick = { selectedCardForActions = it },
                        onTransferClick = { selectedCardForActions = it },
                        onViewHistoryClick = { selectedCardForActions = it },
                        onEditClick = { selectedCardForActions = it },
                        onDeleteClick = { showDeleteConfirmation = it }
                    )
                }
            }

            if (creditCards.isEmpty()) {
                item {
                    EmptyCreditCardState(onAddCard = { showAddCardSheet = true })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddCardSheet) {
        AddEditCreditCardSheet(
            viewModel = viewModel,
            onDismiss = { showAddCardSheet = false }
        )
    }

    selectedCardForActions?.let { card ->
        CreditCardActionsBottomSheet(
            viewModel = viewModel,
            card = card,
            onDismiss = { selectedCardForActions = null }
        )
    }

    showDeleteConfirmation?.let { card ->
        DeleteCreditCardConfirmationDialog(
            card = card,
            onDismiss = { showDeleteConfirmation = null },
            onConfirm = {
                viewModel.deleteCreditCard(card)
                showDeleteConfirmation = null
            }
        )
    }
}

@Composable
fun CreditCardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(
                text = "Credit Cards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage limits, charges & payments",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray
            )
        }
    }
}

@Composable
fun CreditCardSummaryCard(
    totalLimit: Double,
    totalBalance: Double,
    totalAvailable: Double,
    cardCount: Int
) {
    val usagePercent = if (totalLimit > 0) (totalBalance / totalLimit).coerceIn(0.0, 1.0) else 0.0
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(usagePercent) {
        animatedProgress.animateTo(
            usagePercent.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = PillShape,
                    color = if (cardCount > 0) GreenSurface else AmberSurface
                ) {
                    Text(
                        text = "$cardCount Card${if (cardCount != 1) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (cardCount > 0) EmeraldGreen else GoldenAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Limit",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateGray
                    )
                    Text(
                        text = "৳${String.format("%.0f", totalLimit)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireBlue
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Used",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateGray
                    )
                    Text(
                        text = "৳${String.format("%.0f", totalBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralRed
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateGray
                    )
                    Text(
                        text = "৳${String.format("%.0f", totalAvailable)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = animatedProgress.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    trackColor = Color(0xFFE2E8F0),
                    color = if (usagePercent < 0.5) EmeraldGreen else if (usagePercent < 0.8) GoldenAmber else CoralRed,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(usagePercent * 100).toInt()}% of total limit used",
                style = MaterialTheme.typography.bodySmall,
                color = SlateGray,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CreditCardItem(
    card: CreditCard,
    onAddTransactionClick: (CreditCard) -> Unit,
    onPayBillClick: (CreditCard) -> Unit,
    onTransferClick: (CreditCard) -> Unit,
    onViewHistoryClick: (CreditCard) -> Unit,
    onEditClick: (CreditCard) -> Unit,
    onDeleteClick: (CreditCard) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val availableCredit = (card.cardLimit - card.currentBalance).coerceAtLeast(0.0)
    val usagePercent = if (card.cardLimit > 0) (card.currentBalance / card.cardLimit).coerceIn(0.0, 1.0) else 0.0
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(usagePercent) {
        animatedProgress.animateTo(
            usagePercent.toFloat(),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val progressColor by animateColorAsState(
        targetValue = when {
            usagePercent < 0.5 -> EmeraldGreen
            usagePercent < 0.8 -> GoldenAmber
            else -> CoralRed
        },
        label = "progressColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = card.cardName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "**** ${card.cardNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = PillShape,
                        color = when {
                            usagePercent < 0.5 -> GreenSurface
                            usagePercent < 0.8 -> AmberSurface
                            else -> RedSurface
                        }
                    ) {
                        Text(
                            text = "${(usagePercent * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                usagePercent < 0.5 -> EmeraldGreen
                                usagePercent < 0.8 -> GoldenAmber
                                else -> CoralRed
                            }
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = SlateGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CreditCardStatBox(
                    label = "Balance",
                    value = "৳${String.format("%.0f", card.currentBalance)}",
                    color = CoralRed,
                    surfaceColor = RedSurface,
                    modifier = Modifier.weight(1f)
                )
                CreditCardStatBox(
                    label = "Available",
                    value = "৳${String.format("%.0f", availableCredit)}",
                    color = EmeraldGreen,
                    surfaceColor = GreenSurface,
                    modifier = Modifier.weight(1f)
                )
                CreditCardStatBox(
                    label = "Limit",
                    value = "৳${String.format("%.0f", card.cardLimit)}",
                    color = SapphireBlue,
                    surfaceColor = BlueSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = Color(0xFFE2E8F0),
                    color = progressColor,
                    strokeCap = StrokeCap.Round
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = SlateGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Statement: ${card.statementDate}th", style = MaterialTheme.typography.bodySmall, color = SlateGray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isDueSoon(card.dueDate)) CoralRed else SlateGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Due: ${card.dueDate}th",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDueSoon(card.dueDate)) CoralRed else SlateGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionButton(
                            icon = Icons.Default.ShoppingCart,
                            label = "Charge",
                            backgroundColor = SapphireBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onAddTransactionClick(card) }
                        )
                        ActionButton(
                            icon = Icons.Default.Payments,
                            label = "Pay",
                            backgroundColor = EmeraldGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onPayBillClick(card) }
                        )
                        ActionButton(
                            icon = Icons.Default.Send,
                            label = "Transfer",
                            backgroundColor = VioletPurple,
                            modifier = Modifier.weight(1f),
                            onClick = { onTransferClick(card) }
                        )
                        ActionButton(
                            icon = Icons.Default.History,
                            label = "History",
                            backgroundColor = GoldenAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onViewHistoryClick(card) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditCardStatBox(
    label: String,
    value: String,
    color: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = ChipShape,
        color = backgroundColor.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun EmptyCreditCardState(onAddCard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No credit cards yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first credit card to start\nmanaging limits, charges, and payments",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddCard,
            shape = ChipShape,
            colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("Add Credit Card")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCreditCardSheet(
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardLimit by remember { mutableStateOf("") }
    var statementDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }

    if (accounts.isEmpty()) {
        accounts = runBlocking { viewModel.getAllAccounts() }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Add Credit Card",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = cardName,
                onValueChange = { cardName = it },
                label = { Text("Card Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                label = { Text("Card Number (Last 4 Digits)") },
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cardLimit,
                onValueChange = { cardLimit = it },
                label = { Text("Credit Limit (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = statementDate,
                    onValueChange = { statementDate = it },
                    label = { Text("Statement Day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Day") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Optional: Transfer to Account",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Select account (optional)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Transfer to Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = ChipShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text("${account.name} (৳${String.format("%.0f", account.balance)})") },
                            onClick = {
                                selectedAccount = account
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = transferAmount,
                onValueChange = { transferAmount = it },
                label = { Text("Transfer Amount (৳)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val limit = cardLimit.toDoubleOrNull() ?: 0.0
                    val transfer = transferAmount.toDoubleOrNull() ?: 0.0
                    if (cardName.isNotBlank() && limit > 0) {
                        val card = CreditCard(
                            cardName = cardName,
                            cardNumber = cardNumber.takeLast(4).padStart(4, '0'),
                            cardLimit = limit,
                            statementDate = statementDate.toIntOrNull()?.coerceIn(1, 28) ?: 1,
                            dueDate = dueDate.toIntOrNull()?.coerceIn(1, 28) ?: 1
                        )
                        viewModel.addCreditCard(card, transfer.coerceAtMost(limit), selectedAccount?.id)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                enabled = cardName.isNotBlank() && cardLimit.toDoubleOrNull() != null
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Add Credit Card")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardActionsBottomSheet(
    viewModel: CreditCardViewModel,
    card: CreditCard,
    onDismiss: () -> Unit
) {
    var currentAction by remember { mutableStateOf<CardAction?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var accountExpanded by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<CreditCardTransaction>>(emptyList()) }

    if (accounts.isEmpty()) {
        accounts = runBlocking { viewModel.getAllAccounts() }
    }

    LaunchedEffect(card.id) {
        transactions = runBlocking { viewModel.getTransactionsForCard(card.id) }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = currentAction != null)

    ModalBottomSheet(
        onDismissRequest = {
            if (currentAction == null) onDismiss() else currentAction = null
        },
        sheetState = sheetState
    ) {
        if (currentAction != null) {
            CardActionForm(
                action = currentAction!!,
                card = card,
                amount = amount,
                onAmountChange = { amount = it },
                description = description,
                onDescriptionChange = { description = it },
                selectedAccount = selectedAccount,
                accounts = accounts,
                accountExpanded = accountExpanded,
                onAccountExpandedChange = { accountExpanded = it },
                onAccountSelected = { selectedAccount = it },
                onDismiss = { currentAction = null },
                onConfirm = {
                    when (currentAction!!) {
                        CardAction.ADD_CHARGE -> {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) viewModel.addCardTransaction(card, amt, description.ifBlank { "Card charge" })
                        }
                        CardAction.PAY_BILL -> {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) viewModel.payCreditCardBill(card, amt, selectedAccount?.id)
                        }
                        CardAction.TRANSFER -> {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && selectedAccount != null) viewModel.transferFromCreditCard(card, amt, selectedAccount!!.id)
                        }
                        else -> {}
                    }
                    onDismiss()
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.cardName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "**** ${card.cardNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                val availableCredit = card.cardLimit - card.currentBalance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionSummaryBox(
                        label = "Balance",
                        value = "৳${String.format("%.0f", card.currentBalance)}",
                        color = CoralRed,
                        modifier = Modifier.weight(1f)
                    )
                    ActionSummaryBox(
                        label = "Available",
                        value = "৳${String.format("%.0f", availableCredit)}",
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                QuickActionRow(
                    icon = Icons.Default.ShoppingCart,
                    label = "Add Charge",
                    backgroundColor = SapphireBlue,
                    onClick = { currentAction = CardAction.ADD_CHARGE }
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuickActionRow(
                    icon = Icons.Default.Payments,
                    label = "Pay Bill",
                    backgroundColor = EmeraldGreen,
                    onClick = { currentAction = CardAction.PAY_BILL }
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuickActionRow(
                    icon = Icons.Default.Send,
                    label = "Transfer to Account",
                    backgroundColor = VioletPurple,
                    onClick = { currentAction = CardAction.TRANSFER }
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuickActionRow(
                    icon = Icons.Default.History,
                    label = "View Transactions (${transactions.size})",
                    backgroundColor = GoldenAmber,
                    onClick = { currentAction = CardAction.VIEW_HISTORY }
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuickActionRow(
                    icon = Icons.Default.Edit,
                    label = "Edit Card",
                    backgroundColor = SapphireBlue,
                    onClick = { currentAction = CardAction.EDIT }
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuickActionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete Card",
                    backgroundColor = CoralRed,
                    onClick = { currentAction = CardAction.DELETE }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardActionForm(
    action: CardAction,
    card: CreditCard,
    amount: String,
    onAmountChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedAccount: Account?,
    accounts: List<Account>,
    accountExpanded: Boolean,
    onAccountExpandedChange: (Boolean) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val availableCredit = card.cardLimit - card.currentBalance

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (action) {
                    CardAction.ADD_CHARGE -> "Add Charge"
                    CardAction.PAY_BILL -> "Pay Bill"
                    CardAction.TRANSFER -> "Transfer"
                    else -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = card.cardName,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (action == CardAction.PAY_BILL) {
            Surface(
                shape = ChipShape,
                color = RedSurface
            ) {
                Text(
                    text = "Outstanding: ৳${String.format("%.0f", card.currentBalance)}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CoralRed
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (action == CardAction.TRANSFER) {
            Surface(
                shape = ChipShape,
                color = GreenSurface
            ) {
                Text(
                    text = "Available: ৳${String.format("%.0f", availableCredit)}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (action == CardAction.ADD_CHARGE) {
            Surface(
                shape = ChipShape,
                color = BlueSurface
            ) {
                Text(
                    text = "Available: ৳${String.format("%.0f", availableCredit)}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SapphireBlue
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { onDescriptionChange(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = ChipShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphireBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (action == CardAction.PAY_BILL || action == CardAction.TRANSFER) {
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { onAccountExpandedChange(it) }
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "Main Balance (Default)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (action == CardAction.PAY_BILL) "Pay From" else "Transfer To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = ChipShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { onAccountExpandedChange(false) }
                ) {
                    if (action == CardAction.PAY_BILL) {
                        DropdownMenuItem(
                            text = { Text("Main Balance (Default)") },
                            onClick = {
                                onAccountSelected(Account(name = "Main Balance", type = com.rudra.smartworktracker.data.entity.AccountCategory.MOBILE_BANKING, provider = com.rudra.smartworktracker.data.entity.AccountProvider.BKASH, accountNumber = "0", balance = 0.0))
                                onAccountExpandedChange(false)
                            }
                        )
                    }
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text("${account.name} (৳${String.format("%.0f", account.balance)})") },
                            onClick = {
                                onAccountSelected(account)
                                onAccountExpandedChange(false)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { onAmountChange(it) },
            label = { Text("Amount (৳)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = ChipShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SapphireBlue,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (action == CardAction.PAY_BILL && card.currentBalance > 0) {
                Button(
                    onClick = {
                        onAmountChange(String.format("%.0f", card.currentBalance))
                        onConfirm()
                    },
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber)
                ) {
                    Text("Pay Full")
                }
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(if (action == CardAction.PAY_BILL) 1f else 2f),
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (action) {
                        CardAction.ADD_CHARGE -> SapphireBlue
                        CardAction.PAY_BILL -> EmeraldGreen
                        CardAction.TRANSFER -> VioletPurple
                        else -> SapphireBlue
                    }
                ),
                enabled = amount.toDoubleOrNull()?.let { it > 0 } == true &&
                    (action != CardAction.TRANSFER || selectedAccount != null)
            ) {
                Icon(
                    imageVector = when (action) {
                        CardAction.ADD_CHARGE -> Icons.Default.ShoppingCart
                        CardAction.PAY_BILL -> Icons.Default.Payments
                        CardAction.TRANSFER -> Icons.Default.Send
                        else -> Icons.Default.Add
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    when (action) {
                        CardAction.ADD_CHARGE -> "Add Charge"
                        CardAction.PAY_BILL -> "Pay Bill"
                        CardAction.TRANSFER -> "Transfer"
                        else -> "Confirm"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ActionSummaryBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = ChipShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun QuickActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = ChipShape,
        color = backgroundColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun DeleteCreditCardConfirmationDialog(
    card: CreditCard,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CoralRed.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CoralRed
                )
            }
        },
        title = { Text("Delete Credit Card") },
        text = {
            Column {
                Text("Are you sure you want to delete ${card.cardName}?")
                if (card.currentBalance > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = ChipShape,
                        color = RedSurface
                    ) {
                        Text(
                            text = "Warning: Outstanding balance of ৳${String.format("%.0f", card.currentBalance)} will be deducted from your main balance.",
                            modifier = Modifier.padding(12.dp),
                            color = CoralRed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                shape = ChipShape
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = ChipShape) {
                Text("Cancel")
            }
        }
    )
}

enum class CardAction {
    ADD_CHARGE, PAY_BILL, TRANSFER, VIEW_HISTORY, EDIT, DELETE
}

fun isDueSoon(dueDay: Int): Boolean {
    val currentDay = SimpleDateFormat("d", Locale.getDefault()).format(Date()).toInt()
    return dueDay - currentDay <= 5 && dueDay - currentDay >= 0
}
