package com.rudra.smartworktracker.ui.screens.health

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StepsCard(
    currentSteps: Int,
    targetSteps: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentSteps.toFloat() / targetSteps).coerceIn(0f, 1f)
    val stepsColor = when {
        progress >= 1f -> Color(0xFF4CAF50)
        progress >= 0.5f -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                Surface(color = stepsColor.copy(alpha = 0.1f), shape = CircleShape) {
                    Text("${(progress * 100).toInt()}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = stepsColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("$currentSteps", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2196F3))
            Text("/ $targetSteps steps", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2196F3).copy(alpha = 0.7f))
            Spacer(modifier = Modifier.weight(1f))
            Text("Tap to add steps", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3).copy(alpha = 0.5f), fontStyle = FontStyle.Italic)
        }
    }
}
