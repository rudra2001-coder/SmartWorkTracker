package com.rudra.smartworktracker.ui.screens.appearance

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.smartworktracker.ui.navigation.NavigationItem

@Composable
fun AppearanceScreen(navController: NavController) {
    val features = listOf(
        NavigationItem.UserProfile,
        NavigationItem.AddEntry,
        NavigationItem.Reports,
        NavigationItem.Journal,
        NavigationItem.WorkTimer,
        NavigationItem.Focus,
        NavigationItem.MindfulBreak,
        NavigationItem.Habit,
        NavigationItem.Expense,
        NavigationItem.Income,
        NavigationItem.Health,
        NavigationItem.Achievements,
        NavigationItem.Wisdom,
        NavigationItem.Calendar,
        NavigationItem.Analytics,
        NavigationItem.MonthlyReport,
        NavigationItem.Calculation,
        NavigationItem.Backup
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(features) { feature ->
            FeatureCard(feature = feature, onClick = { navController.navigate(feature.route) })
        }
    }
}

@Composable
fun FeatureCard(feature: NavigationItem, onClick: () -> Unit) {

    val haptic = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }

    // Press animation
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = ""
    )

    // Icon pulse animation
    val iconScale by animateFloatAsState(
        targetValue = if (pressed) 1.15f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = ""
    )

    // Neumorphic + glass card
    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 12f
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                pressed = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
                pressed = false
            }
    ) {
        // Glassmorphism background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .blur(10.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}
