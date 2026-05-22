package com.rudra.smartworktracker.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ─────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────

private val BgDeep = Color(0xFF0F0F13)
private val BgCard = Color(0xFF16161C)
private val BgChip = Color(0xFF1C1C24)
private val BorderSubtle = Color(0xFF2A2A38)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8B8BA0)
private val TextMuted = Color(0xFF4A4A60)

// ─────────────────────────────────────────────────────────────
// Data Models
// ─────────────────────────────────────────────────────────────

private data class FeatureChip(
    val emoji: String,
    val title: String,
    val description: String,
    val accentColor: Color
)

private data class OnboardingPage(
    val icon: ImageVector?,
    val categoryTag: String,
    val tagColor: Color,
    val title: String,
    val titleAccent: String,      // second line of title
    val subtitle: String?,        // only for last page (edition label)
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val orbColor1: Color,
    val orbColor2: Color,
    val features: List<FeatureChip>
)

private val OrbIndigo1 = Color(0xFF6366F1)
private val OrbIndigo2 = Color(0xFF8B5CF6)
private val OrbGreen1  = Color(0xFF10B981)
private val OrbGreen2  = Color(0xFF34D399)
private val OrbAmber1  = Color(0xFFF59E0B)
private val OrbAmber2  = Color(0xFFFBBF24)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Filled.Work,
        categoryTag = "PRODUCTIVITY",
        tagColor = OrbIndigo1,
        title = "Track your",
        titleAccent = "work life",
        subtitle = null,
        description = "Log, measure, and optimize every workday with ease.",
        primaryColor = OrbIndigo1,
        secondaryColor = OrbIndigo2,
        orbColor1 = OrbIndigo1,
        orbColor2 = OrbIndigo2,
        features = listOf(
            FeatureChip("📅", "Work logs & calendar",
                "Log Office, Home, Overtime & more with swipeable monthly view", OrbIndigo1),
            FeatureChip("⏱️", "Focus sessions",
                "Pomodoro timers, break tracking & a focus score per session", OrbIndigo2),
            FeatureChip("📊", "Reports & analytics",
                "Visual charts, period comparison & productivity trends", OrbIndigo1),
            FeatureChip("👥", "Team management",
                "Profiles, skills, ratings & duty swap notifications", OrbIndigo2)
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.AccountBalance,
        categoryTag = "FINANCE",
        tagColor = OrbGreen1,
        title = "Master your",
        titleAccent = "money",
        subtitle = null,
        description = "Complete financial toolkit from daily expenses to long-term planning.",
        primaryColor = OrbGreen1,
        secondaryColor = OrbGreen2,
        orbColor1 = OrbGreen1,
        orbColor2 = OrbGreen2,
        features = listOf(
            FeatureChip("💰", "Accounts & expenses",
                "Wallet, Bank, Mobile Banking with real-time balance charts", OrbGreen1),
            FeatureChip("🏦", "Loans & EMI",
                "Full loan lifecycle with installment schedules & reminders", OrbGreen2),
            FeatureChip("💳", "Credit cards",
                "Track charges, pay bills, enforce limits & transfer funds", OrbGreen1),
            FeatureChip("🔄", "Smart automation",
                "Recurring transfers, daily backup & AI spending insights", OrbGreen2)
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.Favorite,
        categoryTag = "WELLNESS",
        tagColor = OrbAmber1,
        title = "Grow every",
        titleAccent = "single day",
        subtitle = null,
        description = "Build better habits, track your health, and reflect on your journey.",
        primaryColor = OrbAmber1,
        secondaryColor = OrbAmber2,
        orbColor1 = OrbAmber1,
        orbColor2 = OrbAmber2,
        features = listOf(
            FeatureChip("✅", "Habit tracker",
                "Streaks with difficulty levels and visual progress tracking", OrbAmber1),
            FeatureChip("❤️", "Health metrics",
                "Sleep, weight, mood, macros & 25+ more daily metrics", OrbAmber2),
            FeatureChip("📝", "Daily journal",
                "Morning intentions, gratitude & evening reflections", OrbAmber1),
            FeatureChip("🏆", "Achievements",
                "Gamified milestones for streaks and focus session goals", OrbAmber2)
        )
    ),
    OnboardingPage(
        icon = Icons.Filled.RocketLaunch,
        categoryTag = "PROFESSIONAL EDITION",
        tagColor = OrbIndigo2,
        title = "Smart Work",
        titleAccent = "Tracker",
        subtitle = "Your all-in-one companion for work, finance, and personal growth — beautifully integrated.",
        description = "",
        primaryColor = OrbIndigo1,
        secondaryColor = OrbGreen1,
        orbColor1 = OrbIndigo1,
        orbColor2 = OrbGreen1,
        features = emptyList()
    )
)

