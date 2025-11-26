package com.rudra.smartworktracker.ui.screens.breaks

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MindfulBreakScreen() {
    val breathingState = remember { Animatable(0.3f) }
    val pulseState = remember { Animatable(0f) }
    var instruction by remember { mutableStateOf("Breathe In") }
    var cycleCount by remember { mutableStateOf(1) }

    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667eea),
            Color(0xFF764ba2),
            Color(0xFFf093fb)
        )
    )

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
            breathingState.animateTo(0.3f, animationSpec = tween(6000, easing = LinearEasing))

            // Short pause
            instruction = "Rest"
            delay(1000)

            cycleCount++
        }
    }

    // Pulse animation for additional visual interest
    LaunchedEffect(Unit) {
        pulseState.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Animated background elements
        FloatingParticles(count = 8, animationProgress = pulseState.value)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Breathing visualization with multiple elements
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Outer pulsing circle
                PulsingCircle(progress = pulseState.value)

                // Main breathing circle
                BreathingCircle(progress = breathingState.value)

                // Central icon
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Mindfulness",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Instruction with emphasis
            Text(
                text = instruction,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cycle counter
            Text(
                text = "Cycle $cycleCount",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Breathing guide text
            Text(
                text = getBreathingGuide(instruction),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun BreathingCircle(progress: Float) {
    val size = 280.dp
    val primaryColor = Color(0xFFffecd2)
    val secondaryColor = Color(0xFFfcb69f)

    Canvas(modifier = Modifier.size(size)) {
        val radius = (this.size.minDimension / 2f) * progress

        // Gradient for the circle
        val gradient = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.8f),
                secondaryColor.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = radius
        )

        // Glow effect
        drawCircle(
            brush = gradient,
            radius = radius * 1.1f
        )

        // Main circle
        drawCircle(
            color = primaryColor,
            style = Stroke(width = 6.dp.toPx()),
            radius = radius
        )

        // Inner glow
        drawCircle(
            color = primaryColor.copy(alpha = 0.3f),
            radius = radius * 0.7f
        )
    }
}

@Composable
fun PulsingCircle(progress: Float) {
    val size = 350.dp
    val pulseColor = Color(0xFFa8edea).copy(alpha = progress * 0.3f)

    Canvas(modifier = Modifier.size(size)) {
        val radius = (this.size.minDimension / 2f) * (0.8f + progress * 0.2f)

        drawCircle(
            color = pulseColor,
            style = Stroke(width = 3.dp.toPx()),
            radius = radius
        )
    }
}

@Composable
fun FloatingParticles(count: Int, animationProgress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until count) {
            val angle = (i * 360f / count) + (animationProgress * 360f)
            val distance = 150.dp.toPx() * (0.7f + animationProgress * 0.3f)

            val x = center.x + distance * cos(Math.toRadians(angle.toDouble())).toFloat()
            val y = center.y + distance * sin(Math.toRadians(angle.toDouble())).toFloat()

            val particleSize = 4.dp.toPx() * (1f + animationProgress * 0.5f)
            val alpha = 0.3f + animationProgress * 0.2f

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(x, y)
            )
        }
    }
}

private fun getBreathingGuide(instruction: String): String {
    return when (instruction) {
        "Breathe In" -> "Fill your lungs completely\nFeel your chest expand"
        "Hold" -> "Hold the breath gently\nMaintain relaxation"
        "Breathe Out" -> "Release slowly and completely\nLet go of tension"
        "Rest" -> "Notice the stillness\nPrepare for next cycle"
        else -> "Follow the breathing pattern"
    }
}

@Preview(showBackground = true)
@Composable
fun MindfulBreakScreenPreview() {
    MaterialTheme {
        MindfulBreakScreen()
    }
}