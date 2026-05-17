package com.rudra.smartworktracker.ui.screens.creditcard

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.CreditCard
import kotlinx.coroutines.delay

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
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf<CreditCard?>(null) }
    var showPayBillDialog by remember { mutableStateOf<CreditCard?>(null) }

    val totalCards = creditCards.size
    val totalLimit = creditCards.sumOf { it.cardLimit }
    val totalBalance = creditCards.sumOf { it.currentBalance }
    val utilization = if (totalLimit > 0) (totalBalance / totalLimit).coerceIn(0.0, 1.0) else 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {},
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCardDialog = true },
                containerColor = VioletPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Credit Card")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                brush = Brush.linearGradient(listOf(SapphireBlue, VioletPurple)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Credit Cards", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Manage your credit cards & bills", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (creditCards.isNotEmpty()) {
                    item { CreditCardStats(totalCards, totalLimit, totalBalance, utilization) }
                }

                items(creditCards, key = { it.id }) { card ->
                    CreditCardItem(
                        card = card,
                        onAddTransactionClick = { showAddTransactionDialog = card },
                        onPayBillClick = { showPayBillDialog = card }
                    )
                }

                if (creditCards.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(80.dp).background(
                                        brush = Brush.linearGradient(listOf(VioletPurple.copy(alpha = 0.3f), SapphireBlue.copy(alpha = 0.2f))),
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(40.dp), tint = VioletPurple.copy(alpha = 0.6f))
                                }
                                Spacer(Modifier.height(16.dp))
                                Text("No credit cards yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tap + to add your first card", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        if (showAddCardDialog) {
            AddCreditCardDialog(
                onDismiss = { showAddCardDialog = false },
                onConfirm = {
                    viewModel.addCreditCard(it)
                    showAddCardDialog = false
                }
            )
        }
        showAddTransactionDialog?.let { card ->
            AddTransactionDialog(
                card = card,
                onDismiss = { showAddTransactionDialog = null },
                onConfirm = { amount, description ->
                    viewModel.addCardTransaction(card, amount, description)
                    showAddTransactionDialog = null
                }
            )
        }
        showPayBillDialog?.let { card ->
            PayBillDialog(
                card = card,
                onDismiss = { showPayBillDialog = null },
                onConfirm = { amount ->
                    viewModel.payCreditCardBill(card, amount)
                    showPayBillDialog = null
                }
            )
        }
    }
}

