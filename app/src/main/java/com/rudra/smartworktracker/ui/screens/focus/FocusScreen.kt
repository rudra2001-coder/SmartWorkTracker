package com.rudra.smartworktracker.ui.screens.focus

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.FocusType

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

const val DEEP_WORK_DURATION = 90 * 60L
const val POMODORO_DURATION = 25 * 60L
const val SHORT_BREAK_DURATION = 5 * 60L
const val LONG_BREAK_DURATION = 15 * 60L

@Composable
fun FocusScreen(viewModel: FocusViewModel = viewModel()) {
    val timerState by viewModel.timerState.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = timerState) {
            is TimerState.Idle -> {
                FocusSelection { type, duration ->
                    viewModel.startFocusSession(type, duration)
                }
            }
            is TimerState.Running -> {
                FocusTimer(
                    state = state,
                    isPaused = isPaused,
                    onPauseResume = { viewModel.pauseResumeTimer() },
                    onStop = { viewModel.stopFocusSession() },
                    onInterruption = { viewModel.recordInterruption() }
                )
            }
            is TimerState.Completed -> {
                FocusCompletion(
                    type = state.type,
                    score = state.score,
                    onRestart = { viewModel.startFocusSession(state.type, if(state.type == FocusType.DEEP_WORK) DEEP_WORK_DURATION else POMODORO_DURATION) },
                    onNewSession = { viewModel.stopFocusSession() }
                )
            }
        }

        AnimatedVisibility(
            visible = timerState is TimerState.Idle,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                SessionStatsButton()
            }
        }
    }
}

@Composable
fun FocusSelection(onStart: (FocusType, Long) -> Unit) {
    var selectedDuration by remember { mutableStateOf<Long?>(null) }
    val customDurationOptions = listOf(15, 30, 45, 60, 90, 120)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(52.dp).background(
                    brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                    shape = RoundedCornerShape(14.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Focus Sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Choose your focus mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        FocusSessionCard(
            title = "Deep Work",
            subtitle = "90 minutes • Intense focus",
            icon = Icons.Default.Work,
            duration = DEEP_WORK_DURATION,
            gradientColors = listOf(SapphireBlue, VioletPurple),
            onClick = { onStart(FocusType.DEEP_WORK, DEEP_WORK_DURATION) }
        )

        FocusSessionCard(
            title = "Pomodoro",
            subtitle = "25 min work • 5 min break",
            icon = Icons.Default.Timer,
            duration = POMODORO_DURATION,
            gradientColors = listOf(EmeraldGreen, SapphireBlue),
            onClick = { onStart(FocusType.POMODORO, POMODORO_DURATION) }
        )

        FocusSessionCard(
            title = "Short Break",
            subtitle = "5 minutes • Quick refresh",
            icon = Icons.Default.Coffee,
            duration = SHORT_BREAK_DURATION,
            gradientColors = listOf(GoldenAmber, CoralRed),
            onClick = { onStart(FocusType.SHORT_BREAK, SHORT_BREAK_DURATION) }
        )

        FocusSessionCard(
            title = "Long Break",
            subtitle = "15 minutes • Full recharge",
            icon = Icons.Default.Spa,
            duration = LONG_BREAK_DURATION,
            gradientColors = listOf(EmeraldGreen, GoldenAmber),
            onClick = { onStart(FocusType.LONG_BREAK, LONG_BREAK_DURATION) }
        )

        Text(
            text = "Custom Duration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(customDurationOptions) { minutes ->
                CustomDurationChip(
                    minutes = minutes,
                    isSelected = selectedDuration == minutes * 60L,
                    onClick = {
                        selectedDuration = minutes * 60L
                        onStart(FocusType.CUSTOM, minutes * 60L)
                    }
                )
            }
        }
    }
}

@Composable
fun FocusSessionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    duration: Long,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false).clickable { onClick() },
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = "${duration / 60} min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = gradientColors[0]
            )
        }
    }
}

