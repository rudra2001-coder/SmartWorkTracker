package com.rudra.smartworktracker.ui.screens.all_funsion

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rudra.smartworktracker.ui.navigation.NavigationItem

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50)
private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

data class FeatureSection(val title: String, val items: List<NavigationItem>)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllFunsionScreen(navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: AllFunsionViewModel = viewModel(factory = AllFunsionViewModelFactory(context.applicationContext as Application))
    val recentFeatures by viewModel.recentFeatures.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val quickAccessFeatures = remember {
        listOf(
            NavigationItem.Accounts,
            NavigationItem.Transfer,
            NavigationItem.AddEntry,
            NavigationItem.WorkTimer,
            NavigationItem.Focus,
            NavigationItem.Calendar,
            NavigationItem.Analytics,
            NavigationItem.Team
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
                    NavigationItem.Wisdom,
                    NavigationItem.Focus,
                    NavigationItem.WorkTimer,
                    NavigationItem.Overtime,
                    NavigationItem.Scheduler,
                    NavigationItem.FutureImpact,
                    NavigationItem.RealityTracker
                )
            ),
            FeatureSection(
                "Financials", listOf(
                    NavigationItem.Accounts,
                    NavigationItem.Transfer,
                    NavigationItem.Income,
                    NavigationItem.Expense,
                    NavigationItem.Savings,
                    NavigationItem.Loans,
                    NavigationItem.EMI,
                    NavigationItem.CreditCard,
                    NavigationItem.FinancialStatement,
                    NavigationItem.Reports,
                    NavigationItem.MonthlyReport,
                    NavigationItem.Calculation,
                    NavigationItem.AddEntry,
                    NavigationItem.Recurring,
                    NavigationItem.SpendAdvisor
                )
            ),
            FeatureSection(
                "General", listOf(
                    NavigationItem.Backup,
                    NavigationItem.BulkImport,
                    NavigationItem.Settings,
                    NavigationItem.Team,
                    NavigationItem.Notifications,
                    NavigationItem.UserProfile
                )
            ),
        )
    }
    val allFeatures = remember { (quickAccessFeatures + featureSections.flatMap { it.items }).distinctBy { it.route } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("App Features", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("All tools in one place", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SearchBar(
                    value = searchText,
                    onValueChange = { searchText = it }
                )
            }

            if (searchText.isBlank()) {
                if (recentFeatures.isNotEmpty()) {
                    item {
                        SectionHeader("Recently Used")
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(recentFeatures) { feature ->
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) { visible = true }
                                FeatureCard(
                                    feature = feature,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onFeatureClicked(feature)
                                        navController.navigate(feature.route)
                                    },
                                    isVisible = visible,
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                        }
                    }
                }

                featureSections.forEach { section ->
                    item {
                        SectionHeader(section.title)
                    }
                    section.items.chunked(2).forEach { rowItems ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { feature ->
                                    var visible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) { visible = true }
                                    FeatureCard(
                                        feature = feature,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.onFeatureClicked(feature)
                                            navController.navigate(feature.route)
                                        },
                                        isVisible = visible,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                val searchResults = allFeatures.filter {
                    it.title.contains(searchText, ignoreCase = true) ||
                        it.description?.contains(searchText, ignoreCase = true) == true
                }
                if (searchResults.isNotEmpty()) {
                    item {
                        SectionHeader("Search Results")
                    }
                    searchResults.chunked(2).forEach { rowItems ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { feature ->
                                    var visible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) { visible = true }
                                    FeatureCard(
                                        feature = feature,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.onFeatureClicked(feature)
                                            navController.navigate(feature.route)
                                        },
                                        isVisible = visible,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
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
        placeholder = { Text("Search features...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = MaterialTheme.colorScheme.primary) },
        shape = ChipShape,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(SapphireBlue, VioletPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getFeatureColor(feature: NavigationItem): Color {
    return when (feature.route) {
        NavigationItem.Income.route, NavigationItem.Savings.route, NavigationItem.AddEntry.route, NavigationItem.Health.route -> Color(0xFF43A047)
        NavigationItem.Expense.route, NavigationItem.Loans.route, NavigationItem.EMI.route, NavigationItem.CreditCard.route -> Color(0xFFE53935)
        NavigationItem.WorkTimer.route, NavigationItem.Focus.route, NavigationItem.Analytics.route, NavigationItem.Calendar.route, NavigationItem.Reports.route, NavigationItem.MonthlyReport.route, NavigationItem.FinancialStatement.route, NavigationItem.Transfer.route -> Color(0xFF1E88E5)
        NavigationItem.Habit.route, NavigationItem.Journal.route, NavigationItem.MindfulBreak.route, NavigationItem.Wisdom.route, NavigationItem.Achievements.route -> Color(0xFF8E24AA)
        NavigationItem.Settings.route, NavigationItem.Backup.route, NavigationItem.UserProfile.route, NavigationItem.Team.route, NavigationItem.Overtime.route, NavigationItem.Scheduler.route -> Color(0xFF546E7A)
        else -> Color(0xFF3949AB)
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
    val featureColor = getFeatureColor(feature)

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing), label = "cardScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500), label = "alpha"
    )
    val entryScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(500), label = "entryScale"
    )

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) featureColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .height(150.dp)
            .shadow(6.dp, CardShape, clip = false)
            .scale(cardScale * entryScale)
            .alpha(alpha)
            .border(
                width = 1.dp,
                color = if (isPressed) featureColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = CardShape
            )
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                featureColor.copy(alpha = 0.7f),
                                featureColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (feature.description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.description!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
