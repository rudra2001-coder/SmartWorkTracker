package com.rudra.smartworktracker.ui.screens.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EnhancedBalanceDonutCard(score: Int, modifier: Modifier = Modifier) {
    val animatedScore = remember { Animatable(0f) }
    val scoreColor = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(score) {
        animatedScore.animateTo(
            score / 100f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawCircle(
                        color = surfaceVariantColor,
                        radius = size.minDimension / 2,
                        style = Stroke(12.dp.toPx())
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = animatedScore.value * 360f,
                        useCenter = false,
                        style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                scoreColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 3
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${(animatedScore.value * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = scoreColor
                    )
                    Text(
                        "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Work-Life Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    when {
                        score > 80 -> "🎉 Excellent! You're maintaining a healthy balance."
                        score > 50 -> "👍 Good balance, but there's room for improvement."
                        else -> "⚠️ Warning: Work overload detected. Take time to recharge."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}