@Composable
fun CustomDurationChip(minutes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(ChipShape)
            .clickable { onClick() },
        color = if (isSelected) VioletPurple else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 0.dp else 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = "$minutes min",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FocusTimer(
    state: TimerState.Running,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onInterruption: () -> Unit
) {
    var elapsedSeconds by remember { mutableStateOf(state.elapsed) }
    val progress = (elapsedSeconds.toFloat() / state.duration.toFloat())
    val timeRemaining = state.duration - elapsedSeconds

    LaunchedEffect(state.elapsed) {
        elapsedSeconds = state.elapsed
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(
                    brush = Brush.linearGradient(listOf(VioletPurple, SapphireBlue)),
                    shape = RoundedCornerShape(10.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.type == FocusType.DEEP_WORK) Icons.Default.Work else Icons.Default.Timer,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = state.type.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedCircularProgress(progress = progress, time = formatTime(timeRemaining))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProgressStat("Time Spent", formatTime(elapsedSeconds))
            ProgressStat("Time Left", formatTime(timeRemaining))
            ProgressStat("Progress", "${(progress * 100).toInt()}%")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = onInterruption,
                containerColor = CoralRed.copy(alpha = 0.15f),
                contentColor = CoralRed,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Warning, "Record Interruption", modifier = Modifier.size(24.dp))
            }

            FloatingActionButton(
                onClick = onPauseResume,
                containerColor = if (isPaused) GoldenAmber else EmeraldGreen,
                contentColor = Color.White,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = onStop,
                containerColor = CoralRed,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Stop, "Stop", modifier = Modifier.size(24.dp))
            }
        }

        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = "PAUSED",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoldenAmber,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        if (!isPaused && timeRemaining < 300) {
            Text(
                text = getMotivationalQuote(progress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}

@Composable
fun ProgressStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun FocusCompletion(type: FocusType, score: Int, onRestart: () -> Unit, onNewSession: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Celebration, "Completed", tint = EmeraldGreen, modifier = Modifier.size(80.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text("Session Complete!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        Spacer(modifier = Modifier.height(8.dp))

        Text("${type.displayName} session finished successfully", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
            shape = CardShape,
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Focus Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = score / 100f,
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        color = when { score >= 80 -> EmeraldGreen; score >= 60 -> GoldenAmber; else -> CoralRed }
                    )
                    Text("$score", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }

                Text(getScoreFeedback(score), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onNewSession, modifier = Modifier.weight(1f)) {
                Text("New Session")
            }
            Button(onClick = onRestart, modifier = Modifier.weight(1f)) {
                Text("Restart Session")
            }
        }
    }
}

@Composable
fun SessionStatsButton() {
    var showStats by remember { mutableStateOf(false) }

    Box {
        FloatingActionButton(
            onClick = { showStats = !showStats },
            containerColor = VioletPurple.copy(alpha = 0.15f),
            contentColor = VioletPurple
        ) {
            Icon(Icons.Default.BarChart, "Session Statistics")
        }

        DropdownMenu(
            expanded = showStats,
            onDismissRequest = { showStats = false }
        ) {
            DropdownMenuItem(text = { Text("Today: 3 sessions") }, onClick = { showStats = false })
            DropdownMenuItem(text = { Text("Weekly Avg: 85%") }, onClick = { showStats = false })
            DropdownMenuItem(text = { Text("Total Focus: 12h 30m") }, onClick = { showStats = false })
        }
    }
}

@Composable
fun AnimatedCircularProgress(progress: Float, time: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    val sweepAngle = animatedProgress * 360f
    val primaryColor = VioletPurple
    val secondaryColor = SapphireBlue

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )

        drawArc(
            brush = Brush.sweepGradient(colors = listOf(secondaryColor, primaryColor, secondaryColor)),
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
    }

    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(time, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("remaining", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

private fun getMotivationalQuote(progress: Float): String = when {
    progress < 0.25 -> "Great start! Keep up the momentum!"
    progress < 0.5 -> "You're doing amazing! Stay focused!"
    progress < 0.75 -> "Halfway there! Push through!"
    else -> "Almost done! Finish strong!"
}

private fun getScoreFeedback(score: Int): String = when {
    score >= 90 -> "Excellent focus! You're on fire!"
    score >= 80 -> "Great work! Very consistent focus!"
    score >= 70 -> "Good session! Room for improvement."
    score >= 60 -> "Decent effort. Try minimizing distractions."
    else -> "Keep practicing! You'll improve!"
}
