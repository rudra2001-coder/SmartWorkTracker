package com.rudra.smartworktracker.ui.screens.breaks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val GreenSurface = Color(0xFFE6FBF4)
private val BlueSurface = Color(0xFFEFF6FF)
private val PurpleSurface = Color(0xFFF5F3FF)
private val AmberSurface = Color(0xFFFFFBEB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulBreakScreen() {
    val context = LocalContext.current
    val viewModel: MindfulBreakViewModel = viewModel(factory = MindfulBreakViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val bgColors = listOf(
        Color(0xFF667eea), Color(0xFF764ba2), Color(0xFF1A73E8), Color(0xFF0D47A1)
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (uiState.sessionState == SessionState.RUNNING) 1f else 0.6f,
        animationSpec = tween(1000)
    )

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = bgColors.map { it.copy(alpha = bgAlpha) }
                    )
                )
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pattern selector
                item { PatternSelector(uiState, viewModel::selectPattern) }

                // Main breathing card
                item { BreathingCard(uiState, viewModel) }

                // Stats row
                if (uiState.totalMinutes > 0 || uiState.sessionsToday > 0) {
                    item { StatsRow(uiState) }
                }

                // Benefits card
                item { BenefitsCard() }
            }

            // Session summary dialog
            if (uiState.showSummary) {
                SessionSummaryDialog(uiState, viewModel::dismissSummary)
            }
        }
    }
}

