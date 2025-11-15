package com.rudra.smartworktracker.ui.screens.all_funsion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.smartworktracker.ui.navigation.NavigationItem

data class FeatureSection(val title: String, val items: List<NavigationItem>)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllFunsionScreen(navController: NavController) {
    val haptic = LocalHapticFeedback.current
    var searchText by remember { mutableStateOf("") }

    val quickAccessFeatures = remember {
        listOf(
            NavigationItem.AddEntry,
            NavigationItem.WorkTimer,
            NavigationItem.Focus,
            NavigationItem.Calendar,
            NavigationItem.Analytics
        )
    }
    val featureSections = remember {
        listOf(
            FeatureSection(
                "Productivity & Wellness", listOf(
                    NavigationItem.Journal,
                    NavigationItem.Habit,
                    NavigationItem.Health,
                    NavigationItem.Achievements,
                    NavigationItem.MindfulBreak,
                    NavigationItem.Wisdom
                )
            ),
            FeatureSection(
                "Financials", listOf(
                    NavigationItem.Income,
                    NavigationItem.Expense,
                    NavigationItem.Reports,
                    NavigationItem.MonthlyReport,
                    NavigationItem.Calculation
                )
            ),
            FeatureSection(
                "General", listOf(
                    NavigationItem.UserProfile,
                    NavigationItem.Backup,
                    NavigationItem.Settings
                )
            )
        )
    }
    val allFeatures = remember { quickAccessFeatures + featureSections.flatMap { it.items } }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("App Features", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
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
                ),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            stickyHeader {
                Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                    SearchBar(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            if (searchText.isBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader("Quick Access", isSticky = false)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(quickAccessFeatures) { feature ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                visible = true
                            }
                            FeatureCard(
                                feature = feature,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(feature.route)
                                },
                                isVisible = visible,
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(160.dp) // Increased height
                            )
                        }
                    }
                }

                featureSections.forEach { section ->
                    stickyHeader {
                        SectionHeader(section.title)
                    }
                    items(section.items, key = { it.route }) { feature ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            visible = true
                        }
                        FeatureCard(
                            feature = feature,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(feature.route)
                            },
                            isVisible = visible
                        )
                    }
                }
            } else {
                val searchResults = allFeatures.filter {
                    it.title.contains(searchText, ignoreCase = true) ||
                            it.description?.contains(searchText, ignoreCase = true) == true
                }
                if(searchResults.isNotEmpty()){
                    stickyHeader {
                        SectionHeader("Search Results")
                    }
                    items(searchResults, key = { it.route }) { feature ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            visible = true
                        }
                        FeatureCard(
                            feature = feature,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate(feature.route)
                            },
                            isVisible = visible
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search features...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, isSticky: Boolean = true) {
    val background = if(isSticky) MaterialTheme.colorScheme.surface.copy(alpha = 0.8f) else Color.Transparent
    Box(modifier = modifier.background(background)){
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 8.dp, start = 4.dp)
                .alpha(0.9f)
        )
    }
}

@Composable
fun FeatureCard(
    feature: NavigationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isVisible: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing), label = ""
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 10.dp,
        animationSpec = tween(150), label = ""
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(200), label = ""
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600), label = ""
    )
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(600), label = ""
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .height(140.dp)
            .scale(cardScale * scale)
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = feature.description ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}