// ─────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onOnboardingFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == pages.lastIndex
    val currentPageData = pages[currentPage]

    // Animate accent color transitions
    val animatedPrimary by animateColorAsState(
        targetValue = currentPageData.primaryColor,
        animationSpec = tween(500),
        label = "primaryColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Ambient orbs behind everything
        AmbientOrbs(
            color1 = currentPageData.orbColor1,
            color2 = currentPageData.orbColor2
        )

        // Subtle noise-like particle layer
        NoiseParticles(color = currentPageData.primaryColor)

        // Skip button (hidden on last page)
        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(tween(300)) + slideInVertically { -it },
            exit = fadeOut(tween(200)) + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 52.dp, end = 20.dp)
                .zIndex(10f)
        ) {
            SkipButton(onClick = {
                scope.launch { pagerState.animateScrollToPage(pages.lastIndex) }
            })
        }

        // Page content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { pageIndex ->
            PageContent(
                page = pages[pageIndex],
                isVisible = pageIndex == currentPage,
                isLastPage = pageIndex == pages.lastIndex
            )
        }

        // Bottom controls pinned at bottom
        BottomControls(
            pagerState = pagerState,
            isLastPage = isLastPage,
            accentColor = animatedPrimary,
            secondaryColor = currentPageData.secondaryColor,
            onNext = { scope.launch { pagerState.animateScrollToPage(currentPage + 1) } },
            onFinish = onOnboardingFinished,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Page Content
// ─────────────────────────────────────────────────────────────

@Composable
private fun PageContent(
    page: OnboardingPage,
    isVisible: Boolean,
    isLastPage: Boolean
) {
    val scrollState = rememberScrollState()

    val entrance = remember { Animatable(0f) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            entrance.snapTo(0f)
            entrance.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = tween(350),
        label = "pageAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha }
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(88.dp))

        // Hero icon
        HeroIconBlock(
            page = page,
            isLastPage = isLastPage,
            progress = entrance.value
        )

        Spacer(Modifier.height(32.dp))

        // Category tag + title + description
        TitleBlock(
            page = page,
            isLastPage = isLastPage,
            progress = entrance.value
        )

        if (!isLastPage) {
            Spacer(Modifier.height(28.dp))

            // Feature chips with staggered entrance
            page.features.forEachIndexed { i, chip ->
                StaggeredFeatureChip(
                    chip = chip,
                    visible = entrance.value >= 0.15f,
                    delayMs = 150 + i * 120
                )
                Spacer(Modifier.height(10.dp))
            }
        } else {
            Spacer(Modifier.height(20.dp))
            FinalPagePills()
        }

        Spacer(Modifier.height(180.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// Hero Icon Block
// ─────────────────────────────────────────────────────────────

@Composable
private fun HeroIconBlock(
    page: OnboardingPage,
    isLastPage: Boolean,
    progress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroAnim")

    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Reverse),
        label = "floatY"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val ringRotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring"
    )

    val scale = lerp(0.7f, 1f, FastOutSlowInEasing.transform(progress))

    Box(
        modifier = Modifier
            .size(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = floatY * progress
                alpha = progress
            },
        contentAlignment = Alignment.Center
    ) {
        // Rotating dashed ring
        Canvas(modifier = Modifier.size(148.dp)) {
            val ringColor = page.primaryColor.copy(alpha = 0.25f * progress)
            val dashCount = 20
            val radius = size.minDimension / 2f
            val cx = size.width / 2f
            val cy = size.height / 2f
            for (d in 0 until dashCount) {
                val angle = Math.toRadians((ringRotate + d * (360f / dashCount)).toDouble())
                val x = cx + radius * cos(angle).toFloat()
                val y = cy + radius * sin(angle).toFloat()
                drawCircle(color = ringColor, radius = 3f, center = Offset(x, y))
            }
        }

        // Glow halo
        Box(
            modifier = Modifier
                .size(130.dp)
                .graphicsLayer { alpha = glowPulse * progress }
                .clip(RoundedCornerShape(38.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            page.primaryColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main icon tile
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(page.primaryColor, page.secondaryColor),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            page.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Title Block
// ─────────────────────────────────────────────────────────────

@Composable
private fun TitleBlock(
    page: OnboardingPage,
    isLastPage: Boolean,
    progress: Float
) {
    val offsetY by animateDpAsState(
        targetValue = (18.dp * (1f - progress.coerceIn(0f, 1f))),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "titleOffset"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer { alpha = progress }
            .offset(y = offsetY)
    ) {
        // Category pill tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(page.tagColor.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = page.categoryTag,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = page.tagColor
            )
        }

        Spacer(Modifier.height(14.dp))

        // Two-line title: first line muted, second line bright
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
        Text(
            text = page.titleAccent,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        Spacer(Modifier.height(14.dp))

        // Description or subtitle
        val descText = if (isLastPage) page.subtitle ?: "" else page.description
        Text(
            text = descText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Feature Chip
// ─────────────────────────────────────────────────────────────

@Composable
private fun StaggeredFeatureChip(
    chip: FeatureChip,
    visible: Boolean,
    delayMs: Int
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs.toLong())
            show = true
        } else {
            show = false
        }
    }

    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(380)) + slideInVertically(tween(380)) { it / 2 },
        exit = fadeOut(tween(200))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BgChip)
                .drawBehind {
                    // left accent bar
                    drawRect(
                        color = chip.accentColor,
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height)
                    )
                }
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(chip.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = chip.emoji, fontSize = 18.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chip.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = chip.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Final Page Feature Pills
// ─────────────────────────────────────────────────────────────

private val finalPills = listOf(
    "Work tracking" to Icons.Filled.Work,
    "Finance tools" to Icons.Filled.AccountBalance,
    "Habit builder" to Icons.Filled.Favorite,
    "Analytics" to Icons.Filled.BarChart,
    "Smart alerts" to Icons.Filled.Notifications,
    "Private & secure" to Icons.Filled.Lock
)

@Composable
private fun FinalPagePills() {
    val colors = listOf(OrbIndigo1, OrbGreen1, OrbAmber1, OrbIndigo2, OrbGreen2, OrbAmber2)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            finalPills.chunked(3).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    rowItems.forEachIndexed { i, (label, icon) ->
                        val color = colors[finalPills.indexOfFirst { it.first == label }]
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Bottom Controls
// ─────────────────────────────────────────────────────────────

@Composable
private fun BottomControls(
    pagerState: PagerState,
    isLastPage: Boolean,
    accentColor: Color,
    secondaryColor: Color,
    onNext: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, BgDeep.copy(alpha = 0.95f), BgDeep),
                    startY = 0f,
                    endY = 160f
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page dots
        AnimatedPageDots(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            activeColor = accentColor
        )

        Spacer(Modifier.height(20.dp))

        // Action button
        if (isLastPage) {
            GetStartedButton(
                primaryColor = accentColor,
                secondaryColor = secondaryColor,
                onClick = onFinish
            )
        } else {
            NextButton(
                accentColor = accentColor,
                onClick = onNext
            )
        }

        if (isLastPage) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No account needed to begin",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NextButton(accentColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Next",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GetStartedButton(
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btnGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "btnGlowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryColor, Color(0xFF8B5CF6), secondaryColor),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Get started",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.RocketLaunch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Page Indicator Dots
// ─────────────────────────────────────────────────────────────

@Composable
private fun AnimatedPageDots(
    pageCount: Int,
    currentPage: Int,
    activeColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 7.dp,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
                label = "dotW"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isActive) activeColor else TextMuted,
                animationSpec = tween(300),
                label = "dotC"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Skip Button
// ─────────────────────────────────────────────────────────────

@Composable
private fun SkipButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp)
    ) {
        Text(
            text = "Skip",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Ambient Orbs (background glow blobs)
// ─────────────────────────────────────────────────────────────

@Composable
private fun AmbientOrbs(color1: Color, color2: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val driftY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -20f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "orbDrift"
    )

    val animatedColor1 by animateColorAsState(
        targetValue = color1, animationSpec = tween(600), label = "orbC1"
    )
    val animatedColor2 by animateColorAsState(
        targetValue = color2, animationSpec = tween(600), label = "orbC2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-left large orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(animatedColor1.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.05f + driftY),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.1f, size.height * 0.05f + driftY)
        )
        // Bottom-right secondary orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(animatedColor2.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.85f - driftY),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(size.width * 0.9f, size.height * 0.85f - driftY)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Noise Particles (fine floating dots)
// ─────────────────────────────────────────────────────────────

@Composable
private fun NoiseParticles(color: Color) {
    val animatedColor by animateColorAsState(
        targetValue = color, animationSpec = tween(500), label = "particleColor"
    )
    // Use a simple frame-tick approach via infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "tick"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val count = 28
        repeat(count) { i ->
            val baseX = size.width * (i.toFloat() / count)
            val baseY = size.height * (0.05f + 0.9f * ((i * 7 + 3) % count).toFloat() / count)
            val ox = sin(tick + i * 1.9f) * 25f
            val oy = cos(tick * 0.6f + i * 2.5f) * 18f
            val alpha = (0.02f + 0.03f * sin(tick * 0.4f + i * 1.2f)).coerceIn(0f, 0.06f)
            val r = 1.5f + 2.5f * (0.5f + 0.5f * sin(tick * 0.25f + i * 1.8f))
            drawCircle(
                color = animatedColor.copy(alpha = alpha),
                radius = r,
                center = Offset(baseX + ox, baseY + oy)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────

// zIndex built-in from androidx.compose.ui

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction.coerceIn(0f, 1f)