@Composable
fun CreditCardStats(totalCards: Int, totalLimit: Double, totalBalance: Double, utilization: Double) {
    var animatedLimit by remember { mutableFloatStateOf(0f) }
    var animatedBalance by remember { mutableFloatStateOf(0f) }
    var animatedUtil by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(totalLimit, totalBalance, utilization) {
        delay(200)
        animatedLimit = totalLimit.toFloat()
        animatedBalance = totalBalance.toFloat()
        animatedUtil = utilization.toFloat()
    }

    val limitAnim by animateFloatAsState(animatedLimit, tween(1000), label = "cl")
    val balanceAnim by animateFloatAsState(animatedBalance, tween(1000), label = "cb")
    val utilAnim by animateFloatAsState(animatedUtil, tween(1000), label = "cu")

    val utilColor = when {
        utilization < 0.3 -> EmeraldGreen
        utilization < 0.7 -> GoldenAmber
        else -> CoralRed
    }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
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
                        brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Card Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    GlassStat(label = "Cards", value = "$totalCards", accentColor = VioletPurple, bgColor = PurpleSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    GlassStat(label = "Total Limit", value = "৳${"%,.0f".format(limitAnim)}", accentColor = EmeraldGreen, bgColor = GreenSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    GlassStat(label = "Balance", value = "৳${"%,.0f".format(balanceAnim)}", accentColor = if (totalBalance > 0) CoralRed else EmeraldGreen, bgColor = if (totalBalance > 0) RedSurface else GreenSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)).background(utilColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${(utilAnim * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = utilColor
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Credit Utilization", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        when {
                            utilization < 0.3 -> "Healthy utilization"
                            utilization < 0.7 -> "Moderate usage"
                            else -> "High utilization"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(utilColor.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(utilAnim).background(
                                brush = Brush.horizontalGradient(listOf(utilColor.copy(alpha = 0.7f), utilColor)),
                                shape = RoundedCornerShape(3.dp)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlassStat(label: String, value: String, accentColor: Color, bgColor: Color) {
    Card(
        modifier = Modifier.shadow(4.dp, RoundedCornerShape(14.dp), clip = false),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
fun CreditCardItem(card: CreditCard, onAddTransactionClick: () -> Unit, onPayBillClick: () -> Unit) {
    val utilization = if (card.cardLimit > 0) (card.currentBalance / card.cardLimit).coerceIn(0.0, 1.0) else 0.0
    val utilColor = when {
        utilization < 0.3 -> EmeraldGreen
        utilization < 0.7 -> GoldenAmber
        else -> CoralRed
    }
    val available = card.cardLimit - card.currentBalance

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    brush = Brush.horizontalGradient(listOf(VioletPurple, SapphireBlue)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                card.cardName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "**** ${card.cardNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Balance", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text(
                                "৳${"%,.0f".format(card.currentBalance)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Limit", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text(
                                "৳${"%,.0f".format(card.cardLimit)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(utilization.toFloat()).background(
                                brush = Brush.horizontalGradient(listOf(utilColor, utilColor.copy(alpha = 0.7f))),
                                shape = RoundedCornerShape(3.dp)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Text(
                                "Statement: ${card.statementDate}th",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            "Due: ${card.dueDate}th",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Available Credit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "৳${"%,.0f".format(available)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }
                        Surface(shape = PillShape, color = utilColor.copy(alpha = 0.1f)) {
                            Text(
                                "${(utilization * 100).toInt()}% used",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = utilColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddTransactionClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = ChipShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPurple)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add TXN", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onPayBillClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = ChipShape,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Bill", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCreditCardDialog(onDismiss: () -> Unit, onConfirm: (CreditCard) -> Unit) {
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardLimit by remember { mutableStateOf("") }
    var statementDate by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("Add New Credit Card", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Card Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ChipShape
                    )
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number (Last 4 Digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ChipShape
                    )
                    OutlinedTextField(
                        value = cardLimit,
                        onValueChange = { cardLimit = it },
                        label = { Text("Card Limit") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ChipShape
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = statementDate,
                            onValueChange = { statementDate = it },
                            label = { Text("Statement Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = ChipShape
                        )
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Due Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = ChipShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                CreditCard(
                                    cardName = cardName,
                                    cardNumber = cardNumber,
                                    cardLimit = cardLimit.toDoubleOrNull() ?: 0.0,
                                    currentBalance = 0.0,
                                    statementDate = statementDate.toIntOrNull() ?: 1,
                                    dueDate = dueDate.toIntOrNull() ?: 1
                                )
                            )
                        },
                        enabled = cardName.isNotBlank() && cardNumber.isNotBlank() && cardLimit.toDoubleOrNull() ?: 0.0 > 0,
                        shape = ChipShape
                    ) { Text("Add Card", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(card: CreditCard, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            brush = Brush.linearGradient(listOf(CoralRed, GoldenAmber)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("Add Transaction", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(shape = PillShape, color = VioletPurple.copy(alpha = 0.08f)) {
                    Text(
                        card.cardName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VioletPurple,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = ChipShape
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ChipShape
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, description) },
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                    ) { Text("Add Expense", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun PayBillDialog(card: CreditCard, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var payFull by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CardShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("Pay Bill", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(shape = PillShape, color = VioletPurple.copy(alpha = 0.08f)) {
                    Text(
                        card.cardName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VioletPurple,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(shape = PillShape, color = CoralRed.copy(alpha = 0.08f)) {
                    Text(
                        "Balance: ৳${"%,.0f".format(card.currentBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CoralRed,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = ChipShape
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = {
                        payFull = !payFull
                        if (payFull) amount = card.currentBalance.toString()
                        else amount = ""
                    },
                    shape = PillShape,
                    color = if (payFull) EmeraldGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (payFull) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (payFull) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Pay Full Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (payFull) FontWeight.Bold else FontWeight.Normal,
                            color = if (payFull) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) },
                        enabled = (amount.toDoubleOrNull() ?: 0.0) > 0,
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) { Text("Pay Now", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