@Composable
private fun PatternSelector(
    uiState: MindfulBreakUiState,
    onSelect: (BreathingPattern) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.1f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SelfImprovement, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Breathing Pattern", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BreathingPatterns.all.forEach { pattern ->
                    val isSelected = pattern.name == uiState.selectedPattern.name
                    val enabled = uiState.sessionState == SessionState.IDLE
                    Surface(
                        onClick = { if (enabled) onSelect(pattern) },
                        modifier = Modifier.weight(1f),
                        shape = ChipShape,
                        color = if (isSelected) SapphireBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SapphireBlue) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                pattern.name.take(10),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            Text(
                                "${pattern.inhaleSec}-${pattern.holdSec}-${pattern.exhaleSec}-${pattern.restSec}",
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
            if (uiState.sessionState != SessionState.IDLE) {
                Spacer(Modifier.height(6.dp))
                Text(
                    uiState.selectedPattern.description,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BreathingCard(
    uiState: MindfulBreakUiState,
    viewModel: MindfulBreakViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Breathing circle with timer overlay
            Box(contentAlignment = Alignment.Center) {
                BreathingCircle(progress = uiState.breathingProgress, phase = uiState.currentInstruction)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                    if (uiState.sessionState == SessionState.RUNNING || uiState.sessionState == SessionState.PAUSED) {
                        Spacer(Modifier.height(4.dp))
                        val mins = uiState.elapsedSeconds / 60
                        val secs = uiState.elapsedSeconds % 60
                        Text(
                            "%02d:%02d".format(mins, secs),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Instruction
            Text(
                text = uiState.currentInstruction,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            // Cycle count
            if (uiState.sessionState == SessionState.RUNNING) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cycle ${uiState.cycleCount + 1}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (uiState.sessionState) {
                    SessionState.IDLE -> {
                        Button(
                            onClick = viewModel::startSession,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(48.dp).padding(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Start Session", fontWeight = FontWeight.Bold)
                        }
                    }
                    SessionState.RUNNING -> {
                        Button(
                            onClick = viewModel::pauseSession,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Pause, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pause", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = viewModel::stopSession,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop", fontWeight = FontWeight.Bold)
                        }
                    }
                    SessionState.PAUSED -> {
                        Button(
                            onClick = viewModel::resumeSession,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = viewModel::stopSession,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stop", fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }

            // Guide text
            if (uiState.sessionState == SessionState.RUNNING) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = getBreathingGuide(uiState.currentInstruction),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BreathingCircle(progress: Float, phase: String) {
    val size = 260.dp

    val phaseColors = when (phase) {
        "Breathe In" -> listOf(Color(0xFFa8edea), Color(0xFFfed6e3))
        "Hold" -> listOf(Color(0xFFfbc2eb), Color(0xFFa6c1ee))
        "Breathe Out" -> listOf(Color(0xFFd4fc79), Color(0xFF96e6a1))
        "Rest" -> listOf(Color(0xFFa1c4fd), Color(0xFFc2e9fb))
        else -> listOf(Color(0xFFa8edea), Color(0xFFfed6e3))
    }

    Canvas(modifier = Modifier.size(size)) {
        val cx = size.toPx() / 2
        val cy = size.toPx() / 2
        val maxRadius = size.toPx() / 2 - 8.dp.toPx()
        val currentRadius = maxRadius * (0.3f + progress * 0.7f)

        // Outer glow ring
        val glowColor = phaseColors[0].copy(alpha = (0.15f + progress * 0.2f))
        drawCircle(color = glowColor, radius = currentRadius + 20.dp.toPx())
        drawCircle(color = glowColor.copy(alpha = 0.1f), radius = currentRadius + 40.dp.toPx())

        // Main gradient circle
        val gradient = Brush.radialGradient(
            colors = listOf(phaseColors[0].copy(alpha = 0.9f), phaseColors[1].copy(alpha = 0.5f), Color.Transparent),
            center = Offset(cx, cy),
            radius = currentRadius
        )
        drawCircle(brush = gradient, radius = currentRadius * 1.15f)

        // Stroked ring
        drawCircle(
            color = phaseColors[0].copy(alpha = 0.7f),
            style = Stroke(width = 4.dp.toPx()),
            radius = currentRadius
        )
        drawCircle(
            color = phaseColors[0].copy(alpha = 0.2f),
            radius = currentRadius * 0.6f
        )
    }
}

@Composable
private fun StatsRow(uiState: MindfulBreakUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill("Today", "${uiState.sessionsToday}", Icons.Default.Today, EmeraldGreen)
            StatPill("Total Min", "${uiState.totalMinutes}", Icons.Default.Timer, GoldenAmber)
            StatPill("Streak", "${uiState.currentStreak}d", Icons.Default.Favorite, CoralRed)
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
    }
}

@Composable
private fun BenefitsCard() {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = Color.White.copy(alpha = 0.08f),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text("Benefits of Breathing", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                val benefits = listOf(
                    "Reduces stress and anxiety",
                    "Improves focus and concentration",
                    "Enhances emotional well-being",
                    "Promotes better sleep",
                    "Lowers blood pressure",
                    "Boosts immune system",
                    "Increases energy levels"
                )
                benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                        )
                        Text(benefit, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryDialog(
    uiState: MindfulBreakUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Session Complete", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        "${uiState.sessionMinutes} min ${uiState.elapsedSeconds % 60} sec",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem("Pattern", uiState.selectedPattern.name, SapphireBlue)
                    SummaryItem("Cycles", "${uiState.cycleCount}", VioletPurple)
                    SummaryItem("Today", "${uiState.sessionsToday}", EmeraldGreen)
                }

                if (uiState.currentStreak > 0) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = PillShape,
                        color = GoldenAmber.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = GoldenAmber, modifier = Modifier.size(16.dp))
                            Text("${uiState.currentStreak} day streak!", fontWeight = FontWeight.Bold, color = GoldenAmber, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = ChipShape,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

private fun getBreathingGuide(instruction: String): String = when (instruction) {
    "Breathe In" -> "Fill your lungs completely\nFeel your chest expand and diaphragm lower"
    "Hold" -> "Hold the breath gently\nMaintain relaxation in your shoulders"
    "Breathe Out" -> "Release slowly and completely\nLet go of all tension and stress"
    "Rest" -> "Notice the stillness within\nPrepare for the next cycle"
    else -> "Follow the breathing pattern\nStay present and aware"
}
