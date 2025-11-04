package com.rudra.smartworktracker.ui.screens.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.ui.components.AnimatedFAB
import kotlinx.coroutines.delay

@Composable
fun WorkTimerScreen(viewModel: WorkTimerViewModel = viewModel()) {
    val timerState by viewModel.timerState.collectAsState()
    var seconds by remember { mutableStateOf(0) }
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = timerState.mode) {
        if (timerState.mode == TimerMode.RUNNING) {
            while (timerState.mode == TimerMode.RUNNING) {
                delay(1000)
                seconds++
            }
        } else {
            seconds = 0
        }
    }

    Scaffold(
        floatingActionButton = {
            if (timerState.mode == TimerMode.RUNNING || timerState.mode == TimerMode.ON_BREAK) {
                AnimatedFAB(
                    expanded = fabExpanded,
                    onToggle = { fabExpanded = !fabExpanded },
                    onAddBreak = { viewModel.startBreak() },
                    onAddLunch = { /* TODO */ }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(it),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val isRunning = timerState.mode == TimerMode.RUNNING
                val progress = if (isRunning) (seconds % 60) / 60f else 0f
                val timeToShow = if (timerState.mode == TimerMode.ON_BREAK) "On Break" else formatTime(seconds)

                AnimatedCircularProgress(
                    progress = progress,
                    time = timeToShow
                )
                Spacer(modifier = Modifier.height(32.dp))

                if(timerState.mode != TimerMode.ON_BREAK) {
                    FloatingActionButton(
                        onClick = {
                            if (isRunning) {
                                viewModel.stopWorkSession()
                            } else {
                                viewModel.startWorkSession()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                } else {
                     Button(onClick = { viewModel.endBreak() }) {
                        Text("End Break")
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCircularProgress(progress: Float, time: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000)
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 15f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 15f, cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }
        Text(
            text = time,
            fontSize = 50.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
