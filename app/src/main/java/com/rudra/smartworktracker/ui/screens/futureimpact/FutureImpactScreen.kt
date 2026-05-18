package com.rudra.smartworktracker.ui.screens.futureimpact

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.CheckInType
import com.rudra.smartworktracker.model.ConsequenceDebt
import com.rudra.smartworktracker.model.Decision
import com.rudra.smartworktracker.model.DecisionCategory
import com.rudra.smartworktracker.model.DecisionType
import com.rudra.smartworktracker.model.FutureIdentity
import com.rudra.smartworktracker.model.UserHistory
import com.rudra.smartworktracker.model.WeeklyReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureImpactScreen(
    viewModel: FutureImpactViewModel = viewModel(
        factory = FutureImpactViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val todayDecisions by viewModel.todayDecisions.collectAsState()
    val weekDecisions by viewModel.weekDecisions.collectAsState()
    val impactStats by viewModel.impactStats.collectAsState()
    val patternWarnings by viewModel.patternWarnings.collectAsState()
    val streaks by viewModel.streaks.collectAsState()
    val dailyScore by viewModel.dailyScore.collectAsState()
    val immediateFeedback by viewModel.immediateFeedback.collectAsState()
    val undoWindow by viewModel.undoWindow.collectAsState()
    val selectedIdentity by viewModel.selectedIdentity.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val totalDebt by viewModel.totalDebt.collectAsState()
    val userHistory by viewModel.userHistory.collectAsState()
    val latestReport by viewModel.latestReport.collectAsState()
    val futureProjection by viewModel.futureProjection.collectAsState()
    val showCheckIn by viewModel.showCheckInPrompt.collectAsState()

    var showConsequenceDialog by remember { mutableStateOf<DecisionType?>(null) }
    var showIdentityDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<DecisionCategory?>(null) }

    // Check-in Dialog
    if (showCheckIn) {
        val checkInType = viewModel.getCheckInType()
        if (checkInType != null) {
            CheckInDialog(
                checkInType = checkInType,
                onDismiss = { viewModel.dismissCheckInPrompt() },
                onSave = { mood, answer ->
                    when (checkInType) {
                        CheckInType.MORNING -> viewModel.saveMorningCheckIn(mood, answer)
                        CheckInType.NIGHT -> viewModel.saveNightCheckIn(mood, answer)
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Log Decision")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Header
                item {
                    FutureSelfHeader(
                        history = userHistory,
                        onIdentityClick = { showIdentityDialog = true }
                    )
                }

                // Daily Score + Identity
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    DailyRealityScoreCard(
                        score = dailyScore,
                        identity = selectedIdentity,
                        identityMessage = viewModel.getIdentityMessage()
                    )
                }

                // Streaks
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    StreakCard(streaks = streaks)
                }

                // Undo Window
                if (undoWindow != null) {
                    item {
                        UndoWindowBanner(
                            undoWindow = undoWindow!!,
                            onRecover = { viewModel.executeRecovery() },
                            onDismiss = { viewModel.dismissUndoWindow() }
                        )
                    }
                }

                // Immediate Feedback
                if (immediateFeedback != null) {
                    item {
                        ImmediateFeedbackCard(feedback = immediateFeedback!!)
                    }
                }

                // Consequence Debt
                if (totalDebt != 0f || debts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        ConsequenceDebtCard(debts = debts, totalDebt = totalDebt)
                    }
                }

                // Future Collapse Simulation
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    FutureCollapseCard(projection = futureProjection)
                }

                // Impact Summary
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    ImpactSummaryCard(stats = impactStats)
                }

                // Pattern Warnings
                if (patternWarnings.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🚨 Reality Alerts", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    items(patternWarnings) { warning ->
                        PatternWarningCard(warning = warning)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Weekly Report
                if (latestReport != null) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        WeeklyRealityReportCard(report = latestReport!!)
                    }
                }

                // Quick Log
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Quick Log", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                item {
                    CategorySelector(selectedCategory, { selectedCategory = it })
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                val filteredTypes = if (selectedCategory != null) {
                    DecisionType.entries.filter { it.category == selectedCategory }
                } else DecisionType.entries.filter { it != DecisionType.CUSTOM }

                items(filteredTypes.chunked(2)) { rowTypes ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowTypes.forEach { type ->
                            DecisionChip(type, { viewModel.addDecision(type) }, { showConsequenceDialog = type }, Modifier.weight(1f))
                        }
                        if (rowTypes.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Today's Decisions
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Today's Decisions", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                if (todayDecisions.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else {
                    items(todayDecisions) { decision ->
                        DecisionItem(decision, { viewModel.deleteDecision(decision.id) }, { showConsequenceDialog = decision.decisionType })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Dialogs
        if (showConsequenceDialog != null) {
            ConsequenceDialog(showConsequenceDialog!!, { showConsequenceDialog = null })
        }

        if (showIdentityDialog) {
            IdentitySelectionDialog(selectedIdentity, { viewModel.setIdentity(it); showIdentityDialog = false }, { showIdentityDialog = false })
        }
    }
}

@Composable
fun FutureSelfHeader(history: UserHistory?, onIdentityClick: () -> Unit) {
    Column {
        Text("Future Self", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Are your actions matching who you want to become?", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        
        if (history != null && history.totalDaysActive > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                onClick = onIdentityClick
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("You've built ${history.totalDaysActive} days of data • ${history.totalDecisions} decisions", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DailyRealityScoreCard(score: Int, identity: FutureIdentity, identityMessage: String) {
    val scoreColor = when {
        score >= 80 -> Color(0xFF2E7D32)
        score >= 60 -> Color(0xFF7CB342)
        score >= 40 -> Color(0xFFF9A825)
        score >= 20 -> Color(0xFFFF9800)
        else -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scoreColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Daily Reality Score", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    if (identity != FutureIdentity.NO_IDENTITY) {
                        Text("🎯 ${identity.displayName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp).clip(CircleShape).background(Brush.radialGradient(listOf(scoreColor, scoreColor.copy(alpha = 0.3f))))) {
                    Text("$score", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            if (identityMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = scoreColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(identityMessage, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = scoreColor, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun StreakCard(streaks: StreakInfo) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = if (streaks.isOnDisciplineStreak) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (streaks.currentDisciplineStreak >= 2) {
                    val infiniteTransition = rememberInfiniteTransition(label = "fire")
                    val scale by infiniteTransition.animateFloat(1f, 1.2f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "fireScale")
                    Text("🔥", fontSize = 24.sp, modifier = Modifier.scale(scale))
                } else Text("💪", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Discipline", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${streaks.currentDisciplineStreak} days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (streaks.isOnDisciplineStreak) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = if (streaks.isOnDamageStreak) Color(0xFFD32F2F).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (streaks.isOnDamageStreak) {
                    val infiniteTransition = rememberInfiniteTransition(label = "warn")
                    val scale by infiniteTransition.animateFloat(1f, 1.2f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "warnScale")
                    Text("⚠️", fontSize = 24.sp, modifier = Modifier.scale(scale))
                } else Text("😴", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Damage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("${streaks.currentDamageStreak} days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (streaks.isOnDamageStreak) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun ConsequenceDebtCard(debts: List<ConsequenceDebt>, totalDebt: Float) {
    val debtColor = if (totalDebt > 20) Color(0xFFD32F2F) else if (totalDebt > 0) Color(0xFFFF9800) else Color(0xFF2E7D32)
    
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = debtColor.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = debtColor)
                Spacer(Modifier.width(8.dp))
                Text("Consequence Debt", fontWeight = FontWeight.Bold, color = debtColor)
                Spacer(Modifier.weight(1f))
                Text("৳${String.format("%.0f", totalDebt)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = debtColor)
            }
            if (debts.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                debts.filter { it.debtAmount != 0f }.forEach { debt ->
                    val catColor = when {
                        debt.debtAmount > 10 -> Color(0xFFD32F2F)
                        debt.debtAmount > 0 -> Color(0xFFFF9800)
                        else -> Color(0xFF2E7D32)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(debt.category.displayName, fontSize = 12.sp)
                        Text("${if (debt.debtAmount > 0) "+" else ""}${String.format("%.0f", debt.debtAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = catColor)
                    }
                }
            }
        }
    }
}

@Composable
fun FutureCollapseCard(projection: FutureProjection) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.width(8.dp))
                Text("Future Collapse Simulation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            
            // Simple visual graph
            FutureGraph(projection = projection)
            
            Spacer(Modifier.height(12.dp))
            Text(projection.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

@Composable
fun FutureGraph(projection: FutureProjection) {
    val lineColor = when (projection.trend) {
        Trend.IMPROVING -> Color(0xFF2E7D32)
        Trend.DECLINING -> Color(0xFFD32F2F)
        Trend.STABLE -> Color(0xFFF9A825)
    }
    
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        val points = listOf(
            projection.currentScore.toFloat(),
            projection.week1Score.toFloat(),
            projection.week2Score.toFloat(),
            projection.week3Score.toFloat(),
            projection.week4Score.toFloat()
        )
        
        val maxY = 100f
        val minY = 0f
        val rangeY = maxY - minY
        
        val stepX = size.width / (points.size - 1)
        
        val path = Path()
        points.forEachIndexed { index, score ->
            val x = index * stepX
            val y = size.height - ((score - minY) / rangeY * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(path, lineColor, style = Stroke(width = 4f))
        
        // Draw points
        points.forEachIndexed { index, score ->
            val x = index * stepX
            val y = size.height - ((score - minY) / rangeY * size.height)
            drawCircle(lineColor, 8f, Offset(x, y))
        }
    }
    
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Now", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
        Text("1W", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
        Text("2W", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
        Text("3W", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
        Text("4W", fontSize = 10.sp, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f))
    }
}

@Composable
fun ImpactSummaryCard(stats: ImpactStats) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("7-Day Impact", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.weekPositive}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("Positive", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.weekNegative}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Text("Negative", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val netColor = if (stats.weekNetImpact >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    Text("${if (stats.weekNetImpact >= 0) "+" else ""}${stats.weekNetImpact}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = netColor)
                    Text("Net", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun WeeklyRealityReportCard(report: WeeklyReport) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(8.dp))
                Text("Weekly Reality Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            Text(report.summary, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Best: ${report.bestImprovement}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                    Text("Worst: ${report.biggestMistake}", fontSize = 12.sp, color = Color(0xFFD32F2F))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${report.positiveDecisions} positive", fontSize = 12.sp)
                    Text("${report.negativeDecisions} negative", fontSize = 12.sp)
                }
            }
            if (report.identityAlignment > 0) {
                Spacer(Modifier.height(4.dp))
                Text("🎯 Identity Alignment: ${report.identityAlignment}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CheckInDialog(checkInType: CheckInType, onDismiss: () -> Unit, onSave: (Int, String) -> Unit) {
    var mood by remember { mutableIntStateOf(3) }
    var answer by remember { mutableStateOf("") }
    
    val question = when (checkInType) {
        CheckInType.MORNING -> "What kind of day will you have?"
        CheckInType.NIGHT -> "Did your actions match your future self?"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (checkInType == CheckInType.MORNING) "☀️ Morning Check-in" else "🌙 Night Check-in") },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                Text(question, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))
                
                Text("How do you feel?", fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..5).forEach { m ->
                        val emoji = when (m) {
                            1 -> "😫"
                            2 -> "😕"
                            3 -> "😐"
                            4 -> "🙂"
                            5 -> "😄"
                            else -> "😐"
                        }
                        val scale by animateFloatAsState(
                            targetValue = if (mood == m) 1.3f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "scale"
                        )
                        Surface(
                            onClick = { mood = m },
                            modifier = Modifier.scale(scale),
                            shape = CircleShape,
                            color = if (mood == m) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(8.dp))
                        }

                    }
                }
                
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(answer, onValueChange = { answer = it }, label = { Text("Your intention/commitment") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(mood, answer) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}

@Composable
fun EmptyStateCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("No decisions logged yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Log decisions to see their impact!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PatternWarningCard(warning: PatternWarning) {
    val (bg, iconColor, icon) = when (warning.severity) {
        PatternSeverity.HIGH -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Default.Warning)
        PatternSeverity.MEDIUM -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Info)
        PatternSeverity.LOW -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Notifications)
    }
    
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(warning.title, fontWeight = FontWeight.Bold, color = iconColor)
            }
            Spacer(Modifier.height(4.dp))
            Text(warning.description, fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            Text("💡 ${warning.advice}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CategorySelector(selected: DecisionCategory?, onSelect: (DecisionCategory?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("All") }) }
        items(DecisionCategory.entries.toList()) { FilterChip(selected = selected == it, onClick = { onSelect(it) }, label = { Text(it.displayName) }) }
    }
}

@Composable
fun DecisionChip(type: DecisionType, onClick: () -> Unit, onInfo: () -> Unit, mod: Modifier = Modifier) {
    val isPos = type.defaultImpact > 0
    val color = if (isPos) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    Surface(modifier = mod, shape = MaterialTheme.shapes.medium, color = color.copy(alpha = 0.1f), onClick = onClick) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isPos) Icons.Default.Add else Icons.Default.Remove, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(type.displayName, fontSize = 12.sp, color = color, modifier = Modifier.weight(1f))
            IconButton(onClick = onInfo, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = color.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DecisionItem(decision: Decision, onDelete: () -> Unit, onInfo: () -> Unit) {
    val isPos = decision.isPositive
    val color = if (isPos) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isPos) Icons.Default.ThumbUp else Icons.Default.ThumbDown, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(decision.decisionType.displayName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                if (decision.notes.isNotBlank()) Text(decision.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            IconButton(onClick = onInfo) { Icon(Icons.Default.Info, contentDescription = "Consequences", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun UndoWindowBanner(undoWindow: UndoWindow, onRecover: () -> Unit, onDismiss: () -> Unit) {
    val remaining = (undoWindow.expiresAt - System.currentTimeMillis()) / 60000
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Undo, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Recovery Window!", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                Text(undoWindow.recoveryAction, fontSize = 13.sp)
                Text("${remaining}min left", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Button(onClick = onRecover, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)), modifier = Modifier.height(36.dp)) {
                Text("Do It!", fontSize = 12.sp)
            }
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) }
        }
    }
}

@Composable
fun ImmediateFeedbackCard(feedback: ImmediateFeedback) {
    val isPos = feedback.decisionType.defaultImpact > 0
    val bg = if (isPos) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val txtColor = if (isPos) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isPos) Icons.Default.TrendingUp else Icons.Default.TrendingDown, contentDescription = null, tint = txtColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Immediate Impact", fontWeight = FontWeight.Bold, color = txtColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(feedback.immediateConsequence, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = txtColor)
            if (feedback.timesThisWeek > 1) {
                Spacer(Modifier.height(4.dp))
                Text("Done this ${feedback.timesThisWeek}x this week", fontSize = 13.sp)
            }
            if (feedback.estimatedWeightChange != 0f) {
                Spacer(Modifier.height(4.dp))
                Text("At this rate: ${if (feedback.estimatedWeightChange > 0) "+" else ""}${String.format("%.1f", feedback.estimatedWeightChange * 4)}kg/month", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
        }
    }
}

@Composable
fun ConsequenceDialog(type: DecisionType, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = if (type.defaultImpact > 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)) }, title = { Text(type.displayName) }, text = {
        Column {
            Text("7-Day", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(type.shortTerm7Day, modifier = Modifier.padding(bottom = 12.dp))
            Text("30-Day", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(type.longTerm30Day, modifier = Modifier.padding(bottom = 12.dp))
            Text("Immediate", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(type.immediateConsequence)
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Got it!") } })
}

@Composable
fun IdentitySelectionDialog(current: FutureIdentity, onSelect: (FutureIdentity) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("🎯 Set Your Future Identity") }, text = {
        Column {
            Text("Who do you want to become?", modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            FutureIdentity.entries.filter { it != FutureIdentity.NO_IDENTITY }.forEach { identity ->
                val isSel = current == identity
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), onClick = { onSelect(identity) }) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(when(identity) {
                            FutureIdentity.FIT_SELF -> "💪"
                            FutureIdentity.RICH_SELF -> "💰"
                            FutureIdentity.DISCIPLINED_SELF -> "🎯"
                            FutureIdentity.HAPPY_SELF -> "😊"
                            FutureIdentity.GROWING_SELF -> "📚"
                            else -> "🎯"
                        }, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(identity.displayName, fontWeight = FontWeight.Medium); Text(identity.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}


