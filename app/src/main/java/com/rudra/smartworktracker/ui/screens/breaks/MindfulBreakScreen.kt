package com.rudra.smartworktracker.ui.screens.breaks

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)

@Composable
fun MindfulBreakScreen() {
    val breathingState = remember { Animatable(0.3f) }
    val pulseState = remember { Animatable(0f) }
    var instruction by remember { mutableStateOf("Breathe In") }
    var cycleCount by remember { mutableStateOf(1) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2), Color(0xFFf093fb))
    )

    LaunchedEffect(Unit) {
        while (true) {
            instruction = "Breathe In"
            breathingState.animateTo(1f, animationSpec = tween(4000, easing = LinearEasing))
            instruction = "Hold"
            delay(4000)
            instruction = "Breathe Out"
            breathingState.animateTo(0.3f, animationSpec = tween(6000, easing = LinearEasing))
            instruction = "Rest"
            delay(1000)
            cycleCount++
        }
    }

    LaunchedEffect(Unit) {
        pulseState.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(backgroundGradient),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PulsingCircle(progress = pulseState.value)
                        BreathingCircle(progress = breathingState.value)
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Mindfulness",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = instruction,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Cycle $cycleCount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = getBreathingGuide(instruction),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text("Benefits", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    val benefits = listOf(
                        "Reduces stress and anxiety",
                        "Improves focus and concentration",
                        "Enhances emotional well-being",
                        "Promotes better sleep"
                    )
                    benefits.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                            Box(Modifier.size(5.dp).background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50)))
                            Text(benefit, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Follow the breathing pattern to relax and refocus",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
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
        val gradient = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = 0.8f), secondaryColor.copy(alpha = 0.4f), Color.Transparent),
            center = center, radius = radius
        )
        drawCircle(brush = gradient, radius = radius * 1.1f)
        drawCircle(color = primaryColor, style = Stroke(width = 6.dp.toPx()), radius = radius)
        drawCircle(color = primaryColor.copy(alpha = 0.3f), radius = radius * 0.7f)
    }
}

@Composable
fun PulsingCircle(progress: Float) {
    val size = 350.dp
    val pulseColor = Color(0xFFa8edea).copy(alpha = progress * 0.3f)

    Canvas(modifier = Modifier.size(size)) {
        val radius = (this.size.minDimension / 2f) * (0.8f + progress * 0.2f)
        drawCircle(color = pulseColor, style = Stroke(width = 3.dp.toPx()), radius = radius)
    }
}

private fun getBreathingGuide(instruction: String): String = when (instruction) {
    "Breathe In" -> "Fill your lungs completely\nFeel your chest expand"
    "Hold" -> "Hold the breath gently\nMaintain relaxation"
    "Breathe Out" -> "Release slowly and completely\nLet go of tension"
    "Rest" -> "Notice the stillness\nPrepare for next cycle"
    else -> "Follow the breathing pattern"
}
