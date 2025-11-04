package com.rudra.smartworktracker.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.rudra.smartworktracker.model.FocusType
import kotlinx.coroutines.delay

const val DEEP_WORK_DURATION = 90 * 60L
const val POMODORO_DURATION = 25 * 60L

@Composable
fun FocusScreen(viewModel: FocusViewModel = viewModel()) {
    val timerState by viewModel.timerState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = timerState) {
            is TimerState.Idle -> {
                FocusSelection { type, duration ->
                    viewModel.startFocusSession(type, duration)
                }
            }
            is TimerState.Running -> {
                FocusTimer(state = state) {
                    viewModel.stopFocusSession()
                }
            }
        }
    }
}

@Composable
fun FocusSelection(onStart: (FocusType, Long) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Start a Focus Session", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onStart(FocusType.DEEP_WORK, DEEP_WORK_DURATION) }, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Deep Work (90 min)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onStart(FocusType.POMODORO, POMODORO_DURATION) }, modifier = Modifier.fillMaxWidth(0.7f)) {
            Text("Pomodoro (25 min)")
        }
    }
}

@Composable
fun FocusTimer(state: TimerState.Running, onStop: () -> Unit) {
    var elapsedSeconds by remember { mutableStateOf(state.elapsed) }

    LaunchedEffect(Unit) {
        var current = state.duration - state.elapsed
        while (current > 0) {
            delay(1000)
            current--
            elapsedSeconds = state.duration - current
        }
        onStop() // Auto-stop when timer finishes
    }

    val progress = (elapsedSeconds.toFloat() / state.duration.toFloat())
    val timeRemaining = state.duration - elapsedSeconds

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(state.type.name.replace("_", " "), fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedCircularProgress(progress = progress, time = formatTime(timeRemaining))
        Spacer(modifier = Modifier.height(32.dp))
        FloatingActionButton(
            onClick = { onStop() },
            containerColor = MaterialTheme.colorScheme.error
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                tint = Color.White
            )
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

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
