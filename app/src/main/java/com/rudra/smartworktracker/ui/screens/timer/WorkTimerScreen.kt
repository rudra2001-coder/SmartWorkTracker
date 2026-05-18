package com.rudra.smartworktracker.ui.screens.timer

import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.SessionType
import com.rudra.smartworktracker.ui.components.AnimatedFAB
import kotlinx.coroutines.delay

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkTimerScreen(viewModel: WorkTimerViewModel = viewModel()) {
    val timerState by viewModel.timerState.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (timerState.mode == TimerMode.RUNNING || timerState.mode == TimerMode.ON_BREAK) {
                AnimatedFAB(
                    expanded = timerState.mode == TimerMode.RUNNING,
                    onToggle = {},
                    onAddBreak = { viewModel.startBreak() },
                    onAddLunch = { viewModel.startLunch() }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                TimerCard(timerState, viewModel)
            }

            item {
                StatsCard(timerState.todayStats)
            }

            if (timerState.sessionHistory.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                        Box(modifier = Modifier.size(28.dp).background(VioletPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, null, tint = VioletPurple, modifier = Modifier.size(16.dp))
                        }
                        Text("Recent Sessions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                items(timerState.sessionHistory) { session ->
                    SessionHistoryItem(session)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TimerCard(timerState: TimerState, viewModel: WorkTimerViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedProgressRing(
                    progress = if (timerState.totalSeconds > 0) timerState.workSeconds.toFloat() / timerState.totalSeconds else 0f,
                    color = SapphireBlue,
                    strokeWidth = 20f,
                    ringSize = 280.dp
                )

                AnimatedProgressRing(
                    progress = if (timerState.totalSeconds > 0) timerState.breakSeconds.toFloat() / timerState.totalSeconds else 0f,
                    color = EmeraldGreen,
                    strokeWidth = 20f,
                    ringSize = 240.dp,
                    startAngle = 90f
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.background(
                            when (timerState.mode) {
                                TimerMode.RUNNING -> EmeraldGreen.copy(alpha = 0.15f)
                                TimerMode.ON_BREAK -> GoldenAmber.copy(alpha = 0.15f)
                                TimerMode.ON_LUNCH -> GoldenAmber.copy(alpha = 0.15f)
                                TimerMode.PAUSED -> CoralRed.copy(alpha = 0.15f)
                                TimerMode.STOPPED -> VioletPurple.copy(alpha = 0.15f)
                            },
                            PillShape
                        ).padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when (timerState.mode) {
                                TimerMode.RUNNING -> "Working"
                                TimerMode.ON_BREAK -> "On Break"
                                TimerMode.ON_LUNCH -> "Lunch Time"
                                TimerMode.PAUSED -> "Paused"
                                TimerMode.STOPPED -> "Ready to Start"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (timerState.mode) {
                                TimerMode.RUNNING -> EmeraldGreen
                                TimerMode.ON_BREAK, TimerMode.ON_LUNCH -> GoldenAmber
                                TimerMode.PAUSED -> CoralRed
                                TimerMode.STOPPED -> VioletPurple
                            }
                        )
                    }

                    Text(
                        text = formatTime(timerState.totalSeconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatChip(icon = Icons.Default.Work, text = formatTime(timerState.workSeconds), color = SapphireBlue)
                        StatChip(icon = Icons.Default.Coffee, text = formatTime(timerState.breakSeconds), color = EmeraldGreen)
                    }
                }
            }

            when (timerState.mode) {
                TimerMode.STOPPED -> {
                    Button(
                        onClick = { viewModel.startWorkSession() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start")
                        Spacer(Modifier.width(8.dp))
                        Text("Start Work", fontWeight = FontWeight.Bold)
                    }
                }

                TimerMode.RUNNING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.pauseWorkSession() }, modifier = Modifier.weight(1f).height(56.dp), shape = ChipShape) {
                            Icon(Icons.Default.Pause, "Pause")
                            Spacer(Modifier.width(8.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = { viewModel.stopWorkSession() },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = ChipShape,
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Stop, "Stop")
                            Spacer(Modifier.width(8.dp))
                            Text("Stop")
                        }
                    }
                }

                TimerMode.PAUSED -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.resumeWorkSession() }, modifier = Modifier.weight(1f).height(56.dp), shape = ChipShape) {
                            Icon(Icons.Default.PlayArrow, "Resume")
                            Spacer(Modifier.width(8.dp))
                            Text("Resume")
                        }
                        OutlinedButton(onClick = { viewModel.stopWorkSession() }, modifier = Modifier.weight(1f).height(56.dp), shape = ChipShape) {
                            Icon(Icons.Default.Stop, "Stop")
                            Spacer(Modifier.width(8.dp))
                            Text("Stop")
                        }
                    }
                }

                TimerMode.ON_BREAK, TimerMode.ON_LUNCH -> {
                    Button(
                        onClick = { viewModel.endBreak() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = ChipShape,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Work, "End Break")
                        Spacer(Modifier.width(8.dp))
                        Text("End ${if (timerState.mode == TimerMode.ON_BREAK) "Break" else "Lunch"}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedProgressRing(progress: Float, color: Color, strokeWidth: Float, ringSize: androidx.compose.ui.unit.Dp, startAngle: Float = -90f) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing = FastOutSlowInEasing))

    Canvas(modifier = Modifier.size(ringSize)) {
        drawArc(color = color.copy(alpha = 0.2f), startAngle = startAngle, sweepAngle = 360f, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round), size = Size(size.width, size.height))
        drawArc(color = color, startAngle = startAngle, sweepAngle = animatedProgress * 360f, useCenter = false, style = Stroke(strokeWidth, cap = StrokeCap.Round), size = Size(size.width, size.height))
        if (animatedProgress > 0) {
            drawCircle(
                brush = Brush.radialGradient(colors = listOf(color.copy(alpha = 0.5f), Color.Transparent), radius = strokeWidth / 2),
                radius = strokeWidth / 2,
                center = Offset(
                    x = size.width / 2 + (size.width / 2 - strokeWidth / 2) *
                            kotlin.math.cos(Math.toRadians((startAngle + animatedProgress * 360).toDouble())).toFloat(),
                    y = size.height / 2 + (size.height / 2 - strokeWidth / 2) *
                            kotlin.math.sin(Math.toRadians((startAngle + animatedProgress * 360).toDouble())).toFloat()
                )
            )
        }
    }
}

@Composable
fun StatChip(icon: ImageVector, text: String, color: Color) {
    Box(
        modifier = Modifier.background(color.copy(alpha = 0.15f), PillShape).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

private val PillShape = RoundedCornerShape(50.dp)

@Composable
fun StatsCard(stats: TodayStats) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(28.dp).background(SapphireBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.BarChart, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                }
                Text("Today's Stats", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(value = formatTime(stats.totalWorkTime), label = "Work Time", icon = Icons.Default.Timer, color = SapphireBlue)
                StatItem(value = stats.totalBreaks.toString(), label = "Breaks", icon = Icons.Default.Coffee, color = GoldenAmber)
                StatItem(value = "%.1f%%".format(stats.productivityScore), label = "Productivity", icon = Icons.Default.TrendingUp, color = EmeraldGreen)
                StatItem(value = stats.sessionsCompleted.toString(), label = "Sessions", icon = Icons.Default.Work, color = VioletPurple)
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SessionHistoryItem(session: SessionItem) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(
                        when (session.type) {
                            SessionType.WORK -> SapphireBlue.copy(alpha = 0.12f)
                            SessionType.BREAK -> GoldenAmber.copy(alpha = 0.12f)
                            SessionType.LUNCH -> CoralRed.copy(alpha = 0.12f)
                        },
                        RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (session.type) {
                            SessionType.WORK -> Icons.Default.Work
                            SessionType.BREAK -> Icons.Default.Coffee
                            SessionType.LUNCH -> Icons.Default.Restaurant
                        }, null,
                        tint = when (session.type) {
                            SessionType.WORK -> SapphireBlue
                            SessionType.BREAK -> GoldenAmber
                            SessionType.LUNCH -> CoralRed
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(session.type.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Started: ${session.startTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(session.duration, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SapphireBlue)
                session.productivityScore?.let { score ->
                    Box(
                        modifier = Modifier.background(
                            when { score >= 80 -> EmeraldGreen.copy(alpha = 0.15f); score >= 60 -> GoldenAmber.copy(alpha = 0.15f); else -> CoralRed.copy(alpha = 0.15f)
                            }, RoundedCornerShape(6.dp)
                        ).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$score%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = when { score >= 80 -> EmeraldGreen; score >= 60 -> GoldenAmber; else -> CoralRed })
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, remainingSeconds) else "%02d:%02d".format(minutes, remainingSeconds)
}
