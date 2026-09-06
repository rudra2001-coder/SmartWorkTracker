package com.rudra.smartworktracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = 800,
    prefix: String = "",
    suffix: String = "",
    color: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = 24.sp
) {
    var animatedValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        animatedValue = 0
        val steps = minOf(targetValue, 30)
        val delayPerStep = durationMillis / steps.coerceAtLeast(1)
        for (i in 0..targetValue) {
            animatedValue = i
            delay(delayPerStep.toLong())
        }
    }

    Text(
        text = "$prefix$animatedValue$suffix",
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun AnimatedDoubleCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    durationMillis: Int = 800,
    prefix: String = "",
    suffix: String = "",
    color: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = 24.sp,
    decimals: Int = 0
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue.toFloat(),
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        )
    }

    val format = if (decimals > 0) "%.${decimals}f" else "%.0f"

    Text(
        text = "$prefix${format.format(animatedValue.value)}$suffix",
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize),
        fontWeight = FontWeight.Bold,
        color = color
    )
}
