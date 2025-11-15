package com.rudra.smartworktracker.ui.screens.all_funsion

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.smartworktracker.ui.navigation.NavigationItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllFunsionScreen(navController: NavController) {
    val features = remember {
        listOf(
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
            NavigationItem.Settings,
            NavigationItem.Calculation,
            NavigationItem.Backup
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "App Features",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(features, key = { _, feature -> feature.route }) { index, feature ->
                    val rowIndex = index / 2
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it * (rowIndex + 1) },
                            animationSpec = tween(
                                durationMillis = 600,
                                delayMillis = rowIndex * 100,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 600,
                                delayMillis = rowIndex * 100
                            )
                        )
                    ) {
                        FeatureCard(
                            feature = feature,
                            onClick = { navController.navigate(feature.route) },
                      //      modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(feature: NavigationItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Enhanced animations
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "card_scale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isPressed) 1.1f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "icon_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = tween(150),
        label = "elevation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "alpha"
    )

    // Color animations
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = tween(200),
        label = "container_color"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(cardScale)
            .alpha(alpha)
            .shadow(
                elevation = if (isPressed) 8.dp else 16.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            containerColor,
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .drawWithCache {
                    // Subtle shimmer effect
                    val shimmerBrush = ShaderBrush(
                        android.graphics.LinearGradient(
                            0f, 0f, size.width, size.height,
                            intArrayOf(
                                Color.Transparent.copy(alpha = 0.1f).value.toInt(),
                                Color.White.copy(alpha = 0.2f).value.toInt(),
                                Color.Transparent.copy(alpha = 0.1f).value.toInt()
                            ),
                            null,
                            android.graphics.Shader.TileMode.CLAMP
                        )
                    )
                    onDrawWithContent {
                        drawContent()
                        if (!isPressed) {
                            drawRect(shimmerBrush, alpha = 0.3f)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Background decorative element
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(20.dp)
            ) {
                // Icon with gradient and animation
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.title,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.9f)
                )

                // Subtlest description if available
//                feature.description?.let { description ->
//                    Text(
//                        text = description,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        textAlign = TextAlign.Center,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier
//                            .alpha(0.7f)
//                            .padding(top = 4.dp)
//                    )
//                }
            }

            // Ripple-like effect on press
            if (isPressed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(24.dp)
                        )
                )
            }
        }
    }
}

// Extension function for better animation control
private fun Modifier.advancedShadow(
    color: Color = Color.Black,
    borderRadius: Int = 0,
    blurRadius: Int = 0,
    offsetY: Int = 0,
    offsetX: Int = 0
) = this.drawWithCache {
    onDrawWithContent {
        drawContent()
    }
}
