package com.rudra.smartworktracker.ui.screens.breaks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MindfulBreakScreen() {
    val breathingState = remember { Animatable(0.5f) }
    var instruction by remember { mutableStateOf("Breathe In") }

    // Breathing cycle logic
    LaunchedEffect(Unit) {
        while (true) {
            // Breathe In
            instruction = "Breathe In"
            breathingState.animateTo(1f, animationSpec = tween(4000, easing = LinearEasing))

            // Hold
            instruction = "Hold"
            delay(4000)

            // Breathe Out
            instruction = "Breathe Out"
            breathingState.animateTo(0.5f, animationSpec = tween(6000, easing = LinearEasing))

            // Short pause
            delay(1000)
        }
    }

    // UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BreathingCircle(progress = breathingState.value)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = instruction,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun BreathingCircle(progress: Float) {
    val size = 250.dp
    val primaryColor = MaterialTheme.colorScheme.primary
    val transparentPrimary = primaryColor.copy(alpha = 0.5f)

    Canvas(modifier = Modifier.size(size)) {
        val radius = (this.size.minDimension / 2f) * progress
        val gradient = Brush.radialGradient(
            colors = listOf(transparentPrimary, Color.Transparent),
            center = center,
            radius = radius
        )

        drawCircle(brush = gradient)
        drawCircle(
            color = primaryColor,
            style = Stroke(width = 4.dp.toPx()),
            radius = radius
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MindfulBreakScreenPreview() {
    MindfulBreakScreen()
}
